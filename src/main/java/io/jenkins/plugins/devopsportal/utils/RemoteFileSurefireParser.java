package io.jenkins.plugins.devopsportal.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Let parse test results of remote files.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class RemoteFileSurefireParser extends MasterToSlaveFileCallable<TestSuiteResult> implements Serializable {

    private final String path;

    public RemoteFileSurefireParser(String path) {
        this.path = path;
    }

    @Override
    public TestSuiteResult invoke(File dir, VirtualChannel channel) throws IOException, InterruptedException {
        TestSuiteResult result = new TestSuiteResult();
        for (FilePath path : new FilePath(dir).list(path)) {
            File file = new File(path.getRemote());
            parse(file, result);
        }
        return result;
    }

    public static boolean parse(@NonNull File file, @NonNull TestSuiteResult result) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("<testsuite ")) {
                    Matcher matcher = Pattern.compile("tests=\"(.*?)\"").matcher(line); // NOSONAR
                    if (matcher.find()) {
                        result.testsPassed += Integer.parseInt(matcher.group(1));
                    }
                    matcher = Pattern.compile("errors=\"(.*?)\"").matcher(line); // NOSONAR
                    if (matcher.find()) {
                        result.testsFailed += Integer.parseInt(matcher.group(1));
                    }
                    matcher = Pattern.compile("failures=\"(.*?)\"").matcher(line); // NOSONAR
                    if (matcher.find()) {
                        result.testsFailed += Integer.parseInt(matcher.group(1));
                    }
                    matcher = Pattern.compile("skipped=\"(.*?)\"").matcher(line); // NOSONAR
                    if (matcher.find()) {
                        result.testsIgnored += Integer.parseInt(matcher.group(1));
                    }
                    result.files.add(file.getName());
                    return true;
                }
            }
        }
        return false;
    }

}
