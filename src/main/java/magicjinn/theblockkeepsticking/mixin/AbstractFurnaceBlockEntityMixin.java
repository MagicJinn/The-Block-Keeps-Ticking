package magicjinn.theblockkeepsticking.mixin;

import java.util.Optional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin implements TickingBlockAccessor {
    // Shadowed fields for easy access to private members
    @Shadow @Final private Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed;
    @Shadow @Final private ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter;
    @Shadow private int litTimeRemaining;

    @Shadow protected DefaultedList<ItemStack> inventory;
    @Shadow @Final protected static int INPUT_SLOT_INDEX;
    @Shadow @Final protected static int FUEL_SLOT_INDEX;
    @Shadow @Final protected static int OUTPUT_SLOT_INDEX;

    @Override
    public boolean Simulate(Long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) (Object) this; // what
        if (!(world instanceof ServerWorld serverWorld))
            return false;

        // reference to input slot itemstack
        ItemStack input = inventory.get(INPUT_SLOT_INDEX);
        // reference to fuel slot itemstack
        ItemStack fuel = inventory.get(FUEL_SLOT_INDEX);
        // reference to output slot itemstack
        ItemStack output = inventory.get(OUTPUT_SLOT_INDEX);

        FuelRegistry fuelRegistry = world.getFuelRegistry();

        if (input.isEmpty()) {
            return false; // Still exit early
        }

        // Find if there's a matching recipe
        Optional<? extends RecipeEntry<? extends AbstractCookingRecipe>> recipeMatch =
                matchGetter.getFirstMatch(new SingleStackRecipeInput(input), serverWorld);
        if (recipeMatch.isEmpty())
            return false;

        // Copy recipe output
        AbstractCookingRecipe recipe = recipeMatch.get().value();
        ItemStack recipeOutput = recipe.result().copy();

        // Get recipe cook time
        int recipeCookTime = recipe.getCookingTime();
        if (recipeCookTime <= 0)
            return false;

        // Compute fuel ticks
        int fuelTicksPerItem = fuelRegistry.isFuel(fuel) ? fuelRegistry.getFuelTicks(fuel) : 0;
        int totalFuelTicks = litTimeRemaining + (fuel.getCount() * fuelTicksPerItem);

        int maxByFuel =
                (int) Math.min(ticksToSimulate / recipeCookTime, totalFuelTicks / recipeCookTime);
        int maxByInput = input.getCount();
        int maxByOutput = 0;

        if (output.isEmpty()) {
            maxByOutput = recipeOutput.getMaxCount();
        } else if (ItemStack.areItemsAndComponentsEqual(output, recipeOutput)) {
            maxByOutput = output.getMaxCount() - output.getCount();
        } else { // Different item in output slot, exit early
            return false;
        }


        int realisticOperations = Math.min(Math.min(maxByFuel, maxByInput), maxByOutput);
        if (realisticOperations <= 0)
            return false;

        // Cheat. Having to unlight the furnace is too much of a pain. Besides, having a furnace be
        // in it's "finished" state when we enter the chunk could possibly cause issues, so it's
        // better to let it finish its final operation on its own during tick time.
        realisticOperations--;

        // Consume input
        input.decrement(realisticOperations);

        // Consume fuel
        int ticksNeeded = realisticOperations * recipeCookTime;
        int ticksConsumed = Math.max(0, ticksNeeded - litTimeRemaining);
        int fuelItemsConsumed =
                (fuelTicksPerItem > 0) ? (int) Math.ceil((double) ticksConsumed / fuelTicksPerItem)
                        : 0;
        fuel.decrement(fuelItemsConsumed);
        int newBurnTime = litTimeRemaining + (fuelItemsConsumed * fuelTicksPerItem) - ticksNeeded;

        // Apply output
        if (output.isEmpty()) {
            ItemStack newOut = recipeOutput.copy();
            newOut.setCount(realisticOperations);
            inventory.set(OUTPUT_SLOT_INDEX, newOut);
        } else {
            output.increment(realisticOperations);
        }

        RecipeEntry<?> entry = (RecipeEntry<?>) recipeMatch.get();
        for (int i = 0; i < realisticOperations; i++) {
            furnace.setLastRecipe(entry);
        }

        // Set new burn time
        litTimeRemaining = newBurnTime;

        return true; // Successfully simulated
    }
}

