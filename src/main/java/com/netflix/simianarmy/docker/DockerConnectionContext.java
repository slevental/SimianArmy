package com.netflix.simianarmy.docker;

import java.io.File;

/**
 * Created by Stas on 12/28/15.
 */
public class DockerConnectionContext {
    private final String tlsPath;
    private final String tlsCert;
    private final String tlsKey;
    private final int port;
    private final String protocol;
    private final boolean usePublic;
    private final String version;

    public DockerConnectionContext(String tlsPath, String tlsCert, String tlsKey, int port, String protocol, boolean usePublic, String version) {
        this.tlsPath = tlsPath;
        this.tlsCert = tlsCert;
        this.tlsKey = tlsKey;
        this.port = port;
        this.protocol = protocol;
        this.usePublic = usePublic;
        this.version = version;
    }

    public boolean isUsePublic() {
        return usePublic;
    }

    public String getTlsCertPath() {
        return new File(tlsPath, tlsCert).getAbsolutePath();
    }

    public String getTlsKeyPath() {
        return new File(tlsPath, tlsKey).getAbsolutePath();
    }

    public String getVersion() {
        return version;
    }

    public String getTlsPath() {
        return tlsPath;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }
}
