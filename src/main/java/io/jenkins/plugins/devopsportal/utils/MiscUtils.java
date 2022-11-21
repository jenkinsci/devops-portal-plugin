package io.jenkins.plugins.devopsportal.utils;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
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

    public static <T> T filterLines(File workingDir, String[] cmd, boolean stopWhenEmpty, Function<List<String>, T> reader){
        try {
            List<String> list = new ArrayList<>();
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(workingDir);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            //process.waitFor();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.isEmpty() && stopWhenEmpty) {
                        break;
                    }
                    list.add(line);
                }
            }

            return reader.apply(list);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
