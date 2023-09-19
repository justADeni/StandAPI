package com.github.justadeni.standapi.event;

import com.github.justadeni.standapi.PacketStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PacketStandEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public PacketStandEvent(Player player, int id, PacketStand packetStand, Action action) {
        this.player = player;
        this.id = id;
        this.packetStand = packetStand;
        this.action = action;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    private final Player player;
    private final int id;
    private final PacketStand packetStand;
    private final Action action;

    public Player getPlayer() {
        return player;
    }

    public int getId() {
        return id;
    }

    public PacketStand getPacketStand() {
        return packetStand;
    }

    public Action getAction() {
        return action;
    }
}
