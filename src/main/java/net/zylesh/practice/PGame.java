package net.zylesh.practice;

import net.zylesh.dystellarcore.utils.TimeCounter;
import net.zylesh.practice.serialize.GameData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import gg.minecraft.server.v1_7_R4.ChatSerializer;
import gg.minecraft.server.v1_7_R4.IChatBaseComponent;
import gg.minecraft.server.v1_7_R4.PacketPlayOutChat;
import gg.zylesh.practice.practicecore.Main;
import gg.zylesh.practice.practicecore.Practice;
import gg.zylesh.practice.practicecore.core.QueueType;
import gg.zylesh.practice.practicecore.events.FightEndEvent;
import gg.zylesh.practice.practicecore.events.PlayerChangeEloEvent;
import gg.zylesh.practice.practicecore.listeners.GameHandler;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.*;

public abstract class PGame {

    public static final Map<UUID, PGame> GAMES = new HashMap<>();

    protected final PArena arena;
    protected final Ladder ladder;
    protected int eloChange;
    protected final boolean ranked;
    protected final boolean competitionEvent;
    protected Location specLocation;
    public final Map<UUID, Integer> hits = new HashMap<>();
    public final Map<UUID, Integer> combo = new HashMap<>();
    protected List<InvMap> invs;
    protected final Set<PUser> spectators = new HashSet<>();
    protected Set<PUser> winners;
    protected Set<PUser> losers;
    protected LocalDateTime date;
    protected final boolean isDuel;
    protected boolean isEnded = false;
    protected boolean isStarted = false;
    public final TimeCounter time;
    public final Map<PUser, Set<Block>> blocksPlaced = new HashMap<>();
    public final Map<PUser, Set<Block>> blocksBroken = new HashMap<>();
    public final Set<Block> blocksPlacedAll = new HashSet<>();
    public final Set<PotionSplashEvent> potions = new HashSet<>();
    public final Map<ProjectileLaunchEvent, Boolean> projectiles = new HashMap<>();
    public final Map<Block, BukkitTask> disappearingBlocksListeners = new HashMap<>();
    public final QueueType queue;
    public final UUID uuid;
    public Location center;

    protected PGame(PArena arena, Ladder ladder, boolean isRanked, boolean isCompetitionEvent, boolean isDuel, QueueType queue) {
        this.time = new TimeCounter();
        this.arena = arena;
        this.queue = queue;
        this.isDuel = isDuel;
        this.ladder = ladder;
        this.ranked = isRanked;
        this.competitionEvent = isCompetitionEvent;
        this.uuid = UUID.randomUUID();
        GAMES.put(uuid, this);
        initInvsSet();
    }

    public GameData getSerializable() {
        return null;
    }

    protected abstract void initInvsSet();

    public abstract void onLeave(PUser user);

    public List<InvMap> getInvs() {
        return invs;
    }

    public Set<PUser> getSpectators() {
        return this.spectators;
    }

    public Location getSpecLocation() {
        return this.specLocation;
    }

    public boolean isDuel() {
        return isDuel;
    }

    public void addSpec(PUser player) {
        if (player.isSpectating())
            return;
        for (PUser spec : spectators) {
            spec.getPlayer().showPlayer(player.getPlayer());
            player.getPlayer().showPlayer(spec.getPlayer());
        }
        spectators.add(player);

        Inventory inv = Bukkit.createInventory(null, InventoryType.PLAYER);

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        compassMeta.setDisplayName(ChatColor.RED + "Teleport");
        compass.setItemMeta(compassMeta);
        inv.setItem(0, compass);
        ItemStack leave = new ItemStack(Material.REDSTONE);
        ItemMeta leaveMeta = leave.getItemMeta();
        leaveMeta.setDisplayName(ChatColor.RED + "Leave");
        leave.setItemMeta(leaveMeta);
        inv.setItem(8, leave);

        player.getPlayer().getInventory().setContents(inv.getContents());
        player.getPlayer().updateInventory();
    }

    public void removeSpec(PUser player) {
        if (!player.isSpectating())
            return;
        spectators.remove(player);
    }

    public Set<PUser> getWinners() {
        return winners;
    }

    public Set<PUser> getLosers() {
        return losers;
    }

    public PArena getArena() {
        return arena;
    }

    public Ladder getLadder() {
        return ladder;
    }

    // Must override
    public void broadcastSound(Sound sound, float volume, float pitch) {
        spectators.forEach(playerUser -> playerUser.getPlayer().playSound(playerUser.getPlayer().getLocation(), sound, volume, pitch));
    }

