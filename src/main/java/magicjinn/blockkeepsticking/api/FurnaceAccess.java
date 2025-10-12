package magicjinn.blockkeepsticking.api;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import magicjinn.blockkeepsticking.simulator.FurnaceSimulator;

public interface FurnaceAccess {
	int getFuelBurnTime(ItemStack stack, World world);
	RecipeType<? extends AbstractCookingRecipe> getRecipeType();

	FurnaceSimulator createSimulator(World world);
	void apply(World world, BlockPos pos, BlockState state, FurnaceSimulator simulator);
}
