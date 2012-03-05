package com.nuclearw.pss;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

public class PssPluginListener extends ServerListener {
	public static Pss plugin;
	
	public PssPluginListener(Pss instance) {
		plugin = instance;
	}
	
    public void onPluginEnable(PluginEnableEvent event) {
        PssPermissionsHandler.onEnable(event.getPlugin());
    }
	
}
