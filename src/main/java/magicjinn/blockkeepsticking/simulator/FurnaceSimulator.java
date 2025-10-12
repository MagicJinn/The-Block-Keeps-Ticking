package magicjinn.blockkeepsticking.simulator;

import magicjinn.blockkeepsticking.BlockKeepsTicking;
import magicjinn.blockkeepsticking.api.FurnaceAccess;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

public class FurnaceSimulator {
	private final Inventory items;
	private int litTime;
	private int litDuration;
	private int cookingProgress;
	private int cookingTotalTime;
	private boolean dataChanged;
	private final World world;
	private final Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed =
			new Reference2IntOpenHashMap<RegistryKey<Recipe<?>>>();
	private ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter;

	
	public FurnaceSimulator(Inventory items, int litTime, int litDuration, int cookingProgress, int cookingTotalTime,
			World world) {
		this.items = items;
		this.litTime = litTime;
		this.litDuration = litDuration;
		this.cookingProgress = cookingProgress;
		this.cookingTotalTime = cookingTotalTime;
		this.world = world;
	}

	
	public boolean hasItemsToProcess() {
		return !ingredient().isEmpty() && (hasFuel() || litTime > 0);
	}

	public ItemStack getOutputCheat(AbstractCookingRecipe recipe) {
		return recipe.craft(new SingleStackRecipeInput(ingredient()),
				(RegistryWrapper.WrapperLookup) world.getRegistryManager());
	}

	public void simulateFinalResult(int tickPassed, World world, FurnaceAccess furnace) {
		if (!hasItemsToProcess()) return;
		
		@SuppressWarnings("unchecked")
		RecipeType<AbstractCookingRecipe> type =
				(RecipeType<AbstractCookingRecipe>) furnace.getRecipeType();

		matchGetter = ServerRecipeManager.createCachedMatchGetter(type);
		SingleStackRecipeInput input = new SingleStackRecipeInput(ingredient());
		@SuppressWarnings("rawtypes")
		RecipeEntry recipe = matchGetter.getFirstMatch(input, (ServerWorld) world).orElse(null);


		if (recipe == null)
			return;

		AbstractCookingRecipe cookingRecipe = (AbstractCookingRecipe) recipe.value();
		cookingTotalTime = cookingRecipe.getCookingTime();
		ItemStack recipeResult = getOutputCheat(cookingRecipe);
		
		// Process at least one complete operation
		int forcedTicks = Math.max(tickPassed, cookingTotalTime);
		int maxOperations = calculateMaxOperations(furnace, forcedTicks, cookingRecipe);
		
		if (maxOperations > 0) {
			@SuppressWarnings("unchecked")
			RecipeEntry<AbstractCookingRecipe> typedRecipe = recipe;
			applyFinalResult(maxOperations, recipeResult, typedRecipe);
			dataChanged = true;
			BlockKeepsTicking.LOGGER.info("Applying final result: {} operations", maxOperations);
		}
	}
	
	private int calculateMaxOperations(FurnaceAccess furnace, int tickPassed, AbstractCookingRecipe recipe) {
		// Ingredientes disponibles
		int ingredientLimit = ingredient().getCount();
		
		// Espacio disponible para resultados
		int maxStackSize = getMaxStackSize(items, getOutputCheat(recipe));
		int currentResultCount = result().isEmpty() ? 0 : result().getCount();
		int outputLimit = (maxStackSize - currentResultCount) / getOutputCheat(recipe).getCount();
		
		// Combustible disponible
		int fuelLimit = calculateFuelLimit(furnace, recipe.getCookingTime());
		
		// Tiempo total disponible (incluyendo progreso actual)
		int timeLimit = (tickPassed + cookingProgress) / recipe.getCookingTime();
		
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
			int burnTime = furnace.getFuelBurnTime(fuelStack, world);
			if (burnTime > 0) {
				totalFuelTicks += burnTime * fuelStack.getCount();
			}
		}
		
		return totalFuelTicks / recipeCookTime;
	}
	
	private void applyFinalResult(int operations, ItemStack recipeResult,
			RecipeEntry<AbstractCookingRecipe> recipe) {
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
		recipesUsed.addTo(recipe.id(), operations);
		
		// Update progress
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
	
	public Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> getRecipesUsed() {
		return recipesUsed;
	}
	
	// Not required anymore, I think
	// private static class SimpleInventory implements Inventory {
	// private final ItemStack stack;
		
	// SimpleInventory(ItemStack stack) {
	// this.stack = stack;
	// }
		
	// @Override
	// public int size() {
	// return 1;
	// }
		
	// @Override
	// public boolean isEmpty() {
	// return stack.isEmpty();
	// }
		
	// @Override
	// public ItemStack getStack(int slot) {
	// return slot == 0 ? stack : ItemStack.EMPTY;
	// }
		
	// @Override
	// public ItemStack removeStack(int slot, int amount) {
	// return ItemStack.EMPTY;
	// }
		
	// @Override
	// public ItemStack removeStack(int slot) {
	// return ItemStack.EMPTY;
	// }
		
	// @Override
	// public void setStack(int slot, ItemStack stack) {}
		
	// @Override
	// public void markDirty() {}
		
	// @Override
	// public boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity player) {
	// return true;
	// }
		
	// @Override
	// public void clear() {}
	// }
}
