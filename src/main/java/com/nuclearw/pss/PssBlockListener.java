package com.nuclearw.pss;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

public class PssBlockListener extends BlockListener {
	public static Pss plugin;
	
	public PssBlockListener(Pss instance) {
		plugin = instance;
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
		int changedMaterial = event.getBlock().getTypeId();
		if(changedMaterial == 63 || changedMaterial == 68) {
		//	plugin.log.info("Block broken was a sign.");
			plugin.removeSign(event.getBlock());
		}
	}
}
