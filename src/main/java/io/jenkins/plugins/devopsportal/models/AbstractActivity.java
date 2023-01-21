package io.jenkins.plugins.devopsportal.models;

import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Abstract class for BUILD activity persistent data.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public abstract class AbstractActivity implements Serializable {

    private final ActivityCategory category;
    private final String applicationComponent;
    private ActivityScore score;
    private final long timestamp; // sec

    protected AbstractActivity(ActivityCategory category, String applicationComponent) {
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

    @SuppressWarnings("unused")
    public String getUUID() {
        return UUID.nameUUIDFromBytes(applicationComponent.getBytes()).toString();
    }

}
