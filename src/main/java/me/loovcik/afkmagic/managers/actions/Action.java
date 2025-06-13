package me.loovcik.afkmagic.managers.actions;

import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.models.AFKPlayer;

public abstract class Action
{
	protected final AFKMagic plugin;

	/**
	 * Sprawdza, czy należy wykonać powiązaną akcję
	 * @param player Kontekst gracza
	 */
	public abstract boolean check(AFKPlayer player);

	/**
	 * Akcja do wykonania, jeśli sprawdzanie zwróci wynik pozytywny
	 * @param player Kontekst gracza
	 */
	public abstract void success(AFKPlayer player);

	/**
	 * Akcja do wykonania, jeśli sprawdzanie zwróci wynik negatywny
	 * @param player Kontekst gracza
	 */
	public void failed(AFKPlayer player){ }

	public Action(AFKMagic plugin){
		this.plugin = plugin;
	}
}