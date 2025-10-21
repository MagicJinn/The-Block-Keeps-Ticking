package magicjinn.theblockkeepsticking.blocks;

import java.util.Optional;
import magicjinn.theblockkeepsticking.accessors.AbstractFurnaceAccessor;
import magicjinn.theblockkeepsticking.framework.ProcessingBlock;
import magicjinn.theblockkeepsticking.util.TickingResult;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class TickingAbstractFurnaceBlockEntity extends ProcessingBlock {
    public static final TickingAbstractFurnaceBlockEntity INSTANCE =
            new TickingAbstractFurnaceBlockEntity();

    @Override
    public Class<AbstractFurnaceBlockEntity> getType() {
        return AbstractFurnaceBlockEntity.class;
    }

    @Override
    public void Simulate(BlockEntity blockInstance, Long ticksToSimulate) {
        if (blockInstance == null || !(blockInstance instanceof AbstractFurnaceBlockEntity))
            return;

        AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) blockInstance;
        World world = furnace.getWorld();
        FuelRegistry fuelRegistry = world.getFuelRegistry();
        AbstractFurnaceAccessor accessor = (AbstractFurnaceAccessor) furnace;
        DefaultedList<ItemStack> inventory = accessor.getInventory();

        ItemStack inputItemStack = inventory.get(accessor.getInputSlotIndex());
        ItemStack fuelItemStack = inventory.get(accessor.getFuelSlotIndex());
        ItemStack outputItemStack = inventory.get(accessor.getOutputSlotIndex());

        int currentlyBurningFuelTimeRemaining = accessor.getCurrentlyBurningFuelTimeRemaining();

        // Get the cooking time for the current recipe
        int recipeCookTime = AbstractFurnaceBlockEntity.getCookTime((ServerWorld) world, furnace);

        if (recipeCookTime <= 0) {
            return; // No valid recipe
        }

        // Calculate max operations based on fuel availability
        int totalFuelTicks = currentlyBurningFuelTimeRemaining;
        int availableFuelItems = fuelItemStack.getCount();
        int fuelTicksPerItem = 0;


        if (fuelRegistry.isFuel(fuelItemStack)) {
            fuelTicksPerItem = fuelRegistry.getFuelTicks(fuelItemStack);
            totalFuelTicks += availableFuelItems * fuelTicksPerItem;
        }

        int maxOperationsByFuel =
                (int) Math.min(ticksToSimulate / recipeCookTime, totalFuelTicks / recipeCookTime);

        // Calculate max operations based on input items
        int maxOperationsByInput = inputItemStack.getCount();

        // Calculate max operations based on output space
        int maxOperationsByOutput = outputItemStack.isEmpty() ? outputItemStack.getMaxCount()
                : (outputItemStack.getMaxCount() - outputItemStack.getCount());

        // The actual number of operations is the minimum of all constraints
        int actualOperations = Math.min(Math.min(maxOperationsByFuel, maxOperationsByInput),
                maxOperationsByOutput);

        if (actualOperations <= 0) {
            return; // Can't perform any operations
        }

        // Calculate fuel consumption
        int fuelTicksNeeded = actualOperations * recipeCookTime;
        int fuelTicksConsumed = Math.max(0, fuelTicksNeeded - currentlyBurningFuelTimeRemaining);
        int fuelItemsToConsume = 0;
        if (fuelTicksPerItem > 0) {
            fuelItemsToConsume = (fuelTicksConsumed + fuelTicksPerItem - 1) / fuelTicksPerItem; // Ceiling
        }
        // division
        // Update fuel state
        int newBurnTimeRemaining = currentlyBurningFuelTimeRemaining
                + (fuelItemsToConsume * fuelTicksPerItem) - fuelTicksNeeded;

        // Apply changes to inventory
        inputItemStack.decrement(actualOperations);
        fuelItemStack.decrement(fuelItemsToConsume);

        // Handle output (you'll need to get the actual recipe result here)
        // This is a simplified version - you'd need to get the actual output from the recipe
        if (outputItemStack.isEmpty()) {
            // Set output to recipe result * actualOperations
            var recipeMatch = accessor.getMatchGetter()
                    .getFirstMatch(new SingleStackRecipeInput(inputItemStack), (ServerWorld) world);
            if (recipeMatch.isPresent()) {
                AbstractCookingRecipe recipe = recipeMatch.get().value();
                ItemStack recipeOutput = recipe.result();
                recipeOutput.setCount(actualOperations);
                inventory.set(accessor.getOutputSlotIndex(), recipeOutput);
            }
        } else {
            outputItemStack.increment(actualOperations);
        }

        // Update the furnace's burn time state
        accessor.setCurrentlyBurningFuelTimeRemaining(newBurnTimeRemaining);
    }
}
