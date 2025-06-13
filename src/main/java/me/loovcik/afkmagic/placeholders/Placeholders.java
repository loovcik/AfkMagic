package me.loovcik.afkmagic.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.core.types.Time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Zapewnia obsługę PlaceholderAPI w pluginie
 */
public class Placeholders extends PlaceholderExpansion
{
	private final AFKMagic plugin;

	public Placeholders(AFKMagic plugin){
		this.plugin = plugin;
	}

	/**
	 * Pobiera identyfikator placeholdera
	 * @return Identyfikator placeholdera
	 */
	@Override
	public @NotNull String getIdentifier()
	{
		return "afkmagic";
	}

	/**
	 * Pobiera autora
	 * @return Nazwa autora
	 */
	@Override
	public @NotNull String getAuthor()
	{
		return String.join(", ", plugin.getDescription().getAuthors());
	}

	/**
	 * Pobiera wersję pluginu
	 * @return Wersja pluginu
	 */
	@Override
	public @NotNull String getVersion()
	{
		return plugin.getDescription().getVersion();
	}

	@Override
	public boolean persist(){
		return true;
	}

	/**
	 * Obsługuje żądania dotyczące placeholdera
	 * @param player Kontekst gracza
	 * @param params Parametry
	 * @return Zawartość placeholdera
	 */
	public String onRequest(OfflinePlayer player, @NotNull String params){
		List<String> parts = List.of(params.split("_"));
		String result = "N/A";
		if (player == null) return result;
		if (!parts.isEmpty()){
			AFKPlayer afkPlayer = AFKPlayer.get(player.getUniqueId());
			result = switch (parts.get(0)){
				case "status" -> {
					if (afkPlayer.isAFK()) yield "Tak";
					else yield "Nie";

				}
				case "time" -> {
					if (afkPlayer == null) yield result;
					switch (parts.get(1)){
						case "afk" -> {
							if (parts.size() > 2 && parts.get(2).equalsIgnoreCase("text"))
								yield Time.ofMillis(afkPlayer.getTotalAfkTime()).format(true);
							else
								yield afkPlayer.getTotalAfkTime().toString();
						}
						case "idle" -> {
							if (parts.size() > 2 && parts.get(2).equalsIgnoreCase("text"))
								yield Time.ofMillis(afkPlayer.getIdleTime()).format(true);
							else
								yield afkPlayer.getIdleTime().toString();
						}
						case "current" -> {
							if (parts.size() > 2 && parts.get(2).equalsIgnoreCase("text"))
								yield Time.ofMillis(afkPlayer.getCurrentAfkTime()).format(true);
							else
								yield afkPlayer.getCurrentAfkTime().toString();
						}
						case "game" -> {
							if (parts.size() > 2 && parts.get(2).equalsIgnoreCase("text"))
								yield Time.ofSeconds(afkPlayer.getGameTime()).format(true);
							else
								yield afkPlayer.getGameTime().toString();
						}
						case "start" -> {
							if (parts.size() > 2 && parts.get(2).equalsIgnoreCase("text"))
							{
								if (afkPlayer.getStartAfkDate() == 0) yield "---";
								yield new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(afkPlayer.getStartAfkDate()));
							}
							else {
								if (afkPlayer.getStartAfkDate() == 0) yield "-1";
								yield afkPlayer.getStartAfkDate().toString();
							}
						}
						default -> { yield "N/A"; }
					}
				}
				case "inactive" -> {
					if (afkPlayer.isInactive()) yield "Tak";
					else yield "Nie";
				}
				case "count" -> {
					yield Integer.toString(AFKPlayer.getPlayers().size());
				}
				default -> "N/A";
			};
		}
		return result;
	}
}