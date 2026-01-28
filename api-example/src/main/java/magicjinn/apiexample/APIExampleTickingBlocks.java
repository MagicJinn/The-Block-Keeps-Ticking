package magicjinn.apiexample;

import magicjinn.apiexample.blocks.TickingChestBlockEntity;
import magicjinn.theblockkeepsticking.api.InitializeTickingBlocks;

/**
 * Entrypoint for registering custom TickingObjects with The Block Keeps
 * Ticking.
 * 
 * This class is registered in fabric.mod.json under the "theblockkeepsticking"
 * entrypoint.
 */
public class APIExampleTickingBlocks implements InitializeTickingBlocks {

    @Override
    public void registerTickingBlocks(TickingBlockRegistry registry) {
        // Register our custom ticking chest
        registry.register(TickingChestBlockEntity.INSTANCE);

        APIExample.LOGGER.info("Registered TickingChest with The Block Keeps Ticking API");
    }
}
