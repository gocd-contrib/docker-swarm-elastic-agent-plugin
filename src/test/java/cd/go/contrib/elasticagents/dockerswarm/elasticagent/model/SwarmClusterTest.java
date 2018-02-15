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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.Constants;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.swarm.*;
import org.junit.Test;

import java.util.*;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.Constants.JOB_IDENTIFIER_LABEL_KEY;
import static com.spotify.docker.client.DockerClient.ListContainersParam.withStatusCreated;
import static com.spotify.docker.client.DockerClient.ListContainersParam.withStatusRunning;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class SwarmClusterTest {

    @Test
    public void shouldCreateSwarmClusterObject() throws Exception {
        final DockerClient dockerClient = mock(DockerClient.class);
        final Node node = mockNode("node-id", "manager", true);
        final List<Node> nodeList = Arrays.asList(node);
        final List<Task> taskList = Arrays.asList(mockTask(node.id(), "service-id"));
        final Service service = mock(Service.class);
        final List<Service> services = Arrays.asList(service);
        final Map<String, String> labels = new HashMap<>();
        labels.put(JOB_IDENTIFIER_LABEL_KEY, new JobIdentifier().toJson());
        labels.put(Constants.CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID);
        final ServiceSpec serviceSpec = ServiceSpec.builder()
                .taskTemplate(TaskSpec.builder().build())
                .labels(labels)
                .build();

        when(service.id()).thenReturn("service-id");
        when(service.spec()).thenReturn(serviceSpec);
        when(dockerClient.listNodes()).thenReturn(nodeList);
        when(dockerClient.listTasks()).thenReturn(taskList);
        when(dockerClient.listServices()).thenReturn(services);

        final SwarmCluster swarmCluster = new SwarmCluster(dockerClient);

        verify(dockerClient, times(1)).listNodes();
        verify(dockerClient, times(1)).listTasks();

        assertThat(swarmCluster.getNodes(), hasSize(1));
        assertThat(swarmCluster.getNodes().get(0).getTasks(), hasSize(1));
    }

    @Test
    public void shouldSkipServicesNotCreatedByPlugin() throws Exception {
        final DockerClient dockerClient = mock(DockerClient.class);
        final Node node = mockNode("node-id", "manager", true);
        final List<Node> nodeList = Arrays.asList(node);
        final List<Task> taskList = Arrays.asList(mockTask(node.id(), "service-id-1"), mockTask(node.id(), "service-id-2"));
        final Service service_1 = mock(Service.class);
        final Service service_2 = mock(Service.class);
        final List<Service> services = Arrays.asList(service_1, service_2);
        final Map<String, String> labels = new HashMap<>();
        labels.put(JOB_IDENTIFIER_LABEL_KEY, new JobIdentifier().toJson());
        labels.put(Constants.CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID);
        final ServiceSpec service1Spec = ServiceSpec.builder()
                .taskTemplate(TaskSpec.builder().build())
                .labels(labels)
                .build();

        final ServiceSpec service2Spec = ServiceSpec.builder()
                .taskTemplate(TaskSpec.builder().build())
                .build();

        when(service_1.id()).thenReturn("service-id-1");
        when(service_1.spec()).thenReturn(service1Spec);

        when(service_2.id()).thenReturn("service-id-2");
        when(service_2.spec()).thenReturn(service2Spec);

        when(dockerClient.listNodes()).thenReturn(nodeList);
        when(dockerClient.listTasks()).thenReturn(taskList);
        when(dockerClient.listServices()).thenReturn(services);

        final SwarmCluster swarmCluster = new SwarmCluster(dockerClient);

        verify(dockerClient, times(1)).listNodes();
        verify(dockerClient, times(1)).listTasks();

        assertThat(swarmCluster.getNodes(), hasSize(1));
        assertThat(swarmCluster.getNodes().get(0).getTasks(), hasSize(1));
    }

    @Test
    public void shouldSortNodesBasedOnLeaderRoleAndHostName() throws Exception {
        final DockerClient dockerClient = mock(DockerClient.class);
        final Node nodeA = mockNode("A", "worker", false);
        final Node nodeB = mockNode("B", "manager", false);
        final Node nodeC = mockNode("C", "manager", false);
        final Node nodeD = mockNode("D", "worker", false);
        final Node leaderNode = mockNode("E", "manager", true);
        final Node nodeF = mockNode("F", "worker", false);
        final Node nodeG = mockNode("G", "manager", false);


        final List<Node> nodeList = Arrays.asList(nodeB, leaderNode, nodeA, nodeC, nodeD, nodeF, nodeG);

        when(dockerClient.listNodes()).thenReturn(nodeList);
        when(dockerClient.listContainers(withStatusRunning(), withStatusCreated())).thenReturn(Collections.emptyList());

        final SwarmCluster swarmCluster = new SwarmCluster(dockerClient);

        assertThat(swarmCluster.getNodes().get(0).getHostname(), is(leaderNode.description().hostname()));
        assertThat(swarmCluster.getNodes().get(1).getHostname(), is(nodeB.description().hostname()));
        assertThat(swarmCluster.getNodes().get(2).getHostname(), is(nodeC.description().hostname()));
        assertThat(swarmCluster.getNodes().get(3).getHostname(), is(nodeG.description().hostname()));
        assertThat(swarmCluster.getNodes().get(4).getHostname(), is(nodeA.description().hostname()));
        assertThat(swarmCluster.getNodes().get(5).getHostname(), is(nodeD.description().hostname()));
        assertThat(swarmCluster.getNodes().get(6).getHostname(), is(nodeF.description().hostname()));
    }

    private Node mockNode(String hostname, String role, boolean leader) {
        final Node node = mock(Node.class);
        final ManagerStatus managerStatus = mock(ManagerStatus.class);

        when(node.id()).thenReturn(UUID.randomUUID().toString());
        when(node.status()).thenReturn(mock(NodeStatus.class));
        when(node.spec()).thenReturn(NodeSpec.builder().name("node-name").availability("active").role(role).build());
        when(node.managerStatus()).thenReturn(managerStatus);
        when(managerStatus.leader()).thenReturn(leader);

        final NodeDescription nodeDescription = mock(NodeDescription.class);
        when(node.description()).thenReturn(nodeDescription);
        when(nodeDescription.hostname()).thenReturn(hostname);
        when(nodeDescription.engine()).thenReturn(mock(EngineConfig.class));
        when(nodeDescription.resources()).thenReturn(mock(Resources.class));
        when(nodeDescription.platform()).thenReturn(mock(Platform.class));

        when(node.createdAt()).thenReturn(new Date());
        when(node.updatedAt()).thenReturn(new Date());

        return node;
    }

    private Task mockTask(String nodeId, String serviceId) {
        final Task task = mock(Task.class);
        final ContainerSpec containerSpec = ContainerSpec.builder().image("gocd-agent:latest").build();
        final TaskSpec taskSpec = TaskSpec.builder().containerSpec(containerSpec).build();
        final TaskStatus taskStatus = mock(TaskStatus.class);
        final Date createdAt = new Date();

        when(task.id()).thenReturn("task-id");
        when(task.createdAt()).thenReturn(createdAt);
        when(task.spec()).thenReturn(taskSpec);
        when(task.nodeId()).thenReturn(nodeId);
        when(task.serviceId()).thenReturn(serviceId);
        when(task.status()).thenReturn(taskStatus);
        when(taskStatus.state()).thenReturn("running");
        return task;
    }
}