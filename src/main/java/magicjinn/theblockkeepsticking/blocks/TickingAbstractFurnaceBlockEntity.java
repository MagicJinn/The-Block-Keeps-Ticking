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

        int inputItemTotalCount = inputItemStack.getCount();
        int outputItemTotalCount = outputItemStack.getCount();
        int realisticOutputCapacity =
                Math.min(inputItemTotalCount, outputItemStack.getMaxCount() - outputItemTotalCount);

        // ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe>
        // matchGetter =
        // accessor.getMatchGetter();

        // Optional<? extends RecipeEntry<? extends AbstractCookingRecipe>> recipeMatch =
        // matchGetter
        // .getFirstMatch(new SingleStackRecipeInput(inputItemStack), (ServerWorld) world);

        // Get the cooking time for the current recipe
        // This implicity uses the item in the input slot
        int recipeCookTime = AbstractFurnaceBlockEntity.getCookTime((ServerWorld) world, furnace);
        TickingResult result = CalculateOperations(ticksToSimulate, recipeCookTime,
                currentlyBurningFuelTimeRemaining);
        realisticOutputCapacity = Math.min(realisticOutputCapacity, result.cycles);
        currentlyBurningFuelTimeRemaining = 0;

        int amountOfFuelItemsUsed = 0;
        if (fuelRegistry.isFuel(fuelItemStack)) { // bucket check
            int fuelTicksForItem = fuelRegistry.getFuelTicks(fuelItemStack);
            TickingResult fuelResult = CalculateOperations(ticksToSimulate, fuelTicksForItem,
                    currentlyBurningFuelTimeRemaining);

            amountOfFuelItemsUsed = fuelResult.cycles;
            if (fuelResult.remainder > 0) {
                // If there is a remainder, we need to use one more item
                amountOfFuelItemsUsed++;
                currentlyBurningFuelTimeRemaining += fuelResult.remainder;
            }
        }
    }
}
