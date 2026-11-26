import gg.meza.stonecraft.getResourcePackFormat
import gg.meza.stonecraft.mod
import gg.meza.stonecraft.tasks.McMetaCreation
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import me.modmuss50.mpp.ReleaseType
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.jvm.tasks.Jar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FilterReader
import java.io.Reader
import java.io.StringReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.time.Duration
import java.util.Properties
import java.util.UUID

plugins {
    id("gg.meza.stonecraft")
    id("me.modmuss50.mod-publish-plugin")
}

if (project.mod.isNeoforge) {
    repositories {
        maven("https://maven.neoforged.net/releases/") {
            name = "NeoForgeSponge"
            content {
                includeGroup("org.spongepowered")
            }
        }
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "SpongePowered"
            content {
                includeGroup("org.spongepowered")
            }
        }
    }

    configurations.matching { it.name == "testRuntimeClasspath" || it.name == "testCompileClasspath" }.configureEach {
        exclude(group = "net.neoforged.fancymodloader", module = "junit-fml")
    }
}

abstract class RecipeTransformReader(input: Reader) : FilterReader(input) {
    private var delegate: StringReader? = null

    protected abstract fun transform(content: String): String

    private fun ensureLoaded(): StringReader {
        var reader = delegate
        if (reader == null) {
            val content = `in`.readText()
            reader = StringReader(transform(content))
            delegate = reader
        }
        return reader
    }

    override fun read(): Int = ensureLoaded().read()

    override fun read(cbuf: CharArray, off: Int, len: Int): Int = ensureLoaded().read(cbuf, off, len)
}

class Pre1205RecipeReader(input: Reader) : RecipeTransformReader(input) {
    override fun transform(content: String): String =
        content.replace("\"id\": \"ultimate_furnace:", "\"item\": \"ultimate_furnace:")
}

class Post1212RecipeReader(input: Reader) : RecipeTransformReader(input) {
    override fun transform(content: String): String =
        content.replace(Regex("""\{\s*"item"\s*:\s*("[^"]+")\s*}"""), "$1")
}

val currentVersion = stonecutter.current.version
val isJava25 = stonecutter.eval(currentVersion, ">=26.1")
val isJava21 = stonecutter.eval(currentVersion, ">=1.20.5")
val isPre121 = stonecutter.eval(currentVersion, "<1.21")
val isPre1205 = stonecutter.eval(currentVersion, "<1.20.5")
val isPost1212 = stonecutter.eval(currentVersion, ">=1.21.2")
val isPre1214 = stonecutter.eval(currentVersion, "<1.21.4")
val isPre1215 = stonecutter.eval(currentVersion, "<1.21.5")
val isGametestRequested = gradle.startParameter.taskNames.any {
    val lower = it.lowercase()
    lower.contains("gametest") || lower.contains("testactiveserver") || lower.contains("testactiveclient")
}
val includeForgeGameTests = project.mod.isForge && !isPre1215 && isGametestRequested

