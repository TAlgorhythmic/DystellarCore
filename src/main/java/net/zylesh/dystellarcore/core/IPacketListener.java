package net.zylesh.dystellarcore.core;

import net.minecraft.server.v1_7_R4.Packet;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

public interface IPacketListener {

    void onPacketReceive(Packet packet, Player player, AtomicBoolean cancel);

    void onPacketSend(Packet packet, Player player, AtomicBoolean cancel);
}
