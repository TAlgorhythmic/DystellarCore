package net.zylesh.practice.serialize;

import net.zylesh.dystellarcore.serialization.InventorySerialization;
import net.zylesh.practice.InvMap;
import net.zylesh.practice.Ladder;
import net.zylesh.practice.PArena;
import net.zylesh.practice.PUser;

import javax.annotation.Nullable;
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
    private final Set<UUID> winners;
    private final Set<UUID> losers;
    private final LocalDateTime date;
    public final int time;

    public GameData(PArena arena, Set<PUser> team1, @Nullable Set<PUser> team2, Ladder ladder, int eloChange, Map<UUID, Integer> hits, Map<UUID, Integer> combo, List<InvMap> invs, Set<PUser> winners, Set<PUser> losers, LocalDateTime date, int time) {
        this.arena = arena.getName();
        for (PUser pUser : team1) this.players.add(pUser.getUuid());
        if (team2 != null) for (PUser pUser : team2) this.players.add(pUser.getUuid());
        this.ladder = ladder.getName();
        this.eloChange = eloChange;
        this.hits = hits;
        this.combo = combo;
        this.winners = new HashSet<>();
        this.losers = new HashSet<>();
        for (PUser p : winners) this.winners.add(p.getUuid());
        for (PUser p : losers) this.losers.add(p.getUuid());
        this.invs = new HashMap<>();
        for (InvMap entry : invs) {
            this.invs.put(entry.getUser().getUuid(), InventorySerialization.inventoryToString(entry.getInv().getContents()));
        }
        this.date = date;
        this.time = time;
    }
    public GameData(String arena, Set<UUID> players, String ladder, int eloChange, Map<UUID, Integer> hits, Map<UUID, Integer> combo, Map<UUID, String> invs, Set<UUID> winners, Set<UUID> losers, LocalDateTime date, int time) {
        this.arena = arena;
        this.players.addAll(players);
        this.ladder = ladder;
        this.eloChange = eloChange;
        this.hits = hits;
        this.combo = combo;
        this.winners = winners;
        this.losers = losers;
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

    public Set<UUID> getWinners() {
        return winners;
    }

    public Set<UUID> getLosers() {
        return losers;
    }

    public LocalDateTime getDate() {
        return date;
    }
}

