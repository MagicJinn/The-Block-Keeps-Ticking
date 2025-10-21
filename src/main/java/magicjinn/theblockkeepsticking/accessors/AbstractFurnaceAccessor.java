package magicjinn.theblockkeepsticking.accessors;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.util.collection.DefaultedList;

public interface AbstractFurnaceAccessor extends TickingBlockAccessor {
    public DefaultedList<ItemStack> getInventory();

    public int getInputSlotIndex();

    public int getFuelSlotIndex();

    public int getOutputSlotIndex();

    public ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> getMatchGetter();

    public int getCurrentlyBurningFuelTimeRemaining();

    public void setCurrentlyBurningFuelTimeRemaining(int ticks);
}
