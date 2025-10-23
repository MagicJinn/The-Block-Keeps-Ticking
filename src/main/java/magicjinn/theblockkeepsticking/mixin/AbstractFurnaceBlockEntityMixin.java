package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import magicjinn.theblockkeepsticking.accessors.AbstractFurnaceAccessor;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.collection.DefaultedList;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin implements AbstractFurnaceAccessor {
    // Shadowed fields for easy access to private members
    @Shadow @Final private Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed;
    @Shadow @Final private ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter;
    @Shadow private int litTimeRemaining;

    @Shadow protected DefaultedList<ItemStack> inventory;
    @Shadow @Final protected static int INPUT_SLOT_INDEX;
    @Shadow @Final protected static int FUEL_SLOT_INDEX;
    @Shadow @Final protected static int OUTPUT_SLOT_INDEX;

    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }

    public int getInputSlotIndex() {
        return INPUT_SLOT_INDEX;
    }

    public int getFuelSlotIndex() {
        return FUEL_SLOT_INDEX;
    }

    public int getOutputSlotIndex() {
        return OUTPUT_SLOT_INDEX;
    }

    public ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> getMatchGetter() {
        return matchGetter;
    }

    public int getCurrentlyBurningFuelTimeRemaining() {
        return litTimeRemaining;
    }

    public void setCurrentlyBurningFuelTimeRemaining(int ticks) {
        this.litTimeRemaining = Math.max(0, ticks);
    }
}
