package me.loovcik.afkmagic.commands.subCommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.core.ChatHelper;
import me.loovcik.core.commands.SimpleSubCommand;

public class ReloadSubCommand extends SimpleSubCommand
{
	private final AFKMagic plugin;

	@Override
	public boolean execute(@NotNull CommandSender sender, String[] args) {
		ChatHelper.message(sender, plugin.configuration.messages.misc.reloading);
		plugin.configuration.loadConfig();
		ChatHelper.message(sender, plugin.configuration.messages.misc.reloaded);
		return true;
	}

	public ReloadSubCommand(AFKMagic plugin){
		super(plugin, plugin.configuration.commands.reload.label, plugin.configuration.commands.reload.aliases, plugin.configuration.commands.reload.permission);
		this.plugin = plugin;
	}
}