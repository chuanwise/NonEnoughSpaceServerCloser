package cn.chuanwise.nessc;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class CloserOutputStream
    extends OutputStream {
    
    private final List<Byte> byteList = new ArrayList<>();
    
    private final OutputStream outputStream;
    
    private Pattern pattern;
    
    public CloserOutputStream(OutputStream outputStream) {
        Objects.requireNonNull(outputStream, "output stream is null!");
        
        this.outputStream = outputStream;
    }
    
    @Override
    public void write(int b) throws IOException {
        if (b == '\n') {
            final byte[] byteArray = new byte[byteList.size()];
            for (int i = 0; i < byteList.size(); i++) {
                byteArray[i] = byteList.get(i);
            }
            byteList.clear();
            
            final String string = new String(byteArray);
    
            // append to System.err
            outputStream.write(byteArray);
            outputStream.write(b);
    
            checkErrLog(string);
        } else if (b != -1) {
            byteList.add((byte) b);
        } else {
            outputStream.write(b);
        }
    }
    
    public void checkErrLog(String log) {
    
        final Config config = Config.getInstance();
        final String broadcastMessage = config.getBroadcastMessage();
        
        if (!config.isEnable()) {
            return;
        }
        if (Objects.isNull(pattern)) {
            flush();
        }
        
        if (pattern.matcher(log.trim()).matches()) {
            final Server server = NESSC.getInstance().getServer();
            for (Player onlinePlayer : server.getOnlinePlayers()) {
                onlinePlayer.kickPlayer(broadcastMessage);
            }
            
            NESSC.getInstance().getLogger().info("non enough spaces, shutdown server!");
            server.shutdown();
        }
    }
    
    public void flush() {
        pattern = Pattern.compile("(\\[.+]\\s?)*\\s*:?\\s*" + Pattern.quote(Config.getInstance().getErrorMessage()));
    }
}
