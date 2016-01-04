package com.netflix.simianarmy.docker;

import com.google.auto.service.AutoService;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import org.jclouds.Constants;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.config.ComputeServiceAdapterContextModule;
import org.jclouds.compute.config.ComputeServiceProperties;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.docker.DockerApi;
import org.jclouds.docker.compute.config.DockerComputeServiceContextModule;
import org.jclouds.docker.compute.config.LoginPortLookupModule;
import org.jclouds.docker.compute.functions.ImageToImage;
import org.jclouds.docker.compute.functions.StateToStatus;
import org.jclouds.docker.compute.options.DockerTemplateOptions;
import org.jclouds.docker.compute.strategy.DockerComputeServiceAdapter;
import org.jclouds.docker.config.DockerHttpApiModule;
import org.jclouds.docker.config.DockerParserModule;
import org.jclouds.docker.domain.Container;
import org.jclouds.docker.domain.Image;
import org.jclouds.docker.domain.State;
import org.jclouds.domain.Location;
import org.jclouds.functions.IdentityFunction;
import org.jclouds.rest.internal.BaseHttpApiMetadata;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import static org.jclouds.compute.config.ComputeServiceProperties.TEMPLATE;
import static org.jclouds.reflect.Reflection2.typeToken;

/**
 * Overriding standard {@link org.jclouds.docker.DockerApiMetadata} due to issues
 * with overriding Guice context, it's impossible to rebind any of already bind
 * implementations, but provided implementations won't support SSH adapter correctly.
 * <p>
 * Metadata, by default is constructed without credentials and port to connect, this
 * prevent SshFactory to be used;
 */
@AutoService(ApiMetadata.class)
public class DockerApiMetadata extends BaseHttpApiMetadata<DockerApi> {
    public static final String DOCKER_CA_CERT_PATH = "docker.cacert.path";

    @Override
    public Builder toBuilder() {
        return new Builder().fromApiMetadata(this);
    }

    public DockerApiMetadata() {
        this(new Builder().initialize(defaultProperties()));
    }

    protected DockerApiMetadata(Builder builder) {
        super(builder);
    }

    public static Properties defaultProperties() {
        Properties properties = BaseHttpApiMetadata.defaultProperties();
        properties.setProperty(Constants.PROPERTY_CONNECTION_TIMEOUT, "5000"); // 5 seconds
        properties.setProperty(ComputeServiceProperties.IMAGE_LOGIN_USER, "root:password");
        properties.setProperty(TEMPLATE, "osFamily=UBUNTU,os64Bit=true");
        properties.setProperty(DOCKER_CA_CERT_PATH, "");
        return properties;
    }

    public static class Builder extends BaseHttpApiMetadata.Builder<DockerApi, Builder> {
        public static final URI DOCUMENTATION = URI.create("https://docs.docker.com/reference/api/docker_remote_api/");

        private String cacertPath = "./";
        private String url;
        private String verison = "1.21";

        protected Builder() {
            super(DockerApi.class);
        }

        @Override
        public DockerApiMetadata build() {
            initialize(DockerApiMetadata.defaultProperties());
            return new DockerApiMetadata(this);
        }

        private Builder initialize(Properties props) {
            props.put(DOCKER_CA_CERT_PATH, new File(cacertPath, "ca.pem").getAbsolutePath());
            id("docker")
                    .name("Docker API")
                    .identityName(new File(cacertPath, "cert.pem").getAbsolutePath())
                    .credentialName(new File(cacertPath, "key.pem").getAbsolutePath())
                    .documentation(DOCUMENTATION)
                    .version(verison)
                    .defaultEndpoint(url)
                    .defaultProperties(props)
                    .view(typeToken(ComputeServiceContext.class))
                    .defaultModules(ImmutableSet.<Class<? extends Module>>of(
                            DockerParserModule.class,
                            DockerHttpApiModule.class,
                            MetaModule.class));
            return this;
        }


        public Builder version(String version) {
            super.version(version);
            this.verison = version;
            return this;
        }

        public Builder cacert(String path) {
            this.cacertPath = path;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Builder fromApiMetadata(ApiMetadata in) {
            return this;
        }
    }

    /**
     * Overriding {@link DockerComputeServiceContextModule} to override
     * metadata mapper - current mapper doesn't allow SSH to be initialized
     */
    public static class MetaModule extends ComputeServiceAdapterContextModule<Container, Hardware, Image, Location> {
        @Override
        protected void configure() {
            super.configure();

            /**
             * Using customized Meta mapper - which is passing fake SSH credentials and PORT
             * to allow SSH factory call, otherwise it will be failed even if SshFactory is implemented
             */
            bind(new TypeLiteral<Function<Container, NodeMetadata>>() {
            }).to(MetaDataMapper.class);

            /**
             * Left without changes as here {@link DockerComputeServiceContextModule}
             */
            bind(new TypeLiteral<ComputeServiceAdapter<Container, Hardware, Image, Location>>() {
            }).to(DockerComputeServiceAdapter.class);
            bind(new TypeLiteral<Function<Image, org.jclouds.compute.domain.Image>>() {
            }).to(ImageToImage.class);
            bind(new TypeLiteral<Function<Hardware, Hardware>>() {
            }).to(Class.class.cast(IdentityFunction.class));
            bind(new TypeLiteral<Function<Location, Location>>() {
            }).to(Class.class.cast(IdentityFunction.class));
            bind(new TypeLiteral<Function<State, NodeMetadata.Status>>() {
            }).to(StateToStatus.class);
            bind(TemplateOptions.class).to(DockerTemplateOptions.class);
            install(new LoginPortLookupModule());
        }
    }
}
