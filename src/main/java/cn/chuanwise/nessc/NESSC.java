package cn.chuanwise.nessc;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Logger;

public class NESSC
    extends JavaPlugin {
    
    private static NESSC INSTANCE;
    
    private PrintStream systemErr;
    private PrintStream systemOut;
    private CloserOutputStream pluginErr;
    private CloserOutputStream pluginOut;
    
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
    
        final Config config = Config.getInstance();
        final Logger logger = getLogger();
    
        if (config.isEnable()) {
            logger.info("NonEnoughSpaceServerCloser enabled!");
        } else {
            logger.warning("NonEnoughSpaceServerCloser not enabled! use \"/nessc\" to enable it!");
        }
    }
    
    @Override
    public void onEnable() {
        getCommand("nessc").setExecutor((commandSender, command, s, strings) -> {
            final Config config = Config.getInstance();

            if (strings.length == 0) {
                final boolean nowEnabled = !config.isEnable();
                config.setEnable(nowEnabled);
    
                try {
                    config.save();
                } catch (IOException e) {
                    commandSender.sendMessage(ChatColor.RED + "[NESSC] internal error occurred when saving config file, see details on console");
                    e.printStackTrace();
                }
                
                if (nowEnabled) {
                    commandSender.sendMessage("NonEnoughSpaceServerCloser enabled!");
                } else {
                    commandSender.sendMessage("NonEnoughSpaceServerCloser disabled!");
                }
            } else if (strings.length == 1) {
                switch (strings[0]) {
                    case "reload":
                        Config.reload();
                        pluginOut.flush();
                        pluginErr.flush();
                        commandSender.sendMessage("[NESSC] config reloaded!");
                        break;
                    case "version":
                        commandSender.sendMessage("[NESSC] plugin info\n" +
                            "complete name: NonEnoughSpaceServerCloser\n" +
                            "alias: NESSC\n" +
                            "version: 1.0\n" +
                            "author: Chuanwise\n" +
                            "github: https://github.com/Chuanwise/NonEnoughSpaceServerCloser");
                        break;
                    case "debug":
                        if (Config.getInstance().isEnable()) {
                            commandSender.sendMessage(ChatColor.RED + "[NESSC] plugin will throw an exception, which will cause the server shutdown!!!");
                        } else {
                            commandSender.sendMessage(ChatColor.YELLOW + "[NESSC] plugin will throw an exception, which won't cause the server shutdown if plugin enabled!");
                        }
                        
                        final Locale locale = Locale.getDefault();
                        if (Objects.equals(locale, Locale.CHINA)) {
                            new IOException("设备上没有空间").printStackTrace();
                        } else if (Objects.equals(locale, Locale.US)) {
                            new IOException("No space left on device").printStackTrace();
                        } else {
                            pluginErr.checkErrLog(Config.getInstance().getErrorMessage());
                        }
                        break;
                    default:
                        printlnCommandFormats(commandSender);
                }
            } else {
                printlnCommandFormats(commandSender);
            }
            
            return true;
        });
    
        systemErr = System.err;
        systemOut = System.out;
        
        pluginErr = new CloserOutputStream(systemErr);
        pluginOut = new CloserOutputStream(systemErr);

        System.setErr(new PrintStream(pluginErr));
        System.setOut(new PrintStream(pluginOut));
    }
    
    public CloserOutputStream getPluginErr() {
        return pluginErr;
    }
    
    public CloserOutputStream getPluginOut() {
        return pluginOut;
    }
    
    public PrintStream getSystemErr() {
        return systemErr;
    }
    
    public PrintStream getSystemOut() {
        return systemOut;
    }
    
    @Override
    public void onDisable() {
        getLogger().info("NonEnoughSpaceServerCloser disabled!");
        
        System.setOut(systemOut);
        System.setErr(systemErr);
    }
    
    private void printlnCommandFormats(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "[NESSC] illegal command syntax, legal format: \n" +
            "/nessc - enable or disable plugin function\n" +
            "/nessc reload - reload plugin config\n" +
            "/nessc version - display plugin info");
    }
}
