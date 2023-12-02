package net.zylesh.practice;

import net.zylesh.dystellarcore.utils.Animations;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum PKillEffect {

    NONE(10, false, false, "practice.killeffect.default"),
    DEATH_ANIMATION(11, true, false, "practice.killeffect.deathanimation"),
    THUNDER(12, true, true, "practice.killeffect.thunder"),
    EXPLOSION(13, true, true, "practice.killeffect.explosion");

    PKillEffect(int slot, boolean deathPlayerEffect, boolean throwItems, String permission) {
        this.displayDeath = deathPlayerEffect;
        this.throwItems = throwItems;
        this.slot = slot;
        this.permission = permission;
    }

    private final String permission;
    private final int slot;
    private final boolean throwItems;
    private final boolean displayDeath;
    private ItemStack icon;

    public String getPermission() {
        return permission;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public boolean isDisplayDeath() {
        return displayDeath;
    }

    public boolean isThrowItems() {
        return throwItems;
    }

    public void playAnimation(CraftPlayer player, boolean throwItems) {
        Animations.Practice.playDeathAnimation(this, player, throwItems);
    }

    public static void initialize() {
        NONE.icon = new ItemStack(Material.STAINED_CLAY);
        ItemMeta metaNone = NONE.icon.getItemMeta();
        metaNone.setDisplayName(ChatColor.DARK_AQUA + "None");
        NONE.icon.setItemMeta(metaNone);

        DEATH_ANIMATION.icon = new ItemStack(Material.REDSTONE);
        ItemMeta metaDeath = DEATH_ANIMATION.icon.getItemMeta();
        metaDeath.setDisplayName(ChatColor.DARK_AQUA + "Basic Death Animation");
        DEATH_ANIMATION.icon.setItemMeta(metaDeath);

        THUNDER.icon = new ItemStack(Material.BLAZE_ROD);
        ItemMeta metaThunder = THUNDER.icon.getItemMeta();
        metaThunder.setDisplayName(ChatColor.DARK_AQUA + "Thunder");
        THUNDER.icon.setItemMeta(metaThunder);

        EXPLOSION.icon = new ItemStack(Material.TNT);
        ItemMeta metaExplosion = EXPLOSION.icon.getItemMeta();
        metaExplosion.setDisplayName(ChatColor.DARK_AQUA + "Explosion");
        EXPLOSION.icon.setItemMeta(metaExplosion);

        for (PKillEffect effect : PKillEffect.values()) {
            PApi.KILL_EFFECTS_BY_SLOT.put(effect.slot, effect);
        }
    }
}
