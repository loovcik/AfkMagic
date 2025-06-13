/*
package org.loovcik.afkmagic.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.loovcik.afkmagic.AFKMagic;

import java.lang.reflect.Field;
import java.util.*;

import static org.loovcik.afkmagic.utils.Extensions.hasPlayerPermission;

public abstract class AFKCommandBase extends BukkitCommand
{
	private final TabCompleter tabCompleter;
	protected AFKMagic plugin;

	protected AFKCommandBase(String name, String desc, List<String> aliases)
	{
		super(name);
		plugin = AFKMagic.getInstance();
		setDescription(desc);
		setAliases(aliases);
		tabCompleter = new AFKTabCompleter(plugin);
		registerCommand();
	}

	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args){
		onCommand(sender, commandLabel, args);
		return true;
	}

	protected void onCommand(CommandSender sender, String commandLabel, String[] args){
		onCommand(sender, args);
	}
	protected abstract void onCommand(CommandSender sender, String[] args);

	private void registerCommand() {
		try {
			final Field serverCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			serverCommandMap.setAccessible(true);
			CommandMap commandMap = (CommandMap) serverCommandMap.get(Bukkit.getServer());
			commandMap.register(getName(), this);
			if (Bukkit.getPluginCommand(getName()) != null) {
				PluginCommand command = Bukkit.getPluginCommand(getName());
				if (!command.getPlugin().equals(plugin)) {
					command.setExecutor(new AFKCommandExecutor());
				}
				if (tabCompleter != null)
					command.setTabCompleter(tabCompleter);
			}
		}
		catch (IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	private class AFKCommandExecutor implements CommandExecutor
	{

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
		{
			return execute(sender, label, args);
		}
	}

	public class AFKTabCompleter implements TabCompleter {
		private final AFKMagic plugin;

		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
			List<String> results = new ArrayList<>();
			if (args.length > 0) {
				StringUtil.copyPartialMatches(args[args.length - 1], switch (args.length)
						{
							case 1 ->
							{
								List<String> tabs = new ArrayList<>();
								if (hasPlayerPermission(sender, plugin.permissions.admin, plugin.permissions.all, plugin.permissions.room, plugin.permissions.roomList, plugin.permissions.bypass, plugin.permissions.list, plugin.permissions.reload, plugin.permissions.roomRemove, plugin.permissions.roomSet, plugin.permissions.roomTeleport, plugin.permissions.status, plugin.permissions.roomList, plugin.permissions.roomRemove, plugin.permissions.time, plugin.permissions.timeAfk, plugin.permissions.timeGame))
									tabs.add("admin");
								if (hasPlayerPermission(sender, plugin.permissions.all, plugin.permissions.others))
									tabs.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
								yield tabs;
							}
							case 2 ->
							{
								List<String> tabs = new ArrayList<>();
								if (args[0].equalsIgnoreCase("admin"))
								{
									if (hasPlayerPermission(sender, plugin.permissions.status, plugin.permissions.all, plugin.permissions.admin))
										tabs.add("status");
									if (hasPlayerPermission(sender, plugin.permissions.time, plugin.permissions.timeAfk, plugin.permissions.timeGame, plugin.permissions.all, plugin.permissions.admin))
										tabs.add("time");
									if (hasPlayerPermission(sender, plugin.permissions.room, plugin.permissions.roomSet, plugin.permissions.roomRemove, plugin.permissions.roomList, plugin.permissions.roomTeleport, plugin.permissions.all, plugin.permissions.admin))
										tabs.add("room");
									if (hasPlayerPermission(sender, plugin.permissions.reload, plugin.permissions.all, plugin.permissions.admin))
										tabs.add("reload");
									if (hasPlayerPermission(sender, plugin.permissions.bypass, plugin.permissions.all, plugin.permissions.admin))
										tabs.add("bypass");

									Collections.sort(tabs);
								}
								yield tabs;
							}
							case 3 -> {
								List<String> tabs = new ArrayList<>();
								if (args[0].equalsIgnoreCase("admin") && (args[1].equalsIgnoreCase("status") || args[1].equalsIgnoreCase("bypass"))){
									boolean can = false;
									if (args[1].equalsIgnoreCase("status") && hasPlayerPermission(sender, plugin.permissions.status, plugin.permissions.admin, plugin.permissions.all))
										can = true;
									if (args[1].equalsIgnoreCase("bypass") && hasPlayerPermission(sender, plugin.permissions.bypass, plugin.permissions.admin, plugin.permissions.all))
										can = true;
									if (can){
										tabs.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
									}
								}
								else if (args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("room")){
									if (hasPlayerPermission(sender, plugin.permissions.roomSet, plugin.permissions.room, plugin.permissions.admin, plugin.permissions.all))
										tabs.add("set");
									if (hasPlayerPermission(sender, plugin.permissions.roomRemove, plugin.permissions.room, plugin.permissions.admin, plugin.permissions.all))
										tabs.add("remove");
									if (hasPlayerPermission(sender, plugin.permissions.roomList, plugin.permissions.room, plugin.permissions.admin, plugin.permissions.all))
										tabs.add("list");
									if (hasPlayerPermission(sender, plugin.permissions.roomTeleport, plugin.permissions.room, plugin.permissions.admin, plugin.permissions.all))
										tabs.add("teleport");
								}
								else if (args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("time")) {
									if (hasPlayerPermission(sender, plugin.permissions.timeAfk, plugin.permissions.time, plugin.permissions.admin, plugin.permissions.all))
										tabs.add("afk");
									if (hasPlayerPermission(sender, plugin.permissions.timeGame, plugin.permissions.time, plugin.permissions.admin, plugin.permissions.all))
										tabs.add("game");
								}
								Collections.sort(tabs);
								yield tabs;
							}
							case 4 -> {
								List<String> tabs = new ArrayList<>();
								if (args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase ("time") && (args[2].equalsIgnoreCase("afk") || args[2].equalsIgnoreCase("game"))){
									boolean can = false;
									if (args[2].equalsIgnoreCase("afk") && hasPlayerPermission(sender, plugin.permissions.timeAfk, plugin.permissions.time, plugin.permissions.admin, plugin.permissions.all))
										can = true;
									else if (args[2].equalsIgnoreCase("game") && hasPlayerPermission(sender, plugin.permissions.timeAfk, plugin.permissions.time, plugin.permissions.admin, plugin.permissions.all))
										can = true;
									if (can)
										tabs.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
								}
								Collections.sort(tabs);
								yield tabs;
							}
							case 5 -> {
								List<String> tabs = new ArrayList<>();
								if (args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase ("time") && (args[2].equalsIgnoreCase("afk") || args[2].equalsIgnoreCase("game"))){
									tabs.add("<time>");
								}
								Collections.sort(tabs);
								yield tabs;
							}
							default -> new ArrayList<String>();
						}, results);
			}


			Collections.sort(results);
			return results;
		}

		public AFKTabCompleter(AFKMagic plugin){
			super();
			this.plugin = plugin;
		}
	}
}*/