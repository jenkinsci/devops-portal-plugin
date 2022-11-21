package io.jenkins.plugins.devopsportal.buildmanager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BuildManager {

    /**
     * Unique code to identify this manager.
     */
    String code();

    /**
     * Display name of this manager.
     */
    String label();

    /**
     * An optional canonical path to the binary.
     * If not provided, the application will try to guess it.
     */
    String binaryPath() default "";

}
