# The Block Keeps Ticking

[![icon.png](icon.png)](https://x.com/Sulmino_/status/1979955766074261928)

<sup>This wonderful mod icon was created by [Sulmino_ on Twitter/X](https://x.com/Sulmino_).</sup>

[![Modrinth: The Block Keeps Ticking](https://img.shields.io/badge/Modrinth-The_Block_Keeps_Ticking-00ae5d?logo=modrinth)](https://modrinth.com/mod/the-block-keeps-ticking) [![CurseForge: The Block Keeps Ticking](https://img.shields.io/badge/CurseForge-The_Block_Keeps_Ticking-f16437?logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/the-block-keeps-ticking)

## Simulate block ticking and entities in unloaded chunks

**The Block Keeps Ticking** is a complete rewrite of [Alive World](https://github.com/What42Pizza/Alive-World-Mod/forks), (which is a fork of Presence Not Required) that allows blocks and entities to continue progressing in unloaded chunks, so your farms, furnaces, and passive mobs keep growing and working even when you're far away.

### Features

* **Block Support:**
  * **Block Entities:** Furnaces, Campfires, Brewing Stands
  * **Crops & Stems:** Wheat, Carrots, Potatoes, Beetroots, Torchflowers, Pumpkins and Melons
  * **Nether Wart:** Growth progression
  * **Cocoa Beans:** Growth progression
  * **Trees:** Sapling growth
  * **Growing Plants:** Kelp, Bamboo, Sugar Cane, Cactus
  * **Dried Ghast & Sniffer Eggs:** Hydration and hatching progression
  * **Budding Amethyst:** Growth progression
  * **Cauldrons:** Water and lava dripping
  * **Mud:** Mud above Dripstone conversion to clay
  * **Sweet Berry Bushes:** Growth progression

<sup>*Note: Due to the way Sniffer Eggs and Dried Ghasts are processed, some precision is lost during simulation.</sup>

* **Time Modes:** Choose between world time (only progresses when playing) or real time (progresses even when offline).

* **Lazy Tax setting:** Configure a percentage reduction of simulated ticks to slow down simulation for your prefered balance level.

* **Per-Object Configuration:** Enable or disable simulation for specific blocks and entities individually.

* **Serverside:** When installed on a server, players do not need to install the mod. Can also be installed in singleplayer worlds.

### Configuration

You can configure the following values with [ModMenu](https://github.com/TerraformersMC/ModMenu) and [YetAnotherConfigLib](https://github.com/isXander/YetAnotherConfigLib), or by editing `config\the-block-keeps-ticking.json`.

* **Time Source:** Choose between "World Time" (only progresses when playing) or "Real Time" (progresses even when offline). Real Time works well with lazy tax. (Default: World Time)

* **Lazy Tax (%):** Simulated ticks are reduced by this percentage. Increasing this value slows down simulation in unloaded chunks. Range: 0-99. (Default: 0)

* **Debug Logging:** Enables detailed logging when simulation occurs, showing which blocks, block entities, and entities are being simulated. (Default: false)

* **Ticking Objects:** Individual toggles to enable or disable simulation for each supported block type and entity. All are enabled by default.
