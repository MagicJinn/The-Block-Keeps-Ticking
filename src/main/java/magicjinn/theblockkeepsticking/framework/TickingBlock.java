package magicjinn.theblockkeepsticking.framework;

import magicjinn.theblockkeepsticking.util.TickingResult;

public abstract class TickingBlock {
    public static TickingResult CalculateOperations(long ticks) {
        int cycles = (int) (ticks / 20);
        int remainder = (int) (ticks % 20);
        return new TickingResult(cycles, remainder);
    }

    public abstract void Simulate(Long ticksToSimulate);

}
