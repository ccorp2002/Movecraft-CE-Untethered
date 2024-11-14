package net.countercraft.movecraft.util;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemUtils {
  protected static final ItemStack airItem = new ItemStack(Material.AIR,0);

  public static ItemStack reduceItemStack(ItemStack stack, int amntChange) {
    if (stack.getAmount() > 1) {
        int amount = stack.getAmount();
        amntChange *= -1;
        if (amount + amntChange > 0) {
            stack.setAmount(amount + amntChange);
        } else {
            stack = airItem;
        }
    } else {
            stack = airItem;
    }
    return stack;
  }
  public static boolean isSimilar(ItemStack i1, ItemStack i2) {
        if (i1 == null) return false;
        if (i2 == null) return false;
        if (i1.getType() == i2.getType()) {
            if (i1.hasItemMeta() && i2.hasItemMeta()) {
                if (i1.getItemMeta().hasCustomModelData() && i2.getItemMeta().hasCustomModelData()) {
                    if (i1.getItemMeta().getCustomModelData() == i2.getItemMeta().getCustomModelData()) return true;
                }
            } else {
                if (!i1.hasItemMeta() && !i2.hasItemMeta()) {
                    return true;
                }
            }
        }
        return false;
    }
}