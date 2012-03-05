package com.nuclearw.pss;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PssPermissionsHandler {
	public static Pss plugin;
	
	public PssPermissionsHandler(Pss instance) {
		plugin = instance;
	}
	
	private enum PermissionType {
		PEX, PERMISSIONS, BUKKIT
	}
	private static PermissionType permissionType;
	private static Object PHandle;
	public static boolean permissionsEnabled = false;

	public static void initialize(Server server) {
		//Could have enabled permissionsCompat, check for PEX again
		if(permissionsEnabled && permissionType != PermissionType.PERMISSIONS) return;
		
		Plugin PEXtest = server.getPluginManager().getPlugin("PermissionsEx");
		Plugin test = server.getPluginManager().getPlugin("Permissions");
		if(PEXtest != null) {
			PHandle = (PermissionManager) PermissionsEx.getPermissionManager();
			permissionType = PermissionType.PEX;
			permissionsEnabled = true;
			plugin.log.info("[PSS] PermissionsEx found.");
		} else if(test != null) {
			PHandle = (PermissionHandler) ((Permissions) test).getHandler();
			permissionType = PermissionType.PERMISSIONS;
			permissionsEnabled = true;
			plugin.log.info("[PSS] Permissions "+test.getDescription().getVersion()+" found.");
		} else {
			//plugin.log.info("[PSS] Permissions not found, falling back to ops.");
			permissionType = PermissionType.BUKKIT;
			permissionsEnabled = true;
			plugin.log.info("[PSS] Using Bukkit Permissions.");
		}
	}
	
	public static void onEnable(Plugin pluginEnabled) {
		//Could have enabled permissionsCompat, check for PEX again
		if(permissionsEnabled && permissionType != PermissionType.PERMISSIONS) return;
		
		String pluginName = pluginEnabled.getDescription().getName();
		if(pluginName.equals("PermissionsEx")) {
			PHandle = (PermissionManager) PermissionsEx.getPermissionManager();
			permissionType = PermissionType.PEX;
			permissionsEnabled = true;
			plugin.log.info("[PSS] PermissionsEx found.");
		} else if(pluginName.equals("Permissions")) {
			PHandle = (PermissionHandler) ((Permissions) pluginEnabled).getHandler();
			permissionType = PermissionType.PERMISSIONS;
			permissionsEnabled = true;
			plugin.log.info("[PSS] Permissions "+pluginEnabled.getDescription().getVersion()+" found.");
		}
	}
  
	public boolean hasPermission(Player player, String permission) {
		if(!permissionsEnabled) return player.isOp();
		switch(permissionType) {
			case PEX:
				return ((PermissionManager) PHandle).has(player, permission);
			case PERMISSIONS:
				return ((PermissionHandler) PHandle).has(player, permission);
			case BUKKIT:
				return player.hasPermission(permission);
			default:
				return true;
		}
	}
}
