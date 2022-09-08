package cn.chuanwise.nessc.config;

import cn.chuanwise.nessc.NESSC;
import cn.chuanwise.nessc.util.Files;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Messages {
    
    private static final String VERSION_PATH = "version";
    private static final String VERSION = "1.0";
    
    private static final String INFO_HEAD = "§7[§3§bNESSC§7] §b";
    private static final String WARN_HEAD = "§7[§6§bNESSC§7] §e";
    private static final String ERROR_HEAD = "§7[§4§bNESSC§7] §c";
    private static final String SUCCESS_HEAD = "§7[§2§bNESSC§7] §a";
    
    public static class Sentence {
        
        private interface Element {
            
            String format(Object... arguments);
        }
        
        private static class TextElement
            implements Element {
            
            private static final TextElement EMPTY = new TextElement("");
            
            public static TextElement of(String string) {
                if (string.isEmpty()) {
                    return EMPTY;
                } else {
                    return new TextElement(string);
                }
            }
    
            private final String string;
    
            private TextElement(String string) {
                this.string = string;
            }
            
            @Override
            public String format(Object... arguments) {
                return string;
            }
        }
        
        private static class ArgumentElement
            implements Element {
            
            private static final List<ArgumentElement> INSTANCES = new ArrayList<>();
            
            private final int index;
            
            public static ArgumentElement of(int index) {
                if (index <= 0) {
                    throw new IllegalArgumentException("index must be bigger than 0, but got " + index + "!");
                }
                while (index >= INSTANCES.size()) {
                    INSTANCES.add(new ArgumentElement(INSTANCES.size()));
                }
                return INSTANCES.get(index - 1);
            }
    
            private ArgumentElement(int index) {
                this.index = index;
            }
            
            @Override
            public String format(Object... arguments) {
                return Objects.toString(arguments[index]);
            }
        }
        
        private final List<Element> elements;
        private final String format;
    
        private Sentence(String format, List<Element> elements) {
            this.format = format;
            this.elements = elements;
        }
        
        public static Sentence of(String format) {
            
            final StringBuilder stringBuilder = new StringBuilder(format.length());
            
            final int length = format.length();
            final List<Element> elements = new ArrayList<>();
    
            final int defaultState = 0;
            final int afterLeftBrace = 1;
    
            int index = 0;
            int state = defaultState;
    
            for (int i = 0; i < length; i++) {
                final char ch = format.charAt(i);
        
                switch (state) {
                    case defaultState:
                        if (ch == '{') {
                            if (stringBuilder.length() > 0) {
                                final String string = stringBuilder.toString();
                                stringBuilder.setLength(0);
                                
                                elements.add(TextElement.of(string));
                            }
                            state = afterLeftBrace;
                        } else {
                            stringBuilder.append(ch);
                        }
                        break;
                    case afterLeftBrace:
                        if (ch == '}') {
                            elements.add(ArgumentElement.of(index));
                            index = 0;
                            state = defaultState;
                        } else if (Character.isDigit(ch)) {
                            index *= 10;
                            index += ch - '0';
                        } else {
                            throw new IllegalStateException("illegal index character: " + ch + " in format '" + format + "'");
                        }
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
    
            switch (state) {
                case defaultState:
                    if (stringBuilder.length() > 0) {
                        elements.add(TextElement.of(stringBuilder.toString()));
                    }
                    break;
                case afterLeftBrace:
                    throw new IllegalArgumentException("unmatched { in format '" + format + "'");
                default:
                    throw new IllegalStateException();
            }
            
            return new Sentence(format, elements);
        }
        
        public String format(Object... arguments) {
            final StringBuilder stringBuilder = new StringBuilder(format.length());
            for (Element element : elements) {
                stringBuilder.append(element.format(arguments));
            }
            return stringBuilder.toString();
        }
    }
    
    protected static Map<String, Sentence> sentences;
    
    @SuppressWarnings("all")
    public static void load() throws IOException {
        // prepare file
        final File configFile = new File(Files.getExistedDataDirectory(), "messages.yml");
    
        if (configFile.isFile()) {
            final FileConfiguration configuration = YamlConfiguration.loadConfiguration(new FileReader(configFile));
        
            final String version = configuration.getString(VERSION_PATH, VERSION);
            if (Objects.equals(version, "1.0")) {
                loadV1Config(configuration);
            } else {
                throw new NoSuchElementException("can not load config file with version: " + version);
            }
        } else {
            configFile.createNewFile();
            
            final String messagePath;
            
            // load default
            if (Locale.getDefault().equals(Locale.CHINA)) {
                messagePath = "messages/zh_CN.yml";
            } else {
                messagePath = "messages/en_US.yml";
            }
            
            try (InputStream inputStream = NESSC.getInstance().getResource(messagePath);
                 OutputStream outputStream = new FileOutputStream(configFile)) {
        
                final int available = inputStream.available();
                final byte[] bytes = new byte[available];
        
                if (inputStream.read(bytes) != available) {
                    throw new IOException("can not read " + available + "B");
                }
        
                final String string = new String(bytes, StandardCharsets.UTF_8);
                outputStream.write(string.getBytes(Charset.defaultCharset()));
            }
            
            // then load it
            load();
        }
    }
    
    private static void loadV1Config(FileConfiguration configuration) {
    
        sentences = new HashMap<>();
        final Set<String> keys = configuration.getKeys(false);
    
        for (String key : keys) {
            final String string = configuration.getString(key);
            sentences.put(key, Sentence.of(string));
        }
    }
    
    public static void save() throws IOException {
    
        final FileConfiguration configuration = new YamlConfiguration();
        saveConfig(configuration);
        
        final File file = new File(Files.getExistedDataDirectory(), "messages.yml");
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(configuration.saveToString());
        }
    }
    
    private static void saveConfig(FileConfiguration configuration) {
        configuration.set(VERSION_PATH, VERSION);
        
        for (Map.Entry<String, Sentence> entry : sentences.entrySet()) {
            configuration.set(entry.getKey(), entry.getValue().format);
        }
    }
    
    public static String format(String key, Object... arguments) {
        Objects.requireNonNull(key, "key is null!");
        Objects.requireNonNull(arguments, "arguments is null!");
    
        final Sentence sentence = sentences.get(key);
        if (Objects.isNull(sentence)) {
            if (arguments.length == 0) {
                return WARN_HEAD + "${" + key + "}";
            } else {
                return WARN_HEAD + "${" + key + "}: " + Arrays.toString(arguments);
            }
        } else {
            return sentence.format(arguments);
        }
    }
}
