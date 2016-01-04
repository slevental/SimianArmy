package com.netflix.simianarmy.client.aws.chaos;

import com.google.common.base.Supplier;
import com.netflix.simianarmy.GroupType;
import com.netflix.simianarmy.basic.chaos.BasicInstanceGroup;
import com.netflix.simianarmy.chaos.ChaosCrawler;
import com.netflix.simianarmy.client.aws.AWSClient;
import org.jclouds.compute.domain.NodeMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 *
 */
public class DockerTagsChaosCrawler extends DelegateCrawler {
    public static final String SEPARATOR = "::";

    private final AWSClient awsClient;

    public enum Type implements GroupType {
        DOCKER_CONTAINER;

        public boolean isContainerId(String id) {
            return id.contains(SEPARATOR);
        }

        public String getInstanceId(String compositeId) {
            return isContainerId(compositeId) ? split(compositeId)[1] : compositeId;
        }

        public String getContainerId(String compositeId) {
            return split(compositeId)[0];
        }

        private String[] split(String composite) {
            String[] split = composite.split(SEPARATOR);
            if (split.length != 2)
                throw new IllegalArgumentException("Not a valid composite ID make sure that DockerTagsChaosCrawler is used");
            return split;
        }
    }

    /**
     * Instantiates a new basic chaos crawler.
     *
     * @param awsClient the aws client
     * @param delegate
     */
    public DockerTagsChaosCrawler(AWSClient awsClient, ChaosCrawler delegate) {
        super(delegate);
        this.awsClient = awsClient;
    }

    @Override
    public List<InstanceGroup> groups(String... names) {
        return super.groups(names).stream().map(in -> new LazyGroup(
                in.name(),
                Type.DOCKER_CONTAINER,
                in.region(),
                () -> {
                    List<String> res = new ArrayList<>();
                    in.instances().parallelStream().forEach(instance -> {
                        List<NodeMetadata> meta = awsClient.listContainersAt(instance);
                        res.addAll(meta.stream()
                                .map(nodeMetadata -> nodeMetadata.getId() + SEPARATOR + instance)
                                .collect(Collectors.toList()));
                    });
                    return res;
                }
        )).collect(toList());
    }

    private static class LazyGroup extends BasicInstanceGroup {
        private final Supplier<List<String>> callback;
        private List<String> instances;

        public LazyGroup(String name, GroupType type, String region, Supplier<List<String>> callback) {
            super(name, type, region);
            this.callback = callback;
        }

        @Override
        public synchronized List<String> instances() {
            if (instances != null)
                return instances;
            return instances = callback.get();
        }
    }
}
