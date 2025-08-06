package me.loovcik.afkmagic.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.core.TinyText;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ExtraPlaceholders  extends PlaceholderExpansion {
	private final AFKMagic plugin;

	public ExtraPlaceholders(AFKMagic plugin){
		this.plugin = plugin;
	}

	/**
	 * Pobiera identyfikator placeholdera
	 * @return Identyfikator placeholdera
	 */
	@Override
	public @NotNull String getIdentifier()
	{
		return "redstone";
	}

	/**
	 * Pobiera autora
	 * @return Nazwa autora
	 */
	@Override
	public @NotNull String getAuthor()
	{
		return String.join(", ", plugin.getPluginMeta().getAuthors());
	}

	/**
	 * Pobiera wersję pluginu
	 * @return Wersja pluginu
	 */
	@Override
	public @NotNull String getVersion()
	{
		return plugin.getPluginMeta().getVersion();
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
		if (!parts.isEmpty()){
			result = switch (parts.get(0)){
				case "tiny":
					List<String> other = new ArrayList<>(parts);
					other.removeFirst();
					String placeholder = String.join("_", other);
					String placeholderValue = plugin.dependencies.placeholderAPI.process(player, "%"+placeholder+"%");
					yield  TinyText.parse(placeholderValue);
				default:
					yield "";
			};
		}
		return result;
	}
}