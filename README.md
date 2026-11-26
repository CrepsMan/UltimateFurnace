# Ultimate Furnace Mod

**Smelt without coal, level up your furnace, and harness thermal energy.**
The **Ultimate Furnace Mod** adds a progression-based, sunlight-powered furnace that grows faster and unlocks an internal solar battery as you smelt, alongside the **Copper Plate**—a thermally conductive utility block that reacts to nearby heat and cold sources.

---

## Ultimate Furnace

### True Sunlight Operation
No coal, lava buckets, or fuel farms required. The Ultimate Furnace runs on real sky access rather than artificial light (torches will not power it):
* **Clear Sky & Glass:** Operates at **100%** efficiency under open sky or transparent blocks like glass.
* **Canopy & Weather:** Leaves reduce sunlight efficiency based on canopy depth (**75% / 50% / 25% / 0%**), while rain drops output to **10%** and thunderstorms block solar generation (**0%**).
* **Underground & Nighttime Battery:** At **Level 1**, the furnace smelts directly from active sunlight. Starting at **Level 2**, it unlocks an internal battery that charges in sunlight (and at half speed while smelting), drains **zero power while idle**, and keeps smelting through nights, storms, or underground.
* **Portable Progression:** Mining the furnace preserves its **level**, **smelt progress**, and **stored power** on the item so you can relocate it—even underground—without losing progress.

### Leveling & Progression
Every item smelted brings your furnace closer to the next tier (up to **Level 5** by default), dramatically boosting speed, charge rate, and battery capacity:

| Level | Smelt Time | Battery Capacity | Charge Rate (Idle) | Power Use (Darkness) |
| :---: | :---: | :---: | :---: | :---: |
| **1** | 20s (400 ticks) | 0 *(Unlocks at Lv 2)* | — | — |
| **2** | 15s (300 ticks) | 6,000 | 2 / tick | 1 / tick |
| **3** | 10s (200 ticks) | 12,000 | 4 / tick | 2 / tick |
| **4** | 5s (100 ticks) | 24,000 | 8 / tick | 4 / tick |
| **5** | 2s (40 ticks) | 48,000 | 15 / tick | 10 / tick |

### Custom GUI & Tooltips
* **Solar & Battery Meter:** Shows live stored energy percentage and current sunlight/battery operating state.
* **Level Progress Bar:** Hover for current level, smelts toward the next tier, and total items smelted.
* **Accurate Status Flames & Block Lighting:** Flames in both the GUI and world glow only while the furnace is actively smelting.

### Configuration & Commands
Customize `max_level`, `items_per_level`, `cook_times`, `max_stored_power`, `power_gain_rate`, `power_use_rate`, and permissions in `config/ultimate_furnace_config.txt`, then apply changes live:
* `/ultimatefurnace info [pos]` — Displays level, smelt count, stored power, cook time, rates, and live sunlight %. Omit `[pos]` to inspect the furnace you are looking at (up to 8 blocks away).
* `/ultimatefurnace set <level|smeltcount|storepower> <value> <pos>` — Adjusts furnace stats *(Operator)*.
* `/ultimatefurnace reload` — Reloads `ultimate_furnace_config.txt` without restarting *(Operator)*.

---

## Copper Plate Block
Crafted from Copper Blocks and Redstone, the **Copper Plate** conducts heat and cold from adjacent thermal blocks (`warm_block` / `cold_block` tags such as Magma, Lava, Campfires, Fire, Ice, Snow, and Powder Snow):
* **6-Sided Thermal Conduction:** Reacts to thermal sources on any adjacent face and conducts through connected Copper Plates up to **4 blocks** away (`4 -> 3 -> 2 -> 1`).
* **Thermal Neutralization:** Placing opposing hot and cold sources next to the same plate cancels out both states back to neutral (`0`).
* **Hot Copper Plate Effects:** Damages and ignites entities standing on top *(sneaking, Fire Resistance, or Frost Walker protects you)*, dries waterlogged blocks, evaporates nearby water, melts snow/ice, and can slowly ignite nearby flammables (respecting fire-spread gamerules).
* **Cold Copper Plate Effects:** Applies Powder Snow freezing to entities on top *(leather boots protect you)*, extinguishes nearby fire, cools lava into Obsidian/Cobblestone, and freezes adjacent water sources into Frosted Ice (which thaws back when the plate is removed).
* **Comparator Output:** Emits a redstone comparator signal based on thermal strength (**15 / 12 / 8 / 4 / 0**).

---

## Compatibility
Available for **Fabric**, **Forge**, and **NeoForge** across **Minecraft 1.19.2 – 26.3** (with feature parity across versions; Fabric targets require **Fabric API**).

---

## Building from Source
* Build & verify all targets: `./gradlew build` (or `.\gradlew.bat build` on Windows)
* Build & collect all release jars into `build/libs/`: `./gradlew buildAndCollect`
* See [`CHANGELOG.md`](CHANGELOG.md) for release notes.

---

## Server Hosting
Want to play **The Ultimate Furnace Mod** with friends on a dedicated survival server?

[![RealisticNode Server Hosting](https://cdn.discordapp.com/attachments/1574690984770535444/1574836486652825642/ULTIMATEFURNACEMOD.png?ex=6b071845&is=6b05c6c5&hm=5b9a8463241e21ea32dc02aca5e180de1c7a1e9116cc145d567fd496a4cf9e31&)](https://realisticnode.com/ultimatefurnacemod)

Get **10% off** your server by signing up through **[realisticnode.com/ultimatefurnacemod](https://realisticnode.com/ultimatefurnacemod)** and using promo code **`ULTIMATEFURNACEMOD`** at checkout.
