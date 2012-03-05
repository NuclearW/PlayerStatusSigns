package com.nuclearw.pss;

import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

public class PssSignListener extends BlockListener {
	public static Pss plugin;
	
	public PssSignListener(Pss instance) {
		plugin = instance;
	}
	
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		BlockState state = event.getBlock().getState();
		if(state instanceof Sign) {
			Sign sign = (Sign) state;
			if(!event.getLine(0).equalsIgnoreCase("[PSS]")) return;
			if(!plugin.hasPermission(player, "pss.create")) return;
			if(event.getLine(1) == null) return;
			Player targetPlayer = plugin.getServer().getPlayer(event.getLine(1));
			String targetName = event.getLine(1);
			if(targetPlayer != null) {
				targetName = targetPlayer.getName();
				event.setLine(1, targetName);
				sign.update(true);
			}
			if(!(targetName.equals(player.getName())) && !(plugin.hasPermission(player, "pss.create.other"))) return;
			plugin.addSign(targetName, event.getBlock());
			plugin.setSigns(targetName, true);
		}
	}
}
