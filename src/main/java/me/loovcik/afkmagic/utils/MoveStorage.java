package me.loovcik.afkmagic.utils;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Klasa pomocnicza, przechowująca informacje o ruchu gracza.
 * Dane te są używane do wykrywania zmian w pozycji gracza, a także
 * do tego, czy ruch jest naturalny. Zazwyczaj, gdy gracz porusza się naturalnie,
 * poza zmianą pozycji, zmienia także kąt patrzenia. Gdy zmieniana jest tylko pozycja,
 * można uznać, że gracz korzysta z mechanizmu do omijania AFK.
 */
public class MoveStorage
{
	/**
	 * Lokalizacja początkowa
	 */
	public Location from;

	/**
	 * Lokalizacja docelowa
	 */
	public Location to;

	/**
	 * Określa, czy zmieniony został kąt patrzenia
	 */
	public boolean isLooking;

	/**
	 * Określa, czy wykryto ruch gracza
	 */
	public boolean isMoving;

	public MoveStorage(PlayerMoveEvent e){
		from = e.getFrom();
		to = e.getTo();
		assert to != null;
		isLooking = to.getPitch() != from.getPitch() || to.getYaw() != from.getYaw();
		isMoving = to.getX() != from.getX() || to.getY() != from.getY() || to.getZ() != from.getZ();
	}
}