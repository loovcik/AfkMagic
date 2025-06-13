package me.loovcik.afkmagic.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.commands.subCommands.BypassSubCommand;
import me.loovcik.afkmagic.commands.subCommands.ListSubCommand;
import me.loovcik.afkmagic.commands.subCommands.ReloadSubCommand;
import me.loovcik.afkmagic.commands.subCommands.StatusSubCommand;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.core.ChatHelper;
import me.loovcik.core.commands.SimpleCommand;

public class AFKCommand extends SimpleCommand
{
	private final AFKMagic plugin;

	@Override
	public boolean execute(@NotNull CommandSender sender, String[] args) {
		if (!super.execute(sender, args)){
			if (sender instanceof Player player){
				if (args == null || args.length == 0)
				{
					AFKPlayer.get(player.getUniqueId()).toggleAFK();
				}
				else {
					if (sender.hasPermission(plugin.configuration.commands.afk.permissions.other)) {
						Player other = Bukkit.getPlayer(args[0]);
						if (other == null) ChatHelper.message(sender, plugin.configuration.messages.misc.noPlayer);
						else {
							AFKPlayer.get(other.getUniqueId()).toggleAFK();;
						}
					}
					else ChatHelper.message(sender, plugin.configuration.messages.misc.noPermission);
				}
				return true;
			}
			else
			{
				ChatHelper.message(sender, plugin.configuration.messages.misc.playerOnlyCommand);
				return false;
			}
		}
		return true;
	}

	public AFKCommand(AFKMagic plugin) {
		super(plugin, plugin.configuration.commands.afk.label, plugin.configuration.commands.afk.aliases.toArray(new String[0]), "Zaawansowana obsługa funkcji AFK", plugin.configuration.commands.afk.permissions.command);
		this.plugin = plugin;

		registerSubCommand(new ListSubCommand(plugin));
		registerSubCommand(new StatusSubCommand(plugin));
		registerSubCommand(new ReloadSubCommand(plugin));
		registerSubCommand(new BypassSubCommand(plugin));
	}
}