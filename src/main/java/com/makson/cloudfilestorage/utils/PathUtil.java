package com.makson.cloudfilestorage.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PathUtil {
    public static String getName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String getParent(String path) {
        return path.substring(0, path.lastIndexOf("/") + 1);
    }
}
