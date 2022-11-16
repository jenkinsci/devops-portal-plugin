package io.jenkins.plugins.devopsportal.models;

import hudson.EnvVars;
import hudson.model.Run;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.time.Instant;

public abstract class AbstractActivity implements Serializable {

    private final ActivityCategory category;
    private final String applicationComponent;
    private ActivityScore score;
    private final long timestamp; // sec

    public AbstractActivity(ActivityCategory category, String applicationComponent) {
        if (category == null) {
            throw new IllegalArgumentException("Activity category is null");
        }
        this.category = category;
        if (applicationComponent == null || applicationComponent.isEmpty()) {
            throw new IllegalArgumentException("Activity application component is null");
        }
        this.applicationComponent = applicationComponent;
        this.timestamp = Instant.now().getEpochSecond();
    }

    public ActivityCategory getCategory() {
        return category;
    }

    public String getApplicationComponent() {
        return applicationComponent;
    }

    public ActivityScore getScore() {
        return score;
    }

    @DataBoundSetter
    public void setScore(ActivityScore score) {
        this.score = score;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static void updateRecordFromRun(BuildStatus record, Run<?,?> run, EnvVars env) {
        record.setBuildJob(env.get("JOB_NAME"));
        record.setBuildNumber(env.get("BUILD_NUMBER"));
        record.setBuildURL(env.get("RUN_DISPLAY_URL")); // TODO Verify this URL
        record.setBuildBranch("");// TODO
        record.setBuildCommit("");//TODO
    }

}
