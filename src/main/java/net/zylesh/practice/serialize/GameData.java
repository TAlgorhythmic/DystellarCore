package net.zylesh.practice.serialize;

import net.zylesh.dystellarcore.serialization.InventorySerialization;
import net.zylesh.practice.Ladder;
import net.zylesh.practice.PArena;
import net.zylesh.practice.PUser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

public class GameData implements Serializable {

    private final String arena;
    private final List<UUID> players = new ArrayList<>();
    private final String ladder;
    private final int eloChange;
    public final Map<UUID, Integer> hits = new HashMap<>();
    public final Map<UUID, Integer> combo = new HashMap<>();
    private final Map<UUID, String> invs = new HashMap<>();
    private final List<UUID> winners = new ArrayList<>();
    private final List<UUID> losers = new ArrayList<>();
    private final LocalDateTime date;
    public final int time;

    public GameData(PArena arena, List<PUser> team1, List<PUser> team2, Ladder ladder, int eloChange, Map<Player, Integer> hits, Map<Player, Integer> combo, Map<UUID, Inventory> invs, List<PUser> winners, List<PUser> losers, LocalDateTime date, int time) {
        this.arena = arena.getName();
        team1.forEach(user -> this.players.add(user.getUuid()));
        team2.forEach(user -> this.players.add(user.getUuid()));
        this.ladder = ladder.getName();
        this.eloChange = eloChange;
        hits.forEach((player, integer) -> this.hits.put(player.getUniqueId(), integer));
        combo.forEach((player, integer) -> this.combo.put(player.getUniqueId(), integer));
        winners.forEach(user -> this.winners.add(user.getUuid()));
        losers.forEach(user -> this.losers.add(user.getUuid()));
        for (Map.Entry<UUID, Inventory> entry : invs.entrySet()) {
            this.invs.put(entry.getKey(), InventorySerialization.inventoryToString(entry.getValue().getContents()));
        }
        this.date = date;
        this.time = time;
    }
    public GameData(String arena, List<UUID> players, String ladder, int eloChange, Map<UUID, Integer> hits, Map<UUID, Integer> combo, Map<UUID, String> invs, List<UUID> winners, List<UUID> losers, LocalDateTime date, int time) {
        this.arena = arena;
        this.players.addAll(players);
        this.ladder = ladder;
        this.eloChange = eloChange;
        this.hits.putAll(hits);
        this.combo.putAll(combo);
        this.winners.addAll(winners);
        this.losers.addAll(losers);
        this.invs.putAll(invs);
        this.date = date;
        this.time = time;
    }

    public String getArena() {
        return arena;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public String getLadder() {
        return ladder;
    }

    public int getEloChange() {
        return eloChange;
    }

    public Map<UUID, String> getInvs() {
        return invs;
    }

    public List<UUID> getWinners() {
        return winners;
    }

    public List<UUID> getLosers() {
        return losers;
    }

    public LocalDateTime getDate() {
        return date;
    }
}

