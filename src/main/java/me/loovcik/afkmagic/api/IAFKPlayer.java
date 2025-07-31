package me.loovcik.afkmagic.api;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface IAFKPlayer
{
	/** Pobiera identyfikator gracza */
	UUID getUniqueId();

	/** Pobiera informację, czy gracz jest aktualnie AFK */
	boolean isAFK();

	/** Pobiera nazwę gracza */
	String getName();

	/** Sprawdza, czy gracz jest ukryty */
	boolean isVanished();

	/** Przełącza stan AFK gracza */
	void toggleAFK();

	/** Konwertuje klasę AFKPlayer na klasę Bukkit Player */
	Player getPlayer();

	/** Zwraca całkowity czas AFK gracza, w sekundach */
	Long getTotalAfkTime();
}