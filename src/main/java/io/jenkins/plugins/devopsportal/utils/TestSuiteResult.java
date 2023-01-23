package io.jenkins.plugins.devopsportal.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TestSuiteResult implements Serializable {

    public int testsPassed = 0;
    public int testsFailed = 0;
    public int testsIgnored = 0;
    public final List<String> files = new ArrayList<>();

}
