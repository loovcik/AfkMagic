package me.loovcik.afkmagic.commands.subCommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.dependencies.VaultAPI;
import me.loovcik.core.ChatHelper;
import me.loovcik.core.commands.SimpleSubCommand;

import java.util.ArrayList;
import java.util.List;

public class BypassSubCommand extends SimpleSubCommand
{
	private final AFKMagic plugin;

	@Override
	public boolean execute(@NotNull CommandSender sender, String[] args){
		if (args == null || args.length < 2) {
			ChatHelper.message(sender, plugin.configuration.messages.misc.unknownCommand);
			return true;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
		if (!target.hasPlayedBefore() || target.getName() == null) {
			ChatHelper.message(sender, plugin.configuration.messages.misc.noPlayer);
			return true;
		}

		// Bypass AFK
		if (args[0].equalsIgnoreCase(plugin.configuration.commands.bypass.afk.label)){
			if (sender.hasPermission(plugin.configuration.commands.bypass.afk.permission)){
				if (plugin.configuration.afk.bypass.permissions.isEmpty()) return true;
				if (VaultAPI.getPermission().playerHas(null, target, plugin.configuration.afk.bypass.permissions.getFirst())) {
					if (VaultAPI.getPermission().playerRemove(null, target, plugin.configuration.afk.bypass.permissions.getFirst()))
						ChatHelper.message(sender, plugin.configuration.messages.bypass.afk.disabled.replaceAll("%player%", target.getName()));
					else ChatHelper.message(sender, "<red>Unable to change that for this player");
				}
				else {
					if (VaultAPI.getPermission().playerAdd(null, target, plugin.configuration.afk.bypass.permissions.getFirst()))
						ChatHelper.message(sender, plugin.configuration.messages.bypass.afk.enabled.replaceAll("%player%", target.getName()));
					else ChatHelper.message(sender, "<red>Unable to change that for this player");
				}
			}
			else ChatHelper.message(sender, plugin.configuration.messages.misc.noPermission);
		}
		// Bypass alts checking
		else if (args[0].equalsIgnoreCase(plugin.configuration.commands.bypass.alts.label)){
			if (sender.hasPermission(plugin.configuration.commands.bypass.alts.permission)){
				if (plugin.configuration.alts.bypass.permissions.isEmpty()) return true;
				if (VaultAPI.getPermission().playerHas(null, target, plugin.configuration.alts.bypass.permissions.getFirst())) {
					if (VaultAPI.getPermission().playerRemove(null, target, plugin.configuration.alts.bypass.permissions.getFirst()))
						ChatHelper.message(sender, plugin.configuration.messages.bypass.alts.disabled.replaceAll("%player%", target.getName()));
					else ChatHelper.message(sender, "<red>Unable to change that for this player");
				}
				else {
					if (VaultAPI.getPermission().playerAdd(null, target, plugin.configuration.alts.bypass.permissions.getFirst()))
						ChatHelper.message(sender, plugin.configuration.messages.bypass.alts.enabled.replaceAll("%player%", target.getName()));
					else ChatHelper.message(sender, "<red>Unable to change that for this player");
				}
			}
			else ChatHelper.message(sender, plugin.configuration.messages.misc.noPermission);
		}
		// Bypass kicking
		else if (args[0].equalsIgnoreCase(plugin.configuration.commands.bypass.kick.label)){
			if (sender.hasPermission(plugin.configuration.commands.bypass.kick.permission)){
				if (plugin.configuration.kick.bypass.permissions.isEmpty()) return true;
				if (VaultAPI.getPermission().playerHas(null, target, plugin.configuration.kick.bypass.permissions.getFirst())) {
					if (VaultAPI.getPermission().playerRemove(null, target, plugin.configuration.kick.bypass.permissions.getFirst()))
						ChatHelper.message(sender, plugin.configuration.messages.bypass.kick.disabled.replaceAll("%player%", target.getName()));
					else ChatHelper.message(sender, "<red>Unable to change that for this player");
				}
				else {
					if (VaultAPI.getPermission().playerAdd(null, target, plugin.configuration.kick.bypass.permissions.getFirst()))
						ChatHelper.message(sender, plugin.configuration.messages.bypass.kick.enabled.replaceAll("%player%", target.getName()));
					else ChatHelper.message(sender, "<red>Unable to change that for this player");
				}
			}
			else ChatHelper.message(sender, plugin.configuration.messages.misc.noPermission);
		}
		// Bypass autoClicker
		else if (args[0].equalsIgnoreCase(plugin.configuration.commands.bypass.autoClicker.label)){
			if (sender.hasPermission(plugin.configuration.commands.bypass.autoClicker.permission)){
				if (plugin.configuration.afk.end.detectors.interact.autoClicker.bypass.permissions.isEmpty()) return true;
				if (VaultAPI.getPermission().playerHas(null, target, plugin.configuration.afk.end.detectors.interact.autoClicker.bypass.permissions.getFirst())) {
					if (VaultAPI.getPermission().playerRemove(null, target, plugin.configuration.afk.end.detectors.interact.autoClicker.bypass.permissions.getFirst()))
						ChatHelper.message(sender, plugin.configuration.messages.bypass.autoClicker.disabled.replaceAll("%player%", target.getName()));
					else ChatHelper.message(sender, "<red>Unable to change that for this player");
				}
				else {
					if (VaultAPI.getPermission().playerAdd(null, target, plugin.configuration.afk.end.detectors.interact.autoClicker.bypass.permissions.getFirst()))
						ChatHelper.message(sender, plugin.configuration.messages.bypass.autoClicker.enabled.replaceAll("%player%", target.getName()));
					else ChatHelper.message(sender, "<red>Unable to change that for this player");
				}
			}
			else ChatHelper.message(sender, plugin.configuration.messages.misc.noPermission);
		}
		// Bypass kick autoClicker
		else if (args[0].equalsIgnoreCase(plugin.configuration.commands.bypass.kickAutoClicker.label)){
			if (sender.hasPermission(plugin.configuration.commands.bypass.kickAutoClicker.permission)){
				if (plugin.configuration.afk.end.detectors.interact.autoClicker.kick.bypass.permissions.isEmpty()) return true;
				if (VaultAPI.getPermission().playerHas(null, target, plugin.configuration.afk.end.detectors.interact.autoClicker.kick.bypass.permissions.getFirst())) {
					if (VaultAPI.getPermission().playerRemove(null, target, plugin.configuration.afk.end.detectors.interact.autoClicker.kick.bypass.permissions.getFirst()))
						ChatHelper.message(sender, plugin.configuration.messages.bypass.kickAutoClicker.disabled.replaceAll("%player%", target.getName()));
					else ChatHelper.message(sender, "<red>Unable to change that for this player");
				}
				else {
					if (VaultAPI.getPermission().playerAdd(null, target, plugin.configuration.afk.end.detectors.interact.autoClicker.kick.bypass.permissions.getFirst()))
						ChatHelper.message(sender, plugin.configuration.messages.bypass.kickAutoClicker.enabled.replaceAll("%player%", target.getName()));
					else ChatHelper.message(sender, "<red>Unable to change that for this player");
				}
			}
			else ChatHelper.message(sender, plugin.configuration.messages.misc.noPermission);
		}
		// Bypass safe room teleport
		else if (args[0].equalsIgnoreCase(plugin.configuration.commands.bypass.room.label)){
			if (sender.hasPermission(plugin.configuration.commands.bypass.room.permission)){
				if (plugin.configuration.room.bypass.permissions.isEmpty()) return true;
				if (VaultAPI.getPermission().playerHas(null, target, plugin.configuration.room.bypass.permissions.getFirst())) {
					if (VaultAPI.getPermission().playerRemove(null, target, plugin.configuration.room.bypass.permissions.getFirst()))
						ChatHelper.message(sender, plugin.configuration.messages.bypass.room.disabled.replaceAll("%player%", target.getName()));
					else ChatHelper.message(sender, "<red>Unable to change that for this player");
				}
				else {
					if (VaultAPI.getPermission().playerAdd(null, target, plugin.configuration.room.bypass.permissions.getFirst()))
						ChatHelper.message(sender, plugin.configuration.messages.bypass.room.enabled.replaceAll("%player%", target.getName()));
					else ChatHelper.message(sender, "<red>Unable to change that for this player");
				}
			}
			else ChatHelper.message(sender, plugin.configuration.messages.misc.noPermission);
		}
		else ChatHelper.message(sender, plugin.configuration.messages.misc.unknownCommand);
		return true;
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, String[] args) {
		List<String> result = new ArrayList<>();
		if (args.length == 1) {
			result.add(plugin.configuration.commands.bypass.afk.label);
			result.add(plugin.configuration.commands.bypass.alts.label);
			result.add(plugin.configuration.commands.bypass.kick.label);
			result.add(plugin.configuration.commands.bypass.room.label);
			result.add(plugin.configuration.commands.bypass.autoClicker.label);
			result.add(plugin.configuration.commands.bypass.kickAutoClicker.label);
		}
		else {
			for (Player player : Bukkit.getOnlinePlayers()) {
				result.add(player.getName());
			}
		}

		return result;
	}

	public BypassSubCommand(AFKMagic plugin){
		super(plugin, plugin.configuration.commands.bypass.label, plugin.configuration.commands.bypass.aliases, plugin.configuration.commands.bypass.permission);
		this.plugin = plugin;
	}
}