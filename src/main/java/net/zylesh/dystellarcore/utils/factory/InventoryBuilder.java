package net.zylesh.dystellarcore.utils.factory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryBuilder {

	public static Inventory packPrompt(ItemStack confirm, ItemStack deny, ItemStack info) {
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "Resource Pack Prompt");

		inv.setItem(2, confirm);
        inv.setItem(4, info);
        inv.setItem(6, deny);

		return inv;
	}
}
