package net.zylesh.practice;

import net.zylesh.practice.practicecore.Practice;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.zylesh.practice.practicecore.util.Msg.ERROR_PARTY_FULL;
import static net.zylesh.practice.practicecore.util.Msg.PARTY_DISBAND_BROADCAST;

public class PParty implements Comparable<PParty>, Serializable {

    private final List<PUser> players;
    private PUser leader;
    private boolean isOpen;
    private final short size;
    private final int hashCode;
    public final List<PUser> awaitingPlayers;

    public PParty(PUser leader, short size) {
        this.leader = leader;
        this.size = size;
        players = new ArrayList<>();
        awaitingPlayers = new ArrayList<>();
        leader.joinParty(this);
        Random random = new Random();
        this.hashCode = random.nextInt();
    }

    public List<PUser> getPlayers() {
        return players;
    }

    public PUser getLeader() {
        return leader;
    }

    public void setLeader(PUser leader) {
        this.leader = leader;
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public void open() {
        this.isOpen = true;
    }

    public void close() {
        this.isOpen = false;
    }

    public void broadcast(String s) {
        for (PUser playerUser : players) {
            playerUser.getPlayer().sendMessage(s);
        }
    }

    public boolean addPlayer(PUser player) {
        if (players.size() >= size) {
            player.getPlayer().sendMessage(ERROR_PARTY_FULL);
            return false;
        } else {
            players.add(player);
            return true;
        }
    }

    public void removePlayer(PUser pl) {
        players.remove(pl);
    }

    public boolean contains(PUser player) {
        return players.contains(player);
    }

    /**
     * Don't use this method to disband a party, it will cause errors.
     * Use PApi.disbandParty(party) instead.
     **/
    public void disband() {
        List<PUser> playersSafe = new ArrayList<>(players);
        playersSafe.forEach(PUser::leaveCurrentParty);
        playersSafe.forEach(playerUser -> playerUser.getPlayer().sendMessage(PARTY_DISBAND_BROADCAST));
    }

    public void onPlayerJoin(PUser playerUser) {
        if (getPlayers().size() == 2) {
            playerUser.getPlayer().getInventory().clear();
            getLeader().getPlayer().getInventory().clear();
            playerUser.getPlayer().getInventory().setContents(Practice.PARTY_INV.getContents());
            getLeader().getPlayer().getInventory().setContents(Practice.PARTY2_INV.getContents());
        } else if (getPlayers().size() == 3) {
            playerUser.getPlayer().getInventory().clear();
            getLeader().getPlayer().getInventory().clear();
            playerUser.getPlayer().getInventory().setContents(Practice.PARTY_INV.getContents());
            getLeader().getPlayer().getInventory().setContents(Practice.PARTY3_INV.getContents());
        } else {
            playerUser.getPlayer().getInventory().clear();
            getLeader().getPlayer().getInventory().clear();
            playerUser.getPlayer().getInventory().setContents(Practice.PARTY_INV.getContents());
            getLeader().getPlayer().getInventory().setContents(Practice.PARTY_INV_LEADER.getContents());
        }
        playerUser.getPlayer().updateInventory();
        getLeader().getPlayer().updateInventory();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public int compareTo(@Nonnull PParty o) {
        return Integer.compare(this.players.size(), o.getPlayers().size());
    }
}
