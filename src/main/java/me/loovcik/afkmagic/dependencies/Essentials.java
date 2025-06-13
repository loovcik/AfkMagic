package me.loovcik.afkmagic.dependencies;

import me.loovcik.core.ChatHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import java.util.UUID;

/**
 * Zapewnia opcjonalną obsługę Essentials
 */
public class Essentials
{
	/**
	 * Przechowuje referencje do klasy pośredniczącej pomiędzy
	 * tym pluginem, a Essentials
	 */
	private EssentialsHook hook;

	/**
	 * Określa, czy obsługa Essentials jest włączona i dostępna
	 * @return True, jeśli Essentials jest dostępny, w przeciwnym razie False
	 */
	public boolean isEnabled() { return hook != null; }

	/**
	 * Ustawia stan AFK w pluginie Essentials, jeśli jest dostępny
	 * @param uuid Identyfikator gracza
	 * @param state Określa, czy AFK ma być włączone, czy wyłączone
	 */
	public void setAFK(UUID uuid, boolean state){
		if (isEnabled())
			hook.setAFK(uuid, state);
	}

	/**
	 * Domyślny konstruktor. Sprawdza, czy plugin Essentials jest dostępny
	 */
	public Essentials(){
		if (Bukkit.getPluginManager().getPlugin("Essentials") != null && Bukkit.getPluginManager().isPluginEnabled("Essentials")){
			hook = new EssentialsHook();
			ChatHelper.console("Essentials support: <green>Yes</green> (" + hook.getVersion() + ")");
		}
		else {
			ChatHelper.console("Essentials support: <red>No</red>");
		}
	}
}

/**
 * Klasa pośrednicząca pomiędzy tym pluginem a pluginem Essentials
 */
@SuppressWarnings("UnstableApiUsage")
class EssentialsHook {
	/**
	 * Pobiera wersję pluginu Essentials
	 * @return Wersja Essentials
	 */
	public String getVersion(){
		return Bukkit.getPluginManager().getPlugin("Essentials").getPluginMeta().getVersion();
	}

	/**
	 * Zmiana statusu AFK gracza w pluginie Essentials
	 * @param uuid Identyfikator gracza
	 * @param state Stan AFK
	 */
	public void setAFK(UUID uuid, boolean state){
		net.ess3.api.IEssentials essentials = (net.ess3.api.IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
		net.ess3.api.IUser user = essentials.getUser(uuid);
		if (user == null)
			return;
		user.setAfkMessage("");
		user.setAfk(state, net.ess3.api.events.AfkStatusChangeEvent.Cause.UNKNOWN);
	}
}