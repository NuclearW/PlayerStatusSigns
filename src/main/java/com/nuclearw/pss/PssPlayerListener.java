package com.nuclearw.pss;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PssPlayerListener extends PlayerListener {
	public static Pss plugin;
	
	public PssPlayerListener(Pss instance) {
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.onJoin(event.getPlayer());
	}
	
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.onLeave(event.getPlayer());
	}
	
	public void onPlayerKick(PlayerKickEvent event) {
		plugin.onLeave(event.getPlayer());
	}
}
