package io.jenkins.plugins.devopsportal.utils;

import java.io.*;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class with miscellaneous functions
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public final class MiscUtils {

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.devopsportal");

    @SuppressWarnings("unused")
    public static String readableFileSize(long size) {
        if (size <= 0) return "0 kB";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @SuppressWarnings("unused")
    public static <T> T filterLines(File workingDir, List<String> cmd, Function<List<String>, T> mapper) {
        return filterLines(workingDir, cmd, mapper, null);
    }

    @SuppressWarnings("unused")
    public static <T> T filterLines(File workingDir, List<String> cmd, Function<List<String>, T> mapper, String matchStop) {
        try {
            List<String> list = new ArrayList<>();
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(workingDir);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while (true) {
                    if (in.ready()) {
                        if ((line = in.readLine()) != null) {
                            list.add(line);
                            if (matchStop != null && line.contains(matchStop)) {
                                break;
                            }
                        }
                        else {
                            break;
                        }
                    }
                }
            }
            return mapper.apply(list);
        }
        catch (IOException | UncheckedIOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unused")
    public static <T, E extends Exception> T callWithTimeout(long timeoutDurationMs, ThrowingSupplier<T, E> supplier) {
        final AtomicReference<T> value = new AtomicReference<>();
        final AtomicReference<Boolean> done = new AtomicReference<>(false);
        Thread thread = new Thread(() -> {
            try {
                value.set(supplier.get());
                done.set(true);
            }
            catch (Exception ex) {
                value.set(null);
                done.set(true);
            }
        });
        thread.start();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                if (thread.isAlive() && !thread.isInterrupted()) {
                    try {
                        thread.interrupt();
                        done.set(true);
                    }
                    catch (Throwable ex) {
                        value.set(null);
                        done.set(true);
                    }
                }
            }
        }, timeoutDurationMs);
        while (!done.get()) {
            // Wait...
        }
        if (!thread.isInterrupted()) {
            thread.interrupt();;
        }
        return value.get();
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

}
