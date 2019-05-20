/*
 * Copyright 2016 ThoughtWorks, Inc.
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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.CreateAgentRequest;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DockerServicesTestElasticAgent extends BaseTest {

    private CreateAgentRequest request;
    private DockerServices dockerServices;
    private ClusterProfileProperties clusterProfile;
    private JobIdentifier jobIdentifier;
    private PluginRequest pluginRequest;

    @Before
    public void setUp() throws Exception {
        pluginRequest = mock(PluginRequest.class);
        jobIdentifier = new JobIdentifier(100L);
        HashMap<String, String> elasticAgentProperties = new HashMap<>();
        elasticAgentProperties.put("Image", "alpine:latest");
        elasticAgentProperties.put("Command", "/bin/sleep\n5");
        clusterProfile = createClusterProfiles();
        request = new CreateAgentRequest("key", elasticAgentProperties, "production", jobIdentifier, clusterProfile);
        dockerServices = new DockerServices();
    }

    @Test
    public void shouldCreateADockerInstance() throws Exception {
        DockerService dockerService = dockerServices.create(request, pluginRequest);
        services.add(dockerService.name());
        assertServiceExist(dockerService.name());
    }

    @Test
    public void shouldTerminateAnExistingContainer() throws Exception {
        DockerService dockerService = dockerServices.create(request, pluginRequest);
        services.add(dockerService.name());

        dockerServices.terminate(dockerService.name(), clusterProfile);

        assertServiceDoesNotExist(dockerService.name());
    }

    @Test
    public void shouldRefreshAllAgentInstancesAtStartUp() throws Exception {
        DockerService dockerService = DockerService.create(request, clusterProfile, docker);
        services.add(dockerService.name());

        DockerServices dockerServices = new DockerServices();
        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createClusterProfiles());
        dockerServices.refreshAll(clusterProfile);
        assertThat(dockerServices.find(dockerService.name()), is(dockerService));
    }

    @Test
    public void shouldNotRefreshAllAgentInstancesAgainAfterTheStartUp() throws Exception {
        DockerServices dockerServices = new DockerServices();
        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createClusterProfiles());
        dockerServices.refreshAll(clusterProfile);

        DockerService dockerService = DockerService.create(request, clusterProfile, docker);
        services.add(dockerService.name());

        dockerServices.refreshAll(clusterProfile);

        assertEquals(dockerServices.find(dockerService.name()), null);
    }

    @Test
    public void shouldNotListTheServiceIfItIsCreatedBeforeTimeout() throws Exception {
        DockerService dockerService = DockerService.create(request, clusterProfile, docker);
        services.add(dockerService.name());

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createClusterProfiles());

        dockerServices.clock = new Clock.TestClock().forward(Period.minutes(9));
        dockerServices.refreshAll(clusterProfile);

        Agents filteredDockerContainers = dockerServices.instancesCreatedAfterTimeout(createClusterProfiles(), new Agents(Arrays.asList(new Agent(dockerService.name(), null, null, null))));

        assertFalse(filteredDockerContainers.containsServiceWithId(dockerService.name()));
    }

    @Test
    public void shouldListTheContainerIfItIsNotCreatedBeforeTimeout() throws Exception {
        DockerService dockerService = DockerService.create(request, clusterProfile, docker);
        services.add(dockerService.name());

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createClusterProfiles());

        dockerServices.clock = new Clock.TestClock().forward(Period.minutes(11));
        dockerServices.refreshAll(clusterProfile);

        Agents filteredDockerContainers = dockerServices.instancesCreatedAfterTimeout(createClusterProfiles(), new Agents(Arrays.asList(new Agent(dockerService.name(), null, null, null))));

        assertTrue(filteredDockerContainers.containsServiceWithId(dockerService.name()));
    }

    @Test
    public void shouldNotCreateContainersIfMaxLimitIsReached() throws Exception {

        HashMap<String, String> elasticAgentProperties = new HashMap<>();
        elasticAgentProperties.put("Image", "alpine:latest");
        // do not allow any containers
        clusterProfile.setMaxDockerContainers(0);
        CreateAgentRequest createAgentRequest = new CreateAgentRequest("key", elasticAgentProperties, "production", jobIdentifier, clusterProfile);
        DockerService dockerService = dockerServices.create(createAgentRequest, pluginRequest);
        if (dockerService != null) {
            services.add(dockerService.name());
        }
        assertNull(dockerService);

        // allow only one container
        clusterProfile.setMaxDockerContainers(1);
        dockerService = dockerServices.create(createAgentRequest, pluginRequest);
        if (dockerService != null) {
            services.add(dockerService.name());
        }
        assertNotNull(dockerService);

        dockerService = dockerServices.create(createAgentRequest, pluginRequest);
        if (dockerService != null) {
            services.add(dockerService.name());
        }
        assertNull(dockerService);
    }

    @Test
    public void shouldTerminateUnregistredContainersAfterTimeout() throws Exception {
        DockerService dockerService = dockerServices.create(request, pluginRequest);

        assertTrue(dockerServices.hasInstance(dockerService.name()));
        dockerServices.clock = new Clock.TestClock().forward(Period.minutes(11));
        dockerServices.terminateUnregisteredInstances(createClusterProfiles(), new Agents());
        assertFalse(dockerServices.hasInstance(dockerService.name()));
        assertServiceDoesNotExist(dockerService.name());
    }

    @Test
    public void shouldNotTerminateUnregistredServiceBeforeTimeout() throws Exception {
        DockerService dockerService = dockerServices.create(request, pluginRequest);
        services.add(dockerService.name());

        assertTrue(dockerServices.hasInstance(dockerService.name()));
        dockerServices.clock = new Clock.TestClock().forward(Period.minutes(9));
        dockerServices.terminateUnregisteredInstances(createClusterProfiles(), new Agents());
        assertTrue(dockerServices.hasInstance(dockerService.name()));
        assertServiceExist(dockerService.name());
    }

    @Test
    public void shouldAddServerHealthMessagesIfMaxContainerLimitIsReached() throws Exception {
        HashMap<String, String> elasticAgentProperties = new HashMap<>();
        elasticAgentProperties.put("Image", "alpine:latest");

        clusterProfile.setMaxDockerContainers(0);
        CreateAgentRequest createAgentRequest = new CreateAgentRequest("key", elasticAgentProperties, "production", jobIdentifier, clusterProfile);
        DockerService dockerService = dockerServices.create(createAgentRequest, pluginRequest);
        assertNull(dockerService);
        ArrayList<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("type", "warning");
        message.put("message", "The number of containers currently running is currently at the maximum permissible limit (0). Not creating any more containers.");
        messages.add(message);
        verify(pluginRequest).addServerHealthMessage(messages);
    }
}
