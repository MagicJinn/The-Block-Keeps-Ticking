package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.collection.DefaultedList;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
    @Shadow @Final private Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed;
    @Shadow @Final private ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter;
    @Shadow private int litTimeRemaining;
    @Shadow private int litTotalTime;
    @Shadow private int cookingTimeSpent;
    @Shadow private int cookingTotalTime;
    @Shadow protected DefaultedList<ItemStack> inventory;
}
