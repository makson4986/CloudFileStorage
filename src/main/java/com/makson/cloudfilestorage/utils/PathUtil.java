package com.makson.cloudfilestorage.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PathUtil {
    public static String getName(String path) {
        String[] parts = splitPath(path);
        return parts[parts.length - 1];
    }

    public static String getParent(String path) {
        String[] parts = splitPath(path);

        if (parts.length > 1) {
            return parts[parts.length - 2];
        }
        return "";
    }

    private static String[] splitPath(String path) {
        return path.split("(?<=/)");
    }
}
