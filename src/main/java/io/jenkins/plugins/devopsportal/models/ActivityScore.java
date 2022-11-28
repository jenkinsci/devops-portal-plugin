package io.jenkins.plugins.devopsportal.models;

/**
 * List of all Scores used to measure achievement of a build activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public enum ActivityScore {

    E, D, B, C, A;

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

    public static ActivityScore parseString(String value) {
        switch (value) {
            case "1.0": return A;
            case "2.0": return B;
            case "3.0": return C;
            case "4.0": return D;
            case "5.0": return E;
        }
        return null;
    }

}
