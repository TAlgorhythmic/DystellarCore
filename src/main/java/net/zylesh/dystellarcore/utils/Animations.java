package net.zylesh.dystellarcore.utils;

import net.minecraft.server.v1_7_R4.*;
import net.zylesh.practice.PKillEffect;
import net.zylesh.practice.PUser;
import net.zylesh.practice.practicecore.core.GameFFA;
import net.zylesh.practice.practicecore.core.GameVersus;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class Animations {

    public static class Practice {
        public static void playDeathAnimation(PKillEffect killEffect, CraftPlayer player, boolean throwItems) {
            PlayerInventory playerInventory = player.getInventory();
            PUser playerUser = PUser.get(player);
            World w = player.getWorld();
            if (throwItems && killEffect.isThrowItems()) {
                List<ItemStack> items = new ArrayList<>();
                if (playerInventory.getItemInHand() != null)
                    items.add(playerInventory.getItemInHand());
                if (playerInventory.getHelmet() != null)
                    items.add(playerInventory.getHelmet());
                if (playerInventory.getChestplate() != null)
                    items.add(playerInventory.getChestplate());
                if (playerInventory.getLeggings() != null)
                    items.add(playerInventory.getLeggings());
                if (playerInventory.getBoots() != null)
                    items.add(playerInventory.getBoots());
                if (playerUser.getLastGame().getLadder().getName().contains("UHC")) {
                    if (!playerInventory.getItemInHand().getType().equals(Material.FISHING_ROD))
                        items.add(new ItemStack(Material.FISHING_ROD));
                }
                for (ItemStack s : items) {
                    if (s != null && !s.getType().equals(Material.AIR)) {
                        w.dropItemNaturally(new Location(w, player.getLocation().getX(), player.getLocation().getY() + 1.0, player.getLocation().getZ()), s);
                    }
                }
            }
            player.getInventory().clear();
            player.getInventory().setHelmet(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setBoots(null);
            EntityPlayer entityPlayer = player.getHandle();
            if (killEffect.isDisplayDeath()) {
                for (PUser psld : playerUser.getLastGame().getSpectators()) {
                    CraftPlayer craftPlayer = (CraftPlayer) psld.getPlayer();
                    PlayerConnection connection = craftPlayer.getHandle().playerConnection;

                    connection.sendPacket(new PacketPlayOutEntityStatus(entityPlayer, (byte) 3));
                }
                if (playerUser.getLastGame() instanceof GameVersus) {
                    GameVersus game = (GameVersus) playerUser.getLastGame();
                    for (PUser psld : game.getTeam1()) {
                        CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                        PlayerConnection connection = pla.getHandle().playerConnection;
                        if (!pla.equals(player)) {
                            connection.sendPacket(new PacketPlayOutEntityStatus(entityPlayer, (byte) 3));
                        }
                    }
                    for (PUser psld : game.getTeam2()) {
                        CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                        PlayerConnection connection = pla.getHandle().playerConnection;
                        if (!pla.equals(player)) {
                            connection.sendPacket(new PacketPlayOutEntityStatus(entityPlayer, (byte) 3));
                        }
                    }
                    for (PUser psld : game.semiDeathPlayers) {
                        CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                        PlayerConnection connection = pla.getHandle().playerConnection;
                        if (!pla.equals(player)) {
                            connection.sendPacket(new PacketPlayOutEntityStatus(entityPlayer, (byte) 3));
                        }
                    }
                } else {
                    GameFFA game = (GameFFA) playerUser.getLastGame();
                    for (PUser psld : game.getPlayers()) {
                        CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                        PlayerConnection connection = pla.getHandle().playerConnection;
                        if (!pla.equals(player)) {
                            connection.sendPacket(new PacketPlayOutEntityStatus(entityPlayer, (byte) 3));
                        }
                    }
                }
            } else {
                for (PUser psld : playerUser.getLastGame().getSpectators()) {
                    CraftPlayer craftPlayer = (CraftPlayer) psld.getPlayer();
                    craftPlayer.hidePlayer(player);
                }
                if (playerUser.getLastGame() instanceof GameVersus) {
                    GameVersus game = (GameVersus) playerUser.getLastGame();
                    for (PUser psld : game.getTeam1()) {
                        CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                        if (!pla.equals(player)) {
                            pla.hidePlayer(player);
                        }
                    }
                    for (PUser psld : game.getTeam2()) {
                        CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                        if (!pla.equals(player)) {
                            pla.hidePlayer(player);
                        }
                    }
                    for (PUser psld : game.semiDeathPlayers) {
                        CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                        if (!pla.equals(player)) {
                            pla.hidePlayer(player);
                        }
                    }
                } else {
                    GameFFA game = (GameFFA) playerUser.getLastGame();
                    for (PUser psld : game.getPlayers()) {
                        CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                        if (!pla.equals(player)) {
                            pla.hidePlayer(player);
                        }
                    }
                }
            }

            switch (killEffect) {
                case DEATH_ANIMATION:
                case NONE:
                    break;
                case THUNDER: {
                    EntityLightning lightning = new EntityLightning(entityPlayer.world, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
                    for (PUser psld : playerUser.getLastGame().getSpectators()) {
                        CraftPlayer craftPlayer = (CraftPlayer) psld.getPlayer();
                        PlayerConnection connection = craftPlayer.getHandle().playerConnection;

                        connection.sendPacket(new PacketPlayOutSpawnEntityWeather(lightning));
                        connection.sendPacket(new PacketPlayOutNamedSoundEffect("ambient.weather.thunder", psld.getPlayer().getLocation().getX(), psld.getPlayer().getLocation().getY(), psld.getPlayer().getLocation().getZ(), 3.0f, 1.0f));
                    }
                    if (playerUser.getLastGame() instanceof GameVersus) {
                        GameVersus game = (GameVersus) playerUser.getLastGame();
                        for (PUser psld : game.getTeam1()) {
                            CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                            PlayerConnection connection = pla.getHandle().playerConnection;
                            connection.sendPacket(new PacketPlayOutSpawnEntityWeather(lightning));
                            connection.sendPacket(new PacketPlayOutNamedSoundEffect("ambient.weather.thunder", psld.getPlayer().getLocation().getX(), psld.getPlayer().getLocation().getY(), psld.getPlayer().getLocation().getZ(), 3.0f, 1.0f));
                        }
                        for (PUser psld : game.getTeam2()) {
                            CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                            pla.hidePlayer(player);
                            PlayerConnection connection = pla.getHandle().playerConnection;
                            connection.sendPacket(new PacketPlayOutSpawnEntityWeather(lightning));
                            connection.sendPacket(new PacketPlayOutNamedSoundEffect("ambient.weather.thunder", psld.getPlayer().getLocation().getX(), psld.getPlayer().getLocation().getY(), psld.getPlayer().getLocation().getZ(), 3.0f, 1.0f));
                        }
                    } else {
                        GameFFA game = (GameFFA) playerUser.getLastGame();
                        for (PUser psld : game.getPlayers()) {
                            CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                            PlayerConnection connection = pla.getHandle().playerConnection;
                            connection.sendPacket(new PacketPlayOutSpawnEntityWeather(lightning));
                            connection.sendPacket(new PacketPlayOutNamedSoundEffect("ambient.weather.thunder", psld.getPlayer().getLocation().getX(), psld.getPlayer().getLocation().getY(), psld.getPlayer().getLocation().getZ(), 3.0f, 1.0f));
                        }
                    }
                    break;
                }
                case EXPLOSION: {
                    for (PUser psld : playerUser.getLastGame().getSpectators()) {
                        CraftPlayer craftPlayer = (CraftPlayer) psld.getPlayer();
                        craftPlayer.playEffect(player.getLocation(), Effect.EXPLOSION_HUGE, 2);
                    }
                    if (playerUser.getLastGame() instanceof GameVersus) {
                        GameVersus game = (GameVersus) playerUser.getLastGame();
                        for (PUser psld : game.getTeam1()) {
                            CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                            pla.playEffect(player.getLocation(), Effect.EXPLOSION_HUGE, 2);
                        }
                        for (PUser psld : game.getTeam2()) {
                            CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                            pla.hidePlayer(player);
                            pla.playEffect(player.getLocation(), Effect.EXPLOSION_HUGE, 2);
                        }
                    } else {
                        GameFFA game = (GameFFA) playerUser.getLastGame();
                        for (PUser psld : game.getPlayers()) {
                            CraftPlayer pla = (CraftPlayer) psld.getPlayer();
                            pla.playEffect(player.getLocation(), Effect.EXPLOSION_HUGE, 2);
                        }
                    }

                    break;
                }
            }
        }
    }
}
