package cn.chuanwise.nessc.util;

import java.util.Objects;

/**
 * 异常工具类
 *
 * @author Chuanwise
 */
public class Exceptions {
    
    private Exceptions() {
        throwInitializeUtilClassException(Exceptions.class);
    }
    
    /**
     * 抛出工具类初始化异常
     *
     * @param utilClass 工具类
     * @throws UnsupportedOperationException 不允许构造工具类的实例
     */
    public static void throwInitializeUtilClassException(Class<?> utilClass) {
        Objects.requireNonNull(utilClass, "util class is null!");
    
        throw new UnsupportedOperationException("can not try to construct an instance of the util class: " + utilClass.getName());
    }
}
