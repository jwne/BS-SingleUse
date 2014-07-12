package org.black_ixx.bossshop.addon.singleuse;


import org.black_ixx.bossshop.api.BossShopAddon;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class SingleUse extends BossShopAddon{

	private UsedStorage storage;
	private BSListener listener;
	
	private String message = "";
	private boolean hiding = false;
	
	@Override
	public void disableAddon() {
		listener.disable();
		storage.save();
	}

	
	@Override
	public void enableAddon() {
		storage = new UsedStorage(this);
		listener = new BSListener(this);
		getServer().getPluginManager().registerEvents(listener, this);
		listener.enable();
		loadConfig();
		
	}
	

	@Override
	public void bossShopReloaded(CommandSender sender) {
		listener.disable();
		listener.enable();
		loadConfig();
		sender.sendMessage(ChatColor.YELLOW+"Reloaded BossShop Addon "+ChatColor.GOLD+getAddonName());
	}
	
	
	@Override
	public String getAddonName() {
		return "BS-SingleUse";
	}

	@Override
	public String getRequiredBossShopVersion() {
		return "1.9.0";
	}
	
	public UsedStorage getStorage(){
		return storage;
	}
	
	public String getMessage(){
		return message;
	}
	
	public boolean hiding(){
		return hiding;
	}

	private void loadConfig(){
		reloadConfig();
		if(getConfig().getString("Message")==null){
			getConfig().set("Message", "&cYou already bought this &6%limit%&c/&6%limit% &ctimes!");
			getConfig().set("HideUsedItems", false);
		}
		saveConfig();
		
		message=getBossShop().getClassManager().getStringManager().transform(getConfig().getString("Message"));	
		hiding = getConfig().getBoolean("HideUsedItems");
	}


}
