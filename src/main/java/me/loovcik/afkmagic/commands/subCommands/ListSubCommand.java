package me.loovcik.afkmagic.commands.subCommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.core.ChatHelper;
import me.loovcik.core.commands.SimpleSubCommand;
import me.loovcik.core.types.Time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListSubCommand extends SimpleSubCommand
{
	private final AFKMagic plugin;

	@Override
	public boolean execute(@NotNull CommandSender sender, String[] args) {
		List<AFKPlayer> afkPlayers = AFKPlayer.getAFKPlayers().stream().filter(x -> !x.isVanished()).toList();
		if (afkPlayers.isEmpty()) {
			ChatHelper.message(sender, plugin.configuration.messages.list.empty);
			return true;
		}

		List<String> afkList = new ArrayList<>();
		Map<String, String> replacements = new HashMap<>();
		replacements.put("%count%", String.valueOf(afkPlayers.size()));
		ChatHelper.message(false, sender, "------------------------------------------");
		ChatHelper.message(false, sender, plugin.configuration.messages.list.header, replacements);
		replacements.clear();
		for (AFKPlayer player : afkPlayers) {
			replacements.put("%player%", player.getName());
			replacements.put("%since%", Time.ofMillis(player.getCurrentAfkTime()).format(false));
			afkList.add(ChatHelper.replace(plugin.configuration.messages.list.element, replacements));
		}

		String result = String.join(", ", afkList);
		ChatHelper.message(false, sender, result);
		ChatHelper.message(false, sender, "------------------------------------------");
		return true;
	}


	public ListSubCommand(AFKMagic plugin) {
		super(plugin, plugin.configuration.commands.list.label, plugin.configuration.commands.list.aliases, plugin.configuration.commands.list.permission);
		this.plugin = plugin;
	}
}