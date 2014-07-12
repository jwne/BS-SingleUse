package org.black_ixx.bossshop.addon.singleuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.black_ixx.bossshop.core.BSBuy;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.events.BSDisplayItemEvent;
import org.black_ixx.bossshop.events.BSPlayerPurchaseEvent;
import org.black_ixx.bossshop.events.BSPlayerPurchasedEvent;
import org.black_ixx.bossshop.managers.config.BSConfigShop;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BSListener implements Listener {

	public BSListener(SingleUse plugin) {
		this.plugin = plugin;
	}

	private SingleUse plugin;
	private HashMap<String, Integer> single_use = new HashMap<String, Integer>();
	private HashMap<UUID, List<String>> already_used = new HashMap<UUID, List<String>>();

	public void loadItems() {
		HashMap<BSConfigShop, List<BSBuy>> all = plugin.getBossShop().getAPI().getAllShopItems("SingleUse");
		int i = 0;
		for (BSConfigShop shop : all.keySet()) {
			List<BSBuy> items = all.get(shop);
			if (items != null) {
				for (BSBuy buy : items) {
					if (buy != null) {
						int x = buy.getConfigurationSection(shop).getInt("SingleUse");
						if (x == 0) {
							x = 1;
						}
						shop.setCustomizable(true);
						single_use.put(shop.getShopName() + "-" + buy.getName(), x);
						i++;
					}
				}
			}
		}
		plugin.printInfo("Loaded " + i + " SingleUse Items");
	}

	@EventHandler
	public void onItemDisplay(BSDisplayItemEvent e) {
		if (plugin.hiding()) {
			String tag = createTag(e.getShop(), e.getShopItem());
			if (single_use.containsKey(tag)) {
				if (reachedLimit(e.getPlayer(), e.getShopItem(), e.getShop(), single_use.get(tag))) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onItemPurchased(BSPlayerPurchasedEvent e) {
		if (single_use.containsKey(e.getShop().getShopName() + "-" + e.getShopItem().getName())) {
			use(e.getPlayer(), e.getShopItem(), e.getShop());
		}
	}

	@EventHandler
	public void onItemPurchase(BSPlayerPurchaseEvent e) {
		String tag = createTag(e.getShop(), e.getShopItem());
		if (single_use.containsKey(tag)) {
			if (reachedLimit(e.getPlayer(), e.getShopItem(), e.getShop(), single_use.get(tag))) {
				e.getPlayer().sendMessage(plugin.getMessage().replaceAll("%limit%", "" + single_use.get(tag)));
				e.setCancelled(true);
			}
		}
	}

	private void use(OfflinePlayer player, BSBuy item, BSShop shop) {
		if (!already_used.containsKey(player.getUniqueId())) {
			already_used.put(player.getUniqueId(), new ArrayList<String>());
		}
		String s = createTag(shop, item);
		int i = getAmountUsed(player, item, shop);
		if (i == 0) {
			already_used.get(player.getUniqueId()).add(s + "-" + 1);
			return;
		}
		List<String> list = already_used.get(player.getUniqueId());
		for (String str : list) {
			if (str.startsWith(s)) {
				list.remove(str);
				i++;
				list.add(s + "-" + i);
				return;
			}
		}

	}

	private boolean reachedLimit(OfflinePlayer player, BSBuy item, BSShop shop, int limit) {
		return getAmountUsed(player, item, shop) >= limit;
	}

	private int getAmountUsed(OfflinePlayer player, BSBuy item, BSShop shop) {
		if (!already_used.containsKey(player.getUniqueId())) {
			return 0;
		}
		List<String> used = already_used.get(player.getUniqueId());
		String tag = createTag(shop, item);
		for (String s : used) {
			if (s.startsWith(tag)) {
				try {
					String x = s.replace(tag + "-", "");
					return Integer.parseInt(x);
				} catch (Exception e) {
					return 1;
				}
			}
		}
		return 0;
	}

	private String createTag(BSShop shop, BSBuy item) {
		return shop.getShopName() + "-" + item.getName();
	}

	private void loadPlayer(OfflinePlayer player, List<String> already_used) {
		if (already_used == null || already_used.isEmpty()) {
			return;
		}
		this.already_used.put(player.getUniqueId(), already_used);
	}

	private void savePlayer(OfflinePlayer player) {
		if (already_used.containsKey(player.getUniqueId())) {
			plugin.getStorage().savePlayer(player, already_used.get(player.getUniqueId()));
		}
	}

	private void removePlayer(OfflinePlayer player) {
		this.already_used.remove(player.getUniqueId());
	}

	public void disable() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			savePlayer(p);
			removePlayer(p);
		}
		already_used.clear();
		single_use.clear();
	}

	public void enable() {
		loadItems();
		for (Player p : Bukkit.getOnlinePlayers()) {
			loadPlayer(p, plugin.getStorage().loadPlayer(p));
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		loadPlayer(e.getPlayer(), plugin.getStorage().loadPlayer(e.getPlayer()));
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		savePlayer(e.getPlayer());
		removePlayer(e.getPlayer());
	}

	@EventHandler
	public void onKicked(PlayerKickEvent e) {
		if (e.isCancelled()) {
			return;
		}
		savePlayer(e.getPlayer());
		removePlayer(e.getPlayer());
	}

}