if (currentVersion == "1.20.5") {
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val fabricLoaderMin = when {
    stonecutter.eval(currentVersion, ">=26.3") -> "0.19.3"
    stonecutter.eval(currentVersion, ">=26.1") -> "0.18.4"
    stonecutter.eval(currentVersion, ">=1.21.11") -> "0.17.3"
    stonecutter.eval(currentVersion, ">=1.21.9") -> "0.17.0"
    stonecutter.eval(currentVersion, ">=1.21.6") -> "0.16.13"
    stonecutter.eval(currentVersion, ">=1.21.5") -> "0.16.10"
    else -> "0.16.9"
}
val forgeLoaderVersion =
    if (project.mod.isForge) {
        project.mod
            .prop("forge_version")
            .substringAfter("-")
            .substringBefore(".")
    } else {
        ""
    }
val neoforgeLoaderVersion =
    if (project.mod.isNeoforge) {
        project.mod
            .prop("neoforge_version")
            .substringBeforeLast(".")
    } else {
        ""
    }

modSettings {
    clientOptions {
        fov = 90
        guiScale = 3
        narrator = false
        darkBackground = true
        musicVolume = 0.0
    }

    variableReplacements =
        mapOf(
            "forgeLoaderVersion" to forgeLoaderVersion,
            "neoforgeLoaderVersion" to neoforgeLoaderVersion,
            "forgeLikeModId" to if (project.mod.isNeoforge) "neoforge" else "forge",
            "forgeLikeLoaderVersion" to if (project.mod.isNeoforge) neoforgeLoaderVersion else forgeLoaderVersion,
            "javaVersion" to when {
                isJava25 -> "25"
                isJava21 -> "21"
                else -> "17"
            },
            "fabricLoaderMin" to fabricLoaderMin,
        )
}

if (project.mod.isNeoforge && isPre1205) {
    val neoforgePackMeta = tasks.register<McMetaCreation>("generateNeoForgePackMCMetaJson") {
        resourcePackVersion.set(getResourcePackFormat(currentVersion).toBigDecimal())
        packDescription.set(project.mod.description)
        sourcePackFiles.from(rootProject.layout.projectDirectory.file("src/main/resources/pack.mcmeta"))
    }
    tasks.processResources {
        from(neoforgePackMeta.flatMap { it.outputFile })
    }
    afterEvaluate {
        tasks.processResources {
            setExcludes(excludes - "pack.mcmeta")
        }
    }
}

tasks.processResources {
    inputs.property("isGametestRequested", isGametestRequested)
    inputs.property("includeForgeGameTests", includeForgeGameTests)
    exclude("data/ultimate_furnace/loot_tables/**")
    if (!isGametestRequested) {
        exclude("data/ultimate_furnace/structure/**")
        exclude("data/ultimate_furnace/structures/**")
    }
    if (!includeForgeGameTests) {
        exclude("data/ultimate_furnace/test_instance/**")
    }
    if (project.mod.isNeoforge && isPre1205) {
        exclude("META-INF/neoforge.mods.toml")
    }
    if (project.mod.isForge || (project.mod.isNeoforge && isPre1205)) {
        from(rootProject.layout.projectDirectory.file("src/main/resources/assets/ultimate_furnace/icon.png")) {
            into("")
        }
    }
    if (isPre1214) {
        exclude("assets/ultimate_furnace/items/**")
    }
    if (isPre121) {
        // Stonecraft already pluralizes vanilla block tags, recipes, loot tables, advancements, and structures for <1.21.
        filesMatching("data/ultimate_furnace/tags/block/**") {
            path = path.replace("data/ultimate_furnace/tags/block/", "data/ultimate_furnace/tags/blocks/")
        }
    }
    if (isPre1205) {
        filesMatching("data/ultimate_furnace/recipe/*.json") {
            filter(Pre1205RecipeReader::class.java)
        }
    } else if (isPost1212) {
        filesMatching("data/ultimate_furnace/recipe/*.json") {
            filter(Post1212RecipeReader::class.java)
        }
    }
}

val collectedLibsDir = rootProject.layout.buildDirectory.dir("libs")
val modVersionForCleanup = project.mod.version
val isFullCollectRequested = gradle.startParameter.taskNames.any {
    val name = it.substringAfterLast(":")
    !it.startsWith(":") && (name == "buildAndCollect" || name == "chiseledBuildAndCollect")
}
val cleanCollectedLibs = rootProject.tasks.findByName("cleanCollectedLibs")
    ?: rootProject.tasks.register("cleanCollectedLibs") {
        doLast {
            val dir = collectedLibsDir.get().asFile
            if (dir.exists()) {
                dir.listFiles { file -> file.isFile && file.name.endsWith(".jar") }?.forEach { jar ->
                    if (isFullCollectRequested || !jar.name.contains("-$modVersionForCleanup+")) {
                        jar.delete()
                    }
                }
            }
        }
    }

tasks.named("buildAndCollect") {
    dependsOn(cleanCollectedLibs)
}

val publishMcVersion = project.mod.prop("minecraft_version")
val publishLoader = project.mod.loader
val publishLoaderName = when (publishLoader) {
    "fabric" -> "Fabric"
    "forge" -> "Forge"
    "neoforge" -> "NeoForge"
    else -> publishLoader
}
val localPropsFile = rootProject.layout.projectDirectory.file("local.properties").asFile
val localProps = Properties().apply {
    if (localPropsFile.isFile) {
        localPropsFile.inputStream().use(::load)
    }
}
val modrinthProjectId = (findProperty("modrinth_id")?.toString()?.trim().orEmpty())
    .ifBlank { providers.environmentVariable("MODRINTH_ID").orNull?.trim().orEmpty() }
val resolvedModrinthToken = providers.environmentVariable("MODRINTH_TOKEN")
    .map { it.trim() }
    .orElse(localProps.getProperty("modrinth.token", "").trim())
    .orNull
    ?.takeIf { it.isNotBlank() }
val publishJarTask = if (stonecutter.eval(publishMcVersion, ">1.21.11")) {
    tasks.named("jar", Jar::class.java)
} else {
    tasks.named("remapJar", Jar::class.java)
}

publishMods {
    val changelogFile = rootProject.layout.projectDirectory.file("CHANGELOG.md").asFile
    changelog.set(if (changelogFile.isFile) changelogFile.readText(Charsets.UTF_8) else "No changelog provided.")
    type.set(ReleaseType.STABLE)
    file.set(publishJarTask.flatMap { it.archiveFile })
    version.set("${project.mod.version}+mc$publishMcVersion-$publishLoader")
    displayName.set("Ultimate Furnace ${project.mod.version} ($publishLoaderName $publishMcVersion)")
    modLoaders.set(listOf(publishLoader))
    // Keep mod-publish-plugin locked to dryRun = true because 2.2.1 cannot set Modrinth status=unlisted/draft.
    dryRun.set(true)

    modrinth {
        projectId.set(modrinthProjectId)
        if (resolvedModrinthToken != null) {
            accessToken.set(resolvedModrinthToken)
        }
        minecraftVersions.set(listOf(publishMcVersion))
        announcementTitle.set("Download Ultimate Furnace ${project.mod.version} ($publishLoaderName $publishMcVersion) on Modrinth")
        if (project.mod.isFabric) {
            requires("fabric-api")
        }
    }
}

val draftTargetsKey = "ultimateFurnace.modrinthDraftTargets"
@Suppress("UNCHECKED_CAST")
val draftTargets = if (rootProject.extensions.extraProperties.has(draftTargetsKey)) {
    rootProject.extensions.extraProperties.get(draftTargetsKey) as MutableMap<String, Map<String, Any>>
} else {
    LinkedHashMap<String, Map<String, Any>>().also {
        rootProject.extensions.extraProperties.set(draftTargetsKey, it)
    }
}

val collectedJarFile = rootProject.layout.buildDirectory
    .file("libs/${project.mod.id}-$publishLoader-${project.mod.version}+mc$publishMcVersion.jar")
    .get()
    .asFile

draftTargets[project.path] = mapOf(
    "targetPath" to project.path,
    "mcVersion" to publishMcVersion,
    "loader" to publishLoader,
    "loaderName" to publishLoaderName,
    "modVersion" to project.mod.version,
    "versionNumber" to "${project.mod.version}+mc$publishMcVersion-$publishLoader",
    "displayName" to "Ultimate Furnace ${project.mod.version} ($publishLoaderName $publishMcVersion)",
    "collectedJar" to collectedJarFile,
    "subprojectJar" to publishJarTask.flatMap { it.archiveFile },
)

if (rootProject.tasks.findByName("publishModrinthUnlisted") == null) {
    val rootDir = rootProject.layout.projectDirectory.asFile
    val rootModVersion = project.mod.version
    val uploadedManifestFile = rootProject.layout.buildDirectory.file("modrinth-uploaded-versions.json").get().asFile

    rootProject.tasks.register("publishModrinthUnlisted") {
        group = "publishing"
        description = "Uploads all Stonecutter targets to Modrinth as unlisted versions via POST /v2/version (supports -PdryRun=true)."
        outputs.upToDateWhen { false }

        doLast {
            fun jsonString(value: String): String = buildString(value.length + 2) {
                append('"')
                for (ch in value) {
                    when (ch) {
                        '"' -> append("\\\"")
                        '\\' -> append("\\\\")
                        '\b' -> append("\\b")
                        '\u000C' -> append("\\f")
                        '\n' -> append("\\n")
                        '\r' -> append("\\r")
                        '\t' -> append("\\t")
                        else -> if (ch < ' ') {
                            append("\\u")
                            append(ch.code.toString(16).padStart(4, '0'))
                        } else {
                            append(ch)
                        }
                    }
                }
                append('"')
            }

            val isDryRun = project.findProperty("dryRun")
                ?.toString()
                ?.trim()
                ?.equals("true", ignoreCase = true) == true

            val changelogFile = File(rootDir, "CHANGELOG.md")
            if (!changelogFile.isFile) {
                throw GradleException("Missing CHANGELOG.md at ${changelogFile.absolutePath}")
            }
            val changelogText = changelogFile.readText(Charsets.UTF_8)

            val gradleProps = Properties().apply {
                val gradlePropsFile = File(rootDir, "gradle.properties")
                if (gradlePropsFile.isFile) {
                    gradlePropsFile.inputStream().use(::load)
                }
            }
            val projectId = gradleProps.getProperty("modrinth_id", "")
                .trim()
                .ifBlank { System.getenv("MODRINTH_ID")?.trim().orEmpty() }
            if (projectId.isBlank()) {
                throw GradleException("Missing modrinth_id in gradle.properties (or MODRINTH_ID environment variable).")
            }

            val localProperties = Properties().apply {
                val localFile = File(rootDir, "local.properties")
                if (localFile.isFile) {
                    localFile.inputStream().use(::load)
                }
            }
            val token = System.getenv("MODRINTH_TOKEN")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: localProperties.getProperty("modrinth.token", "").trim().takeIf { it.isNotEmpty() }

            if (!isDryRun && token == null) {
                throw GradleException("Missing Modrinth PAT token: set MODRINTH_TOKEN env var or modrinth.token in local.properties.")
            }

            val targets = draftTargets.values.toList()
            if (targets.isEmpty()) {
                throw GradleException("No Stonecutter publish targets were collected.")
            }

            data class ResolvedTarget(
                val mcVersion: String,
                val loader: String,
                val loaderName: String,
                val versionNumber: String,
                val displayName: String,
                val jarFile: File,
                val dependenciesJson: String,
            )

            val resolvedTargets = targets.map { entry ->
                val collected = entry["collectedJar"] as File
                @Suppress("UNCHECKED_CAST")
                val subprojectProvider = entry["subprojectJar"] as Provider<RegularFile>
                val subprojectJar = subprojectProvider.get().asFile
                val jarFile = when {
                    collected.isFile -> collected
                    subprojectJar.isFile -> subprojectJar
                    else -> throw GradleException(
                        "Missing built jar for ${entry["versionNumber"]}: checked ${collected.absolutePath} and ${subprojectJar.absolutePath}. Run .\\gradlew.bat buildAndCollect first."
                    )
                }
                val loader = entry["loader"] as String
                val depsJson = if (loader == "fabric") {
                    """[{"project_id":"P7dR8mSH","dependency_type":"required"}]"""
                } else {
                    "[]"
                }
                ResolvedTarget(
                    mcVersion = entry["mcVersion"] as String,
                    loader = loader,
                    loaderName = entry["loaderName"] as String,
                    versionNumber = entry["versionNumber"] as String,
                    displayName = entry["displayName"] as String,
                    jarFile = jarFile,
                    dependenciesJson = depsJson,
                )
            }

            val total = resolvedTargets.size
            val httpClient = if (!isDryRun) {
                HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build()
            } else {
                null
            }
            val userAgent = "UltimateFurnace-GradlePublisher/$rootModVersion"
            val endpoint = URI.create("https://api.modrinth.com/v2/version")
            val uploadedEntries = mutableListOf<Map<String, String>>()

            try {
                resolvedTargets.forEachIndexed { index, target ->
                    val num = index + 1
                    val payloadJson = buildString {
                        append("{")
                        append("\"project_id\":").append(jsonString(projectId)).append(",")
                        append("\"version_number\":").append(jsonString(target.versionNumber)).append(",")
                        append("\"name\":").append(jsonString(target.displayName)).append(",")
                        append("\"changelog\":").append(jsonString(changelogText)).append(",")
                        append("\"game_versions\":[").append(jsonString(target.mcVersion)).append("],")
                        append("\"loaders\":[").append(jsonString(target.loader)).append("],")
                        append("\"version_type\":\"release\",")
                        append("\"featured\":false,")
                        append("\"status\":\"unlisted\",")
                        append("\"requested_status\":\"unlisted\",")
                        append("\"dependencies\":").append(target.dependenciesJson).append(",")
                        append("\"file_parts\":[\"file\"],")
                        append("\"primary_file\":\"file\"")
                        append("}")
                    }

                    if (isDryRun) {
                        val sizeKb = (target.jarFile.length() + 1023L) / 1024L
                        logger.lifecycle(
                            "[DRY RUN] [$num/$total] ${target.versionNumber} -> \"${target.displayName}\" | " +
                                "jar=${target.jarFile.name} (${sizeKb} KB) | status=unlisted | deps=${target.dependenciesJson}"
                        )
                        return@forEachIndexed
                    }

                    val boundary = "----UltimateFurnaceBoundary${UUID.randomUUID().toString().replace("-", "")}"
                    val bodyStream = ByteArrayOutputStream()
                    val crlf = "\r\n".toByteArray(Charsets.UTF_8)

                    bodyStream.write("--$boundary\r\n".toByteArray(Charsets.UTF_8))
                    bodyStream.write("Content-Disposition: form-data; name=\"data\"\r\n".toByteArray(Charsets.UTF_8))
                    bodyStream.write("Content-Type: application/json; charset=UTF-8\r\n\r\n".toByteArray(Charsets.UTF_8))
                    bodyStream.write(payloadJson.toByteArray(Charsets.UTF_8))
                    bodyStream.write(crlf)

                    bodyStream.write("--$boundary\r\n".toByteArray(Charsets.UTF_8))
                    bodyStream.write(
                        "Content-Disposition: form-data; name=\"file\"; filename=\"${target.jarFile.name}\"\r\n"
                            .toByteArray(Charsets.UTF_8)
                    )
                    bodyStream.write("Content-Type: application/java-archive\r\n\r\n".toByteArray(Charsets.UTF_8))
                    bodyStream.write(Files.readAllBytes(target.jarFile.toPath()))
                    bodyStream.write(crlf)
                    bodyStream.write("--$boundary--\r\n".toByteArray(Charsets.UTF_8))

                    val request = HttpRequest.newBuilder(endpoint)
                        .timeout(Duration.ofSeconds(120))
                        .header("Authorization", token!!)
                        .header("User-Agent", userAgent)
                        .header("Content-Type", "multipart/form-data; boundary=$boundary")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(bodyStream.toByteArray()))
                        .build()

                    val response = httpClient!!.send(request, HttpResponse.BodyHandlers.ofString(Charsets.UTF_8))
                    val statusCode = response.statusCode()
                    if (statusCode !in 200..299) {
                        throw GradleException(
                            "Modrinth unlisted upload failed [$num/$total] for ${target.versionNumber}: " +
                                "HTTP $statusCode - ${response.body()}"
                        )
                    }

                    @Suppress("UNCHECKED_CAST")
                    val parsed = JsonSlurper().parseText(response.body()) as? Map<String, Any?>
                    val createdId = (parsed?.get("id") as? String)?.takeIf { it.isNotBlank() } ?: "unknown"
                    val createdStatus = (parsed?.get("status") as? String)?.takeIf { it.isNotBlank() } ?: "unlisted"

                    uploadedEntries.add(
                        linkedMapOf(
                            "versionNumber" to target.versionNumber,
                            "id" to createdId,
                            "status" to createdStatus,
                        )
                    )

                    logger.lifecycle(
                        "[UPLOADED] [$num/$total] ${target.versionNumber} -> \"${target.displayName}\" " +
                            "(HTTP $statusCode, id=$createdId, status=$createdStatus)"
                    )

                    if (num < total) {
                        Thread.sleep(250L)
                    }
                }
            } finally {
                if (!isDryRun && uploadedEntries.isNotEmpty()) {
                    uploadedManifestFile.parentFile.mkdirs()
                    uploadedManifestFile.writeText(
                        JsonOutput.prettyPrint(JsonOutput.toJson(uploadedEntries)),
                        Charsets.UTF_8,
                    )
                    logger.lifecycle("Saved ${uploadedEntries.size} uploaded version records to ${uploadedManifestFile.absolutePath}")
                }
            }
        }
    }
}

