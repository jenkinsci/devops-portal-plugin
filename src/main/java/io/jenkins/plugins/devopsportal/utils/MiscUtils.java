package io.jenkins.plugins.devopsportal.utils;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class with miscellaneous functions
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public final class MiscUtils {

    public static String readableFileSize(long size) {
        if (size <= 0) return "0 kB";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static <T> T filterLines(File workingDir, List<String> cmd, Function<List<String>, T> mapper) {
        return filterLines(workingDir, cmd, mapper, null);
    }

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

}
