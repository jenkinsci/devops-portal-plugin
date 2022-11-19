package io.jenkins.plugins.devopsportal.models;

/**
 * List of all BUILD activities.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public enum ActivityCategory {

    BUILD(BuildActivity.class),
    UNIT_TEST(UnitTestActivity.class),
    QUALITY_AUDIT(QualityAuditActivity.class),
    DEPENDENCIES_ANALYSIS(DependenciesAnalysisActivity.class),
    PERFORMANCE_TEST(PerformanceTestActivity.class),
    IMAGE_RELEASE(ImageReleaseActivity.class);

    private final Class<? extends AbstractActivity> clazz;

    ActivityCategory(Class<? extends AbstractActivity> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends AbstractActivity> getClazz() {
        return clazz;
    }

}
