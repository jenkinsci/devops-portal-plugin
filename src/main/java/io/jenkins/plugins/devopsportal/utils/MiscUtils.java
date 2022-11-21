package io.jenkins.plugins.devopsportal.utils;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

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
        try {
            List<String> list = new ArrayList<>();
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(workingDir);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            //process.waitFor();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                while (true) {
                    final String line = callWithTimeout(5000, in::readLine);
                    if (line == null) {
                        break;
                    }
                    list.add(line);
                }

                /*StringBuilder line = new StringBuilder();
                char c;
                while ((c = (char) in.read()) != -1){
                    if (c == '\n') {
                        list.add(line.toString().trim());
                        System.out.println(line);
                        line = new StringBuilder();
                    }
                    else {
                        line.append(c);
                    }
                }
                if (line.length() > 0) {
                    System.out.println(line);
                    list.add(line.toString().trim());
                }
                */

                /*while ((line = in.readLine()) != null) {
                    if (line.isEmpty() && stopWhenEmpty) {
                        break;
                    }
                    list.add(line);
                }*/

            }

            return mapper.apply(list);
        }
        catch (IOException ex) {
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

}
