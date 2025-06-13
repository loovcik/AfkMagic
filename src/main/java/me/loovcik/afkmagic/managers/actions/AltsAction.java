package me.loovcik.afkmagic.managers.actions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.core.ChatHelper;

import java.util.*;

import static me.loovcik.afkmagic.managers.ConfigurationManager.getRandom;
import static me.loovcik.afkmagic.utils.Extensions.getPlayersWithPermission;
import static me.loovcik.afkmagic.utils.Extensions.listToString;

/**
 * Akcje do wykonania podczas sprawdzania multikont
 */
public class AltsAction extends Action
{
	/**
	 * Sprawdza, czy opcja sprawdzania multikont
	 * jest włączona oraz czy gracz posiada multikonta
	 * wykorzystywane do AFK
	 * @param player Kontekst gracza
	 */
	@Override
	public boolean check(AFKPlayer player)
	{
		if (!plugin.configuration.alts.use) return false;
		if (plugin.altsManager.isBypassed(player)) return false;
		boolean hasAlts = plugin.altsManager.hasAlts(player);
		return player.isAFK() && hasAlts;
	}

	/**
	 * Akcja do wykonania, jeśli gracz posiada
	 * multikonta wykorzystywane do AFK
	 * @param player Kontekst gracza
	 */
	@Override
	public void success(AFKPlayer player)
	{
		if (!player.isAltsDetected()){
			player.setAltsDetected(true);
			player.setAltsDetectedCount(player.getAltsDetectedCount()+1);
		}
		long detectTime = System.currentTimeMillis() - player.getAltsDetectTime();
		if (plugin.configuration.alts.use && player.getAltsDetectTime() > 0)
		{
			if (player.getCurrentAfkTime() >= plugin.configuration.alts.warning.time.toMilliseconds() && detectTime > plugin.configuration.alts.warning.time.toMilliseconds())
				successWarn(player);
			if (plugin.configuration.alts.kick.use && player.getCurrentAfkTime() >= plugin.configuration.alts.kick.time.toMilliseconds() && detectTime > plugin.configuration.alts.kick.time.toMilliseconds())
				successKick(player);
			if (plugin.configuration.alts.ban.use && player.getCurrentAfkTime() >= plugin.configuration.alts.ban.time.toMilliseconds() && detectTime > plugin.configuration.alts.kick.time.toMilliseconds())
				successBan(player);
		}
	}

	private void successWarn(AFKPlayer player){
		for (AFKPlayer p : plugin.altsManager.getAll(player)){
			if (plugin.altsManager.isFirst(p))
			{
				String rMessage = getRandom(plugin.configuration.alts.warning.admins.messages);
				if (plugin.configuration.alts.warning.admins.use && !p.isAltsWarned() && !rMessage.equalsIgnoreCase(""))
				{
					List<Player> receivers = getPlayersWithPermission(plugin.configuration.alts.warning.admins.permissions);
					Map<String, String> replacements = new HashMap<>();
					replacements.put("%player%", plugin.altsManager.getAll(player).get(0).getName());
					replacements.put("%alts%", listToString(plugin.altsManager.getAll(player).stream().map(AFKPlayer::getName).filter(name -> !name.equals(plugin.altsManager.getAll(player).getFirst().getName())).toList()));
					receivers.forEach((x) -> x.sendMessage(ChatHelper.replace(rMessage, replacements)));
				}
			}
			if (plugin.altsManager.isFirst(p))
			{
				String rMessage = getRandom(plugin.configuration.alts.warning.self.messages);
				if (plugin.configuration.alts.warning.self.use && !p.isAltsWarned() && !rMessage.equalsIgnoreCase(""))
				{
					List<AFKPlayer> alts = plugin.altsManager.getAll(player);
					alts.removeIf((x) -> x.getUniqueId() == p.getUniqueId());
					List<String> names = new ArrayList<>();
					for (AFKPlayer a : alts)
						names.add(a.getName());
					Map<String, String> replacements = new HashMap<>();
					replacements.put("%player%", p.getName());
					replacements.put("%alts%", listToString(names));
					ChatHelper.message(p.getPlayer(), rMessage, replacements);

				}
			}
			else {
				String rMessage = getRandom(plugin.configuration.alts.warning.alts.messages);
				if(plugin.configuration.alts.warning.alts.use && !p.isAltsWarned() && !rMessage.equalsIgnoreCase(""))
				{

					List<String> afkUsers = plugin.altsManager.getAll(player).stream().filter(AFKPlayer::isAFK).map(AFKPlayer::getName).toList();
					Map<String, String> replacements = new HashMap<>();
					replacements.put("%player%", p.getName());
					replacements.put("%alts%", listToString(afkUsers));
					ChatHelper.message(p.getPlayer(), rMessage, replacements);
				}
			}

			p.setAltsWarned(true);
		}
	}

	private void successKick(AFKPlayer player){
		List<AFKPlayer> alts = plugin.altsManager.getAll(player);
		alts.forEach(p -> {
			if (!p.isAltsKicked())
			{
				p.setAltsKicked(true);
				if (plugin.configuration.alts.kick.limit > 1 && p.getAltsKickedCount() > plugin.configuration.alts.kick.limit){
					successBan(player);
					return;
				}
				p.setKickCount(p.getKickCount()+1);
				p.setAltsKickedCount(p.getAltsDetectedCount()+1);

				Map<String, String> replacements = new HashMap<>();
				replacements.put("%player%", p.getName());
				replacements.put("%reason%", plugin.configuration.alts.kick.reason);
				plugin.commandManager.run(plugin.configuration.alts.kick.commands, replacements);
				if (plugin.configuration.alts.kick.useDefault)
				{
					if (Bukkit.getPlayer(p.getUniqueId()) != null)
					{
						plugin.commandManager.run("lkick %player% %reason%", replacements);
					}
				}
			}
		});
	}

	private void successBan(AFKPlayer player){
		List<AFKPlayer> alts = plugin.altsManager.getAll(player);
		alts.forEach(p -> {
			if (!p.isAltsBaned())
			{
				p.setAltsBaned(true);
				Map<String, String> replacements = new HashMap<>();
				replacements.put("%player%", p.getName());
				replacements.put("%reason%", plugin.configuration.alts.ban.reason);
				plugin.commandManager.run(plugin.configuration.alts.ban.commands, replacements);
				if (plugin.configuration.alts.ban.useDefault)
				{
					if (Bukkit.getPlayer(p.getUniqueId()) != null)
					{
						plugin.commandManager.run("ban %player% %reason%", replacements);
					}
				}
			}
		});
	}

	public AltsAction(AFKMagic plugin)
	{
		super(plugin);
	}
}