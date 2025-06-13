package me.loovcik.afkmagic.dependencies;

import me.loovcik.core.ChatHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.placeholders.Placeholders;

public class PlaceholderAPI
{
	private PlaceholderAPIHook hook;

	public boolean isEnabled() { return hook != null; }

	public String process(OfflinePlayer op, String input){
		if (isEnabled()) return hook.process(op, input);
		return input;
	}

	public String process(String input){
		return process(null, input);
	}

	public void register(){
		if (isEnabled()) hook.placeholders.register();
	}

	public void unregister(){
		if (isEnabled()) hook.placeholders.unregister();
	}

	public PlaceholderAPI(AFKMagic plugin){
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			hook = new PlaceholderAPIHook(plugin);
			ChatHelper.console("PlaceholderAPI support: <green>Yes</green> ("+ Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion() + ")");
		}
		else ChatHelper.console("PlaceholderAPI support: <red>No</red>");
	}
}

@SuppressWarnings("UnstableApiUsage")
class PlaceholderAPIHook {
	public final Placeholders placeholders;
	public String getVersion(){
		return Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getPluginMeta().getVersion();
	}



	public String process(OfflinePlayer op, String input){
		return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(op, input);
	}

	public PlaceholderAPIHook(AFKMagic plugin){
		placeholders = new Placeholders(plugin);
	}
}