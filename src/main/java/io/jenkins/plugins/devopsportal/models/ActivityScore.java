package io.jenkins.plugins.devopsportal.models;

public enum ActivityScore {

    D, B, C, A;

    public static ActivityScore min(ActivityScore... scores) {
        if (scores == null || scores.length == 0) {
            return A;
        }
        ActivityScore r = A;
        for (ActivityScore score : scores) {
            if (score.ordinal() < r.ordinal()) {
                r = score;
            }
        }
        return r;
    }

}
