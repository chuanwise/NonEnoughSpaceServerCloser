package cn.chuanwise.nessc;

import cn.chuanwise.nessc.task.Detector;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
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
        
        final Logger logger = getLogger();
    
        // load config
        try {
            Config.load();
            logger.info("配置信息已载入");
        } catch (IOException e) {
            logger.severe("无法载入配置信息！");
            e.printStackTrace();
        }
    
        if (Config.isEnable()) {
            logger.info("NonEnoughSpaceServerCloser 已启动！");
        } else {
            logger.warning("NonEnoughSpaceServerCloser 功能尚未启动，可以使用 /nessc 启动！");
        }
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
    
        getLogger().info("NonEnoughSpaceServerCloser 已关闭，期待下次与你重逢！");
    }
    
    public Detector getDetector() {
        return detector;
    }
}
