package cn.chuanwise.nessc;

import cn.chuanwise.nessc.config.Config;
import cn.chuanwise.nessc.config.Messages;
import cn.chuanwise.nessc.util.Files;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler
    implements CommandExecutor, TabCompleter {
    
    private static final List<String> FIRST_COMPLETE = Arrays.asList("reload", "info", "test");
    
    @Override
    @SuppressWarnings("all")
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        
        // remove empty strings
        strings = Arrays.stream(strings)
            .filter(Objects::nonNull)
            .filter(x -> !x.isEmpty())
            .toArray(String[]::new);
        
        if (strings.length == 0) {
    
            final String reversePermission = "nessc.reverse";
            if (!commandSender.hasPermission(reversePermission)) {
                commandSender.sendMessage(Messages.format("lack-permission", reversePermission));
                return true;
            }
            
            // enable of disable NESSC
            final boolean setTo = !Config.isEnable();
            Config.setEnable(setTo);
            try {
                Config.save();
            } catch (IOException e) {
                commandSender.sendMessage(Messages.format("save-config-fail"));
                e.printStackTrace();
                return true;
            }
    
            if (setTo) {
                commandSender.sendMessage(Messages.format("function-enabled"));
            } else {
                commandSender.sendMessage(Messages.format("function-disabled"));
            }
            return true;
        }
        
        if (strings.length == 1) {
            switch (strings[0]) {
                case "reload":
                    final String reloadPermission = "nessc.reload";
                    if (!commandSender.hasPermission(reloadPermission)) {
                        commandSender.sendMessage(Messages.format("lack-permission", reloadPermission));
                        return true;
                    }
                    
                    try {
                        Messages.load();
                        Config.load();
                        
                        commandSender.sendMessage(Messages.format("config-reload-successfully"));
                    } catch (IOException e) {
                        commandSender.sendMessage(Messages.format("config-reload-fail", e));
                        e.printStackTrace();
                    }
                    return true;
                case "info":
                    commandSender.sendMessage(Messages.format("plugin-info", "1.1"));
                    return true;
                case "test":
                    final String testPermission = "nessc.test";
                    if (!commandSender.hasPermission(testPermission)) {
                        commandSender.sendMessage(Messages.format("lack-permission", testPermission));
                        return true;
                    }
    
                    if (Config.isEnable()) {
                        commandSender.sendMessage(Messages.format("testing-will-cause-shutdown"));
        
                        final File file;
                        try {
                            file = Files.getExistedFile(new File(Files.getExistedDataDirectory(), ".test-big-file"), "test file");
                        } catch (IOException e) {
                            commandSender.sendMessage(Messages.format("can-not-create-test-file"));
                            e.printStackTrace();
                            return true;
                        }
    
                        commandSender.sendMessage(Messages.format("saving-data-before-testing"));

                        // save players
                        NESSC.getInstance().getServer().savePlayers();
                        
                        // save worlds
                        for (World world : NESSC.getInstance().getServer().getWorlds()) {
                            world.save();
                        }
        
                        NESSC.getInstance().getServer().getScheduler().runTaskAsynchronously(NESSC.getInstance(), () -> {
                            try (OutputStream outputStream = new FileOutputStream(file)) {
    
                                commandSender.sendMessage(Messages.format("testing"));
    
                                byte[] bytes = new byte[(int) Config.getMinUsableBytes()];
                                while (!NESSC.getInstance().getDetector().getClosing().get()) {
                                    try {
                                        outputStream.write(bytes);
                                    } catch (IOException ignored) {
                                    }
                                }
                            } catch (IOException exception) {
                                commandSender.sendMessage(Messages.format("fail-to-start-test"));
                                exception.printStackTrace();
                            } finally {
                                file.delete();
                            }
                        });
                    } else {
                        commandSender.sendMessage(Messages.format("function-has-not-enable-yet"));
                    }
                    return true;
            }
        }
        
        displayUsage(commandSender);
        
        return true;
    }
    
    private void displayUsage(CommandSender commandSender) {
        commandSender.sendMessage(Messages.format("plugin-usage"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) {
            return FIRST_COMPLETE;
        }
        
        // remove empty string
        final List<String> arguments = Arrays.stream(strings)
            .filter(Objects::nonNull)
            .filter(x -> !x.isEmpty())
            .collect(Collectors.toList());
        
        if (arguments.isEmpty()) {
            return FIRST_COMPLETE;
        }
        
        final String finalArgument = strings[strings.length - 1];
        final boolean finalComplete = Objects.isNull(finalArgument) || finalArgument.isEmpty();
        
        if (arguments.size() == 1) {
            if (finalComplete) {
                return Collections.emptyList();
            }
    
            final String argument = arguments.get(0);
            return FIRST_COMPLETE.stream()
                .filter(x -> x.startsWith(argument))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
