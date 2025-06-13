package me.loovcik.afkmagic.utils;

import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Tasks
{
	/**
	 * Lista aktualnie działających zadań
	 */
	private final List<BukkitTask> tasks = new ArrayList<>();

	/**
	 * Lista zadań do wykonania podczas wyłączania pluginu
	 */
	private final List<Runnable> shutdownTasks = new ArrayList<>();

	/**
	 * Dodaje zadanie do listy aktualnie wykonywanych zadań
	 * @param task Zadanie, które zostanie dodanie
	 */
	public void addTask(BukkitTask task) { tasks.add(task); }

	/**
	 * Usuwa zadanie z listy aktualnie wykonywanych zadań
	 * @param task Zadanie, które zostanie usunięte
	 */
	public void removeTask(BukkitTask task)
	{
		task.cancel();
		tasks.remove(task);
	}

	/**
	 * Dodaje zadanie, które zostanie wykonane podczas wyłączania pluginu
	 * @param task Zadanie, które zostanie dodane
	 */
	public void addShutdownTask(Runnable task) { shutdownTasks.add(task); }

	/**
	 * Zatrzymuje wszystkie aktywne zadania
	 */
	public void stopAllTask()
	{
		tasks.forEach(BukkitTask::cancel);
		shutdownTasks.forEach(Runnable::run);
	}
}