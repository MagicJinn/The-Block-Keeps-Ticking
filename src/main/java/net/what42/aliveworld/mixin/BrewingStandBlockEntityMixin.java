package net.what42.aliveworld.mixin;

import net.what42.aliveworld.api.BrewingStandAccess;
import net.what42.aliveworld.simulator.BrewingSimulator;
import net.what42.aliveworld.util.ContainerUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin implements BrewingStandAccess {
	@Shadow private int brewTime;
	@Shadow private int fuel;
	@Shadow private net.minecraft.util.collection.DefaultedList<net.minecraft.item.ItemStack> inventory;
	
	@Override
	public BrewingSimulator createSimulator() {
		return new BrewingSimulator(ContainerUtils.createContainer(inventory), brewTime, fuel);
	}
	
	@Override
	public void apply(World world, BlockPos pos, BlockState state, BrewingSimulator simulator) {
		if (simulator.isDataChanged()) {
			ContainerUtils.extractContainer(simulator.getItems(), inventory);
			brewTime = simulator.getBrewTime();
			fuel = simulator.getFuel();
			((BrewingStandBlockEntity)(Object)this).markDirty();
		}
	}
}
