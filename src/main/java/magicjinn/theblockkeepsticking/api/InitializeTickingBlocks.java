package magicjinn.theblockkeepsticking.api;

import magicjinn.theblockkeepsticking.util.TickingObject;

/**
 * Entrypoint interface for mods that want to register custom TickingObjects.
 * 
 * <p>
 * To use this entrypoint, implement this interface in your mod and register it
 * in your fabric.mod.json:
 * 
 * <pre>{@code
 * "entrypoints": {
 *     "theblockkeepsticking": [
 *         "[[YOUR-MODID]].YourTickingBlocksInitializer"
 *     ]
 * }
 * }</pre>
 * 
 * <p>
 * Example implementation:
 * 
 * <pre>{@code
 * public class YourTickingBlocksInitializer implements InitializeTickingBlocks {
 *     @Override
 *     public void registerTickingBlocks(TickingBlockRegistry registry) {
 *         registry.register(new YourCustomTickingBlock());
 *     }
 * }
 * }</pre>
 */
public interface InitializeTickingBlocks {
    /**
     * Called during mod initialization to register custom TickingObjects.
     * 
     * @param registry The registry to register TickingObjects to
     */
    void registerTickingBlocks(TickingBlockRegistry registry);

    /**
     * Registry interface for registering TickingObjects.
     */
    @FunctionalInterface
    interface TickingBlockRegistry {
        /**
         * Registers a TickingObject to be simulated when chunks are loaded.
         * 
         * @param tickingObject The TickingObject to register
         */
        void register(TickingObject tickingObject);
    }
}
