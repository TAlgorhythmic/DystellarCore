package net.zylesh.dystellarcore.serialization;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Suffix;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.punishments.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.mariadb.jdbc.MariaDbPoolDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
                "SELECT chat, messages, suffix, punishments, lang FROM players WHERE uuid = ?;"
        )) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            User user;
            if (resultSet.next()) {
                user = new User(uuid);
                user.setGlobalChatEnabled(resultSet.getBoolean("chat"));
                user.setPrivateMessagesActive(resultSet.getBoolean("messages"));
                user.setSuffix(Suffix.valueOf(resultSet.getString("suffix")));
                String[] punishments = resultSet.getString("punishments").split(":");
                for (String s : punishments) user.addPunishment(Punishments.deserialize(s));
                user.setLanguage(resultSet.getString("lang"));
                return user;
            } else {
                return new User(uuid);
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
                "REPLACE players(uuid, chat, messages, suffix, punishments, lang) VALUES(?, ?, ?, ?, ?, ?);"
        )) {
            statement.setString(1, playerUser.getUUID().toString());
            statement.setBoolean(2, playerUser.isGlobalChatEnabled());
            statement.setBoolean(3, playerUser.isPrivateMessagesActive());
            statement.setString(4, playerUser.getSuffix().name());
            if (playerUser.getPunishments().isEmpty()) statement.setString(5, null);
            else {
                StringBuilder sb = new StringBuilder();
                for (Punishment p : playerUser.getPunishments()) {
                    sb.append(Punishments.serialize(p)).append(":");
                }
                statement.setString(5, sb.toString());
            }
            statement.setString(6, playerUser.getLanguage());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not save data for " + playerUser.getUUID());
        }
    }

    public static void deletePlayerData(UUID uuid) {
        try (Connection connection = DATA_SOURCE.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM players WHERE uuid = ?;"
        )) {
            statement.setString(1, uuid.toString());
            statement.execute();
            if (User.getUsers().containsKey(uuid)) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(DystellarCore.getInstance(), () -> {
                    User user = new User(uuid);
                    User.getUsers().replace(uuid, user);
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not delete data for " + uuid.toString());
        }
    }

    public static void deleteAllData() {
        try (Connection connection = DATA_SOURCE.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT uuid, chat, messages, suffix, punishments, lang FROM players;"
        )) {
            ResultSet resultSet = statement.executeQuery();
            List<User> players = new ArrayList<>();
            while (resultSet.next()) {
                User user = new User(UUID.fromString(resultSet.getString("uuid")));
                user.setGlobalChatEnabled(resultSet.getBoolean("chat"));
                user.setPrivateMessagesActive(resultSet.getBoolean("messages"));
                user.setSuffix(Suffix.valueOf(resultSet.getString("suffix")));
                String[] punishments = resultSet.getString("punishments").split(":");
                for (String s : punishments) user.addPunishment(Punishments.deserialize(s));
                user.setLanguage(resultSet.getString("lang"));
                players.add(user);
            }
            Bukkit.getScheduler().runTaskAsynchronously(DystellarCore.getInstance(), () -> {
                players.forEach(playerUser1 -> deletePlayerData(playerUser1.getUUID()));
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "You deleted all data.");
            });
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not delete data.");
        }
    }

}
