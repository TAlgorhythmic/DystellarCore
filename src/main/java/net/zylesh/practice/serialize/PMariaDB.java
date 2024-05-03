package net.zylesh.practice.serialize;

import net.zylesh.dystellarcore.serialization.InventorySerialization;
import net.zylesh.dystellarcore.serialization.MariaDB;
import net.zylesh.practice.Ladder;
import net.zylesh.practice.PApi;
import net.zylesh.practice.PKillEffect;
import net.zylesh.practice.PUser;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PMariaDB {

    /**
     * Highly recommended to do this async
     * @param user player to save
     */
    public static void savePlayerToDatabase(PUser user) {
        try (Connection connection = MariaDB.getConnection(); PreparedStatement statement = connection.prepareStatement("REPLACE practice_players(uuid, name, rank, displayRank, requests, visibility, invs, elo, effect, kills, deaths, effects, dnd, pingRange) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            statement.setString(1, user.getUuid().toString());
            statement.setString(2, user.getName());
            statement.setString(3, user.getRank());
            statement.setBoolean(4, user.isDisplayRank());
            statement.setBoolean(5, user.isDuelRequestsEnabled());
            statement.setInt(6, user.getPlayerVisibility());
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<Ladder, ItemStack[]> entry : user.getInvsEdited().entrySet()) {
                builder.append(entry.getKey().getName()).append("=").append(InventorySerialization.inventoryToString(entry.getValue())).append(";");
            }
            statement.setString(7, builder.toString());
            StringBuilder builderElo = new StringBuilder();
            for (Map.Entry<Ladder, Integer> entry : user.elo.entrySet()) {
                builderElo.append(entry.getKey().getName()).append("=").append(entry.getValue()).append(";");
            }
            statement.setString(8, builderElo.toString());
            statement.setString(9, user.killEffect.name());
            statement.setInt(10, user.kills);
            statement.setInt(11, user.deaths);
            builder = new StringBuilder();
            for (PKillEffect effect : user.ownedEffects) builder.append(effect.name()).append(";");
            statement.setString(12, builder.toString());
            statement.setInt(13, user.getDoNotDisturbMode());
            statement.setBoolean(14, user.isPingRange());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not save data for " + user.getName());
        }
    }

    public static PUser loadPlayerFromDatabase(UUID uuid, boolean loadInvs) {
        try (Connection connection = MariaDB.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT name, rank, displayRank, requests, visibility, invs, elo, effect, kills, deaths, effects, dnd, pingRange FROM practice_players WHERE uuid = ?;"
        )) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String rank = resultSet.getString("rank");
                boolean displayRank = resultSet.getBoolean("displayRank");
                boolean duelRequests = resultSet.getBoolean("requests");
                int visibility = resultSet.getInt("visibility");
                Map<Ladder, ItemStack[]> invs = new HashMap<>();
                String[] invsSplit = resultSet.getString("invs").split(";");
                for (String entry : invsSplit) {
                    String[] split = entry.split("=");
                    if (PApi.LADDERS.containsKey(split[0])) {
                        invs.put(PApi.LADDERS.get(split[0]), InventorySerialization.stringToInventory(split[1]));
                    }
                }
                Map<Ladder, Integer> elo = new HashMap<>();
                String[] eloSplit = resultSet.getString("elo").split(";");
                for (String entry : eloSplit) {
                    String[] split = entry.split("=");
                    if (PApi.LADDERS.containsKey(split[0])) {
                        elo.put(PApi.LADDERS.get(split[0]), Integer.parseInt(split[1]));
                    }
                }
                PKillEffect effect = PKillEffect.valueOf(resultSet.getString("effect"));
                int kills = resultSet.getInt("kills");
                int deaths = resultSet.getInt("deaths");
                EnumSet<PKillEffect> effectEnumSet = EnumSet.noneOf(PKillEffect.class);
                String[] effects = resultSet.getString("effects").split(";");
                for (String e : effects) {
                    try {
                        effectEnumSet.add(PKillEffect.valueOf(e));
                    } catch (IllegalArgumentException ignored) {}
                }
                byte doNotDisturb = (byte) resultSet.getInt("dnd");
                boolean pingRange = resultSet.getBoolean("pingRange");
                return new PUser(uuid, name, rank, effect, displayRank, duelRequests, visibility, invs, elo, kills, deaths, effectEnumSet, loadInvs, doNotDisturb, pingRange);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not load data for " + uuid.toString());
            return null;
        }
        Bukkit.getLogger().info("Data for " + uuid + " not found, creating new data.");
        return new PUser(uuid);
    }

    public static Set<PUser> getAllPlayers() {
        Set<PUser> players = new HashSet<>();
        try (Connection connection = MariaDB.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT uuid, name, rank, displayRank, requests, visibility, invs, elo, effect, kills, deaths, effects, dnd FROM practice_players;"
        )) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String name = resultSet.getString("name");
                String rank = resultSet.getString("rank");
                boolean displayRank = resultSet.getBoolean("displayRank");
                boolean duelRequests = resultSet.getBoolean("requests");
                int visibility = resultSet.getInt("visibility");
                Map<Ladder, ItemStack[]> invs = new HashMap<>();
                String[] invsSplit = resultSet.getString("invs").split(";");
                for (String entry : invsSplit) {
                    String[] split = entry.split("=");
                    if (PApi.LADDERS.containsKey(split[0])) {
                        invs.put(PApi.LADDERS.get(split[0]), InventorySerialization.stringToInventory(split[1]));
                    }
                }
                Map<Ladder, Integer> elo = new HashMap<>();
                String[] eloSplit = resultSet.getString("elo").split(";");
                for (String entry : eloSplit) {
                    String[] split = entry.split("=");
                    if (PApi.LADDERS.containsKey(split[0])) {
                        elo.put(PApi.LADDERS.get(split[0]), Integer.parseInt(split[1]));
                    }
                }
                PKillEffect effect = PKillEffect.valueOf(resultSet.getString("effect"));
                int kills = resultSet.getInt("kills");
                int deaths = resultSet.getInt("deaths");
                EnumSet<PKillEffect> effectEnumSet = EnumSet.noneOf(PKillEffect.class);
                String[] effects = resultSet.getString("effects").split(";");
                for (String e : effects) {
                    try {
                        effectEnumSet.add(PKillEffect.valueOf(e));
                    } catch (IllegalArgumentException ignored) {}
                }
                byte doNotDisturb = (byte) resultSet.getInt("dnd");
                boolean pingRange = resultSet.getBoolean("pingRange");
                PUser user = new PUser(uuid, name, rank, effect, displayRank, duelRequests, visibility, invs, elo, kills, deaths, effectEnumSet, false, doNotDisturb, pingRange);
                players.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Fatal error loading all data from players database.");
        }
        return players;
    }

    public static void deletePlayerData(UUID uuid) {
        try (Connection connection = MariaDB.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM players WHERE uuid = ?;"
        )) {
            statement.setString(1, uuid.toString());
            statement.execute();
            if (PUser.getUsers().containsKey(uuid)) {
                PUser user = new PUser(uuid);
                PUser.getUsers().replace(uuid, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not delete data for " + uuid.toString());
        }
    }

    public static void saveRanked(GameData data) {
        try (Connection connection = MariaDB.getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT practice_ranked(arena, ladder, players, results, eloChange, invs, hits, combos, gameDate, seconds) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            statement.setString(1, data.getArena());
            statement.setString(2, data.getLadder());
            StringBuilder sb = new StringBuilder();
            data.getPlayers().forEach(uuid -> sb.append(uuid).append(";"));
            statement.setString(3, sb.toString());
            StringBuilder sb1 = new StringBuilder();
            for (UUID uuid : data.getWinners()) sb1.append(uuid).append(",");
            sb1.append(";");
            for (UUID uuid : data.getLosers()) sb1.append(uuid).append(",");
            statement.setString(4, sb1.toString());
            statement.setInt(5, data.getEloChange()); // eloChange
            StringBuilder sb2 = new StringBuilder();
            data.getInvs().forEach((uuid, s) -> sb2.append(uuid).append("=").append(s));
            statement.setString(6, sb2.toString());
            StringBuilder sb3 = new StringBuilder();
            data.hits.forEach((uuid, s) -> sb3.append(uuid).append("=").append(s));
            statement.setString(7, sb3.toString());
            StringBuilder sb4 = new StringBuilder();
            data.combo.forEach((uuid, s) -> sb4.append(uuid).append("=").append(s));
            statement.setString(8, sb4.toString());
            statement.setString(9, data.getDate().format(DateTimeFormatter.ISO_DATE_TIME));
            statement.setInt(10, data.time);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Could save match data.");
        }
    }



    public static Set<GameData> loadRankedsFromDatabase() {
        Set<GameData> data = new HashSet<>();
        try (Connection connection = MariaDB.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT arena, ladder, players, results, eloChange, invs, hits, combos, gameDate, seconds FROM practice_rankeds;")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String arena = resultSet.getString("arena");
                String ladder = resultSet.getString("ladder");

                String[] uuids = resultSet.getString("players").split(";");
                Set<UUID> players = new HashSet<>();
                for (String uuid : uuids) players.add(UUID.fromString(uuid));

                String[] results = resultSet.getString("results").split(";");

                String[] results0 = results[0].split(",");
                Set<UUID> winners = new HashSet<>();
                for (String string : results0) winners.add(UUID.fromString(string));

                String[] results1 = results[1].split(",");
                Set<UUID> losers = new HashSet<>();
                for (String string : results1) losers.add(UUID.fromString(string));

                int eloChange = resultSet.getInt("eloChange");
                Map<UUID, String> invs = new HashMap<>();
                for (String s : resultSet.getString("invs").split(";")) {
                    String[] entry = s.split("=");
                    invs.put(UUID.fromString(entry[0]), entry[1]);
                }
                Map<UUID, Integer> hits = new HashMap<>();
                for (String s : resultSet.getString("hits").split(";")) {
                    String[] entry = s.split("=");
                    hits.put(UUID.fromString(entry[0]), Integer.parseInt(entry[2]));
                }
                Map<UUID, Integer> combos = new HashMap<>();
                for (String s : resultSet.getString("combos").split(";")) {
                    String[] entry = s.split("=");
                    combos.put(UUID.fromString(entry[0]), Integer.parseInt(entry[2]));
                }
                LocalDateTime date = LocalDateTime.parse(resultSet.getString("gameDate"), DateTimeFormatter.ISO_DATE_TIME);
                int time = resultSet.getInt("seconds");
                data.add(new GameData(arena, players, ladder, eloChange, hits, combos, invs, winners, losers, date, time));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Could not fetch rankeds data.");
        }
        return data;
    }

    public static Set<GameData> getPlayerRankedData(UUID uuid) {
        return loadRankedsFromDatabase().stream()
                .filter(data1 -> data1.getPlayers().contains(uuid))
                .collect(Collectors.toSet());
    }

    public static Set<GameData> getPlayerRankedWinsData(UUID uuid) {
        return loadRankedsFromDatabase().stream()
                .filter(data1 -> data1.getPlayers().contains(uuid) && data1.getWinners().contains(uuid))
                .collect(Collectors.toSet());
    }
}
