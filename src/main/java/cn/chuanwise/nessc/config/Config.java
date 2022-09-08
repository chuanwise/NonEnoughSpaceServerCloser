package cn.chuanwise.nessc.config;

import cn.chuanwise.nessc.NESSC;
import cn.chuanwise.nessc.util.Files;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;

public class Config {
    
    private static final String VERSION_PATH = "version";
    private static final String VERSION = "2";
    
    @Deprecated
    private static final String KICK_MESSAGE_PATH = "kick-message";
    @Deprecated
    private static final String DEFAULT_KICK_MESSAGE = "§7[§4§lNESSC§7] §c服务器外存不足，请立刻联系管理员以修复此问题！";
    @Deprecated
    private static String kickMessage = DEFAULT_KICK_MESSAGE;
    
    private static final String ENABLE_PATH = "enable";
    private static final boolean DEFAULT_ENABLE = true;
    private static boolean enable = DEFAULT_ENABLE;
    
    private static final String MIN_USABLE_BYTES_PATH = "min-usable-bytes";
    private static final int DEFAULT_MIN_USABLE_BYTES = 1024;
    private static long minUsableBytes = DEFAULT_MIN_USABLE_BYTES;
    
    private static final String CHECK_INTERVAL_PATH = "check-interval";
    private static final long DEFAULT_CHECK_INTERVAL = 20;
    private static long checkInterval = DEFAULT_CHECK_INTERVAL;
    
    /**
     * 载入配置信息
     *
     * @throws IOException 载入过程中出现的异常
     */
    public static void load() throws IOException {
        // prepare file
        final File configFile = new File(Files.getExistedDataDirectory(), "config.yml");
        
        if (configFile.isFile()) {
            final FileConfiguration configuration = YamlConfiguration.loadConfiguration(new FileReader(configFile));
    
            final String version = configuration.getString(VERSION_PATH);
            switch (version) {
                case "1.0":
                    loadV1Config(configuration);
                    break;
                case "2":
                    loadV2Config(configuration);
                    break;
                default:
                    throw new NoSuchElementException("can not load config file with version: " + version);
            }
        } else {
            save();
        }
    }
    
    /**
     * 保存配置信息
     *
     * @throws IOException 保存过程中出现的异常
     */
    public static void save() throws IOException {
        final FileConfiguration configuration = new YamlConfiguration();
        saveConfig(configuration);
    
        final File file = new File(Files.getExistedDataDirectory(), "config.yml");
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(configuration.saveToString());
        }
    }
    
    public static String getKickMessage() {
        return kickMessage;
    }
    
    public static void setKickMessage(String kickMessage) {
        Config.kickMessage = kickMessage;
    }
    
    public static long getMinUsableBytes() {
        return minUsableBytes;
    }
    
    public static void setMinUsableBytes(long minUsableBytes) {
        Config.minUsableBytes = minUsableBytes;
    }
    
    public static boolean isEnable() {
        return enable;
    }
    
    public static void setEnable(boolean enable) {
        Config.enable = enable;
    }
    
    public static long getCheckInterval() {
        return checkInterval;
    }
    
    public static void setCheckInterval(long checkInterval) {
        Config.checkInterval = checkInterval;
    }
    
    @SuppressWarnings("all")
    private static void loadV1Config(FileConfiguration configuration) {
        loadV2Config(configuration);
        
        // read kick message and add to sentence
        final String kickMessage = configuration.getString(KICK_MESSAGE_PATH, DEFAULT_KICK_MESSAGE);
        Messages.sentences.put("kick-message", Messages.Sentence.of(kickMessage));
    }
    
    private static void loadV2Config(FileConfiguration configuration) {
        enable = configuration.getBoolean(ENABLE_PATH, DEFAULT_ENABLE);
        minUsableBytes = configuration.getInt(MIN_USABLE_BYTES_PATH, DEFAULT_MIN_USABLE_BYTES);
        checkInterval = configuration.getLong(CHECK_INTERVAL_PATH, DEFAULT_CHECK_INTERVAL);
    
        if (minUsableBytes <= 0) {
            NESSC.getInstance().getLogger().warning("illegal " + MIN_USABLE_BYTES_PATH + ": " + minUsableBytes + ", " +
                "use default '" + DEFAULT_MIN_USABLE_BYTES + "'");
            minUsableBytes = DEFAULT_MIN_USABLE_BYTES;
        }
        if (checkInterval < 10) {
            NESSC.getInstance().getLogger().warning("too small " + CHECK_INTERVAL_PATH + ": " + checkInterval + ", " +
                "use default '" + DEFAULT_CHECK_INTERVAL + "'");
            checkInterval = DEFAULT_CHECK_INTERVAL;
        }
    }
    
    private static void saveConfig(FileConfiguration configuration) {
        configuration.set(VERSION_PATH, VERSION);
        configuration.set(ENABLE_PATH, enable);
        configuration.set(KICK_MESSAGE_PATH, kickMessage);
        configuration.set(CHECK_INTERVAL_PATH, checkInterval);
        configuration.set(MIN_USABLE_BYTES_PATH, minUsableBytes);
    }
}