if (rootProject.tasks.findByName("publishModrinthDrafts") == null) {
    rootProject.tasks.register("publishModrinthDrafts") {
        group = "publishing"
        description = "Alias for publishModrinthUnlisted."
        dependsOn(rootProject.tasks.named("publishModrinthUnlisted"))
    }
}

if (rootProject.tasks.findByName("setModrinthVersionStatus") == null) {
    val rootDir = rootProject.layout.projectDirectory.asFile
    val rootModVersion = project.mod.version
    val uploadedManifestFile = rootProject.layout.buildDirectory.file("modrinth-uploaded-versions.json").get().asFile

    rootProject.tasks.register("setModrinthVersionStatus") {
        group = "publishing"
        description = "Updates Modrinth status (unlisted or listed) for all $rootModVersion+ versions without re-uploading jars."
        outputs.upToDateWhen { false }

        doLast {
            val targetStatus = (project.findProperty("versionStatus")?.toString()?.trim()?.lowercase().orEmpty())
                .ifBlank { "unlisted" }
            if (targetStatus != "unlisted" && targetStatus != "listed") {
                throw GradleException("Invalid -PversionStatus='$targetStatus'. Allowed values: unlisted, listed.")
            }

            val isDryRun = project.findProperty("dryRun")
                ?.toString()
                ?.trim()
                ?.equals("true", ignoreCase = true) == true

            val gradleProps = Properties().apply {
                val gradlePropsFile = File(rootDir, "gradle.properties")
                if (gradlePropsFile.isFile) {
                    gradlePropsFile.inputStream().use(::load)
                }
            }
            val projectId = gradleProps.getProperty("modrinth_id", "")
                .trim()
                .ifBlank { System.getenv("MODRINTH_ID")?.trim().orEmpty() }
            if (projectId.isBlank()) {
                throw GradleException("Missing modrinth_id in gradle.properties (or MODRINTH_ID environment variable).")
            }

            val localProperties = Properties().apply {
                val localFile = File(rootDir, "local.properties")
                if (localFile.isFile) {
                    localFile.inputStream().use(::load)
                }
            }
            val token = System.getenv("MODRINTH_TOKEN")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: localProperties.getProperty("modrinth.token", "").trim().takeIf { it.isNotEmpty() }
                ?: throw GradleException("Missing Modrinth PAT token: set MODRINTH_TOKEN env var or modrinth.token in local.properties.")

            val userAgent = "UltimateFurnace-GradlePublisher/$rootModVersion"
            val httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build()

            val listEndpoint = URI.create("https://api.modrinth.com/v2/project/$projectId/version")
            val listRequest = HttpRequest.newBuilder(listEndpoint)
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", token)
                .header("User-Agent", userAgent)
                .GET()
                .build()

            val listResponse = httpClient.send(listRequest, HttpResponse.BodyHandlers.ofString(Charsets.UTF_8))
            if (listResponse.statusCode() !in 200..299) {
                throw GradleException(
                    "Failed to list Modrinth versions for project $projectId: " +
                        "HTTP ${listResponse.statusCode()} - ${listResponse.body()}"
                )
            }

            @Suppress("UNCHECKED_CAST")
            val apiVersions = (JsonSlurper().parseText(listResponse.body()) as? List<Map<String, Any?>>)
                ?: throw GradleException("Unexpected Modrinth response when listing versions for project $projectId.")

            val versionPrefix = "$rootModVersion+"
            val mergedById = LinkedHashMap<String, MutableMap<String, String>>()

            for (apiVer in apiVersions) {
                val id = (apiVer["id"] as? String)?.trim().orEmpty()
                val versionNumber = (apiVer["version_number"] as? String)?.trim().orEmpty()
                val status = (apiVer["status"] as? String)?.trim().orEmpty().ifBlank { "unknown" }
                if (id.isNotEmpty() && versionNumber.startsWith(versionPrefix)) {
                    mergedById[id] = linkedMapOf(
                        "id" to id,
                        "versionNumber" to versionNumber,
                        "status" to status,
                    )
                }
            }

            var manifestEntriesLoaded = 0
            if (uploadedManifestFile.isFile) {
                @Suppress("UNCHECKED_CAST")
                val manifestVersions = JsonSlurper().parseText(uploadedManifestFile.readText(Charsets.UTF_8)) as? List<Map<String, Any?>>
                manifestVersions?.forEach { entry ->
                    val id = (entry["id"] as? String)?.trim().orEmpty()
                    val versionNumber = (entry["versionNumber"] as? String)?.trim().orEmpty()
                    val status = (entry["status"] as? String)?.trim().orEmpty().ifBlank { "unknown" }
                    if (id.isNotEmpty() && id != "unknown" && versionNumber.startsWith(versionPrefix)) {
                        manifestEntriesLoaded++
                        if (!mergedById.containsKey(id)) {
                            mergedById[id] = linkedMapOf(
                                "id" to id,
                                "versionNumber" to versionNumber,
                                "status" to status,
                            )
                        }
                    }
                }
            }

            val allMatching = mergedById.values.toList()
            val toUpdate = allMatching.filter { it["status"] != targetStatus }

            logger.lifecycle(
                "Found ${apiVersions.size} API versions + $manifestEntriesLoaded manifest entries " +
                    "(${allMatching.size} unique matching '$versionPrefix*', ${toUpdate.size} needing update to '$targetStatus')."
            )
            if (toUpdate.isEmpty()) {
                logger.lifecycle("All matching '$versionPrefix*' versions already have status '$targetStatus'.")
                return@doLast
            }

            val total = toUpdate.size
            val patchBody = """{"status":"$targetStatus"}"""
            val updatedIds = HashSet<String>()

            toUpdate.forEachIndexed { index, versionEntry ->
                val num = index + 1
                val versionId = versionEntry.getValue("id")
                val versionNumber = versionEntry.getValue("versionNumber")
                val currentStatus = versionEntry.getValue("status")

                if (isDryRun) {
                    logger.lifecycle(
                        "[DRY RUN] [$num/$total] $versionNumber ($versionId): $currentStatus -> $targetStatus"
                    )
                    return@forEachIndexed
                }

                val patchEndpoint = URI.create("https://api.modrinth.com/v2/version/$versionId")
                val patchRequest = HttpRequest.newBuilder(patchEndpoint)
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", token)
                    .header("User-Agent", userAgent)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(patchBody, Charsets.UTF_8))
                    .build()

                val patchResponse = httpClient.send(patchRequest, HttpResponse.BodyHandlers.ofString(Charsets.UTF_8))
                val statusCode = patchResponse.statusCode()
                if (statusCode != 200 && statusCode != 204) {
                    throw GradleException(
                        "Failed to update Modrinth version [$num/$total] $versionNumber ($versionId): " +
                            "HTTP $statusCode - ${patchResponse.body()}"
                    )
                }

                updatedIds.add(versionId)
                logger.lifecycle(
                    "[UPDATED] [$num/$total] $versionNumber ($versionId): $currentStatus -> $targetStatus (HTTP $statusCode)"
                )

                if (num < total) {
                    Thread.sleep(250L)
                }
            }

            if (!isDryRun && updatedIds.isNotEmpty() && uploadedManifestFile.isFile) {
                @Suppress("UNCHECKED_CAST")
                val manifestList = (JsonSlurper().parseText(uploadedManifestFile.readText(Charsets.UTF_8)) as? List<Map<String, Any?>>)
                    ?.map { entry ->
                        val id = entry["id"] as? String
                        if (id != null && updatedIds.contains(id)) {
                            linkedMapOf(
                                "versionNumber" to (entry["versionNumber"]?.toString() ?: ""),
                                "id" to id,
                                "status" to targetStatus,
                            )
                        } else {
                            entry
                        }
                    }
                if (manifestList != null) {
                    uploadedManifestFile.writeText(
                        JsonOutput.prettyPrint(JsonOutput.toJson(manifestList)),
                        Charsets.UTF_8,
                    )
                }
            }
        }
    }
}
