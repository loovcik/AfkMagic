package me.loovcik.afkmagic.events;

import lombok.Getter;
import me.loovcik.afkmagic.models.AFKPlayer;

/**
 * Zdarzenie wywoływane, gdy gracz
 * zostanie wyrzucony za AFK
 */
@Getter
public class AFKKickEvent extends AFKEvent
{
	/**
	 *  Pobiera informację o komunikacie, który jest powiązany z wyrzuceniem gracza
	 */
	private final String reason;

	public AFKKickEvent(AFKPlayer player, String reason){
		super(player);
		this.reason = reason;
	}
}