package me.loovcik.afkmagic.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.loovcik.afkmagic.models.AFKPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Extensions
{
	private static final String locationRegEx1 = "X=(\\d+), Y=(\\d+), Z=(\\d+)(, world=(\\S+))?";
	private static final String locationRegEx2 = "(\\d+) (\\d+) (\\d+)( (\\S+))?";

	/**
	 * Sprawdza, czy gracz ma dowolne uprawnienia z podanej listy
	 * @param player Gracz, który zostanie sprawdzony
	 * @param permissions Lista uprawnień do sprawdzenia
	 * @return True, jeśli gracz posiada przynajmniej jedno uprawnienie z listy, w przeciwnym razie false
	 */
	public static boolean hasPlayerPermission(Player player, List<String> permissions)
	{
		for (String permission : permissions)
			if (player.hasPermission(permission)) return true;
		return false;
	}

	/**
	 * Sprawdza, czy gracz ma dowolne uprawnienia z podanej listy
	 * @param player Gracz, który zostanie sprawdzony
	 * @param perms Lista uprawnień do sprawdzenia
	 * @return True, jeśli gracz posiada przynajmniej jedno uprawnienie z listy, w przeciwnym razie false
	 */
	public static boolean hasPlayerPermission(Player player, String... perms){
		return hasPlayerPermission(player, Arrays.stream(perms).toList());
	}

	/**
	 * Sprawdza, czy gracz wywołujący komendę ma dowolne uprawnienia z podanej listy
	 * @param sender Gracz, który zostanie sprawdzony
	 * @param permissions Lista uprawnień do sprawdzenia
	 * @return True, jeśli gracz posiada przynajmniej jedno uprawnienie z listy, w przeciwnym razie false
	 */
	public static boolean hasPlayerPermission(CommandSender sender, List<String> permissions)
	{
		for (String permission : permissions){
			if (sender.hasPermission(permission)) return true;
		}
		return false;
	}

	/**
	 * Sprawdza, czy gracz wywołujący komendę ma dowolne uprawnienia z podanej listy
	 * @param sender Gracz, który zostanie sprawdzony
	 * @param perms Lista uprawnień do sprawdzenia
	 * @return True, jeśli gracz posiada przynajmniej jedno uprawnienie z listy, w przeciwnym razie false
	 */
	public static boolean hasPlayerPermission(CommandSender sender, String... perms){
		return hasPlayerPermission(sender, Arrays.stream(perms).toList());
	}

	/**
	 * Pobiera listę graczy online, którzy posiadają jedno z określonych uprawnień
	 * @param permissions Lista uprawnień do sprawdzenia
	 * @return Lista graczy, którzy posiadają jedną z podanych permisji
	 */
	public static List<Player> getPlayersWithPermission(List<String> permissions){
		List<Player> result = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()){
			if (hasPlayerPermission(player, permissions))
				result.add(player);
		}
		return result;
	}

	/**
	 * Konwertuje lokalizację na tekst w formacie<br>
	 * <code>X=x, Y=y, Z=z, world=world</code>
	 * @param loc Lokalizacja do przekonwertowania
	 * @return Tekstowy odpowiednik lokalizacji
	 */
	public static String locationToString(Location loc){
		return "X="+Math.floor(loc.getX())+", Y="+Math.floor(loc.getY())+", Z="+Math.floor(loc.getZ())+", world="+loc.getWorld().getName();
	}

	/**
	 * Konwertuje tekst na lokalizację
	 * @param loc Lokalizacja w formacie<br>
	 *            <code>X=x, Y=y, Z=z, world=world</code><br>
	 *            lub<br>
	 *            <code>X Y Z world</code>
	 * @return Lokalizacja
	 */
	public static Location toLocation(String loc){
		Pattern pattern = Pattern.compile(locationRegEx1, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(loc);
		if (!matcher.find())
		{
			pattern = Pattern.compile(locationRegEx2, Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(loc);
		}


		if (matcher.find()){
			MatchResult result = matcher.toMatchResult();
			int x = Integer.getInteger(result.group(0));
			int y = Integer.getInteger(result.group(1));
			int z = Integer.getInteger(result.group(2));
			String world = "world_normal";
			if (result.groupCount() > 3)
				world = result.group(5);
			return new Location(Bukkit.getWorld(world), x, y, z);
		}
		else {
			return null;
		}
	}

	/**
	 * Konwertuje listę tekstów do jednego ciągu.
	 * Kolejne teksty są oddzielane od siebie przecinkiem.
	 * @param list Lista tekstów do złączenia
	 * @return Ciąg odpowiadający liście tekstów
	 */
	public static String listToString(List<String> list){
		StringBuilder result = new StringBuilder();
		int i = 1;
		for (String text : list){
			result.append(text);
			if (list.size() > i)
				result.append(", ");
			i++;
		}
		return result.toString();
	}

	/**
	 * Sprawdza, czy gracz jest w jednym z podanych światów
	 * @param player Gracz, którego należy sprawdzić
	 * @param worlds Lista światów
	 * @return True, jeśli gracz znajduje się w jednym z podanych światów
	 */
	public static boolean isOnWorld(AFKPlayer player, List<String> worlds){
		for (String world : worlds)
			if (isOnWorld(player, world)) return true;
		return false;
	}

	/**
	 * Sprawdza, czy gracz znajduje się w podanym świecie
	 * @param player Gracz, którego należy sprawdzić
	 * @param world Nazwa świata
	 * @return True, jeśli gracz znajduje się w podanym świecie
	 */
	public static boolean isOnWorld(AFKPlayer player, String world){
		return player.getPlayer().getWorld().getName().equalsIgnoreCase(world);
	}

	/**
	 * Sprawdza, czy podany tekst zawiera przynajmniej jeden element
	 * z podanych wzorców
	 * @param text Tekst do sprawdzenia
	 * @param patterns Wzorce, które powinny znajdować się w tekście
	 * @return True, jeśli przynajmniej jeden wzorzec znajduje się w tekście
	 */
	public static boolean containsAny(String text, String[] patterns){
		for (String pattern : patterns){
			if (text.contains(pattern)) return true;
		}
		return false;
	}

	/**
	 * Sprawdza, czy podany tekst zawiera przynajmniej jeden element
	 * z podanych wzorców
	 * @param text Tekst do sprawdzenia
	 * @param patterns Wzorce, które powinny znajdować się w tekście
	 * @return True, jeśli przynajmniej jeden wzorzec znajduje się w tekście
	 */
	public static boolean containsAny(String text, List<String> patterns){
		return containsAny(text, patterns.toArray(new String[] { }));
	}

}