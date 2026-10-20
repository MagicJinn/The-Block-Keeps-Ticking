package magicjinn.theblockkeepsticking.neoforge.event;

import magicjinn.theblockkeepsticking.api.InitializeTickingBlocks;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.neoforged.bus.api.Event;

/**
 * Fired during The Block Keeps Ticking init so NeoForge mods can register custom
 * {@link TickingObject}s.
 */
public final class RegisterTickingBlocksEvent extends Event {
    private final InitializeTickingBlocks.TickingBlockRegistry registry;

    public RegisterTickingBlocksEvent(InitializeTickingBlocks.TickingBlockRegistry registry) {
        this.registry = registry;
    }

    public void register(TickingObject tickingObject) {
        registry.register(tickingObject);
    }
}
