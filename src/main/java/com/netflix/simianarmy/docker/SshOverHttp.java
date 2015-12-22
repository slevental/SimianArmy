package com.netflix.simianarmy.docker;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import org.jclouds.compute.domain.ExecChannel;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.docker.DockerApi;
import org.jclouds.docker.domain.Exec;
import org.jclouds.docker.domain.ExecCreateParams;
import org.jclouds.docker.domain.ExecInspect;
import org.jclouds.docker.domain.ExecStartParams;
import org.jclouds.docker.features.MiscApi;
import org.jclouds.io.Payload;
import org.jclouds.ssh.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Stas on 12/18/15.
 */
public class SshOverHttp implements SshClient {
    private final String id;
    private final MiscApi miscApi;

    public SshOverHttp(DockerApi api, String id) {
        this.miscApi = api.getMiscApi();
        this.id = id;
    }

    @Override
    public String getUsername() {
        return "root";
    }

    @Override
    public String getHostAddress() {
        return id;
    }

    @Override
    public void put(String path, Payload contents) {
        throw new UnsupportedOperationException("not supported though Docker API");
    }

    @Override
    public Payload get(String path) {
        throw new UnsupportedOperationException("not supported though Docker API");
    }

    @Override
    public ExecResponse exec(String command) {
        Execution execution = runInContainer(command);
        return new ExecResponse(execution.stdout, "", execution.exitstatus);
    }

    @Override
    public ExecChannel execChannel(String command) {
        throw new UnsupportedOperationException("not supported though Docker API");
    }

    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void put(String path, String contents) {
        Execution execution = runInContainer("cat << EOF > " + path + "\n" + escapeBash(contents) + "EOF");
        if (execution.exitstatus != 0)
            throw new IllegalArgumentException("Cannot save script via docker API, exitcode "
                    + execution.exitstatus
                    + " != 0");
    }

    private Execution runInContainer(String command) {
        try {
            Exec exec = miscApi.execCreate(id, ExecCreateParams.builder()
                    .cmd(ImmutableList.of("bash", "-c", command))
                    .attachStdout(true)
                    .attachStderr(false).build());
            InputStream output = miscApi.execStart(exec.id(), ExecStartParams.create(false));
            ByteArrayOutputStream buff = new ByteArrayOutputStream();
            output.read(new byte[8]);
            ByteStreams.copy(output, buff);
            ExecInspect insp = miscApi.execInspect(exec.id());
            return new Execution(insp.exitCode(), buff.toString("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String escapeBash(String contents) {
        return contents
                .replaceAll("[$]", "\\\\\\$")
                .replaceAll("[`]", "\\\\`");
    }

    private static class Execution {
        private final String stdout;
        private final int exitstatus;

        public Execution(int exitstatus, String stdout) {
            this.exitstatus = exitstatus;
            this.stdout = stdout;
        }
    }
}
