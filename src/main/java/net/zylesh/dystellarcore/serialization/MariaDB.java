package net.zylesh.dystellarcore.serialization;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Suffix;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.punishments.Ban;
import net.zylesh.dystellarcore.core.punishments.Blacklist;
import net.zylesh.dystellarcore.core.punishments.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.mariadb.jdbc.MariaDbDataSource;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class MariaDB {

    public static boolean ENABLED;
    private static String HOST;
    private static int PORT;
    private static String DATABASE;
    private static String USER;
    private static String PASSWORD;
    private static MariaDbDataSource DS;

    public static void loadFromConfig() {
        ENABLED = DystellarCore.getInstance().getConfig().getBoolean("mariadb.enabled");
        HOST = DystellarCore.getInstance().getConfig().getString("mariadb.host");
        PORT = DystellarCore.getInstance().getConfig().getInt("mariadb.port");
        DATABASE = DystellarCore.getInstance().getConfig().getString("mariadb.database");
        USER = DystellarCore.getInstance().getConfig().getString("mariadb.user");
        PASSWORD = DystellarCore.getInstance().getConfig().getString("mariadb.password");
    }

    public static void dataSourceTestInit() throws SQLException {
        MariaDbDataSource ds = new MariaDbDataSource("jdbc:mariadb://" + HOST + ":" + PORT + "/" + DATABASE);
        ds.setLoginTimeout(5);
        ds.setUser(USER);
        ds.setPassword(PASSWORD);
        DS = ds;
        try (Connection connection = getConnection()) {
            if (!connection.isValid(5)) {
                throw new SQLException("Could not create a connection.");
            }
        }
    }

    @Nullable
    public static User loadPlayerFromDatabase(UUID uuid, String IP, String name) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT chat, messages, suffix, punishments, notes, lang, inbox FROM players_core WHERE uuid = ?;"
        )) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            User user;
            if (resultSet.next()) {
                user = new User(uuid, IP, name);
                user.setGlobalChatEnabled(resultSet.getBoolean("chat"));
                user.setPrivateMessagesActive(resultSet.getBoolean("messages"));
                user.setSuffix(Suffix.valueOf(resultSet.getString("suffix")));
                String[] punishments = resultSet.getString("punishments") != null ? resultSet.getString("punishments").split(":") : null;
                if (punishments != null) for (String s : punishments) user.addPunishment(Punishments.deserialize(s));
                user.getNotes().addAll(Punishments.deserializeNotes(resultSet.getString("notes")));
                user.setLanguage(resultSet.getString("lang"));
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

    @Nullable
    public static Mapping loadMapping(String IP) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT something1, something2, punishments FROM mappings WHERE something0 = ?;")) {
            statement.setString(1, IP);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) return null;
            Set<Punishment> punishmentSet = new HashSet<>();
            for (String s : rs.getString("punishments").split(":")) punishmentSet.add(Punishments.deserialize(s));
            return new Mapping(UUID.fromString(rs.getString("something2")), IP, rs.getString("something1"), punishmentSet);
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not load mapping for " + IP);
        }
        return null;
    }

    /**
     * Highly recommended to do this async
     * @param user player to save
     */
    public static void savePlayerToDatabase(User user) {
        StringBuilder ipP = null;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("REPLACE players_core(uuid, chat, messages, suffix, punishments, notes, lang, inbox) VALUES(?, ?, ?, ?, ?, ?, ?, ?);")
        ) {
            statement.setString(1, user.getUUID().toString());
            statement.setBoolean(2, user.isGlobalChatEnabled());
            statement.setBoolean(3, user.isPrivateMessagesActive());
            statement.setString(4, user.getSuffix().name());
            if (user.getPunishments().isEmpty()) statement.setString(5, null);
            else {
                StringBuilder sb = new StringBuilder();
                for (Punishment p : user.getPunishments()) {
                    sb.append(Punishments.serialize(p)).append(":");
                    if ((p instanceof Ban && ((Ban) p).isAlsoIP()) || p instanceof Blacklist) {
                        if (ipP == null) ipP = new StringBuilder();
                        ipP.append(Punishments.serialize(p)).append(":");
                    }
                }
                statement.setString(5, sb.toString());
            }
            if (user.getNotes().isEmpty()) statement.setString(6, null);
            else statement.setString(6, Punishments.serializeNotes(user.getNotes()));
            statement.setString(7, user.getLanguage());
            statement.setString(8, null); // TODO inboxes system
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not save data for " + user.getUUID());
        }
        try (Connection connection = getConnection(); PreparedStatement statement1 = connection.prepareStatement("REPLACE mappings(something0, something1, something2, punishments) VALUES(?, ?, ?);")) // UUID, IP, Name.)
        {
            statement1.setString(1, user.getUUID().toString());
            statement1.setString(2, user.getIp());
            statement1.setString(3, user.getName());
            statement1.setString(4, null);
            statement1.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not save UUID mappings for " + user.getUUID());
        }
        try (Connection connection = getConnection(); PreparedStatement statement2 = connection.prepareStatement("REPLACE mappings(something0, something1, something2, punishments) VALUES(?, ?, ?);")) // IP, Name, UUID. // UUID, IP, Name.)
        {
            statement2.setString(1, user.getIp());
            statement2.setString(2, user.getName());
            statement2.setString(3, user.getUUID().toString());
            if (ipP == null) statement2.setString(4, null);
            else statement2.setString(4, ipP.toString());
            statement2.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not save IP mappings for " + user.getIp());
        }
        try (Connection connection = getConnection(); PreparedStatement statement3 = connection.prepareStatement("REPLACE mappings(something0, something1, something2, punishments) VALUES(?, ?, ?);")) // IP, Name, UUID. // UUID, IP, Name.)
        {
            statement3.setString(1, user.getName());
            statement3.setString(2, user.getUUID().toString());
            statement3.setString(3, user.getIp());
            statement3.setString(4, null);
            statement3.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not save name mappings for " + user.getName());
        }
    }

    public static void deletePlayerData(User user) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM players_core WHERE uuid = ?;"
        )) {
            statement.setString(1, user.getUUID().toString());
            statement.execute();
            if (User.getUsers().containsKey(user.getUUID())) {
                User realUser = User.get(user.getUUID());
                User user1 = new User(realUser.getUUID(), realUser.getIp(), realUser.getName());
                User.getUsers().replace(realUser.getUUID(), user1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not delete data for " + user.toString());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DS.getConnection();
    }

    public static void deleteAllData(CommandSender sender) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT uuid FROM players_core;"
        )) {
            ResultSet resultSet = statement.executeQuery();
            List<User> players = new ArrayList<>();
            while (resultSet.next()) {
                User user = new User(UUID.fromString(resultSet.getString("uuid")), "", "");
                players.add(user);
            }
            players.forEach(MariaDB::deletePlayerData);
            sender.sendMessage(ChatColor.GREEN + "You deleted all data.");
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not delete data.");
        }
    }

    /*
    UUID, IP, Name
    IP, Name, UUID
    Name, UUID, IP
     */

    @Nullable
    public static UUID loadUUID(String aString) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT something1, something2 FROM mappings WHERE something0 = ?;")) {
            statement.setString(1, aString);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) return null;
            if (stringIsIP(aString)) {
                return UUID.fromString(rs.getString("something2"));
            } else {
                return UUID.fromString(rs.getString("something1"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not load UUID from " + aString);
        }
        return null;
    }

    @Nullable
    public static String loadIP(String aString) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT something1, something2 FROM mappings WHERE something0 = ?;")) {
            statement.setString(1, aString);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) return null;
            if (stringIsUUID(aString)) {
                return rs.getString("something1");
            } else {
                return rs.getString("something2");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not load UUID from " + aString);
        }
        return null;
    }

    @Nullable
    public static String loadName(String aString) {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT something1, something2 FROM mappings WHERE something0 = ?;")) {
            statement.setString(1, aString);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) return null;
            if (stringIsUUID(aString)) {
                return rs.getString("something2");
            } else if (stringIsIP(aString)) {
                return rs.getString("something1");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Could not load UUID from " + aString);
        }
        return null;
    }

    private static boolean stringIsIP(String s) {
        return s.split("\\.").length == 4 && s.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+");
    }

    private static boolean stringIsUUID(String s) {
        return s.length() == 36;
    }
}
