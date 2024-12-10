package net.zylesh.dystellarcore.services.messaging;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.google.common.io.ByteArrayDataInput;

import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.punishments.Punishment;
import net.zylesh.dystellarcore.serialization.Punishments;
import net.zylesh.dystellarcore.utils.Utils;

public class Handler {

	public static void handlePunData(ByteArrayDataInput in) {
		String string = in.readUTF();
		Player player = Bukkit.getPlayer(string);

		if (player == null) return;
		User user = User.get(player);
		Utils.sendPluginMessage(player, Types.PUNISHMENTS_DATA_RESPONSE, Punishments.serializePunishments(user.getPunishments()));
	}

	public static void handlePunDataRes(Player p, ByteArrayDataInput in) {
		String string = in.readUTF();
		Player player = Bukkit.getPlayer(string);

		if (player == null)
			return;
		UUID target = UUID.fromString(in.readUTF());
		Set<Punishment> punishments = Punishments.deserializePunishments(in.readUTF(), new HashSet<>());

		invs.put(p.getUniqueId(), new AbstractMap.SimpleImmutableEntry<>(target, new Punishment[27]));
		Inventory inv = createInventory(p, punishments);
		p.openInventory(inv);
	}
}
