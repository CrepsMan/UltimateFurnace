[CmdletBinding()]
param(
    [string[]]$Targets = @(
        "1.19.2-forge",
        "1.20.2-neoforge",
        "1.20.4-forge",
        "1.20.5-fabric",
        "1.20.5-neoforge",
        "1.21.0-forge",
        "1.21.5-fabric",
        "1.21.10-forge",
        "1.21.11-neoforge",
        "26.3-fabric",
        "26.3-neoforge"
    ),
    [switch]$SkipUnitTests
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$gradlew = Join-Path $repoRoot "gradlew.bat"
if (-not (Test-Path -LiteralPath $gradlew)) {
    throw "Could not find gradlew.bat at '$gradlew'."
}

function Invoke-GradleStep {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    $prevPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $lines = [System.Collections.Generic.List[string]]::new()
    try {
        & $gradlew @Arguments 2>&1 | ForEach-Object {
            $text = if ($_ -is [System.Management.Automation.ErrorRecord]) {
                $_.Exception.Message
            } else {
                $_.ToString()
            }
            $lines.Add($text)
            Write-Host $text
        }
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $prevPreference
    }

    return [PSCustomObject]@{
        ExitCode = $exitCode
        Output   = ($lines -join [Environment]::NewLine)
    }
}

function Clear-GameTestWorlds {
    $testServerRoot = Join-Path $repoRoot "run\testserver"
    if (-not (Test-Path -LiteralPath $testServerRoot)) {
        return
    }

    foreach ($loader in @("fabric", "forge", "neoforge")) {
        $loaderDir = Join-Path $testServerRoot $loader
        if (-not (Test-Path -LiteralPath $loaderDir)) {
            continue
        }
        foreach ($worldDir in @("world", "gametestserver")) {
            $targetPath = Join-Path $loaderDir $worldDir
            if (Test-Path -LiteralPath $targetPath) {
                Remove-Item -LiteralPath $targetPath -Recurse -Force -ErrorAction SilentlyContinue
            }
        }
    }
}

$results = [System.Collections.Generic.List[object]]::new()

Push-Location $repoRoot
try {
    if (-not $SkipUnitTests) {
        Write-Host ""
        Write-Host "============================================================" -ForegroundColor Cyan
        Write-Host "Running JUnit 5 Unit Tests (.\gradlew.bat test)" -ForegroundColor Cyan
        Write-Host "============================================================" -ForegroundColor Cyan

        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        $unitResult = Invoke-GradleStep -Arguments @("test", "--console=plain")
        $sw.Stop()

        $unitPassed = ($unitResult.ExitCode -eq 0)
        $results.Add([PSCustomObject]@{
            Target   = "unit-tests (JUnit 5)"
            Status   = if ($unitPassed) { "PASS" } else { "FAIL" }
            Tests    = if ($unitPassed) { "passed" } else { "exit $($unitResult.ExitCode)" }
            Duration = "{0:N1}s" -f $sw.Elapsed.TotalSeconds
            Notes    = if ($unitPassed) { "" } else { "See unit test output above" }
        })
    }

    foreach ($target in $Targets) {
        Write-Host ""
        Write-Host "============================================================" -ForegroundColor Cyan
        Write-Host "Boundary GameTest Target: $target" -ForegroundColor Cyan
        Write-Host "============================================================" -ForegroundColor Cyan

        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        $switchResult = Invoke-GradleStep -Arguments @("Set active project to $target", "--console=plain")
        if ($switchResult.ExitCode -ne 0) {
            $sw.Stop()
            $results.Add([PSCustomObject]@{
                Target   = $target
                Status   = "FAIL"
                Tests    = "switch failed"
                Duration = "{0:N1}s" -f $sw.Elapsed.TotalSeconds
                Notes    = "Set active project failed (exit $($switchResult.ExitCode))"
            })
            continue
        }

        Clear-GameTestWorlds

        $testResult = Invoke-GradleStep -Arguments @("testActiveServer", "--console=plain")
        $sw.Stop()

        $output = $testResult.Output
        $hasZeroTests = $output -match "0 GAME TESTS COMPLETE"
        $countMatch = [regex]::Match($output, "(\d+)\s+GAME TESTS COMPLETE")
        $allPassedMatch = [regex]::Match($output, "All\s+(\d+)\s+required tests passed")

        $passed = ($testResult.ExitCode -eq 0) -and (-not $hasZeroTests) -and $allPassedMatch.Success
        $testsLabel = if ($countMatch.Success) {
            "$($countMatch.Groups[1].Value) tests"
        } elseif ($allPassedMatch.Success) {
            "$($allPassedMatch.Groups[1].Value) tests"
        } else {
            "0 / crashed"
        }

        $notes = ""
        if (-not $passed) {
            if ($hasZeroTests) {
                $notes = "0 game tests ran"
            } elseif ($output -match "NoSuchMethodError:\s*([^\r\n]+)") {
                $notes = "NoSuchMethodError: $($Matches[1].Trim())"
            } elseif ($testResult.ExitCode -ne 0) {
                $notes = "Gradle exit code $($testResult.ExitCode)"
            } else {
                $notes = "Missing GameTest pass summary"
            }
        }

        $results.Add([PSCustomObject]@{
            Target   = $target
            Status   = if ($passed) { "PASS" } else { "FAIL" }
            Tests    = $testsLabel
            Duration = "{0:N1}s" -f $sw.Elapsed.TotalSeconds
            Notes    = $notes
        })
    }
} finally {
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Yellow
    Write-Host "Resetting active Stonecutter project to vcsVersion..." -ForegroundColor Yellow
    Write-Host "============================================================" -ForegroundColor Yellow
    $resetResult = Invoke-GradleStep -Arguments @("Reset active project", "--console=plain")
    if ($resetResult.ExitCode -ne 0) {
        Write-Warning "Failed to reset active Stonecutter project (exit $($resetResult.ExitCode))."
    }
    Pop-Location
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor White
Write-Host "Boundary Regression Summary" -ForegroundColor White
Write-Host "============================================================" -ForegroundColor White
$results | Format-Table -AutoSize Target, Status, Tests, Duration, Notes | Out-String | Write-Host

$failedCount = @($results | Where-Object { $_.Status -ne "PASS" }).Count
$passedCount = $results.Count - $failedCount
Write-Host "Total: $passedCount passed, $failedCount failed ($($results.Count) checks)."

if ($failedCount -gt 0) {
    exit 1
}
exit 0
