package magicjinn.blockkeepsticking.mixin;

import magicjinn.blockkeepsticking.BlockKeepsTicking;
import magicjinn.blockkeepsticking.api.FurnaceAccess;
import magicjinn.blockkeepsticking.simulator.FurnaceSimulator;
import magicjinn.blockkeepsticking.util.BlockEntityUtils;
import magicjinn.blockkeepsticking.util.ContainerUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin implements FurnaceAccess {
	@Shadow protected DefaultedList<ItemStack> inventory;
	@Shadow protected int burnTime;
	@Shadow protected int fuelTime;
	@Shadow protected int cookTime;
	@Shadow protected int cookTimeTotal;
	@Shadow @Final private RecipeType<? extends AbstractCookingRecipe> recipeType;
	@Shadow @Final private Object2IntOpenHashMap<Identifier> recipesUsed;
	
	@Shadow protected abstract int getFuelTime(ItemStack fuel);
	
	@Override
	public FurnaceSimulator createSimulator(World world) {
		return new FurnaceSimulator(ContainerUtils.createContainer(inventory), burnTime, fuelTime, cookTime,
				cookTimeTotal,world);
	}
	
	@Override
	public int getFuelBurnTime(ItemStack fuel) {
		return getFuelTime(fuel);
	}
	
	@Override
	public RecipeType<? extends AbstractCookingRecipe> getRecipeType() {
		return recipeType;
	}
	
	@Override
	public void apply(World world, BlockPos pos, BlockState state, FurnaceSimulator simulator) {
		boolean oldLit = burnTime > 0;
		boolean dataChanged = simulator.isDataChanged();
		
		if (dataChanged) {
			// Actualizar inventario
			ContainerUtils.extractContainer(simulator.getContainer(), inventory);
			
			// Actualizar estados
			burnTime = simulator.getLitTime();
			fuelTime = simulator.getLitDuration();
			cookTime = simulator.getCookingProgress();
			cookTimeTotal = simulator.getCookingTotalTime();
			
			// Actualizar recetas usadas
			for (Object2IntMap.Entry<Identifier> entry : simulator.getRecipesUsed().object2IntEntrySet()) {
				recipesUsed.addTo(entry.getKey(), entry.getIntValue());
			}
			
			BlockKeepsTicking.LOGGER.info("Aplying changes on furnace {}: burnTime={}, cookTime={}/{}", pos, burnTime,
					cookTime, cookTimeTotal);
			
			// Actualizar estado de encendido si cambió
			if (oldLit != burnTime > 0) {
				state = state.with(AbstractFurnaceBlock.LIT, burnTime > 0);
				world.setBlockState(pos, state, 3);
			}
			
			// Marcar cambios
			BlockEntityUtils.markChanged(world, pos, state);
		}
	}
}
