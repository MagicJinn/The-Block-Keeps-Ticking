package magicjinn.theblockkeepsticking.util;

import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Base class for objects that can be simulated in unloaded chunks.
 * 
 * <p>
 * To create a custom ticking object for your mod:
 * <ol>
 * <li>Extend this class</li>
 * <li>Implement {@link #getType()}, {@link #getName()}, and
 * {@link #Simulate}</li>
 * <li>Override {@link #getModId()} to return your mod's ID</li>
 * <li>Register it via the {@code theblockkeepsticking} entrypoint</li>
 * </ol>
 * 
 * <p>
 * Example:
 * 
 * <pre>
 * {
 *     &#64;code
 *     public class MyTickingBlock extends TickingObject {
 *         public static final MyTickingBlock INSTANCE = new MyTickingBlock();
 * 
 *         &#64;Override
 *         public Class<?> getType() {
 *             return MyBlock.class;
 *         }
 * 
 *         &#64;Override
 *         public String getName() {
 *             return "my_ticking_block";
 *         }
 * 
 *         &#64;Override
 *         public String getModId() {
 *             return "my_mod_id";
 *         }
 * 
 *         @Override
 *         public boolean Simulate(Object obj, long ticks, World world, BlockState state, BlockPos pos) {
 *             // If your block is a vanilla block, from another mod, or simply requires a
 *             // mixin,
 *             // add the TickingAccessor interface to your mixin implementation.
 *             // For custom blocks, it is also recommended to implement the TickingAccessor
 *             // interface.
 *             // You can then access the simulation function as follows:
 *             if (objectInstance instanceof MyBlock myBlock) {
 *                 return ((TickingAccessor) myBlock).Simulate(ticksToSimulate, world, state, pos);
 *             }
 * 
 *             // Else, if the block is simple enough to simulate it without "native" access
 *             // or mixins, you can also put the simulation logic here.
 *             // Though, for clarity, it is recommended to use the TickingAccessor
 *             // interface anyway, so that every single TickingObject has roughly the same
 *             // structure.
 * 
 *             return false; // If we reached this point, the simulation was not performed
 *         }
 *     }
 * }
 * </pre>
 */
public abstract class TickingObject {
    /**
     * Gets the type of block/entity this TickingObject handles.
     * Add
     * {@code public static final [HANDLING_TYPE] INSTANCE = new [HANDLING_TYPE]();}
     * to the subclass to implement this properly.
     * 
     * @return The class type this TickingObject handles (e.g., Block, BlockEntity,
     *         or Entity subclass)
     */
    public abstract Class<?> getType();

    /**
     * Gets the unique name identifier for this TickingObject.
     * This name is used for configuration and logging purposes.
     * 
     * @return A unique name for this ticking object
     */
    public abstract String getName();

    /**
     * Gets the mod ID that registered this TickingObject.
     * Override this method to return your mod's ID.
     * 
     * @return The mod ID (defaults to "TheBlockKeepsTicking" for built-in objects)
     */
    public String getModId() {
        return TheBlockKeepsTicking.class.getSimpleName();
    }

    /**
     * Performs the simulation logic for this object.
     * 
     * @param objectInstance  The block, block entity, or entity instance to
     *                        simulate
     * @param ticksToSimulate The number of ticks to simulate
     * @param world           The world the object is in
     * @param state           The block state (null for entities)
     * @param pos             The block position (null for entities)
     * @return true if simulation was performed, false otherwise
     */
    public abstract boolean Simulate(Object objectInstance, long ticksToSimulate, World world,
            BlockState state, BlockPos pos);
}
