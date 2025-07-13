package com.makson.cloudfilestorage.utils;

import com.makson.cloudfilestorage.models.User;
import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class PathUtil {
    private static final String USER_ROOT_DIRECTORY = "user-%d-files/";

    public static String getName(String path) {
        String[] parts = splitPath(path);
        return parts[parts.length - 1];
    }

    public static String getParent(String path) {
        String[] parts = splitPath(path);

        var parentDirectory = Arrays.copyOfRange(parts, 0, parts.length - 1);
        return String.join("", parentDirectory);
    }

    public static String[] splitPath(String path) {
        return path.split("(?<=/)");
    }

    public static String resolve(String... path) {
        StringBuilder pathBuilder = new StringBuilder();

        for (String p : path) {
            pathBuilder.append(p);
        }

        return pathBuilder.toString();
    }

    public static String getFullPathRelativeUserDirectory(String path, User user) {
        return resolve(USER_ROOT_DIRECTORY.formatted((user.getId())), path);
    }
}