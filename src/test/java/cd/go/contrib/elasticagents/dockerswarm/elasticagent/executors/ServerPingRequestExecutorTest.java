/*
 * Copyright 2022 Thoughtworks, Inc.
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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.*;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.CreateAgentRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ServerPingRequest;
import org.joda.time.Period;
import org.junit.jupiter.api.Test;

import java.util.*;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.Agent.ConfigState.Disabled;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

public class ServerPingRequestExecutorTest extends BaseTest {

    @Test
    public void testShouldDisableAndTerminateDockerServicesRunningAfterTimeout() throws Exception {
        String agentId = UUID.randomUUID().toString();

        ClusterProfileProperties clusterProfileProperties = createClusterProfileProperties();
        Agent agent1 = new Agent(agentId, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled); //idle time elapsed
        Agent agent1AfterDisabling = new Agent(agentId, Agent.AgentState.Idle, Agent.BuildState.Idle, Disabled); //idle time elapsed

        final Agents allAgentsInitially = new Agents(Arrays.asList(agent1));
        final Agents allAgentsAfterDisablingIdleAgents = new Agents(Arrays.asList(agent1AfterDisabling));
        DockerService dockerServiceForAgent1 = new DockerService(agentId, new Date(), null, "", null);
        DockerServices agentInstances = new DockerServices();
        agentInstances.clock = new Clock.TestClock().forward(Period.minutes(11));

        agentInstances.register(dockerServiceForAgent1);

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.listAgents()).thenReturn(allAgentsInitially, allAgentsAfterDisablingIdleAgents, new Agents());

        HashMap<String, DockerServices> clusterSpecificInstances = new HashMap<>();
        clusterSpecificInstances.put(clusterProfileProperties.uuid(), agentInstances);

        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.allClusterProfileProperties()).thenReturn(Collections.singletonList(clusterProfileProperties));
        new ServerPingRequestExecutor(serverPingRequest, clusterSpecificInstances, pluginRequest).execute();

        verify(pluginRequest, atLeastOnce()).disableAgents(Arrays.asList(agent1));
        verify(pluginRequest, atLeastOnce()).deleteAgents(Collections.singletonList(agent1AfterDisabling));
    }

    @Test
    public void testShouldDisableAndTerminateDockerServicesRunningAfterTimeoutOnMultipleClusters() throws Exception {
        String agentId1 = UUID.randomUUID().toString();
        String agentId2 = UUID.randomUUID().toString();

        ClusterProfileProperties clusterProfileProperties1 = createClusterProfileProperties();
        ClusterProfileProperties clusterProfileProperties2 = createClusterProfileProperties();
        clusterProfileProperties2.setMaxDockerContainers(2);
        Agent agent1 = new Agent(agentId1, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled); //idle time elapsed
        Agent agent1AfterDisabling = new Agent(agentId1, Agent.AgentState.Idle, Agent.BuildState.Idle, Disabled); //idle time elapsed

        Agent agent2 = new Agent(agentId2, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled); //idle time elapsed
        Agent agent2AfterDisabling = new Agent(agentId2, Agent.AgentState.Idle, Agent.BuildState.Idle, Disabled); //idle time elapsed

        final Agents allAgentsInitially1 = new Agents(Arrays.asList(agent1, agent2));
        final Agents allAgentsAfter1GotDeleted = new Agents(Arrays.asList(agent2));
        final Agents allAgentsAfterDisablingIdleAgent1 = new Agents(Arrays.asList(agent1AfterDisabling, agent2));
        final Agents allAgentsAfterDisablingIdleAgent2 = new Agents(Arrays.asList(agent2AfterDisabling));

        DockerService dockerServiceForAgent1 = new DockerService(agentId1, new Date(), null, "", null);
        DockerServices agentInstances1 = new DockerServices();
        agentInstances1.clock = new Clock.TestClock().forward(Period.minutes(11));

        agentInstances1.register(dockerServiceForAgent1);

        DockerService dockerServiceForAgent2 = new DockerService(agentId2, new Date(), null, "", null);
        DockerServices agentInstances2 = new DockerServices();
        agentInstances2.clock = new Clock.TestClock().forward(Period.minutes(11));

        agentInstances2.register(dockerServiceForAgent2);

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.listAgents()).thenReturn(allAgentsInitially1,
                allAgentsAfterDisablingIdleAgent1,
                allAgentsAfter1GotDeleted,
                allAgentsAfterDisablingIdleAgent2,
                new Agents());

        HashMap<String, DockerServices> clusterSpecificInstances = new HashMap<>();
        clusterSpecificInstances.put(clusterProfileProperties1.uuid(), agentInstances1);
        clusterSpecificInstances.put(clusterProfileProperties2.uuid(), agentInstances2);

        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.allClusterProfileProperties()).thenReturn(Arrays.asList(clusterProfileProperties1, clusterProfileProperties2));
        new ServerPingRequestExecutor(serverPingRequest, clusterSpecificInstances, pluginRequest).execute();

        verify(pluginRequest, atLeastOnce()).disableAgents(Arrays.asList(agent1));
        verify(pluginRequest, atLeastOnce()).deleteAgents(Arrays.asList(agent1AfterDisabling));

        verify(pluginRequest, atLeastOnce()).disableAgents(Arrays.asList(agent2));
        verify(pluginRequest, atLeastOnce()).deleteAgents(Arrays.asList(agent2AfterDisabling));
    }

    @Test
    public void testShouldTerminateUnregisteredInstances() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.allClusterProfileProperties()).thenReturn(Arrays.asList(createClusterProfiles()));
        when(pluginRequest.listAgents()).thenReturn(new Agents());
        verifyNoMoreInteractions(pluginRequest);
        HashSet<Object> containers = new HashSet<>();

        DockerServices agentInstances = new DockerServices();
        agentInstances.clock = new Clock.TestClock().forward(Period.minutes(11));
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine");
        properties.put("Command", "/bin/sleep\n5");
        CreateAgentRequest createAgentRequest = new CreateAgentRequest(null, properties, null, new JobIdentifier(), createClusterProfiles());
        DockerService container = agentInstances.create(createAgentRequest, pluginRequest);
        containers.add(container.name());

        HashMap<String, DockerServices> dockerContainers = new HashMap<String, DockerServices>() {{
            put(createClusterProfiles().uuid(), agentInstances);
        }};

        new ServerPingRequestExecutor(serverPingRequest, dockerContainers, pluginRequest).execute();

        assertFalse(agentInstances.hasInstance(container.name()));
    }

    @Test
    public void testShouldDeleteAndDisableMissingAgents() throws Exception {
        Agent agentInCluster = new Agent("agent1", Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled); //idle time elapsed
        Agent missingAgent = new Agent("agent2", Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled); //idle just created

        PluginRequest pluginRequest = mock(PluginRequest.class);
        Agents agents = new Agents();
        agents.add(agentInCluster);
        agents.add(missingAgent);
        when(pluginRequest.listAgents()).thenReturn(agents);

        DockerService dockerServiceInCluster = new DockerService("agent1", new Date(), null, null, null);
        DockerServices agentInstances = new DockerServices();
        agentInstances.register(dockerServiceInCluster);
        HashMap<String, DockerServices> dockerServices = new HashMap<String, DockerServices>() {{
            put(createClusterProfiles().uuid(), agentInstances);
        }};

        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.allClusterProfileProperties()).thenReturn(Collections.singletonList(createClusterProfileProperties()));

        new ServerPingRequestExecutor(serverPingRequest, dockerServices, pluginRequest).execute();

        verify(pluginRequest, atLeastOnce()).disableAgents(Arrays.asList(missingAgent));
        verify(pluginRequest, atLeastOnce()).deleteAgents(Collections.singletonList(missingAgent));
    }

}
