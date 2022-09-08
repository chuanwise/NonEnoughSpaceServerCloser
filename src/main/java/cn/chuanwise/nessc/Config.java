package cn.chuanwise.nessc;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class Config {
    
    private static Config instance;
    
    private static final String VERSION_PATH = "version";
    private static final String ERROR_MESSAGE_PATH = "message.error";
    private static final String KICK_MESSAGE_PATH = "message.kick";
    private static final String ENABLE_PATH = "enable";
    
    public static Config getInstance() {
        
        if (Objects.isNull(instance)) {
            reload();
        }
        
        return instance;
    }
    
    public static void reload() {
        final File file = getFile();
        if (file.isFile()) {
        
            final FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            final String version = configuration.getString(VERSION_PATH);
        
            if (Objects.isNull(version)) {
                throw new NullPointerException("version code is null!");
            } else if (version.equals("1.0")) {
                instance = loadV1Config(configuration);
            } else {
                throw new IllegalArgumentException("illegal version code: " + version);
            }
        } else {
            instance = new Config();
        }
    }
    
    private static Config loadV1Config(FileConfiguration configuration) {
        final Config config = new Config();
        config.enable = configuration.getBoolean(ENABLE_PATH);
        config.errorMessage = configuration.getString(ERROR_MESSAGE_PATH);
        config.kickMessage = configuration.getString(KICK_MESSAGE_PATH);
        return config;
    }
    
    private static File getFile() {
        // create data folder
        final File dataFolder = NESSC.getInstance().getDataFolder();
        if (!dataFolder.isDirectory() && !dataFolder.mkdirs()) {
            throw new IllegalStateException("can not create data folder: " + dataFolder.getAbsolutePath());
        }
    
        // create file
        return new File(dataFolder, "config.yml");
    }
    
    private boolean enable = true;
    
    private String errorMessage;
    
    private String kickMessage = "[NESSC] 服务器外存不足，请立刻联系管理员！";
    
    public boolean isEnable() {
        return enable;
    }
    
    public void setEnable(boolean enable) {
        this.enable = enable;
    }
    
    public void save() throws IOException {
        // create file
        final File file = getFile();
        if (!file.isFile() && !file.createNewFile()) {
            throw new IllegalStateException("can not create config file!");
        }
    
        final FileConfiguration configuration = new YamlConfiguration();
        configuration.set(VERSION_PATH, "1.0");
        configuration.set(ENABLE_PATH, enable);
        configuration.set(ERROR_MESSAGE_PATH, errorMessage);
        configuration.set(KICK_MESSAGE_PATH, kickMessage);
        configuration.save(file);
    }
    
    public String getErrorMessage() {
        if (Objects.isNull(errorMessage)) {
            final Locale locale = Locale.getDefault();
            if (Objects.equals(locale, Locale.CHINA)) {
                errorMessage = "java.io.IOException: 设备上没有空间";
            } else if (Objects.equals(locale, Locale.US)) {
                errorMessage = "java.io.IOException: No space left on device";
            } else {
                errorMessage = "";
                enable = false;
            }
        }
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getKickMessage() {
        return kickMessage;
    }
    
    public void setKickMessage(String kickMessage) {
        this.kickMessage = kickMessage;
    }
}
