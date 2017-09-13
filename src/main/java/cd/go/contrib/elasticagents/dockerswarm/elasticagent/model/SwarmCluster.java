/*
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.model;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class SwarmCluster {
    private final List<DockerNode> nodes;

    public SwarmCluster(DockerClient dockerClient) throws DockerException, InterruptedException {
        nodes = dockerClient.listNodes().stream().map(node -> new DockerNode(node)).collect(toList());

        final Map<String, DockerNode> dockerNodeMap = nodes.stream().distinct().collect(toMap(DockerNode::getId, node -> node));

        getContainers(dockerClient).stream()
                .forEach(container -> {
                    final DockerContainer dockerContainer = new DockerContainer(container);
                    final DockerNode dockerNode = dockerNodeMap.get(dockerContainer.getNodeId());
                    if (dockerNode != null) {
                        dockerNode.add(dockerContainer);
                    }
                });
    }

    private List<Container> getContainers(DockerClient dockerClient) throws DockerException, InterruptedException {
        return dockerClient.listContainers(DockerClient.ListContainersParam.withStatusRunning(), DockerClient.ListContainersParam.withStatusCreated());
    }

    public List<DockerNode> getNodes() {
        return nodes;
    }
}
