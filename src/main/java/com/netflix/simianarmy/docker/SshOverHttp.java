package com.netflix.simianarmy.docker;

import com.google.common.collect.ImmutableList;
import org.jclouds.compute.domain.ExecChannel;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.docker.DockerApi;
import org.jclouds.docker.domain.Exec;
import org.jclouds.docker.domain.ExecCreateParams;
import org.jclouds.docker.domain.ExecInspect;
import org.jclouds.docker.features.MiscApi;
import org.jclouds.io.Payload;
import org.jclouds.ssh.SshClient;

import java.io.IOException;
import java.io.InputStream;

import static org.jclouds.docker.domain.ExecStartParams.create;

/**
 * Created by Stas on 12/18/15.
 */
public class SshOverHttp implements SshClient {
    private static final int HEADER_SIZE = 8;
    private static final int BUFFER_SIZE = 8192;
    private static final int STDERR = 2;

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
        return new ExecResponse(execution.stdout, execution.stderr, execution.exitstatus);
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
                    .cmd(ImmutableList.of("sh", "-c", command))
                    .attachStdout(true)
                    .attachStderr(false)
                    .build());

            byte[] buff = new byte[BUFFER_SIZE];


            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            try (InputStream istream = miscApi.execStart(exec.id(), create(false))) {
                int limit = istream.read(buff);
                if (limit > 0) {
                    byte type = buff[0];
                    StringBuilder out = type == STDERR ? stderr : stdout;
                    out.append(new String(buff, 8, limit - 8));
                    while ((limit = istream.read(buff)) > 0) {
                        out.append(new String(buff, 0, limit));
                    }
                }
                ExecInspect insp = miscApi.execInspect(exec.id());
                return new Execution(insp.exitCode(),
                        stdout.toString(),
                        stderr.toString());
            }
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
        private final String stderr;
        private final int exitstatus;

        public Execution(int exitstatus, String stdout, String stderr) {
            this.exitstatus = exitstatus;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
