package me.loovcik.afkmagic;

import lombok.Getter;
import me.loovcik.afkmagic.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import me.loovcik.afkmagic.api.AFKMagicAPI;
import me.loovcik.afkmagic.commands.AFKCommand;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.afkmagic.utils.Tasks;
import me.loovcik.core.ChatHelper;
import me.loovcik.core.commands.SimpleCommand;

/**
 * Głowna klasa pluginu
 */
@SuppressWarnings({"UnstableApiUsage"})
public final class AFKMagic extends JavaPlugin {
    /** Pobiera bieżącą instancję pluginu */
    @Getter
    private static AFKMagic instance;

    /**
     * Przechowuje wszystkie listenery eventów
     */
    public AFKMagicListeners listeners;

    /**
     * Zapewnia obsługę opcjonalnych zależności
     */
    public DependenciesManager dependencies;

    /**
     * Zapewnia obsługę pliku konfiguracyjnego
     */
    public ConfigurationManager configuration;

    /**
     * Zapewnia wygodne wykonywanie komend
     */
    public CommandManager commandManager;

    /**
     * Zapewnia zarządzanie wykrywaniem multikont
     */
    public AltsManager altsManager;

    /**
     * Umożliwia łatwe zarządzanie czasem
     */
    public TimeManager timeManager;

    /**
     * Umożliwia zmianę sposobu spawnowania się mobów
     */
    public SpawnManager spawnManager;

    public ActionsManager actions;

    /**
     * Zapewnia obsługę wątków
     */
    public Tasks tasks;

    /**
     * Ustawia wymagane elementy podczas uruchamiania
     * pluginu
     */
    @Override
    public void onEnable() {
        ChatHelper.setPlugin(this);
        ChatHelper.console("Author: <gold>Loovcik</gold>");
        ChatHelper.console("Version: <gold>"+ getPluginMeta().getVersion()+"</gold>");

        saveDefaultConfig();

        tasks = new Tasks();
        dependencies = new DependenciesManager(this);
        commandManager = new CommandManager(this);

        configuration = new ConfigurationManager(this);
        configuration.load();
        ChatHelper.setPrefix(configuration.global.prefix);

        spawnManager = new SpawnManager(this);
        listeners = new AFKMagicListeners(this);
        timeManager = new TimeManager(this);
        altsManager = new AltsManager(this);
        dependencies.placeholderAPI.register();
        actions = new ActionsManager(this);

        tasks.addShutdownTask(AFKPlayer::unloadAll);

        registerAPI();
        tasks.addTask(Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for(AFKPlayer player : AFKPlayer.getPlayers()){
                if (!player.isAFK()) player.check();
            }
        }, 20, configuration.afk.interval.toTicks()));
        registerCommands();
    }

    /**
     * Zapewnia bezpieczne zakończenie działania
     * pluginu
     */
    @Override
    public void onDisable() {
        tasks.stopAllTask();
        dependencies.placeholderAPI.unregister();
        listeners.getAFKMachineDetectionTask().cancel();
        actions.unregisterAll();
        unregisterCommands();
        getLogger().info("Plugin successfully disabled");
    }

    /**
     * Rejestruje obsługę komend
     */
    private void registerCommands(){
       new AFKCommand(this).register();
        SimpleCommand.scheduleCommandSync(this);
    }

    private void unregisterCommands(){
        SimpleCommand.getCommands().forEach(SimpleCommand::unregister);
        SimpleCommand.scheduleCommandSync(this);
    }

    /**
     * Rejestruje interfejs API
     */
    private void registerAPI(){
        new AFKMagicAPI(this);

    }

    @Override
    public void onLoad() {
        instance = this;
    }
}