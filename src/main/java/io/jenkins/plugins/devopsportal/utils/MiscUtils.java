package io.jenkins.plugins.devopsportal.utils;

import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class with miscellaneous functions
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public final class MiscUtils {

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.devopsportal");

    private MiscUtils() {
    }

    @SuppressWarnings("unused")
    public static String readableFileSize(long size) {
        if (size <= 0) return "0 kB";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        if (digitGroups > 4) {
            digitGroups = 4;
        }
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static List<String> split(String value, String separator) {
        if (value != null && !value.isEmpty()) {
            return Arrays
                    .stream(value.split(separator))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static File checkFilePathIllegalAccess(String parent, String child) {
        if (parent == null || parent.trim().isEmpty() || child == null || child.trim().isEmpty()) {
            return null;
        }
        File parentFile = new File(parent);
        File childFile = new File(parent, child);
        try {
            parentFile = parentFile.toPath().toAbsolutePath().normalize().toFile().getCanonicalFile();
            childFile = childFile.toPath().toAbsolutePath().normalize().toFile().getCanonicalFile();
        }
        catch (SecurityException ex) {
            LOGGER.warning("SecurityManager stopped an attempt to access a file: " + childFile);
        }
        catch (IOException ex) {
            return null;
        }
        try {
            Path parentPath = parentFile.toPath().toRealPath(LinkOption.NOFOLLOW_LINKS);
            Path childPath = childFile.toPath().toRealPath(LinkOption.NOFOLLOW_LINKS);
            if (!childPath.startsWith(parentPath)) {
                throw new SecurityException();
            }
        }
        catch (SecurityException ex) {
            LOGGER.warning("checkFilePathIllegalAccess() stopped an attempt to read a file: " + childFile);
        }
        catch (IOException ex) {
            return null;
        }
        return childFile;
    }

    public static void checkNotEmpty(String... values) {
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException();
            }
        }
    }

    public static boolean isValidURL(@Nullable String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            // JENSEC-1938 Restrict href protocol to only allow some https / http schemes
            final String scheme = new URI(url).getScheme();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        }
        catch (URISyntaxException ex) {
            return false;
        }
    }

    public static String getStringOrEmpty(Map<String, Object> map, String key) {
        if (map == null || key == null || !map.containsKey(key)) {
            return "";
        }
        return "" + map.get(key);
    }

    public static String getStringOrEmpty(List<Map<String, Object>> map, String itemKey, String itemValue,
                                          String valueKey) {
        if (map == null || itemKey == null || itemValue == null || valueKey == null) {
            return "";
        }
        for (Map<String, Object> item : map) {
            if (!item.containsKey(itemKey) || !item.containsKey(valueKey)) {
                continue;
            }
            if (!itemValue.equalsIgnoreCase("" + item.get(itemKey))) {
                continue;
            }
            return "" + item.get(valueKey);
        }
        return "";
    }

    public static int getIntOrZero(Map<String, Object> map, String key) {
        if (map == null || key == null || !map.containsKey(key)) {
            return 0;
        }
        try {
            return Integer.parseInt("" + map.get(key));
        }
        catch (Exception ex) {
            return 0;
        }
    }

    public static float getFloatOrZero(String value) {
        try {
            return Float.parseFloat(value);
        }
        catch (Exception ex) {
            return 0;
        }
    }

    public static int getIntOrZero(String value) {
        try {
            return Integer.parseInt(value);
        }
        catch (Exception ex) {
            return 0;
        }
    }

}
