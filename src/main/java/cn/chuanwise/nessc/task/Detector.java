package cn.chuanwise.nessc.task;

import cn.chuanwise.nessc.config.Config;
import cn.chuanwise.nessc.NESSC;
import cn.chuanwise.nessc.config.Messages;
import cn.chuanwise.nessc.event.NonEnoughSpaceEvent;
import cn.chuanwise.nessc.util.Files;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Detector
    implements Runnable {
    
    private final File rootDirectory = Files.getExistedDataDirectory()
        .getParentFile()
        .getAbsoluteFile()
        .getParentFile();
    
    private final Server server = NESSC.getInstance().getServer();
    
    private final AtomicBoolean closing = new AtomicBoolean(false);
    
    public AtomicBoolean getClosing() {
        return closing;
    }
    
    @Override
    public void run() {
        if (Config.isEnable()) {
            final long usableSpace = rootDirectory.getUsableSpace();
            final long minUsableBytes = Config.getMinUsableBytes();
        
            if (usableSpace < minUsableBytes) {
            
                // call event
                final NonEnoughSpaceEvent event = new NonEnoughSpaceEvent();
                server.getPluginManager().callEvent(event);
            
                if (!event.isCancelled()) {
                    closing.set(true);
                    
                    server.getConsoleSender().sendMessage(Messages.format("closing-server", usableSpace, minUsableBytes));
                
                    final Collection<? extends Player> onlinePlayers = server.getOnlinePlayers();
                    for (Player onlinePlayer : onlinePlayers) {
                        onlinePlayer.kickPlayer(Messages.format("kick-message"));
                    }
                
                    server.shutdown();
                }
            }
        }
    }
}
