package magicjinn.theblockkeepsticking.framework;

import magicjinn.theblockkeepsticking.util.TickingResult;

public class TickingBlock {
    public static TickingResult CalculateOperations(long ticks) {
        int cycles = (int) (ticks / 20);
        int remainder = (int) (ticks % 20);
        return new TickingResult(cycles, remainder);
    }

}

