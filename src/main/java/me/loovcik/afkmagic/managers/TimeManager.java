package me.loovcik.afkmagic.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.core.types.Time;
import me.loovcik.core.ChatHelper;

import java.util.Objects;

/**
 * Klasa obliczająca czasy AFK
 */
public class TimeManager
{
	private final AFKMagic plugin;
	private Objective objective;

	/**
	 * Zmienia aktualny czas gry gracza
	 * @param player Gracz, którego dotyczy zmiana
	 * @param time Czas, który zostanie ustawiony
	 */
	public void setGameTime(AFKPlayer player, Time time){
		objective.getScore(player.getName()).setScore((int)((long) time.toMilliseconds()));
	}

	/**
	 * Ustawia całkowity czas AFK gracza
	 * @param player Gracz, którego dotyczy zmiana
	 * @param time Czas, który zostanie ustawiony
	 */
	public void setTotalAFKTime(AFKPlayer player, Time time){
		player.setTotalAfkTime(time.toMilliseconds());
		player.save();
	}

	/**
	 * Zadanie obliczające czasy
	 */
	private Runnable run(){
		return () -> {
			for (Player p : Bukkit.getOnlinePlayers()){
				AFKPlayer player = AFKPlayer.get(p.getUniqueId());
				if (objective != null)
					player.setGameTime((long)objective.getScore(player.getName()).getScore() / 20);
				if (player.isAFK()){
					player.setCurrentAfkTime(System.currentTimeMillis() - player.getStartAfkDate());
					player.calculateAFKTime();
					plugin.actions.check(player);
				}
			}
		};
	}

	public TimeManager(AFKMagic plugin)
	{
		this.plugin = plugin;
		ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		Scoreboard scoreboard = Objects.requireNonNull(scoreboardManager).getMainScoreboard();

		Criteria criteria = Criteria.create("minecraft.custom:minecraft.play_time");
		boolean criteriaFound;
		try
		{
			objective = scoreboard.getObjectivesByCriteria(criteria).stream().filter(x -> x.getName().equalsIgnoreCase("rs_play_time_custom")).findFirst().get();
			ChatHelper.console("Using '<green>"+objective.getName()+"</green>' objective to read player game time");
			criteriaFound = true;
		}
		catch (Exception e)	{ criteriaFound = false; }

		if (!criteriaFound)
			try
			{
				objective = scoreboard.getObjectivesByCriteria(criteria).stream().findFirst().get();
				ChatHelper.console("Using '<green>"+objective.getName()+"</green>' objective to read player game time");
				criteriaFound = true;
			}
			catch (Exception e)	{ criteriaFound = false; }

		if (!criteriaFound){
			ChatHelper.console("<red>Unknown objective");
			ChatHelper.console("<red>Playtime objective not found!");
		}

		plugin.tasks.addTask(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, run(), 5, 20));
	}
}