    public boolean isRanked() {
        return ranked;
    }

    public abstract void onPlayerDeath(PUser playerUser);

    protected abstract void die(PUser playerUser, boolean addBack);

    protected abstract void respawn(PUser playerUser);

    protected abstract void nextRound0();

    public abstract void nextRound(@Nullable PUser winner);

    protected abstract void giveItems();

    public abstract void nextRound(@Nullable Set<PUser> winners);

    public void resetArena() {
        for (Set<Block> blocks : blocksPlaced.values()) {
            for (Block block : blocks) {
                block.setType(Material.AIR);
            }
        }
    }

    public boolean isCompetitionEvent() {
        return competitionEvent;
    }

    public Map<UUID, Integer> getHits() {
        return hits;
    }

    public Map<UUID, Integer> getCombo() {
        return combo;
    }

    // Must override
    public void broadcastToGame(String s) {
        if (!spectators.isEmpty()) {
            for (PUser p : spectators) {
                p.getPlayer().sendMessage(s);
            }
        }
    }

    public LocalDateTime getDate() {
        return date;
    }

    public int getEloChange() {
        return eloChange;
    }

    public boolean isEnded() {
        return isEnded;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public abstract void endUnexpectedly(String message);

    public void end(Set<PUser> winner, Set<PUser> loser, boolean createInvs) {
        this.winners = winner;
        this.losers = loser;
        if (createInvs) {
            for (PUser p : winners) {
                GameHandler.INSTANCE.createInventory(p, p.getPlayer().getHealth());
            }
        }
        this.date = LocalDateTime.now();
        this.time.getAndEndTask();
        this.isEnded = true;
        if (!isDuel) {
            for (PUser player : winner) {
                player.kills++;
            }
            for (PUser player : loser) {
                player.deaths++;
            }
        }
        Bukkit.getPluginManager().callEvent(new FightEndEvent(winner.toArray(new PUser[0]), loser.toArray(new PUser[0]), ladder, arena, this, hits));
        if (winner.size() == 1 && loser.size() == 1) {
            PUser w = winner.iterator().next();
            PUser l = loser.iterator().next();
            IChatBaseComponent component = ChatSerializer.a("[\"\",{\"text\":\"Match Results:\",\"bold\":true,\"color\":\"yellow\"},{\"text\":\" (Click to see inventories)\",\"color\":\"aqua\"}]");
            IChatBaseComponent component1 = ChatSerializer.a("{\"text\":\"---------------------------------\",\"strikethrough\":true,\"color\":\"yellow\"}");
            IChatBaseComponent component2 = ChatSerializer.a("[\"\",{\"text\":\" \"},{\"text\":\" - Winner\",\"color\":\"green\"},{\"text\":\": \"},{\"text\":\"" + w.getName() + "\",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/lastinv " + w.getName() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§a(Click to see inventory)\"}},{\"text\":\" \"},{\"text\":\"- Loser\",\"color\":\"red\"},{\"text\":\": \"},{\"text\":\"" + l.getName() + "\",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/lastinv " + l.getName() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§a(Click to see inventory)\"}}]");
            IChatBaseComponent component3 = ChatSerializer.a("{\"text\":\"---------------------------------\",\"strikethrough\":true,\"color\":\"yellow\"}");
            PacketPlayOutChat chat = new PacketPlayOutChat(component);
            PacketPlayOutChat chat1 = new PacketPlayOutChat(component1);
            PacketPlayOutChat chat2 = new PacketPlayOutChat(component2);
            PacketPlayOutChat chat3 = new PacketPlayOutChat(component3);
            w.getPlayer().sendMessage(" ");
            ((CraftPlayer) w.getPlayer()).getHandle().playerConnection.sendPacket(chat);
            ((CraftPlayer) w.getPlayer()).getHandle().playerConnection.sendPacket(chat1);
            ((CraftPlayer) w.getPlayer()).getHandle().playerConnection.sendPacket(chat2);
            ((CraftPlayer) w.getPlayer()).getHandle().playerConnection.sendPacket(chat3);
            l.getPlayer().sendMessage(" ");
            ((CraftPlayer) l.getPlayer()).getHandle().playerConnection.sendPacket(chat);
            ((CraftPlayer) l.getPlayer()).getHandle().playerConnection.sendPacket(chat1);
            ((CraftPlayer) l.getPlayer()).getHandle().playerConnection.sendPacket(chat2);
            ((CraftPlayer) l.getPlayer()).getHandle().playerConnection.sendPacket(chat3);
            for (PUser spec : spectators) {
                spec.getPlayer().sendMessage(" ");
                ((CraftPlayer) spec.getPlayer()).getHandle().playerConnection.sendPacket(chat);
                ((CraftPlayer) spec.getPlayer()).getHandle().playerConnection.sendPacket(chat1);
                ((CraftPlayer) spec.getPlayer()).getHandle().playerConnection.sendPacket(chat2);
                ((CraftPlayer) spec.getPlayer()).getHandle().playerConnection.sendPacket(chat3);
            }
        } else {
            StringBuilder builder1 = new StringBuilder();
            StringBuilder builder2 = new StringBuilder();
            PUser w = winner.iterator().next();
            PUser l = loser.iterator().next();
            for (PUser p : winner) {
                if (w.equals(p)) {
                    builder1.append("{\"text\":\"").append(p.getName()).append("\",\"color\":\"white\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/lastinv ").append(p.getName()).append("\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§a(Click to see inventory)\"}}");
                } else {
                    builder1.append(",{\"text\":\",\",\"color\":\"white\"},").append("{\"text\":\"").append(p.getName()).append("\",\"color\":\"white\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/lastinv ").append(p.getName()).append("\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§a(Click to see inventory)\"}}");
                }
            }
            for (PUser p : loser) {
                if (l.equals(p)) {
                    builder2.append("{\"text\":\"").append(p.getName()).append("\",\"color\":\"white\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/lastinv ").append(p.getName()).append("\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§a(Click to see inventory)\"}}");
                } else {
                    builder2.append(",{\"text\":\",\",\"color\":\"white\"},").append("{\"text\":\"").append(p.getName()).append("\",\"color\":\"white\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/lastinv ").append(p.getName()).append("\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§a(Click to see inventory)\"}}");
                }
            }
            IChatBaseComponent component = ChatSerializer.a("[\"\",{\"text\":\"Match Results:\",\"bold\":true,\"color\":\"yellow\"},{\"text\":\" (Click to see inventories)\",\"color\":\"aqua\"}]");
            IChatBaseComponent component1 = ChatSerializer.a("{\"text\":\"---------------------------------\",\"strikethrough\":true,\"color\":\"yellow\"}");
            IChatBaseComponent component2 = ChatSerializer.a("[\"\",{\"text\":\" \"},{\"text\":\"- Winners: \",\"color\":\"green\"}," + builder1 + "]");
            IChatBaseComponent component3 = ChatSerializer.a("[\"\",{\"text\":\" \"},{\"text\":\"- Losers: \",\"color\":\"red\"}," + builder2 + "]");
            IChatBaseComponent component4 = ChatSerializer.a("{\"text\":\"---------------------------------\",\"strikethrough\":true,\"color\":\"yellow\"}");
            PacketPlayOutChat chat = new PacketPlayOutChat(component);
            PacketPlayOutChat chat1 = new PacketPlayOutChat(component1);
            PacketPlayOutChat chat2 = new PacketPlayOutChat(component2);
            PacketPlayOutChat chat3 = new PacketPlayOutChat(component3);
            PacketPlayOutChat chat4 = new PacketPlayOutChat(component4);
            for (PUser p : winner) {
                CraftPlayer player = (CraftPlayer) p.getPlayer();
                player.sendMessage(" ");
                player.getHandle().playerConnection.sendPacket(chat);
                player.getHandle().playerConnection.sendPacket(chat1);
                player.getHandle().playerConnection.sendPacket(chat2);
                player.getHandle().playerConnection.sendPacket(chat3);
                player.getHandle().playerConnection.sendPacket(chat4);
            }
            for (PUser p : loser) {
                CraftPlayer player = (CraftPlayer) p.getPlayer();
                player.sendMessage(" ");
                player.getHandle().playerConnection.sendPacket(chat);
                player.getHandle().playerConnection.sendPacket(chat1);
                player.getHandle().playerConnection.sendPacket(chat2);
                player.getHandle().playerConnection.sendPacket(chat3);
                player.getHandle().playerConnection.sendPacket(chat4);
            }
            for (PUser p : spectators) {
                CraftPlayer player = (CraftPlayer) p.getPlayer();
                player.sendMessage(" ");
                player.getHandle().playerConnection.sendPacket(chat);
                player.getHandle().playerConnection.sendPacket(chat1);
                player.getHandle().playerConnection.sendPacket(chat2);
                player.getHandle().playerConnection.sendPacket(chat3);
                player.getHandle().playerConnection.sendPacket(chat4);
            }
        }
        Bukkit.getScheduler().runTaskLater(Main.INSTANCE, () -> {
            for (PUser p : winner) {
                p.goBackToLobby();
            }
            for (PUser p : loser) {
                p.goBackToLobby();
            }
            for (PUser p : spectators) {
                p.goBackToLobby();
            }
            if (queue != null) {
                Practice.updateQueueVariable(ladder, queue);
            }
        }, 80);

        if (ranked) {
            int elo = 0;
            for (PUser win : winner) {
                elo += win.elo.get(ladder);
            }
            int elol = 0;
            for (PUser los : loser) {
                elol += los.elo.get(ladder);
            }
            elo /= winner.size();
            elol /= loser.size();
            if (elo == elol) {
                for (PUser win : winner) {
                    int newElo = win.elo.get(ladder) + 16;
                    win.elo.put(ladder, newElo);
                    Bukkit.getPluginManager().callEvent(new PlayerChangeEloEvent(win, newElo, 16));
                }
                for (PUser los : loser) {
                    int newElo = los.elo.get(ladder) - 16;
                    los.elo.put(ladder, newElo);
                    Bukkit.getPluginManager().callEvent(new PlayerChangeEloEvent(los, newElo, 16));
                }
                this.eloChange = 16;
            } else if (elo > elol) {
                int percentage = elol * 100 / elo;
                int eloChange = (16 * percentage / 100) * 60 / percentage;
                for (PUser win : winner) {
                    int newElo = win.elo.get(ladder) + eloChange;
                    win.elo.put(ladder, newElo);
                    Bukkit.getPluginManager().callEvent(new PlayerChangeEloEvent(win, newElo, eloChange));
                }
                for (PUser los : loser) {
                    int newElo = los.elo.get(ladder) - eloChange;
                    los.elo.put(ladder, newElo);
                    Bukkit.getPluginManager().callEvent(new PlayerChangeEloEvent(los, newElo, eloChange));
                }
                this.eloChange = eloChange;
            } else {
                int percentage = elol * 100 / elo;
                int eloChange = (16 * percentage / 100) * 140 / percentage;
                for (PUser win : winner) {
                    int newElo = win.elo.get(ladder) + eloChange;
                    win.elo.put(ladder, newElo);
                    Bukkit.getPluginManager().callEvent(new PlayerChangeEloEvent(win, newElo, eloChange));
                }
                for (PUser los : loser) {
                    int newElo = los.elo.get(ladder) - eloChange;
                    los.elo.put(ladder, newElo);
                    Bukkit.getPluginManager().callEvent(new PlayerChangeEloEvent(los, newElo, eloChange));
                }
                this.eloChange = eloChange;
            }
            if (winner.size() == 1 && loser.size() == 1) {
                PUser w = winner.iterator().next();
                PUser l = loser.iterator().next();
                broadcastToGame(ChatColor.BLUE + "Elo Updates:");
                broadcastToGame(ChatColor.GREEN + w.getName() + " - " + w.elo.get(ladder) + ChatColor.GOLD + " (+" + this.eloChange + ") " + ChatColor.WHITE + "| " + ChatColor.RED + l.getName() + " - " + l.elo.get(ladder) + ChatColor.GRAY + " (-" + eloChange + ")");
            } else {
                StringBuilder winners = new StringBuilder();
                StringBuilder losers = new StringBuilder();
                for (PUser win : winner) {
                    winners.append(ChatColor.GREEN).append(win.getName()).append(ChatColor.GOLD).append(" (+").append(eloChange).append(")").append(ChatColor.GREEN).append(", ");
                }

                for (PUser los : loser) {
                    losers.append(ChatColor.RED).append(los.getName()).append(ChatColor.GRAY).append(" (-").append(eloChange).append(")").append(ChatColor.RED).append(", ");
                }
                String winnersString = winners.substring(0, winners.length() - 2) + ".";
                String losersString = losers.substring(0, losers.length() - 2) + ".";
                broadcastToGame(ChatColor.BLUE + "Elo Updates:");
                broadcastToGame("| - " + ChatColor.GREEN + "Winners: " + winnersString);
                broadcastToGame("| - " + ChatColor.RED + "Losers: " + losersString);
            }
        }
    }
}
