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
package com.netflix.simianarmy.docker;

import com.netflix.simianarmy.client.aws.chaos.DockerTagsChaosCrawler;
import com.netflix.simianarmy.client.aws.chaos.TagsChaosCrawler;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * The Class BasicSimianArmyContext.
 */
public class TagsBasedDockerChaosArmyContext extends BasicDockerChaosArmyContext {

    public static final String TAGS_OPTION = "simianarmy.docker.ec2tags";

    public TagsBasedDockerChaosArmyContext() {
    }

    /**
     * Create the specific client within passed region, using the appropriate AWS credentials provider
     * and client configuration.
     *
     * @param clientRegion
     */
    @Override
    protected void createClient(String clientRegion) {
        this.client = createAWSClient(clientRegion, config, awsCredentialsProvider, awsClientConfig);
        setCloudClient(client);

        String tags = config.getStr(TAGS_OPTION);

        if (isBlank(tags)) {
            throw new IllegalStateException("Tags should be specified in option (comma-separated list): " + TAGS_OPTION);
        }

        setChaosCrawler(new DockerTagsChaosCrawler(client, new TagsChaosCrawler(client, tags.split(","))));
    }
}
