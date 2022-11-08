package io.jenkins.plugins.devopsportal;

import java.io.Serializable;

public enum MonitoringStatus implements Serializable {

    SUCCESS("icon-blue"),
    FAILURE("icon-red"),
    INVALID_HTTPS("icon-yellow"),
    INVALID_CONFIGURATION("icon-yellow"),
    DISABLED("icon-disabled");

    private final String icon;

    MonitoringStatus(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public static String defaultIcon() {
        return DISABLED.getIcon();
    }

}
