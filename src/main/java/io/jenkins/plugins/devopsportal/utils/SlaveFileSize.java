package io.jenkins.plugins.devopsportal.utils;

import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class SlaveFileSize extends MasterToSlaveFileCallable<Long> implements Serializable {

    @Override
    public Long invoke(File file, VirtualChannel channel) throws IOException, InterruptedException {
        return file.length();
    }

}
