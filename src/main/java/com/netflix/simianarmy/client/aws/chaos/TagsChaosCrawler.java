/*
 *
 *  Copyright 2012 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.simianarmy.client.aws.chaos;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.netflix.simianarmy.GroupType;
import com.netflix.simianarmy.basic.chaos.BasicInstanceGroup;
import com.netflix.simianarmy.chaos.ChaosCrawler;
import com.netflix.simianarmy.client.aws.AWSClient;

import java.util.*;

import static com.google.common.base.Joiner.on;
import static org.apache.commons.lang.ArrayUtils.indexOf;

/**
 * The Class TagsChaosCrawler.
 * This will crawl all instances tagged with provided tags grouping by them.
 * <p>
 * For instance if your machine having AWS tags: Role, Env
 */
public class TagsChaosCrawler implements ChaosCrawler {

    public enum Type implements GroupType {
        TAGGED_EC2
    }

    /**
     * The aws client.
     */
    protected final AWSClient awsClient;

    /**
     * Tags instances should be grouped by
     */
    protected final String[] tags;

    /**
     * Instantiates a new basic chaos crawler.
     *
     * @param awsClient the aws client
     */
    public TagsChaosCrawler(AWSClient awsClient, String... tags) {
        this.awsClient = awsClient;
        this.tags = tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<?> groupTypes() {
        return EnumSet.of(Type.TAGGED_EC2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<InstanceGroup> groups() {
        return groups((String[]) null);
    }

    @Override
    public List<InstanceGroup> groups(String... names) {
        Multimap<String, Instance> instances = getInstancesGroupedByTags(tags);

        if (instances.isEmpty()) {
            return ImmutableList.of();
        }

        List<InstanceGroup> result = new ArrayList<>(instances.size());
        for (Map.Entry<String, Collection<Instance>> group : instances.asMap().entrySet()) {
            if (names != null && indexOf(names, group.getKey()) < 0)
                continue;
            InstanceGroup instanceGroup = new BasicInstanceGroup(group.getKey(), Type.TAGGED_EC2, awsClient.region());
            result.add(instanceGroup);
            for (Instance instance : group.getValue()) {
                instanceGroup.addInstance(instance.getInstanceId());
            }
        }
        return result;
    }

    protected Multimap<String, Instance> getInstancesGroupedByTags(String[] tags) {
        Multimap<String, Instance> groupBy = HashMultimap.create();
        /**
         * Grouping by tags' values
         */
        for (Instance instance : awsClient.describeEc2WithTags(tags)) {
            String[] name = new String[tags.length];
            int tagsFound = 0;
            for (Tag tag : instance.getTags()) {
                String tgName = tag.getKey();
                int i = indexOf(tags, tgName);
                if (i < 0) continue;
                name[i] = tag.getValue();
                tagsFound++;
            }

            if (tagsFound == tags.length)
                groupBy.put(on(".").join(name), instance);
        }
        return groupBy;
    }
}
