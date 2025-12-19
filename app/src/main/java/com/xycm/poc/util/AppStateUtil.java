package com.xycm.poc.util;

/**
 * app 状态
 */
public class AppStateUtil {

    private static boolean foreground = true;

    public static void setForeground(boolean value) {
        foreground = value;
    }

    public static boolean isAppForeground() {
        return foreground;
    }
}
