package magicjinn.blockkeepsticking.simulator;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.world.World;

public class BrewingSimulator {
	private final Inventory items;
	private int brewTime;
	private int fuel;
	private boolean dataChanged;
	private final World world;
	
	public BrewingSimulator(Inventory items, int brewTime, int fuel, World world) {
		this.items = items;
		this.brewTime = brewTime;
		this.fuel = fuel;
		this.world = world;
	}
	
	public boolean canBrew() {
		if (fuel <= 0 || items.getStack(3).isEmpty()) {
			return false;
		}
		
		BrewingRecipeRegistry registry = world.getBrewingRecipeRegistry();
		ItemStack ingredient = items.getStack(3);
		for (int i = 0; i < 3; i++) {
			ItemStack potion = items.getStack(i);
			if (!potion.isEmpty() && registry.isValidIngredient(ingredient)) {
				return true;
			}
		}
		return false;
	}
	
	public void simulateFinalResult(int ticksPassed) {
		if (!canBrew()) return;
		
		// Calculate how many complete brewing operations can occur
		// comment already translated}}
		int totalTime = brewTime + ticksPassed;
		int completedBrews = totalTime / 400; // 400 ticks per brewing operation
		int fuelNeeded = (completedBrews + 19) / 20; // 20 operations per fuel unit

		if (completedBrews > 0 && fuel >= fuelNeeded) {
			BrewingRecipeRegistry registry = world.getBrewingRecipeRegistry();
			ItemStack ingredient = items.getStack(3);
			boolean didBrew = false;
			
			// Process all potions at once
			for (int i = 0; i < 3; i++) {
				ItemStack potion = items.getStack(i);
				if (!potion.isEmpty() && registry.isValidIngredient(ingredient)) {
					ItemStack result = registry.craft(ingredient, potion);
					if (!result.isEmpty()) {
						items.setStack(i, result);
						didBrew = true;
					}
				}
			}
			
			if (didBrew) {
				// Consume ingredient and fuel
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