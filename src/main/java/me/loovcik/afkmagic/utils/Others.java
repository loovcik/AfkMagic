package me.loovcik.afkmagic.utils;

import org.bukkit.Sound;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.core.ChatHelper;

public final class Others
{
	/**
	 * Odtwarza wybrany dźwięk dla określonego gracza
	 * @param soundName Nazwa dźwięku
	 * @param player Gracz, który ma usłyszeć dźwięk
	 */
	public static void playSound(String soundName, AFKPlayer player){
		try
		{
			int volume = 10;
			int pitch = 1;
			player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.valueOf(soundName), volume, pitch);
		}
		catch (Exception e){
			ChatHelper.console("<red>Unknown sound '"+soundName+"'");
		}
	}

	private Others() {}
}