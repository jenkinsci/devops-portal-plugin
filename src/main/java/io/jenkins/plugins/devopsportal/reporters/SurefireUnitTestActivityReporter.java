package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ActivityScore;
import io.jenkins.plugins.devopsportal.models.BuildStatus;
import io.jenkins.plugins.devopsportal.models.UnitTestActivity;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Build step of a project used to record a UNIT_TEST activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class SurefireUnitTestActivityReporter extends AbstractActivityReporter<UnitTestActivity> {

    private String surefireReportPath;

    @DataBoundConstructor
    public SurefireUnitTestActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
    }

    public String getSurefireReportPath() {
        return surefireReportPath;
    }

    @DataBoundSetter
    public void setSurefireReportPath(String surefireReportPath) {
        this.surefireReportPath = surefireReportPath;
    }

    @Override
    public void updateActivity(@NonNull BuildStatus status, @NonNull UnitTestActivity activity,
                               @NonNull TaskListener listener, @NonNull EnvVars env) {


        final File file = surefireReportPath == null ? null :
                new File(env.get("WORKSPACE", ""), surefireReportPath);

        if (file == null || !file.exists() || !file.canRead()) {
            listener.getLogger().println(Messages.FormValidation_Error_FileNotReadable()
                    .replace("%file%", surefireReportPath));
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("<testsuite ")) {
                    Matcher matcher = Pattern.compile("tests=\"(.*?)\" errors=\"(.*?)\" skipped=\"(.*?)\" failures=\"(.*?)\"")
                            .matcher(line);
                    if (matcher.find()) {
                        activity.setTestCoverage(0);
                        activity.setTestsPassed(Integer.parseInt(matcher.group(1)));
                        activity.setTestsFailed(Integer.parseInt(matcher.group(2)) + Integer.parseInt(matcher.group(4)));
                        activity.setTestsIgnored(Integer.parseInt(matcher.group(3)));
                        activity.setScore(activity.getTestsFailed() > 0 ? ActivityScore.D : ActivityScore.A);
                        break;
                    }
                }
            }
        }
        catch (IOException e) {
            listener.getLogger().println(Messages.FormValidation_Error_FileNotReadable()
                    .replace("%file%", surefireReportPath));
        }
    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.UNIT_TEST;
    }

    @Symbol("reportSurefireTest")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        public DescriptorImpl() {
            super(Messages.SurefireUnitTestActivityReporter_DisplayName());
        }

    }

}
