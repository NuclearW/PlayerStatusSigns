package com.nuclearw.pss;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Pss extends JavaPlugin{
	static String mainDirectory = "plugins" + File.separator + "PlayerStatusSigns";
	static File versionFile = new File(mainDirectory + File.separator + "VERSION");
	static File languageFile = new File(mainDirectory + File.separator + "lang");
	static File signsFile = new File(mainDirectory + File.separator + "signs");
	
	private final PssPlayerListener playerListener = new PssPlayerListener(this);
	private final PssSignListener signListener = new PssSignListener(this);
	private final PssBlockListener blockListener = new PssBlockListener(this);

	Logger log = Logger.getLogger("Minecraft");
	
	/*
	 * 0 = Online
	 * 1 = Offline
	 * 2 = AFK
	 * 3 = Since
	 * 4 = You are afk
	 * 5 = You are not afk
	 */
	String[] language = new String[6];

	public HashMap<String, Block[]> signs = new HashMap<String, Block[]>();
	public HashMap<String, Boolean> afkState = new HashMap<String, Boolean>();
	
	public void onEnable() {
		new File(mainDirectory).mkdir();
		
		if(!versionFile.exists()) {
			updateVersion();
		} else {
			String vnum = readVersion();
			if(vnum.equals("0.1")) updateVersion();
			if(vnum.equals("0.2")) updateVersion();
			if(vnum.equals("0.2.1")) updateVersion();
			if(vnum.equals("0.3")) updateVersion();
			if(vnum.equals("0.4.1")) updateVersion();
		}
		
		Properties prop = new Properties();
		
		if(!languageFile.exists()) {
			try {
				languageFile.createNewFile();
				FileOutputStream out = new FileOutputStream(languageFile);
				prop.put("online", "Online");
				prop.put("offline", "Offline");
				prop.put("afk", "AFK");
				prop.put("since", "Since");
				prop.put("now-afk", "You are AFK.");
				prop.put("not-afk", "You are not AFK.");
				prop.store(out, "Loaclization.");
				out.flush();
				out.close();
				prop.clear();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		FileInputStream langin;
		try {
			langin = new FileInputStream(languageFile);
			prop.load(langin);
			langin.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		if(!prop.containsKey("online") || !prop.containsKey("offline") || !prop.containsKey("afk") || !prop.containsKey("since") || !prop.containsKey("not-afk") || !prop.containsKey("now-afk")) {
			log.severe("[PSS] PlayerStatusSigns lang file incomplete! Reverting to default!");
			try {
				languageFile.createNewFile();
				FileOutputStream out = new FileOutputStream(languageFile);
				prop.put("online", "Online");
				prop.put("offline", "Offline");
				prop.put("afk", "AFK");
				prop.put("since", "Since");
				prop.put("now-afk", "You are AFK.");
				prop.put("not-afk", "You are not AFK.");
				prop.store(out, "Loaclization.");
				out.flush();
				out.close();
				prop.clear();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		this.language[0] = prop.getProperty("online");
		this.language[1] = prop.getProperty("offline");
		this.language[2] = prop.getProperty("afk");
		this.language[3] = prop.getProperty("since");
		this.language[4] = prop.getProperty("now-afk");
		this.language[5] = prop.getProperty("not-afk");
		
		if(signsFile.exists()) loadSigns();
		afkState.clear();
		
		PluginManager pluginManager = getServer().getPluginManager();

		pluginManager.registerEvents(playerListener, this);
		pluginManager.registerEvents(signListener, this);
		pluginManager.registerEvents(blockListener, this);
		
		log.addHandler(new Handler() {
			public void publish(LogRecord logRecord) {
				String mystring = logRecord.getMessage();
				if(mystring.contains(" lost connection: ")) {
					String myarray[] = mystring.split(" ");
					String playerName = myarray[0];
					String DisconnectMessage = myarray[3];
					if(DisconnectMessage.equals("disconnect.quitting")) return;
					onLeave(playerName);
				}
			}
			public void flush() {}
			public void close() {
			}
		});
		
		log.info("[PSS] PlayerStatusSigns version "+this.getDescription().getVersion()+" loaded.");
	}
	
	public void onDisable() {
		saveSigns();
		log.info("[PSS] PlayerStatusSigns version "+this.getDescription().getVersion()+" unloaded.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("afk")) {
			if(!isPlayer(sender)) return true;
		//	log.info("is a player");
			if(!this.hasPermission((Player) sender, "pss.afk")) return true;
		//	log.info("has permission");
			String playerName = ((Player) sender).getName();
			Block[] pSigns = signs.get(playerName);
			if(pSigns == null) return true;
		//	log.info("has a sign");
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("on")) {
					setSigns(pSigns, 2, playerName);
					this.afkState.put(playerName, true);
					sender.sendMessage(language[4]);
				} else if(args[0].equalsIgnoreCase("off")) {
					setSigns(pSigns, 0, playerName);
					this.afkState.put(playerName, false);
					sender.sendMessage(language[5]);
				}
			} else {
				if(!this.afkState.containsKey(playerName) || !this.afkState.get(playerName)) {
					setSigns(pSigns, 2, playerName);
					this.afkState.put(playerName, true);
					sender.sendMessage(language[4]);
				} else {
					setSigns(pSigns, 0, playerName);
					this.afkState.put(playerName, false);
					sender.sendMessage(language[5]);
				}
			}
		}
		if(cmd.getName().equalsIgnoreCase("pss")) {
			if(isPlayer(sender)) {
				if(!this.hasPermission((Player) sender, "pss.admin")) return true;
			}
			if(args.length > 1 || args.length == 0) return false;
			char first = args[0].toLowerCase().charAt(0);
			switch (first) {
				case 'd':
					sender.sendMessage(signs.toString());
					return true;
				case 'l':
					loadSigns();
				case 's':
					saveSigns();
				case 'c':
					checkSigns();
			}
		}
		return true;
	}
	
	public void onJoin(Player player) {
		if(!signs.containsKey(player.getName())) return;
		Block[] blocks = signs.get(player.getName());
		setSigns(blocks, 0, player.getName());
	}
	
	public void onLeave(Player player) {
		onLeave(player.getName());
	}
	
	public void onLeave(String playerName) {
		if(!signs.containsKey(playerName)) return;
		long time = System.currentTimeMillis();
		Block[] blocks = signs.get(playerName);
		setSigns(blocks, 1, playerName, time);
		
	}
	
	public void checkSigns() {
		Set<String> keys = signs.keySet();
		Iterator<String> i = keys.iterator();
		while(i.hasNext()) {
			String playerName = i.next();
			Block[] blocks = signs.get(playerName);
			for(Block b : blocks) {
				Chunk chunk = b.getChunk();
				World world = b.getWorld();
				if(!world.isChunkLoaded(chunk)) world.loadChunk(chunk);
				final BlockState bState = b.getState();
				if(!(bState instanceof Sign)) {
					this.removeSign(playerName, b);
					return;
				}
			}
		}
	}
	
	public void setSigns(String playerName) {
		setSigns(playerName, false);
	}
	
	public void setSigns(String playerName, Boolean newSign) {
		if(getServer().getPlayer(playerName) != null) {
			setSigns(signs.get(playerName), 0, playerName);
		} else {
			if(newSign) {
				setSigns(signs.get(playerName), 1, playerName, Long.parseLong(Integer.toString(-1)));
			} else {
				setSigns(signs.get(playerName), 1, playerName, Long.parseLong(Integer.toString(0)));
			}
		}
	}
	
	public void setSigns(Block[] blocks, int mode, String playerName) {
		setSigns(blocks, mode, playerName, System.currentTimeMillis());
	}
	
	public void setSigns(Block[] blocks, int mode, String playerName, Long time) {
		for(Block b : blocks) {

			Chunk chunk = b.getChunk();
			World world = b.getWorld();
			if(!world.isChunkLoaded(chunk)) world.loadChunk(chunk);
			
			final BlockState bState = b.getState();
			if(!(bState instanceof Sign)) {
				this.removeSign(playerName, b);
				return;
			}
			
			if(mode == 0) {
				String tUsername = null;
			//	log.info(Integer.toString(playerName.length()));
				if(playerName.length() >= 15) {
					tUsername = playerName;
				} else {
					tUsername = ("&a"+playerName).replaceAll("(&([a-f0-9]))", "\u00A7$2");
				}
				final String mUsername = tUsername;
				final String mOnline = ("&a"+this.language[0]).replaceAll("(&([a-f0-9]))", "\u00A7$2");
				getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						Sign sign = (Sign) bState;
						sign.setLine(0, mUsername);
						sign.setLine(1, mOnline);
						sign.setLine(2, "");
						sign.setLine(3, "");
						sign.update();
					}
				});
			} else if(mode == 1) {
				//At this point we know they are offline, but not necessarily when they left.
				if(time == 0) {
					Sign sign = (Sign) bState;
					//See if it already has an offline statement.  It might already have the time.
					if(sign.getLine(1).contains(language[1])) return;
					//Maybe they were online?
					//If so we need to presume they are offline from right now.
					if(sign.getLine(1).contains(language[0])) time = System.currentTimeMillis();
					//Also maybe they were AFK?
					//Same assumption
					if(sign.getLine(1).contains(language[2])) time = System.currentTimeMillis();
				}
				String tUsername = null;
			//	log.info(Integer.toString(playerName.length()));
				if(playerName.length() >= 15) {
					tUsername = playerName;
				} else {
					tUsername = ("&c"+playerName).replaceAll("(&([a-f0-9]))", "\u00A7$2");
				}
				final String mUsername = tUsername;
				final String mOffline = ("&c"+this.language[1]).replaceAll("(&([a-f0-9]))", "\u00A7$2");
				String tSince = null;
				String tDate = null;
				//Newsign if -1
				if(time == -1) {
					tSince = "";
					tDate = "";
				} else {
					tSince = ("&c"+this.language[3]).replaceAll("(&([a-f0-9]))", "\u00A7$2");
					tDate = ("&c"+DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(time))).replaceAll("(&([a-f0-9]))", "\u00A7$2");
				}
				final String mSince = tSince;
				final String mDate = tDate;
				getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						Sign sign = (Sign) bState;
						sign.setLine(0, mUsername);
						sign.setLine(1, mOffline);
						sign.setLine(2, mSince);
						sign.setLine(3, mDate);
						sign.update();
					}
				});
			} else if(mode == 2) {
				String tUsername = null;
			//	log.info(Integer.toString(playerName.length()));
				if(playerName.length() >= 15) {
					tUsername = playerName;
				} else {
					tUsername = ("&e"+playerName).replaceAll("(&([a-f0-9]))", "\u00A7$2");
				}
				final String mUsername = tUsername;
				final String mAfk = ("&e"+this.language[2]).replaceAll("(&([a-f0-9]))", "\u00A7$2");
				final String mSince = ("&e"+this.language[3]).replaceAll("(&([a-f0-9]))", "\u00A7$2");
				final String mDate = ("&e"+DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(time))).replaceAll("(&([a-f0-9]))", "\u00A7$2");
				getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						Sign sign = (Sign) bState;
						sign.setLine(0, mUsername);
						sign.setLine(1, mAfk);
						sign.setLine(2, mSince);
						sign.setLine(3, mDate);
						sign.update();
					}
				});
			}
		}
	}
	
	public boolean hasPermission(Player player, String permission) {
		return player.hasPermission(permission);
	}

	public void removeSign(Block block) {
		for(String playerName : signs.keySet()) {
			Block[] blocks = signs.get(playerName);
			ArrayList<Block> testBlocksList = new ArrayList<Block>(Arrays.asList(blocks));
			if(testBlocksList.contains(block)) {
				removeSign(playerName, block);
				return;
			}
		}
	}
	
	public void removeSign(String targetName, Block block) {
		Block[] blocks = signs.get(targetName);
		//How could this happen?
		if(blocks == null) {
			log.severe("[PSS] Unexpected Error 1.");
			return;
		}
		if(blocks.length-1 < 1) {
			signs.remove(targetName);
		} else {
			ArrayList<Block> newBlocksList = new ArrayList<Block>(Arrays.asList(blocks));
			newBlocksList.remove(block);
			Block[] newBlocks = new Block[newBlocksList.size()];
			newBlocks = newBlocksList.toArray(newBlocks);
			signs.put(targetName, newBlocks);
			/*
			Block[] newBlocks = new Block[blocks.length-1];
			int i = 0;
			for(Block b : blocks) {
				if(b == block) break;
				newBlocks[i] = b;
				i++;
			}
			*/
		}
		this.saveSigns();
	}
	
	public void addSign(String targetName, Block block) {
		Block[] blocks = signs.get(targetName);
		if(blocks == null) {
			ArrayList<Block> newBlocksList = new ArrayList<Block>();
			newBlocksList.add(block);
			Block[] newBlocks = new Block[newBlocksList.size()];
			newBlocks = newBlocksList.toArray(newBlocks);
			signs.put(targetName, newBlocks);
		} else {
			ArrayList<Block> newBlocksList = new ArrayList<Block>(Arrays.asList(blocks));
			newBlocksList.add(block);
			Block[] newBlocks = new Block[newBlocksList.size()];
			newBlocks = newBlocksList.toArray(newBlocks);
			signs.put(targetName, newBlocks);
		}
		/*
		Block[] newBlocks = new Block[blocks.length+1];
		int i = 0;
		for(Block b : blocks) {
			newBlocks[i] = b;
			i++;
		}
		newBlocks[i] = block;
		*/
		this.saveSigns();
	}
	
	public void saveSigns() {
		String store = "<";
		Set<String> keys = signs.keySet();
		Iterator<String> i = keys.iterator();
		while(i.hasNext()) {
			String player = i.next();
			store += player + ";";
			Block[] blocks = signs.get(player);
			for(Block b : blocks) {
				store += Integer.toString(b.getX()) + ";";
				store += Integer.toString(b.getY()) + ";";
				store += Integer.toString(b.getZ()) + ";";
				store += b.getWorld().getName() + ";";
			}
			store = store.substring(0, store.length()-1)+">" + System.getProperty("line.separator") + "<";
		}
		store = store.substring(0, store.length()-1);
		try {
			signsFile.createNewFile();
			BufferedWriter vout = new BufferedWriter(new FileWriter(signsFile));
			vout.write(store);
			vout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (SecurityException ex) {
			ex.printStackTrace();
		}
		/*
		try {
			ObjectOutputStream obj = new ObjectOutputStream(new FileOutputStream(signsFile));
			obj.writeObject(signs);
			obj.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
	
	public void loadSigns() {
		byte[] buffer = new byte[(int) signsFile.length()];
		signs.clear();
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(signsFile));
			f.read(buffer);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (f != null) try { f.close(); } catch (IOException ignored) { }
		}
		String store = new String(buffer);
		String[] data = new String[5];
		if(store.isEmpty()) return;
		while(store.contains("<")) {
			int cutBegin = store.indexOf('<');
			int cutEnd = store.indexOf('>');
			String storeEntry = store.substring(cutBegin+1, cutEnd);
		//	log.info("storeEntry: "+storeEntry);
			data[0] = storeEntry.substring(0, storeEntry.indexOf(';'));
		//	log.info("data: "+data[0]);
			storeEntry = storeEntry.substring(storeEntry.indexOf(';')+1);
			int i = 1;
			while(storeEntry.contains(";") || i == 4 || i == 5) {
				if(i == 5) {
					String player = data[0];
					int x = Integer.parseInt(data[1]);
					int y = Integer.parseInt(data[2]);
					int z = Integer.parseInt(data[3]);
					String world = data[4];
					
					Block dataBlock = getServer().getWorld(world).getBlockAt(x, y, z);
					
					Block[] blocks = signs.get(player);
					if(blocks == null) {
						ArrayList<Block> newBlocksList = new ArrayList<Block>();
						newBlocksList.add(dataBlock);
						Block[] newBlocks = new Block[newBlocksList.size()];
						newBlocks = newBlocksList.toArray(newBlocks);
						signs.put(player, newBlocks);
					} else {
						ArrayList<Block> newBlocksList = new ArrayList<Block>(Arrays.asList(blocks));
						newBlocksList.add(dataBlock);
						Block[] newBlocks = new Block[newBlocksList.size()];
						newBlocks = newBlocksList.toArray(newBlocks);
						signs.put(player, newBlocks);
					}
					setSigns(data[0]);
					i = 1;
					break;
				}
				if(i == 4 && !storeEntry.contains(";")) {
					data[i] = storeEntry.substring(0);
				} else {
					data[i] = storeEntry.substring(0, storeEntry.indexOf(';'));
				}
		//		log.info("data "+Integer.toString(i)+": "+data[i]);
				storeEntry = storeEntry.substring(storeEntry.indexOf(';')+1);
				i++;
			}
			store = store.substring(cutEnd+1);
		//	log.info("store: "+store);
		}
		
		String player = data[0];
		int x = Integer.parseInt(data[1]);
		int y = Integer.parseInt(data[2]);
		int z = Integer.parseInt(data[3]);
		String world = data[4];
		
		Block dataBlock = getServer().getWorld(world).getBlockAt(x, y, z);
		
		Block[] blocks = signs.get(player);
		if(blocks == null) {
			ArrayList<Block> newBlocksList = new ArrayList<Block>();
			newBlocksList.add(dataBlock);
			Block[] newBlocks = new Block[newBlocksList.size()];
			newBlocks = newBlocksList.toArray(newBlocks);
			signs.put(player, newBlocks);
		} else {
			ArrayList<Block> newBlocksList = new ArrayList<Block>(Arrays.asList(blocks));
			newBlocksList.add(dataBlock);
			Block[] newBlocks = new Block[newBlocksList.size()];
			newBlocks = newBlocksList.toArray(newBlocks);
			signs.put(player, newBlocks);
		}
		setSigns(data[0]);
	}
	
	public void updateVersion() {
		try {
			versionFile.createNewFile();
			BufferedWriter vout = new BufferedWriter(new FileWriter(versionFile));
			vout.write(this.getDescription().getVersion());
			vout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (SecurityException ex) {
			ex.printStackTrace();
		}
	}

	public String readVersion() {
		byte[] buffer = new byte[(int) versionFile.length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(versionFile));
			f.read(buffer);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (f != null) try { f.close(); } catch (IOException ignored) { }
		}
		
		return new String(buffer);
	}
	
	public boolean isPlayer(CommandSender sender) {
		return sender != null && sender instanceof Player;
	}
}
