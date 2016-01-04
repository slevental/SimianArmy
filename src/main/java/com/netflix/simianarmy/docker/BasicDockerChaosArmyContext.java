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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.netflix.simianarmy.basic.BasicChaosMonkeyContext;
import com.netflix.simianarmy.basic.BasicConfiguration;
import com.netflix.simianarmy.client.aws.AWSClient;
import com.netflix.simianarmy.client.aws.chaos.ASGChaosCrawler;
import com.netflix.simianarmy.client.aws.chaos.DockerTagsChaosCrawler;

/**
 * The Class BasicSimianArmyContext.
 */
public class BasicDockerChaosArmyContext extends BasicChaosMonkeyContext {

    public BasicDockerChaosArmyContext() {
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
        setChaosCrawler(new DockerTagsChaosCrawler(client, new ASGChaosCrawler(client)));
    }

    protected static AWSClient createAWSClient(String clientRegion, BasicConfiguration config, AWSCredentialsProvider credentialsProvider, ClientConfiguration cfg) {
        String tlsPath = config.getStr("simianarmy.docker.tlsca.path");
        String tlsCert = config.getStr("simianarmy.docker.tlscert");
        String tlsKey = config.getStr("simianarmy.docker.tlskey");
        boolean usePublic = config.getBool("simianarmy.docker.connect.public");
        int port = (int) config.getNumOrElse("simianarmy.docker.port", 2376);
        String protocol = config.getStr("simianarmy.docker.api.protocol");
        String version = config.getStr("simianarmy.docker.api.version");
        DockerConnectionContext dockerCtx = new DockerConnectionContext(tlsPath, tlsCert, tlsKey, port, protocol, usePublic, version);
        return new AWSClient(clientRegion, credentialsProvider, cfg, dockerCtx);
    }
}
