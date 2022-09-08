package cn.chuanwise.nessc;

import cn.chuanwise.nessc.util.Files;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

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
    
            if (!commandSender.hasPermission("nessc.reverse")) {
                commandSender.sendMessage("§7[§4§lNESSC§7] §c你，莫得权限 §7- §fnessc.reverse");
                return true;
            }
            
            // enable of disable NESSC
            final boolean setTo = !Config.isEnable();
            Config.setEnable(setTo);
            try {
                Config.save();
            } catch (IOException e) {
                commandSender.sendMessage("§7[§4§lNESSC§7] §e无法保存插件配置文件！");
                e.printStackTrace();
                return true;
            }
    
            if (setTo) {
                commandSender.sendMessage("§7[§2§lNESSC§7] §a插件功能已启动，将§l会§a在外存耗尽时关闭服务器。");
            } else {
                commandSender.sendMessage("§7[§6§lNESSC§7] §e插件功能已关闭，将§l不会§e在外存耗尽时关闭服务器。");
            }
            return true;
        }
        
        if (strings.length == 1) {
            switch (strings[0]) {
                case "reload":
                    if (!commandSender.hasPermission("nessc.reload")) {
                        commandSender.sendMessage("§7[§4§lNESSC§7] §c你，莫得权限 §7- §fnessc.reload");
                        return true;
                    }
                    
                    try {
                        Config.load();
                        commandSender.sendMessage("§7[§2§lNESSC§7] §a插件配置已重载");
                    } catch (IOException e) {
                        commandSender.sendMessage("§7[§4§lNESSC§7] §c重载配置时出现异常，详见控制台");
                        e.printStackTrace();
                    }
                    return true;
                case "info":
                    commandSender.sendMessage("§7[§3§lNESSC§7] §b插件信息\n" +
                        "§7-> §b全名§7：§fNonEnoughSpaceServerCloser\n" +
                        "§7-> §b缩写§7：§fNESSC\n" +
                        "§7-> §b版本§7：§f1.0\n" +
                        "§7-> §b作者§7：§fChuanwise\n" +
                        "§7-> §bQ 群§7：§f1028959718\n" +
                        "§7-> §b仓库§7：§fhttps://github.com/Chuanwise/NonEnoughSpaceServerCloser");
                    return true;
                case "test":
                    if (!commandSender.hasPermission("nessc.test")) {
                        commandSender.sendMessage("§7[§4§lNESSC§7] §c你，莫得权限 §7- §fnessc.test");
                        return true;
                    }
    
                    if (Config.isEnable()) {
                        commandSender.sendMessage("§7[§4§lNESSC§7] §c进行本次测试将导致服务器被关闭！");
        
                        final File file;
                        try {
                            file = Files.getExistedFile(new File(Files.getExistedDataDirectory(), ".test-big-file"), "test file");
                        } catch (IOException e) {
                            commandSender.sendMessage("§7[§4§lNESSC§7] §c无法创建测试文件 test，测试被停止");
                            e.printStackTrace();
                            return true;
                        }
    
                        commandSender.sendMessage("§7[§3§lNESSC§7] §b正在保存服务器数据以免丢失");

                        // save players
                        NESSC.getInstance().getServer().savePlayers();
                        
                        // save worlds
                        for (World world : NESSC.getInstance().getServer().getWorlds()) {
                            world.save();
                        }
        
                        NESSC.getInstance().getServer().getScheduler().runTaskAsynchronously(NESSC.getInstance(), () -> {
                            try (OutputStream outputStream = new FileOutputStream(file)) {
    
                                commandSender.sendMessage("§7[§6§lNESSC§7] §e正在耗尽外存资源。测试结束后，插件将删除产生的大文件");
    
                                byte[] bytes = new byte[(int) Config.getMinUsableBytes()];
                                while (!NESSC.getInstance().getDetector().getClosing().get()) {
                                    try {
                                        outputStream.write(bytes);
                                    } catch (IOException ignored) {
                                    }
                                }
                            } catch (IOException exception) {
                                commandSender.sendMessage("§7[§4§lNESSC§7] §c启动测试失败");
                                exception.printStackTrace();
                            } finally {
                                file.delete();
                            }
                        });
                    } else {
                        commandSender.sendMessage("§7[§4§lNESSC§7] §c插件功能尚未开启，请使用 /nessc 启动后再测试");
                    }
                    return true;
            }
        }
        
        displayUsage(commandSender);
        
        return true;
    }
    
    private void displayUsage(CommandSender commandSender) {
        commandSender.sendMessage("§7[§3§lNESSC§7] §b用法\n" +
            "§7-> /§bnessc §7- §f启动或关闭插件功能\n" +
            "§7-> /§bnessc reload §7- §f重载插件配置\n" +
            "§7-> /§bnessc info §7- §f显示插件信息\n" +
            "§7-> /§bnessc test throw §7- §f抛出异常以测试功能\n" +
            "§7-> /§bnessc test write §7- §f耗尽外存空间以测试功能\n" +
            "§7-> /§bnessc §c[others] §7- §f你已经发现它了 :D");
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
