package me.loovcik.afkmagic.managers;

import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.managers.actions.*;
import me.loovcik.afkmagic.models.AFKPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Menedżer zarządzający wykonywaniem akcji
 * określonych w pliku konfiguracyjnym, które mają
 * być uruchamiane po określonym czasie AFK
 */
public class ActionsManager
{
	private final AFKMagic plugin;
	private final List<Action> actions = new ArrayList<>();

	/**
	 * Sprawdza, czy nie należy wykonać dowolnej akcji
	 * @param player Gracz, którego dotyczy sprawdzanie
	 */
	public void check(AFKPlayer player){
		assert player != null;
		for (Action action : actions){
			check(action, player);
		}
	}

	/**
	 * Sprawdza, czy nie należy wykonać akcji
	 * @param action Rodzaj akcji, którą należy sprawdzić
	 * @param player Gracz, którego dotyczy sprawdzanie
	 */
	public void check(Action action, AFKPlayer player){
		if (action.check(player))
			action.success(player);
		else action.failed(player);
	}

	/**
	 * Rejestruje akcję
	 * @param action Akcja, która zostanie zarejestrowana
	 */
	public void register(Action action){
		if (actions.contains(action))
			return;
		actions.add(action);
	}

	/**
	 * Wyrejestrowuje daną akcję
	 * @param action Akcja, która zostanie wyrejestrowana
	 */
	public void unregister(Action action){
		if (!actions.contains(action))
			return;
		actions.remove(action);
	}

	/**
	 * Wyrejestrowuje wszystkie akcje
	 */
	public void unregisterAll(){
		actions.clear();
	}

	/**
	 * Rejestruje predefiniowane akcje
	 */
	private void registerInternal(){
		register(new WarnAction(plugin));
		register(new KickAction(plugin));
		register(new RoomAction(plugin));
		register(new AltsAction(plugin));
	}

	public ActionsManager(AFKMagic plugin){
		this.plugin = plugin;
		registerInternal();
	}
}