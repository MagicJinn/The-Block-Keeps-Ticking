package magicjinn.blockkeepsticking.simulator;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.BrewingRecipeRegistry;

public class BrewingSimulator {
	private final Inventory items;
	private int brewTime;
	private int fuel;
	private boolean dataChanged;
	
	public BrewingSimulator(Inventory items, int brewTime, int fuel) {
		this.items = items;
		this.brewTime = brewTime;
		this.fuel = fuel;
	}
	
	public boolean canBrew() {
		if (fuel <= 0 || items.getStack(3).isEmpty()) {
			return false;
		}
		
		ItemStack ingredient = items.getStack(3);
		for (int i = 0; i < 3; i++) {
			ItemStack potion = items.getStack(i);
			if (!potion.isEmpty() && BrewingRecipeRegistry.isValidIngredient(ingredient)) {
				return true;
			}
		}
		return false;
	}
	
	public void simulateFinalResult(int ticksPassed) {
		if (!canBrew()) return;
		
		// Calcula cuántas operaciones completas de brewing pueden ocurrir
		int totalTime = brewTime + ticksPassed;
		int completedBrews = totalTime / 400;  // 400 ticks por operación de brewing
		int fuelNeeded = (completedBrews + 19) / 20;  // 20 operaciones por unidad de fuel

		if (completedBrews > 0 && fuel >= fuelNeeded) {
			ItemStack ingredient = items.getStack(3);
			boolean didBrew = false;
			
			// Procesar todas las pociones de una vez
			for (int i = 0; i < 3; i++) {
				ItemStack potion = items.getStack(i);
				if (!potion.isEmpty() && BrewingRecipeRegistry.isValidIngredient(ingredient)) {
					ItemStack result = BrewingRecipeRegistry.craft(ingredient, potion);
					if (!result.isEmpty()) {
						items.setStack(i, result);
						didBrew = true;
					}
				}
			}
			
			if (didBrew) {
				// Consumir ingrediente y combustible
				ingredient.decrement(1);
				items.setStack(3, ingredient);
				fuel -= fuelNeeded;
				brewTime = totalTime % 400;
				dataChanged = true;
			}
		} else {
			brewTime = totalTime % 400;
		}
	}
	
	public Inventory getItems() {
		return items;
	}
	
	public int getBrewTime() {
		return brewTime;
	}
	
	public int getFuel() {
		return fuel;
	}
	
	public boolean isDataChanged() {
		return dataChanged;
	}
}
