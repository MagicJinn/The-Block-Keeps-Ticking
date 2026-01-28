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

### API usage

**The Block Keeps Ticking** exposes a small API so other mods can register their own ticking objects to be simulated in unloaded chunks.

#### Registering custom ticking objects

To register custom ticking objects for your mod:

1. **Create a `TickingObject` implementation** for each block, block entity, or entity you want to simulate.  
   Extend `TickingObject` and implement at least:
   * `INSTANCE` – a static final instance of the `TickingObject`
   * `getType()` – returns the class you want to handle (e.g. a `Block`, `BlockEntity`, or `Entity` subclass)
   * `getName()` – a unique name, only used for configuration and logging
   * `Simulate(...)` – contains the simulation logic

   **Important:** You are responsible for fully implementing your own `Simulate(...)` method. **The Block Keeps Ticking** only tells you **how many** ticks should be simulated and **when**; it does not provide default behavior or automatically advance your blocks or entities for you. To make this easier, there are helper methods available in the provided API and utility classes that you can call from inside your `Simulate(...)` implementation.

   Optionally override `getModId()` to return your mod's ID, so logs and config clearly show which mod registered the object.

2. **Expose a `TickingAccessor` (recommended)**  
   For vanilla or third‑party content, add the `TickingAccessor` interface to a mixin targeting the relevant class, and forward the simulation to your existing tick logic. For custom blocks/entities you can either:
   * implement `TickingAccessor` and delegate, or
   * implement the simulation logic directly inside your `TickingObject` (not recommended, but possible for simple blocks).

3. **Implement the `InitializeTickingBlocks` entrypoint**  
   Create a class in your mod that implements `InitializeTickingBlocks` and registers your ticking objects:

   ```java
   public class YourTickingBlocksInitializer implements InitializeTickingBlocks {
       @Override
       public void registerTickingBlocks(TickingBlockRegistry registry) {
           registry.register(YourTickingBlock.INSTANCE);
           // register more ticking objects here if needed
       }
   }
   ```

4. **Register the entrypoint in `fabric.mod.json`**  
   In your mod's `fabric.mod.json`, add the `theblockkeepsticking` entrypoint pointing to your initializer:

   ```json
   "entrypoints": {
     "theblockkeepsticking": [
       "[[YOUR-MODID]].YourTickingBlocksInitializer"
     ]
   }
   ```

When **The Block Keeps Ticking** initializes, it will discover all `theblockkeepsticking` entrypoints and call `registerTickingBlocks`, wiring your `TickingObject`s into the simulation system.

#### API example project

This repository includes an **API example mod** under the `api-example` folder. It demonstrates:

* How to implement a custom `TickingObject` (`TickingChestBlockEntity`) for a vanilla `ChestBlockEntity`
* How to use a mixin plus the `TickingAccessor` interface to bridge into existing tick logic
* How to hook into the `theblockkeepsticking` entrypoint (`APIExampleTickingBlocks`) and register your ticking objects

You can use this example as a starting point for integrating your own mod with **The Block Keeps Ticking**.
