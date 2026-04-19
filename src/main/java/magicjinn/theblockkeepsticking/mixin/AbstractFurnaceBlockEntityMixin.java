package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin implements TickingAccessor {

    @Shadow
    @Final
    protected static final int SLOT_INPUT = 0;
    @Shadow
    @Final
    protected static final int SLOT_FUEL = 1;
    @Shadow
    @Final
    protected static final int SLOT_RESULT = 2;

    @Shadow
    @Final
    private RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;
    @Shadow
    private int litTimeRemaining;

    @Shadow
    protected NonNullList<ItemStack> items;

    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) (Object) this;
        if (!(level instanceof ServerLevel serverLevel)) { // Need ServerLevel for recipes and fuel values
            return false;
        }

        // Reference to input slot stack
        ItemStack input = items.get(SLOT_INPUT);
        // Reference to fuel slot stack
        ItemStack fuel = items.get(SLOT_FUEL);
        // Reference to output slot stack
        ItemStack output = items.get(SLOT_RESULT);

        if (input.isEmpty()) {
            return false; // Still exit early
        }

        // Find if there's a matching recipe
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        RecipeHolder<? extends AbstractCookingRecipe> recipeHolder = quickCheck.getRecipeFor(recipeInput, serverLevel)
                .orElse(null);
        if (recipeHolder == null)
            return false;

        AbstractCookingRecipe recipe = recipeHolder.value();
        if (recipe == null)
            return false;
        // Recipe output for one craft (same idea as vanilla burnResult)
        ItemStack recipeOutput = recipe.assemble(recipeInput);

        if (recipeOutput.isEmpty())
            return false;

        // Get recipe cook time
        int recipeCookTime = recipe.cookingTime();
        if (recipeCookTime <= 0)
            return false;

        // Compute fuel ticks
        int fuelTicksPerItem = serverLevel.fuelValues().burnDuration(fuel);
        int totalFuelTicks = litTimeRemaining + (fuel.getCount() * fuelTicksPerItem);

        int maxByFuel = (int) Math.min(ticksToSimulate / recipeCookTime, totalFuelTicks / recipeCookTime);
        int maxByInput = input.getCount();

        int maxStackSize = furnace.getMaxStackSize();
        int perCraft = Math.max(1, recipeOutput.getCount());
        int maxResultCount = Math.min(maxStackSize, recipeOutput.getMaxStackSize());
        int maxByOutput;
        if (output.isEmpty()) {
            maxByOutput = maxResultCount / perCraft;
        } else if (ItemStack.isSameItemSameComponents(output, recipeOutput)) {
            maxByOutput = (maxResultCount - output.getCount()) / perCraft;
        } else // Different item in output slot, exit early
            return false;

        int realisticOperations = Math.min(Math.min(maxByFuel, maxByInput), maxByOutput);
        if (realisticOperations <= 0)
            return false;

        // Cheat: having to unlight the furnace is too much of a pain. Besides, having a
        // furnace in its "finished" state when we enter the chunk could cause issues,
        // so it's better to let it finish its final operation on its own during tick
        // time.
        realisticOperations = Math.max(0, realisticOperations - 1);

        // Consume input
        input.shrink(realisticOperations);

        // Consume fuel
        int ticksNeeded = realisticOperations * recipeCookTime;
        int ticksConsumed = Math.max(0, ticksNeeded - litTimeRemaining);
        int fuelItemsConsumed = (fuelTicksPerItem > 0) ? (int) Math.ceil((double) ticksConsumed / fuelTicksPerItem)
                : 0;
        fuel.shrink(fuelItemsConsumed);
        int newBurnTime = litTimeRemaining + (fuelItemsConsumed * fuelTicksPerItem) - ticksNeeded;

        // Apply output
        int outputDelta = realisticOperations * recipeOutput.getCount();
        if (output.isEmpty()) {
            ItemStack newOut = recipeOutput.copy();
            newOut.setCount(outputDelta);
            items.set(SLOT_RESULT, newOut);
        } else {
            output.grow(outputDelta);
        }

        // Track recipes used (XP when the player takes items), same as vanilla
        // setRecipeUsed per craft
        for (int i = 0; i < realisticOperations; i++) {
            furnace.setRecipeUsed(recipeHolder);
        }

        // Set new burn time
        litTimeRemaining = newBurnTime;

        return true; // Successfully simulated
    }
}
