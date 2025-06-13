package me.loovcik.afkmagic.commands.subCommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.core.types.Time;
import me.loovcik.core.ChatHelper;
import me.loovcik.core.commands.SimpleSubCommand;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StatusSubCommand extends SimpleSubCommand
{
	private final AFKMagic plugin;

	@Override
	public boolean execute(@NotNull CommandSender sender, String[] args) {
		if (args == null || args.length == 0) {
			if (sender.hasPermission(plugin.configuration.commands.status.permissions.command))
				showPlayerStatus(sender, sender.getName());
			else ChatHelper.message(sender, plugin.configuration.messages.misc.noPermission);
		}
		else {
			if (sender.hasPermission(plugin.configuration.commands.status.permissions.other))
				showPlayerStatus(sender, args[0]);
			else ChatHelper.message(sender, plugin.configuration.messages.misc.noPermission);
		}
		return true;
	}

	private void showPlayerStatus(CommandSender sender, String userName){
		if (userName == null) {
			ChatHelper.message(sender, plugin.configuration.messages.misc.noPlayerSpecified);
			return;
		}
		OfflinePlayer player = Bukkit.getOfflinePlayer(userName);
		if (!player.hasPlayedBefore()) {
			ChatHelper.message(sender, plugin.configuration.messages.misc.noPlayer);
			return;
		}

		AFKPlayer afkPlayer = AFKPlayer.get(player.getUniqueId());
		if (afkPlayer == null || afkPlayer.getLastActivity() == 0) {
			ChatHelper.message(sender, plugin.configuration.messages.status.noInformation);
			return;
		}

		Map<String, String> replacements = new HashMap<>();
		replacements.put("%player%", afkPlayer.getName());
		replacements.put("%status%", afkPlayer.isAFK() ? "<green>"+plugin.configuration.messages.yesWord : "<red>"+plugin.configuration.messages.noWord);
		replacements.put("%totalTime%", Time.ofMillis(afkPlayer.getTotalAfkTime()).format());
		replacements.put("%startTime%", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(afkPlayer.getStartAfkDate())));
		replacements.put("%currentTime%", Time.ofMillis(afkPlayer.getCurrentAfkTime()).format());
		replacements.put("%kick%", String.valueOf(afkPlayer.getKickCount()));
		replacements.put("%altsKicks%", String.valueOf(afkPlayer.getAltsKickedCount()));
		replacements.put("%altsDetections%", String.valueOf(afkPlayer.getAltsDetectedCount()));
		replacements.put("%room%", afkPlayer.isInsideAfkRoom() ? "<green>"+plugin.configuration.messages.yesWord : "<red>"+plugin.configuration.messages.noWord);
		replacements.put("%inactive%", afkPlayer.isInactive() ? "<green>"+plugin.configuration.messages.yesWord : "<red>"+plugin.configuration.messages.noWord);
		if (afkPlayer.getPlayer() != null)
			replacements.put("%activity%", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(afkPlayer.getLastActivity())));
		else replacements.put("%activity%", "<red>Only for online players");
		Date date = new Date(afkPlayer.getAltsDetectTime());
		if (date.equals(new Date(0L)))
			replacements.put("%altsDetectionDate%", "<red>-</red>");
		else
			replacements.put("%altsDetectionDate%", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(date));

		ChatHelper.message(false, sender, "&e=============================================");
		// Show information for all players
		for (String text : plugin.configuration.messages.status.players)
			ChatHelper.message(false, sender, text, replacements);
		// Show AFK information
		if (afkPlayer.isAFK())
			for (String text : plugin.configuration.messages.status.afk)
				ChatHelper.message(false, sender, text, replacements);
		// Show advanced information
		if (sender.hasPermission("afkmagic.status.advanced"))
			for (String text : plugin.configuration.messages.status.advanced)
				ChatHelper.message(false, sender, text, replacements);
		// Show activity information
		if (sender.hasPermission("afkmagic.status.activity"))
			for (String text : plugin.configuration.messages.status.activity)
				ChatHelper.message(false, sender, text, replacements);
		// Show admins information
		if (sender.hasPermission("afkmagic.status.admin"))
			for (String text : plugin.configuration.messages.status.admins)
				ChatHelper.message(false, sender, text, replacements);
		ChatHelper.message(false, sender, "&e=============================================");
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, String[] args) {
		if (sender.hasPermission(plugin.configuration.commands.status.permissions.other))
			return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
		return List.of();
	}

	public StatusSubCommand(AFKMagic plugin){
		super(plugin, plugin.configuration.commands.status.label, plugin.configuration.commands.status.aliases, plugin.configuration.commands.status.permissions.command);
		this.plugin = plugin;
	}
}