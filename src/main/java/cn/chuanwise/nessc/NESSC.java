package cn.chuanwise.nessc;

import cn.chuanwise.nessc.config.Config;
import cn.chuanwise.nessc.config.Messages;
import cn.chuanwise.nessc.task.Detector;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Logger;

public class NESSC
    extends JavaPlugin {
    
    private static NESSC INSTANCE;
    
    private Detector detector;
    private int taskCode;
    
    public static NESSC getInstance() {
        if (Objects.isNull(INSTANCE)) {
            throw new NoSuchElementException("plugin hasn't load!");
        }
        return INSTANCE;
    }
    
    @Override
    public void onLoad() {
        // set instance
        INSTANCE = this;
        
        final ConsoleCommandSender consoleSender = getServer().getConsoleSender();
    
        // load config
        try {
            Messages.load();
            Config.load();
    
            consoleSender.sendMessage(Messages.format("config-loaded"));
        } catch (IOException e) {
            consoleSender.sendMessage(Messages.format("fail-to-load-config", e));
            e.printStackTrace();
        }
        
        consoleSender.sendMessage(Messages.format("logo-ascii"));
        consoleSender.sendMessage(Messages.format("plugin-enabled"));
    }
    
    @Override
    public void onEnable() {
    
        // bstats
//        new Metrics(this, 16377);
        
        detector = new Detector();
        taskCode = getServer().getScheduler().scheduleSyncRepeatingTask(this, detector, 0, Config.getCheckInterval());
        
        // register commands
        final PluginCommand command = getCommand("nessc");
        final CommandHandler commandHandler = new CommandHandler();
        
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);
    }
    
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTask(taskCode);
    
        final ConsoleCommandSender consoleSender = getServer().getConsoleSender();
        consoleSender.sendMessage(Messages.format("plugin-disabled"));
    }
    
    public Detector getDetector() {
        return detector;
    }
}
