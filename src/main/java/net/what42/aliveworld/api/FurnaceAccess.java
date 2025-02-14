package net.what42.aliveworld.api;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.what42.aliveworld.simulator.FurnaceSimulator;

public interface FurnaceAccess {
	int getFuelBurnTime(ItemStack stack);
	RecipeType<? extends AbstractCookingRecipe> getRecipeType();
	FurnaceSimulator createSimulator();
	void apply(World world, BlockPos pos, BlockState state, FurnaceSimulator simulator);
}
