package me.loovcik.afkmagic.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import me.loovcik.afkmagic.AFKMagic;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * Klasa pomocnicza, która monitoruje zmiany pozycji graczy
 */
public class MoveMonitor
{
	/**
	 * Lista lokalizacji graczy
	 */
	private final HashMap<UUID, Locations> playerLocationTotals = new HashMap<>();

	/**
	 * Domyślny konstruktor. W momencie tworzenia klasy,
	 * uruchamiany jest cykliczne zadanie, która sprawdza aktualne ruchy graczy
	 */
	public MoveMonitor(){
		AFKMagic.getInstance().tasks.addTask(Bukkit.getScheduler().runTaskTimerAsynchronously(AFKMagic.getInstance(), run(), 5, 5));
	}

	/**
	 * Sprawdza ruch gracza
	 * @param uuid Identyfikator gracza
	 * @param movement Informacje o lokalizacji początkowej i docelowej gracza
	 * @param posTrigger Minimalna zmiana pozycji gracza, która zostanie uznana za ruch gracza
	 * @param lookTrigger Minimalna zmiana kąta patrzenia, która zostanie uznana za ruch gracza
	 */
	public void logAndCheckMovement(UUID uuid, MoveStorage movement, double posTrigger, double lookTrigger){
		logMovement(uuid, movement.to);

		Locations locations = getPlayerMovementTotal(uuid);
		if(movement.isMoving) movement.isMoving = locations.checkPosition(posTrigger);
		if(movement.isLooking) movement.isLooking = locations.checkLook(lookTrigger);
	}

	/**
	 * Odświeża informacje o lokalizacji gracza, bez sprawdzania
	 * @param uuid Identyfikator gracza
	 * @param location Informacje o lokalizacji początkowej i docelowej gracza
	 */
	public void logMovement(UUID uuid, Location location){
		Locations locations = getPlayerMovementTotal(uuid);
		locations.add(location);
	}

	/**
	 * Dane o zmianach lokalizacji gracza
	 * @param uuid Identyfikator gracza
	 * @return Zwraca zapamiętane lokalizacje gracza
	 */
	private Locations getPlayerMovementTotal(UUID uuid){
		if(!playerLocationTotals.containsKey(uuid))
			playerLocationTotals.put(uuid, new MoveMonitor.Locations());
		return playerLocationTotals.get(uuid);
	}

	/**
	 * Zadanie sprawdzające pozycje graczy
	 * @return Uchwyt do zadania
	 */
	private Runnable run(){
		return () -> {
			for (Player p : Bukkit.getOnlinePlayers()){
				Locations locations = getPlayerMovementTotal(p.getUniqueId());
				long allowedTime = 2*50L;
				if (locations.time + allowedTime < new Date().getTime())
					logMovement(p.getUniqueId(), p.getLocation());
			}
		};
	}

	private static class Locations {
		private final int samples = 10;
		private final Location[] locations = new Location[samples];
		private int i = 0;
		protected long time;

		/**
		 * Dodaje lokalizację do listy lokalizacji
		 * @param location Lokalizacja, która zostanie dodana
		 */
		public void add(Location location){
			time = new Date().getTime();
			locations[i] = location;
			i++;
			if (i == samples) i = 0;
		}

		/**
		 * Sprawdza, czy gracz poruszył się wystarczająco, aby
		 * mogło zostać to zaliczone jako jego ruch
		 * @param trigger Wymagany dystans, który musi poruszyć się gracz
		 * @return True, jeśli gracz poruszył się dalej niż wymagana odległość, w przeciwnym razie false
		 */
		public boolean checkPosition(double trigger){
			double total = 0;
			for (int i = 1; i < locations.length; i++){
				Location to = locations[i];
				Location from = locations[i - 1];
				if (to == null || from == null){
					if (total == 0) return false;
					else continue;
				}
				try {
					total += to.distanceSquared(from);
				}
				catch (IllegalArgumentException e) { return true; }
			}
			return Math.sqrt(total) >= trigger;
		}

		/**
		 * Sprawdza, czy gracz zmienił swój kąt patrzenia wystarczająco, aby uznać to za jego ruch
		 * @param trigger Wymagana zmiana kąta patrzenia
		 * @return True, jeśli gracz rozejrzał się wystarczająco, w przeciwnym razie false
		 */
		public boolean checkLook(double trigger){
			double total = 0;
			for (int i = 1; i < locations.length; i++){
				Location to = locations[i];
				Location from = locations[i - 1];
				if (to == null || from == null){
					if (total == 0) return false;
					else continue;
				}
				total += to.getDirection().angle(from.getDirection());
			}
			return total > trigger;
		}
	}
}