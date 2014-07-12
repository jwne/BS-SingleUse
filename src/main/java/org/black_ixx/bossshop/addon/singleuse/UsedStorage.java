package org.black_ixx.bossshop.addon.singleuse;

import java.util.List;

import org.black_ixx.bossshop.api.BSAddonConfig;
import org.bukkit.OfflinePlayer;

public class UsedStorage extends BSAddonConfig {
	private SingleUse plugin;

	public UsedStorage(SingleUse plugin) {
		super(plugin, "used");

		this.plugin = plugin;
	}

	public List<String> loadPlayer(OfflinePlayer player) {
		if (getConfig().isSet("players." + player.getUniqueId())) {
			return getConfig().getStringList("players." + player.getUniqueId());
		} else if (getConfig().isSet("players." + player.getName())) {
			// Convert to UUID
			List<String> nameList = getConfig().getStringList("players." + player.getName());
			
			plugin.getLogger().info("Converting " + player.getName());
			
			getConfig().set("players." + player.getName(), null);
			savePlayer(player, nameList);

			return nameList;
		}

		return null;
	}

	public void savePlayer(OfflinePlayer player, List<String> list) {
		getConfig().set("players." + player.getUniqueId(), list);
		plugin.getStorage().saveAsync();
	}

}
