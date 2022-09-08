package cn.chuanwise.nessc.util;

import cn.chuanwise.nessc.NESSC;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * 文件夹工具类
 *
 * @author Chuanwise
 */
public class Files {
    
    private static File existedLibFolder;
    
    private Files() {
        throw new UnsupportedOperationException("can not call the constructor of an util class: " + Files.class.getName());
    }
    
    /**
     * 获取确保存在的插件数据文件夹
     *
     * @return 插件数据文件夹
     */
    public static File getExistedDataDirectory() {
        return getExistedDirectory(NESSC.getInstance().getDataFolder(), "data");
    }
    
    /**
     * 获取确保存在的插件依赖文件夹
     *
     * @return 插件依赖文件夹
     */
    public static File getExistedLibDirectory() {
        if (Objects.isNull(existedLibFolder)) {
            existedLibFolder = getExistedDirectory(new File(getExistedDataDirectory(), "libs"), "lib");
        }
        return existedLibFolder;
    }
    
    /**
     * 获取确保存在的文件
     *
     * @param file 文件
     * @param fileName 文件名
     * @return 文件
     * @throws IOException 创建文件时出现异常
     * @throws NullPointerException file 为 null
     * @throws NullPointerException fileName 为 null
     */
    public static File getExistedFile(File file, String fileName) throws IOException {
        Objects.requireNonNull(file, "file is null!");
        Objects.requireNonNull(fileName, "file name is null!");
        
        if (!file.isFile() && !file.createNewFile()) {
            throw new IllegalStateException("can not create " + fileName + " file: " + file.getAbsolutePath());
        }
        return file;
    }
    
    /**
     * 获取确保存在的文件夹
     *
     * @param directory 文件夹
     * @param directoryName 文件夹名
     * @return 文件夹
     * @throws NullPointerException directory 为 null
     * @throws NullPointerException directoryName 为 null
     */
    public static File getExistedDirectory(File directory, String directoryName) {
        Objects.requireNonNull(directory, "directory is null!");
        Objects.requireNonNull(directoryName, "directory name is null!");
        
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IllegalStateException("can not create " + directoryName + " directory: " + directory.getAbsolutePath());
        }
        return directory;
    }
}
