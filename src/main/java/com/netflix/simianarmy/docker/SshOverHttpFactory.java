package com.netflix.simianarmy.docker;

import com.google.common.net.HostAndPort;
import com.google.inject.AbstractModule;
import org.jclouds.docker.DockerApi;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.ssh.SshClient;
import org.jclouds.ssh.config.ConfiguresSshClient;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for Dummy SSH, which uses Docker MiscAPI for execution
 * instead of explicit SSH connection
 */
@Singleton
public class SshOverHttpFactory implements SshClient.Factory {

    @Inject
    DockerApi api;

    @Override
    public SshClient create(HostAndPort socket, LoginCredentials credentials) {
        return new SshOverHttp(api, credentials.getUser());
    }

    @Override
    public boolean isAgentAvailable() {
        return false;
    }

    @ConfiguresSshClient
    public static class Module extends AbstractModule {
        @Override
        protected void configure() {
            bind(SshClient.Factory.class).to(SshOverHttpFactory.class);
        }
    }
}
