package com.nuclearw.pss;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class PssBlockListener implements Listener {
	public static Pss plugin;

	public PssBlockListener(Pss instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		int changedMaterial = event.getBlock().getTypeId();
		if(changedMaterial == 63 || changedMaterial == 68) {
		//	plugin.log.info("Block broken was a sign.");
			plugin.removeSign(event.getBlock());
		}
	}
}
