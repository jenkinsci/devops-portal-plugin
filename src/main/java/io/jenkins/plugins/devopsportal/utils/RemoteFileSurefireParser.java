package io.jenkins.plugins.devopsportal.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import io.jenkins.plugins.devopsportal.models.UnitTestActivity;
import jenkins.MasterToSlaveFileCallable;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Let parse test results of remote files.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class RemoteFileSurefireParser extends MasterToSlaveFileCallable<Integer> implements Serializable {

    private final UnitTestActivity activity;
    private final String path;

    public RemoteFileSurefireParser(@NonNull UnitTestActivity activity, String path) {
        this.activity = activity;
        this.path = path;
    }

    @Override
    public Integer invoke(File dir, VirtualChannel channel) throws IOException, InterruptedException {
        int i = 0;
        for (FilePath path : new FilePath(dir).list(path)) {
            i++;
            File file = new File(path.getRemote());
            parse(file, activity);
        }
        return i;
    }

    public static final void parse(@NonNull File file, @NonNull UnitTestActivity activity) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("<testsuite ")) {
                    Matcher matcher = Pattern
                            .compile("tests=\"(.*?)\" errors=\"(.*?)\" skipped=\"(.*?)\" failures=\"(.*?)\"") //NOSONAR
                            .matcher(line);
                    if (matcher.find()) {
                        activity.addTestsPassed(Integer.parseInt(matcher.group(1)));
                        activity.addTestsFailed(Integer.parseInt(matcher.group(2)) + Integer.parseInt(matcher.group(4)));
                        activity.addTestsIgnored(Integer.parseInt(matcher.group(3)));
                        activity.updateScore();
                        break;
                    }
                }
            }
        }
    }

}
