package magicjinn.blockkeepsticking.util;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import java.util.List;

public class ContainerUtils {
	public static Inventory createContainer(List<ItemStack> items) {
		SimpleInventory container = new SimpleInventory(items.size());
		for (int i = 0; i < items.size(); i++) {
			container.setStack(i, items.get(i).copy());
		}
		return container;
	}
	
	public static void extractContainer(Inventory container, DefaultedList<ItemStack> items) {
		int size = Math.min(container.size(), items.size());
		for (int i = 0; i < size; i++) {
			items.set(i, container.getStack(i));
		}
	}
	
	public static void extractContainer(DefaultedList<ItemStack> source, DefaultedList<ItemStack> target) {
		int size = Math.min(source.size(), target.size());
		for (int i = 0; i < size; i++) {
			target.set(i, source.get(i));
		}
	}
	
	public static DefaultedList<ItemStack> copyItemList(DefaultedList<ItemStack> items) {
		DefaultedList<ItemStack> newList = DefaultedList.ofSize(items.size(), ItemStack.EMPTY);
		for (int i = 0; i < items.size(); i++) {
			newList.set(i, items.get(i).copy());
		}
		return newList;
	}
}
