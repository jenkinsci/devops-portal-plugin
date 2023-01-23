package io.jenkins.plugins.devopsportal.utils;

import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Let compute the length of a remote file.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class RemoteFileSizeGetter extends MasterToSlaveFileCallable<Long> implements Serializable {

    @Override
    public Long invoke(File file, VirtualChannel channel) throws IOException, InterruptedException {
        if (!file.exists()) {
            return -1L;
        }
        return file.length();
    }

}
