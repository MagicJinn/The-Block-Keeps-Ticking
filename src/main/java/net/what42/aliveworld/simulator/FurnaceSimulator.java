package net.what42.aliveworld.simulator;

import net.what42.aliveworld.AliveWorld;
import net.what42.aliveworld.api.FurnaceAccess;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;

public class FurnaceSimulator {
	private final Inventory items;
	private int litTime;
	private int litDuration;
	private int cookingProgress;
	private int cookingTotalTime;
	private boolean dataChanged;
	private final Object2IntOpenHashMap<Identifier> recipesUsed = new Object2IntOpenHashMap<>();
	
	public FurnaceSimulator(Inventory items, int litTime, int litDuration, int cookingProgress, int cookingTotalTime) {
		this.items = items;
		this.litTime = litTime;
		this.litDuration = litDuration;
		this.cookingProgress = cookingProgress;
		this.cookingTotalTime = cookingTotalTime;
	}
	
	public boolean hasItemsToProcess() {
		return !ingredient().isEmpty() && (hasFuel() || litTime > 0);
	}
	
	public void simulateFinalResult(int tickPassed, World world, FurnaceAccess furnace) {
		if (!hasItemsToProcess()) return;
		
		@SuppressWarnings("unchecked")
		RecipeType<AbstractCookingRecipe> type = (RecipeType<AbstractCookingRecipe>) furnace.getRecipeType();
		Optional<AbstractCookingRecipe> recipe = world.getRecipeManager().getFirstMatch(type, new SimpleInventory(ingredient()), world);
		
		if (recipe.isEmpty()) return;
		
		AbstractCookingRecipe cookingRecipe = recipe.get();
		cookingTotalTime = cookingRecipe.getCookTime();
		ItemStack recipeResult = cookingRecipe.getOutput();
		
		// Procesar al menos una operación completa
		int forcedTicks = Math.max(tickPassed, cookingTotalTime);
		int maxOperations = calculateMaxOperations(furnace, forcedTicks, cookingRecipe);
		
		if (maxOperations > 0) {
			applyFinalResult(maxOperations, recipeResult, cookingRecipe);
			dataChanged = true;
			AliveWorld.LOGGER.info("Applying final result: {} operations", maxOperations);
		}
	}
	
	private int calculateMaxOperations(FurnaceAccess furnace, int tickPassed, AbstractCookingRecipe recipe) {
		// Ingredientes disponibles
		int ingredientLimit = ingredient().getCount();
		
		// Espacio disponible para resultados
		int maxStackSize = getMaxStackSize(items, recipe.getOutput());
		int currentResultCount = result().isEmpty() ? 0 : result().getCount();
		int outputLimit = (maxStackSize - currentResultCount) / recipe.getOutput().getCount();
		
		// Combustible disponible
		int fuelLimit = calculateFuelLimit(furnace, recipe.getCookTime());
		
		// Tiempo total disponible (incluyendo progreso actual)
		int timeLimit = (tickPassed + cookingProgress) / recipe.getCookTime();
		
		// Garantizar al menos una operación si hay suficientes recursos
		if (ingredientLimit > 0 && outputLimit > 0 && fuelLimit > 0) {
			timeLimit = Math.max(1, timeLimit);
		}
		
		return Math.min(Math.min(ingredientLimit, outputLimit), 
			   Math.min(fuelLimit, timeLimit));
	}
	
	private int calculateFuelLimit(FurnaceAccess furnace, int recipeCookTime) {
		int totalFuelTicks = litTime;
		ItemStack fuelStack = fuel();
		
		if (!fuelStack.isEmpty()) {
			int burnTime = furnace.getFuelBurnTime(fuelStack);
			if (burnTime > 0) {
				totalFuelTicks += burnTime * fuelStack.getCount();
			}
		}
		
		return totalFuelTicks / recipeCookTime;
	}
	
	private void applyFinalResult(int operations, ItemStack recipeResult, AbstractCookingRecipe recipe) {
		// Consumir ingredientes
		ingredient().decrement(operations);
		
		// Agregar resultados
		if (result().isEmpty()) {
			items.setStack(2, recipeResult.copy());
			result().setCount(operations * recipeResult.getCount());
		} else {
			result().increment(operations * recipeResult.getCount());
		}
		
		// Consumir combustible
		int totalCookingTime = operations * cookingTotalTime - cookingProgress;
		consumeFuelForTime(totalCookingTime);
		
		// Registrar recetas usadas
		recipesUsed.addTo(recipe.getId(), operations);
		
		// Actualizar progreso
		cookingProgress = 0;
	}
	
	private void consumeFuelForTime(int requiredTicks) {
		if (litTime >= requiredTicks) {
			litTime -= requiredTicks;
			return;
		}
		
		requiredTicks -= litTime;
		litTime = 0;
		
		if (requiredTicks > 0 && !fuel().isEmpty()) {
			int burnTime = fuel().isEmpty() ? 0 : getFuelBurnTime(fuel());
			if (burnTime > 0) {
				int fuelNeeded = (requiredTicks + burnTime - 1) / burnTime;
				fuel().decrement(fuelNeeded);
				litTime = fuelNeeded * burnTime - requiredTicks;
				litDuration = burnTime;
			}
		}
	}
	
	// ... resto de métodos auxiliares sin cambios ...
	private boolean hasFuel() {
		return litTime > 0 || !fuel().isEmpty();
	}
	
	private int getMaxStackSize(Inventory container, ItemStack stack) {
		return Math.min(container.getMaxCountPerStack(), stack.getMaxCount());
	}
	
	private ItemStack ingredient() {
		return items.getStack(0);
	}
	
	private ItemStack fuel() {
		return items.getStack(1);
	}
	
	private ItemStack result() {
		return items.getStack(2);
	}
	
	private int getFuelBurnTime(ItemStack stack) {
		return litDuration;  // Usamos el último valor conocido de duración del combustible
	}
	
	public Inventory getContainer() {
		return items;
	}
	
	public int getLitTime() {
		return litTime;
	}
	
	public int getLitDuration() {
		return litDuration;
	}
	
	public int getCookingProgress() {
		return cookingProgress;
	}
	
	public int getCookingTotalTime() {
		return cookingTotalTime;
	}
	
	public boolean isDataChanged() {
		return dataChanged;
	}
	
	public Object2IntOpenHashMap<Identifier> getRecipesUsed() {
		return recipesUsed;
	}
	
	private static class SimpleInventory implements Inventory {
		private final ItemStack stack;
		
		SimpleInventory(ItemStack stack) {
			this.stack = stack;
		}
		
		@Override
		public int size() {
			return 1;
		}
		
		@Override
		public boolean isEmpty() {
			return stack.isEmpty();
		}
		
		@Override
		public ItemStack getStack(int slot) {
			return slot == 0 ? stack : ItemStack.EMPTY;
		}
		
		@Override
		public ItemStack removeStack(int slot, int amount) {
			return ItemStack.EMPTY;
		}
		
		@Override
		public ItemStack removeStack(int slot) {
			return ItemStack.EMPTY;
		}
		
		@Override
		public void setStack(int slot, ItemStack stack) {}
		
		@Override
		public void markDirty() {}
		
		@Override
		public boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity player) {
			return true;
		}
		
		@Override
		public void clear() {}
	}
}
