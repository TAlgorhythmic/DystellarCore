package net.zylesh.dystellarcore.serialization;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.mariadb.jdbc.MariaDbPoolDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class MariaDB {

    public static boolean ENABLED;
    private static String HOST;
    private static int PORT;
    private static String DATABASE;
    private static String USER;
    private static String PASSWORD;

    public static DataSource DATA_SOURCE;

    public static void loadFromConfig() {
        ENABLED = DystellarCore.getInstance().getConfig().getBoolean("mariadb.enabled");
        HOST = DystellarCore.getInstance().getConfig().getString("mariadb.host");
        PORT = DystellarCore.getInstance().getConfig().getInt("mariadb.port");
        DATABASE = DystellarCore.getInstance().getConfig().getString("mariadb.database");
        USER = DystellarCore.getInstance().getConfig().getString("mariadb.user");
        PASSWORD = DystellarCore.getInstance().getConfig().getString("mariadb.password");
    }

    public static void dataSourceTestInit() throws SQLException {
        MariaDbPoolDataSource dataSource = new MariaDbPoolDataSource();
        dataSource.setUrl("jdbc:mariadb://" + HOST + ":" + PORT + "/" + DATABASE);
        dataSource.setUser(USER);
        dataSource.setPassword(PASSWORD);
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(5)) {
                throw new SQLException("Could not create a connection.");
            }
        }
        DATA_SOURCE = dataSource;
    }

    public static User loadPlayerFromDatabase(UUID uuid) {
        try (Connection connection = DATA_SOURCE.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT name, kit, killeffect, wineffect, glass, kitslist, killeffectlist, wineffectlist, glasslist, matchmaking, coins, elo, kills, deaths, wins FROM players WHERE uuid = ?;"
        )) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            User user;
            if (resultSet.next()) {
                user = new User(uuid);
                user.kit = SkywarsAPI.KITS_MAP.get(resultSet.getString("kit"));
                user.killEffect = KillEffect.valueOf(resultSet.getString("killeffect"));
                user.winEffect = WinEffect.valueOf(resultSet.getString("wineffect"));
                user.glass = SkywarsAPI.GLASS_MAP.get(resultSet.getString("glass"));
                for (String s : resultSet.getString("kitslist").split(";")) {
                    if (!s.isEmpty())
                        user.ownedKits.add(SkywarsAPI.KITS_MAP.get(s));
                }
                for (String s : resultSet.getString("killeffectlist").split(";")) {
                    if (!s.isEmpty())
                        user.ownedKillEffects.add(KillEffect.valueOf(s));
                }
                for (String s : resultSet.getString("wineffectlist").split(";")) {
                    if (!s.isEmpty())
                        user.ownedWinEffects.add(WinEffect.valueOf(s));
                }
                for (String s : resultSet.getString("glasslist").split(";")) {
                    if (!s.isEmpty())
                        user.ownedGlasses.add(SkywarsAPI.GLASS_MAP.get(s));
                }
                user.matchmaking = resultSet.getInt("matchmaking");
                user.coins = resultSet.getInt("coins");
                user.elo = resultSet.getInt("elo");
                user.kills = resultSet.getInt("kills");
                user.deaths = resultSet.getInt("deaths");
                user.wins = resultSet.getInt("wins");
                return user;
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not load data for " + uuid.toString());
        }
        return null;
    }


    /**
     * Highly recommended to do this async
     * @param playerUser player to save
     */
    public static void savePlayerToDatabase(User playerUser) {
        try (Connection connection = DATA_SOURCE.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "REPLACE players(uuid, name, kit, killeffect, wineffect, glass, kitslist, killeffectlist, wineffectlist, glasslist, matchmaking, coins, elo, kills, deaths, wins) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
        )) {
            statement.setString(1, playerUser.getUuid().toString());
            statement.setString(2, playerUser.getName());
            if (playerUser.kit == null) {
                statement.setString(3, null);
            } else {
                statement.setString(3, playerUser.kit.getName());
            }
            statement.setString(4, playerUser.killEffect.name());
            statement.setString(5, playerUser.winEffect.name());
            if (playerUser.glass == null) {
                statement.setString(6, null);
            } else {
                statement.setString(6, playerUser.glass.getName());
            }
            int i = 0;
            Iterator<Kit> kitIterator = playerUser.ownedKits.iterator();
            StringBuilder kitsBuilder = new StringBuilder();
            while (kitIterator.hasNext()) {
                if (i == 0) {
                    i++;
                    kitsBuilder.append(kitIterator.next().getName());
                    continue;
                }
                kitsBuilder.append(";").append(kitIterator.next().getName());
            }
            Iterator<KillEffect> killEffectIterator = playerUser.ownedKillEffects.iterator();
            StringBuilder killBuilder = new StringBuilder();
            while (killEffectIterator.hasNext()) {
                if (i == 0) {
                    i++;
                    killBuilder.append(killEffectIterator.next().name());
                    continue;
                }
                killBuilder.append(";").append(killEffectIterator.next().name());
            }
            i = 0;
            Iterator<WinEffect> winEffectIterator = playerUser.ownedWinEffects.iterator();
            StringBuilder winBuilder = new StringBuilder();
            while (winEffectIterator.hasNext()) {
                if (i == 0) {
                    i++;
                    winBuilder.append(winEffectIterator.next().name());
                    continue;
                }
                winBuilder.append(";").append(winEffectIterator.next().name());
            }
            i = 0;
            Iterator<Glass> glassIterator = playerUser.ownedGlasses.iterator();
            StringBuilder glassBuilder = new StringBuilder();
            while (glassIterator.hasNext()) {
                if (i == 0) {
                    i++;
                    glassBuilder.append(glassIterator.next().getName());
                    continue;
                }
                glassBuilder.append(";").append(glassIterator.next().getName());
            }
            statement.setString(7, kitsBuilder.toString());
            statement.setString(8, killBuilder.toString());
            statement.setString(9, winBuilder.toString());
            statement.setString(10, glassBuilder.toString());
            statement.setInt(11, playerUser.matchmaking);
            statement.setInt(12, playerUser.coins);
            statement.setInt(13, playerUser.elo);
            statement.setInt(14, playerUser.kills);
            statement.setInt(15, playerUser.deaths);
            statement.setInt(16, playerUser.wins);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not save data for " + playerUser.getName());
        }
    }

    public static void deletePlayerData(UUID uuid) {
        try (Connection connection = DATA_SOURCE.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM players WHERE uuid = ?;"
        )) {
            statement.setString(1, uuid.toString());
            statement.execute();
            if (SkywarsAPI.onlinePlayers.containsKey(uuid)) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Skywars.INSTANCE, () -> {
                    Player player = Bukkit.getPlayer(uuid);
                    PlayerUser user = new PlayerUser(uuid, player.getName());
                    SkywarsAPI.onlinePlayers.replace(uuid, user);
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not delete data for " + uuid.toString());
        }
    }

    public static void deleteAllData() {
        if (SkywarsAPI.onlinePlayers.isEmpty()) {
            try (Connection connection = DATA_SOURCE.getConnection(); PreparedStatement statement = connection.prepareStatement(
                    "SELECT uuid, name, kit, killeffect, wineffect, glass, kitslist, killeffectlist, wineffectlist, glasslist, matchmaking, coins, elo FROM players;"
            )) {
                ResultSet resultSet = statement.executeQuery();
                List<PlayerUser> players = new ArrayList<>();
                while (resultSet.next()) {
                    PlayerUser playerUser = new PlayerUser(UUID.fromString(resultSet.getString("uuid")), resultSet.getString("name"));
                    playerUser.kit = SkywarsAPI.KITS_MAP.get(resultSet.getString("kit"));
                    playerUser.killEffect = KillEffect.valueOf(resultSet.getString("killeffect"));
                    playerUser.winEffect = WinEffect.valueOf(resultSet.getString("wineffect"));
                    playerUser.glass = SkywarsAPI.GLASS_MAP.get(resultSet.getString("glass"));
                    for (String s : resultSet.getString("kitslist").split(";")) playerUser.ownedKits.add(SkywarsAPI.KITS_MAP.get(s));
                    for (String s : resultSet.getString("killeffectlist").split(";")) playerUser.ownedKillEffects.add(KillEffect.valueOf(s));
                    for (String s : resultSet.getString("wineffectlist").split(";")) playerUser.ownedWinEffects.add(WinEffect.valueOf(s));
                    for (String s : resultSet.getString("glasslist").split(";")) playerUser.ownedGlasses.add(SkywarsAPI.GLASS_MAP.get(s));
                    playerUser.matchmaking = resultSet.getInt("matchmaking");
                    playerUser.coins = resultSet.getInt("coins");
                    playerUser.elo = resultSet.getInt("elo");
                    playerUser.kills = resultSet.getInt("kills");
                    playerUser.deaths = resultSet.getInt("deaths");
                    playerUser.wins = resultSet.getInt("wins");
                    players.add(playerUser);
                }
                Bukkit.getScheduler().runTaskAsynchronously(Skywars.INSTANCE, () -> {
                    players.forEach(playerUser1 -> deletePlayerData(playerUser1.getUuid()));
                    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "You deleted all data.");
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Bukkit.getLogger().log(Level.SEVERE, "Could not delete data.");
            }
        } else {
            Bukkit.getLogger().log(Level.INFO, "There are players online in the server, the server must be empty in order to delete data!");
        }
    }

}
