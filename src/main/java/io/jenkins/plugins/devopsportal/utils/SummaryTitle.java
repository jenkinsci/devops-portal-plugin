package io.jenkins.plugins.devopsportal.utils;

import java.io.Serializable;

public class SummaryTitle implements Serializable {

    private String health;
    private String icon; // heart-outline skull-outline
    private String title; // Healthy

    public SummaryTitle(String health, String icon, String title) {
        this.health = health;
        this.icon = icon;
        this.title = title;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
