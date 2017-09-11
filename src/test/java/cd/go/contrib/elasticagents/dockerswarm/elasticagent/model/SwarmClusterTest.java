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

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.swarm.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.spotify.docker.client.DockerClient.ListContainersParam.withStatusCreated;
import static com.spotify.docker.client.DockerClient.ListContainersParam.withStatusRunning;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class SwarmClusterTest {

    @Test
    public void shouldCreateSwarmClusterObject() throws Exception {
        final DockerClient dockerClient = mock(DockerClient.class);
        final List<Node> nodeList = Arrays.asList(mockNode());
        final List<Container> containerList = Arrays.asList(mockContainer());

        when(dockerClient.listNodes()).thenReturn(nodeList);
        when(dockerClient.listContainers(withStatusRunning(), withStatusCreated())).thenReturn(containerList);

        final SwarmCluster swarmCluster = new SwarmCluster(dockerClient);

        verify(dockerClient, times(1)).listNodes();
        verify(dockerClient, times(1)).listContainers(withStatusRunning(), withStatusCreated());

        assertThat(swarmCluster.getNodes(), hasSize(1));
        assertThat(swarmCluster.getNodes().get(0).getContainers(), hasSize(1));
    }

    private Node mockNode() {
        final Node node = mock(Node.class);

        when(node.id()).thenReturn("node-id");
        when(node.status()).thenReturn(mock(NodeStatus.class));
        when(node.spec()).thenReturn(NodeSpec.builder().name("node-name").availability("active").role("manager").build());
        when(node.managerStatus()).thenReturn(mock(ManagerStatus.class));

        final NodeDescription nodeDescription = mock(NodeDescription.class);
        when(node.description()).thenReturn(nodeDescription);
        when(nodeDescription.engine()).thenReturn(mock(EngineConfig.class));
        when(nodeDescription.resources()).thenReturn(mock(Resources.class));
        when(nodeDescription.platform()).thenReturn(mock(Platform.class));

        when(node.createdAt()).thenReturn(new Date());
        when(node.updatedAt()).thenReturn(new Date());

        return node;
    }

    private Container mockContainer() {
        final Container container = mock(Container.class);

        when(container.id()).thenReturn("container-id");
        when(container.created()).thenReturn(1L);
        when(container.image()).thenReturn("gocd-agent:latest");
        when(container.imageId()).thenReturn("image-id");
        when(container.state()).thenReturn("running");
        when(container.status()).thenReturn("Up 3 minutes");
        when(container.command()).thenReturn("make cross");
        when(container.labels()).thenReturn(new ImmutableMap.Builder<String, String>().put("com.docker.swarm.node.id", "node-id").build());

        return container;
    }
}