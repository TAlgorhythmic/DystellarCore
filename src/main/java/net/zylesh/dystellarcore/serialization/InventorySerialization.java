package net.zylesh.dystellarcore.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class InventorySerialization {

    @SuppressWarnings("deprecation")
    public static String inventoryToString(ItemStack[] invInventory) {
        StringBuilder serialization = new StringBuilder(invInventory.length + ";");
        for (int i = 0; i < invInventory.length; i++) {
            ItemStack is = invInventory[i];
            if (is != null) {
                StringBuilder serializedItemStack = new StringBuilder();

                String isType = String.valueOf(is.getType().getId());
                serializedItemStack.append("t@").append(isType);

                if (is.getDurability() != 0) {
                    String isDurability = String.valueOf(is.getDurability());
                    serializedItemStack.append(":d@").append(isDurability);
                }

                if (is.getAmount() != 1) {
                    String isAmount = String.valueOf(is.getAmount());
                    serializedItemStack.append(":a@").append(isAmount);
                }

                Map<Enchantment,Integer> isEnch = is.getEnchantments();
                if (!isEnch.isEmpty()) {
                    for (Map.Entry<Enchantment,Integer> ench : isEnch.entrySet()) {
                        serializedItemStack.append(":e@").append(ench.getKey().getId()).append("@").append(ench.getValue());
                    }
                }

                if (is.getItemMeta().hasDisplayName()) {
                    String isMeta = is.getItemMeta().getDisplayName();
                    serializedItemStack.append(":m@").append(isMeta);
                }

                serialization.append(i).append("#").append(serializedItemStack).append(";");
            }
        }
        return serialization.toString();
    }

    @SuppressWarnings("deprecation")
    public static ItemStack[] stringToInventory(String invString) {
        String[] serializedBlocks = invString.split(";");
        ItemStack[] deserializedInventory = new ItemStack[serializedBlocks.length];

        for (int i = 1; i < serializedBlocks.length; i++) {
            String[] serializedBlock = serializedBlocks[i].split("#");

            ItemStack is = null;
            boolean createdItemStack = false;

            String[] serializedItemStack = serializedBlock[1].split(":");
            for (String itemInfo : serializedItemStack) {
                String[] itemAttribute = itemInfo.split("@");
                if (itemAttribute[0].equals("t")) {
                    is = new ItemStack(Material.getMaterial(Integer.parseInt(itemAttribute[1])));
                    createdItemStack = true;
                } else if (itemAttribute[0].equals("d") && createdItemStack) {
                    is.setDurability(Short.parseShort(itemAttribute[1]));
                } else if (itemAttribute[0].equals("a") && createdItemStack) {
                    is.setAmount(Integer.parseInt(itemAttribute[1]));
                } else if (itemAttribute[0].equals("e") && createdItemStack) {
                    is.addEnchantment(Enchantment.getById(Integer.parseInt(itemAttribute[1])), Integer.parseInt(itemAttribute[2]));
                } else if (itemAttribute[0].equals("m") && createdItemStack) {
                    ItemMeta meta = is.getItemMeta();
                    meta.setDisplayName(itemAttribute[1]);
                    is.setItemMeta(meta);
                }
            }
            deserializedInventory[i] = is;
        }

        return deserializedInventory;
    }
}
