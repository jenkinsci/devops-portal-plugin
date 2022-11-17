package io.jenkins.plugins.devopsportal.utils;

import io.jenkins.plugins.devopsportal.Messages;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class TimeAgoUtils {

    public static final List<Long> TIMES = Arrays.asList(
        TimeUnit.DAYS.toMillis(365),
        TimeUnit.DAYS.toMillis(30),
        TimeUnit.DAYS.toMillis(1),
        TimeUnit.HOURS.toMillis(1),
        TimeUnit.MINUTES.toMillis(1),
        TimeUnit.SECONDS.toMillis(1)
    );

    private static String getUnit(int index) {
        switch (index) {
            case 0: return Messages.TimeUnits_Year();
            case 1: return Messages.TimeUnits_Month();
            case 2: return Messages.TimeUnits_Day();
            case 3: return Messages.TimeUnits_Hour();
            case 4: return Messages.TimeUnits_Minute();
            case 5: return Messages.TimeUnits_Second();
        }
        throw new IllegalArgumentException();
    }

    public static String toDuration(long durationMilli) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < TIMES.size(); i++) {
            long temp = durationMilli / TIMES.get(i);
            if (temp > 0) {
                res.append(temp).append(" ").append(getUnit(i)).append(temp != 1 ? "s" : "");
                break;
            }
        }
        if ("".equals(res.toString()))
            return Messages.TimeAgo_Prefix() + " 0 " + getUnit(5) + " " + Messages.TimeAgo_Suffix();
        else
            return Messages.TimeAgo_Prefix() + " " + res + " " + Messages.TimeAgo_Suffix();
    }

}
