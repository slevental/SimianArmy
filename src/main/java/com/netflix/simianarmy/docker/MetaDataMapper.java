package com.netflix.simianarmy.docker;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.jclouds.collect.Memoized;
import org.jclouds.compute.domain.*;
import org.jclouds.compute.functions.GroupNamingConvention;
import org.jclouds.docker.domain.Container;
import org.jclouds.docker.domain.State;
import org.jclouds.domain.Location;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.providers.ProviderMetadata;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Stas on 12/21/15.
 */
@Singleton
public class MetaDataMapper implements Function<Container, NodeMetadata> {
    private static final Integer FAKE_PORT = 22;

    private final ProviderMetadata providerMetadata;
    private final Function<State, NodeMetadata.Status> toPortableStatus;
    private final GroupNamingConvention nodeNamingConvention;
    private final Supplier<Map<String, ? extends Image>> images;
    private final Supplier<Set<? extends Location>> locations;

    @Inject
    MetaDataMapper(ProviderMetadata providerMetadata,
                   Function<State, NodeMetadata.Status> toPortableStatus, GroupNamingConvention.Factory namingConvention,
                   Supplier<Map<String, ? extends Image>> images, @Memoized Supplier<Set<? extends Location>> locations) {
        this.providerMetadata = providerMetadata;
        this.toPortableStatus = toPortableStatus;
        this.nodeNamingConvention = namingConvention.createWithoutPrefix();
        this.images = images;
        this.locations = locations;
    }

    @Nullable
    @Override
    public NodeMetadata apply(@Nullable Container container) {
        String name = cleanUpName(container.name());
        String group = nodeNamingConvention.extractGroup(name);
        NodeMetadataBuilder builder = new NodeMetadataBuilder();
        builder.ids(container.id())
                .name(name)
                .group(group)
                .hostname(container.config().hostname())
                .hardware(new HardwareBuilder()
                        .id("")
                        .ram(container.config().memory())
                        .processor(new Processor(container.config().cpuShares(), container.config().cpuShares()))
                        .build());
        builder.status(toPortableStatus.apply(container.state()));
        builder.loginPort(FAKE_PORT);
        builder.credentials(LoginCredentials.builder()
                .identity(container.id()).credential(name)
                .build());
        builder.publicAddresses(getPublicIpAddresses());
        builder.privateAddresses(getPrivateIpAddresses(container));
        builder.location(Iterables.getOnlyElement(locations.get()));
        String imageId = container.image();
        builder.imageId(imageId);
        if (images.get().containsKey(imageId)) {
            Image image = images.get().get(imageId);
            builder.operatingSystem(image.getOperatingSystem());
        }
        return builder.build();
    }

    private String cleanUpName(String name) {
        return name.startsWith("/") ? name.substring(1) : name;
    }

    private Iterable<String> getPrivateIpAddresses(Container container) {
        if (container.networkSettings() == null) return ImmutableList.of();
        return ImmutableList.of(container.networkSettings().ipAddress());
    }

    private List<String> getPublicIpAddresses() {
        String dockerIpAddress = URI.create(providerMetadata.getEndpoint()).getHost();
        return ImmutableList.of(dockerIpAddress);
    }
}
