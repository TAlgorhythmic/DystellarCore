package net.zylesh.practice.serialize;

import net.zylesh.dystellarcore.serialization.InventorySerialization;
import net.zylesh.practice.Ladder;
import net.zylesh.practice.PArena;
import net.zylesh.practice.PUser;
import org.bukkit.inventory.Inventory;

import java.time.LocalDateTime;
import java.util.*;

public class GameData {

    private final String arena;
    private final Set<UUID> players = new HashSet<>();
    private final String ladder;
    private final int eloChange;
    public final Map<UUID, Integer> hits;
    public final Map<UUID, Integer> combo;
    private final Map<UUID, String> invs;
    private final UUID[] winners;
    private final UUID[] losers;
    private final LocalDateTime date;
    public final int time;

    public GameData(PArena arena, PUser[] team1, PUser[] team2, Ladder ladder, int eloChange, Map<UUID, Integer> hits, Map<UUID, Integer> combo, Map<UUID, Inventory> invs, PUser[] winners, PUser[] losers, LocalDateTime date, int time) {
        this.arena = arena.getName();
        for (PUser pUser : team1) this.players.add(pUser.getUuid());
        for (PUser pUser : team2) this.players.add(pUser.getUuid());
        this.ladder = ladder.getName();
        this.eloChange = eloChange;
        this.hits = hits;
        this.combo = combo;
        this.winners = new UUID[winners.length];
        this.losers = new UUID[losers.length];
        for (int i = 0; i < winners.length; i++) this.winners[i] = winners[i].getUuid();
        for (int i = 0; i < losers.length; i++) this.losers[i] = losers[i].getUuid();
        this.invs = new HashMap<>();
        for (Map.Entry<UUID, Inventory> entry : invs.entrySet()) {
            this.invs.put(entry.getKey(), InventorySerialization.inventoryToString(entry.getValue().getContents()));
        }
        this.date = date;
        this.time = time;
    }
    public GameData(String arena, UUID[] players, String ladder, int eloChange, Map<UUID, Integer> hits, Map<UUID, Integer> combo, Map<UUID, String> invs, UUID[] winners, UUID[] losers, LocalDateTime date, int time) {
        this.arena = arena;
        Collections.addAll(this.players, players);
        this.ladder = ladder;
        this.eloChange = eloChange;
        this.hits = hits;
        this.combo = combo;
        this.winners = new UUID[winners.length];
        this.losers = new UUID[losers.length];
        System.arraycopy(winners, 0, this.winners, 0, winners.length);
        System.arraycopy(losers, 0, this.losers, 0, losers.length);
        this.invs = invs;
        this.date = date;
        this.time = time;
    }

    public String getArena() {
        return arena;
    }

    public Set<UUID> getPlayers() {
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

    public UUID[] getWinners() {
        return winners;
    }

    public UUID[] getLosers() {
        return losers;
    }

    public LocalDateTime getDate() {
        return date;
    }
}

