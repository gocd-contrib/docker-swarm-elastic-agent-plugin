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

import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DockerServicesTestElasticAgent extends BaseTest {

    private CreateAgentRequest request;
    private DockerServices dockerServices;
    private PluginSettings settings;
    private JobIdentifier jobIdentifier;

    @Before
    public void setUp() throws Exception {
        jobIdentifier = new JobIdentifier(100L);
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine:latest");
        request = new CreateAgentRequest("key", properties, "production", jobIdentifier);
        dockerServices = new DockerServices();
        settings = createSettings();
    }

    @Test
    public void shouldCreateADockerInstance() throws Exception {
        DockerService dockerService = dockerServices.create(request, settings);
        services.add(dockerService.name());
        assertServiceExist(dockerService.name());
    }

    @Test
    public void shouldTerminateAnExistingContainer() throws Exception {
        DockerService dockerService = dockerServices.create(request, settings);
        services.add(dockerService.name());

        dockerServices.terminate(dockerService.name(), settings);

        assertServiceDoesNotExist(dockerService.name());
    }

    @Test
    public void shouldRefreshAllAgentInstancesAtStartUp() throws Exception {
        DockerService dockerService = DockerService.create(request, settings, docker);
        services.add(dockerService.name());

        DockerServices dockerServices = new DockerServices();
        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());
        dockerServices.refreshAll(pluginRequest);
        assertThat(dockerServices.find(dockerService.name()), is(dockerService));
    }

    @Test
    public void shouldNotRefreshAllAgentInstancesAgainAfterTheStartUp() throws Exception {
        DockerServices dockerServices = new DockerServices();
        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());
        dockerServices.refreshAll(pluginRequest);

        DockerService dockerService = DockerService.create(request, settings, docker);
        services.add(dockerService.name());

        dockerServices.refreshAll(pluginRequest);

        assertEquals(dockerServices.find(dockerService.name()), null);
    }

    @Test
    public void shouldNotListTheServiceIfItIsCreatedBeforeTimeout() throws Exception {
        DockerService dockerService = DockerService.create(request, settings, docker);
        services.add(dockerService.name());

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());

        dockerServices.clock = new Clock.TestClock().forward(Period.minutes(9));
        dockerServices.refreshAll(pluginRequest);

        Agents filteredDockerContainers = dockerServices.instancesCreatedAfterTimeout(createSettings(), new Agents(Arrays.asList(new Agent(dockerService.name(), null, null, null))));

        assertFalse(filteredDockerContainers.containsServiceWithId(dockerService.name()));
    }

    @Test
    public void shouldListTheContainerIfItIsNotCreatedBeforeTimeout() throws Exception {
        DockerService dockerService = DockerService.create(request, settings, docker);
        services.add(dockerService.name());

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());

        dockerServices.clock = new Clock.TestClock().forward(Period.minutes(11));
        dockerServices.refreshAll(pluginRequest);

        Agents filteredDockerContainers = dockerServices.instancesCreatedAfterTimeout(createSettings(), new Agents(Arrays.asList(new Agent(dockerService.name(), null, null, null))));

        assertTrue(filteredDockerContainers.containsServiceWithId(dockerService.name()));
    }

    @Test
    public void shouldNotCreateContainersIfMaxLimitIsReached() throws Exception {
        PluginSettings settings = createSettings();

        // do not allow any containers
        settings.setMaxDockerContainers(0);

        DockerService dockerService = dockerServices.create(request, settings);
        if (dockerService != null) {
            services.add(dockerService.name());
        }
        assertNull(dockerService);

        // allow only one container
        settings.setMaxDockerContainers(1);
        dockerService = dockerServices.create(request, settings);
        if (dockerService != null) {
            services.add(dockerService.name());
        }
        assertNotNull(dockerService);

        dockerService = dockerServices.create(request, settings);
        if (dockerService != null) {
            services.add(dockerService.name());
        }
        assertNull(dockerService);
    }

    @Test
    public void shouldTerminateUnregistredContainersAfterTimeout() throws Exception {
        DockerService dockerService = dockerServices.create(request, settings);

        assertTrue(dockerServices.hasInstance(dockerService.name()));
        dockerServices.clock = new Clock.TestClock().forward(Period.minutes(11));
        dockerServices.terminateUnregisteredInstances(createSettings(), new Agents());
        assertFalse(dockerServices.hasInstance(dockerService.name()));
        assertServiceDoesNotExist(dockerService.name());
    }

    @Test
    public void shouldNotTerminateUnregistredServiceBeforeTimeout() throws Exception {
        DockerService dockerService = dockerServices.create(request, settings);
        services.add(dockerService.name());

        assertTrue(dockerServices.hasInstance(dockerService.name()));
        dockerServices.clock = new Clock.TestClock().forward(Period.minutes(9));
        dockerServices.terminateUnregisteredInstances(createSettings(), new Agents());
        assertTrue(dockerServices.hasInstance(dockerService.name()));
        assertServiceExist(dockerService.name());
    }
}
