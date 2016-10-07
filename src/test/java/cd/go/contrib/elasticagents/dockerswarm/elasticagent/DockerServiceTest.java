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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.CreateAgentRequest;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.swarm.Service;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DockerServiceTest extends BaseTest {

    private CreateAgentRequest request;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "gocdcontrib/ubuntu-docker-elastic-agent");
        request = new CreateAgentRequest("key", properties, "production");
    }

    @Test
    public void shouldCreateService() throws Exception {
        DockerService dockerService = DockerService.create(request, createSettings(), docker);
        services.add(dockerService.name());
        assertServiceExist(dockerService.name());
    }

    @Test
    public void shouldNotCreateServiceIfTheImageIsNotProvided() throws Exception {
        CreateAgentRequest request = new CreateAgentRequest("key", new HashMap<String, String>(), "production");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Must provide `Image` attribute.");

        DockerService.create(request, createSettings(), docker);
    }

    @Test
    public void shouldStartServiceWithCorrectEnvironment() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");
        properties.put("Environment", "A=B\nC=D\r\nE=F\n\n\nX=Y");

        PluginSettings settings = createSettings();
        settings.setEnvironmentVariables("GLOBAL=something");
        DockerService service = DockerService.create(new CreateAgentRequest("key", properties, "prod"), settings, docker);
        services.add(service.name());

        Service serviceInfo = docker.inspectService(service.name());

        assertThat(serviceInfo.spec().taskTemplate().containerSpec().env(), hasItems("A=B", "C=D", "E=F", "X=Y", "GLOBAL=something"));
        DockerService dockerService = DockerService.fromService(serviceInfo);

        assertThat(dockerService.properties().get("Environment"), is(properties.get("Environment")));
    }

    @Test
    public void shouldStartContainerWithAutoregisterEnvironmentVariables() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");

        DockerService service = DockerService.create(new CreateAgentRequest("key", properties, "prod"), createSettings(), docker);
        services.add(service.name());
        Service serviceInfo = docker.inspectService(service.name());
        assertThat(serviceInfo.spec().taskTemplate().containerSpec().env(), hasItem("GO_EA_AUTO_REGISTER_KEY=key"));
        assertThat(serviceInfo.spec().taskTemplate().containerSpec().env(), hasItem("GO_EA_AUTO_REGISTER_ENVIRONMENT=prod"));
        assertThat(serviceInfo.spec().taskTemplate().containerSpec().env(), hasItem("GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID=" + service.name()));
        assertThat(serviceInfo.spec().taskTemplate().containerSpec().env(), hasItem("GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID=" + Constants.PLUGIN_ID));
    }

    @Test
    public void shouldStartContainerWithCorrectCommand() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");
        properties.put("Command", "cat\n/etc/hosts\n/etc/group");

        DockerService service = DockerService.create(new CreateAgentRequest("key", properties, "prod"), createSettings(), docker);
        services.add(service.name());
        Service serviceInfo = docker.inspectService(service.name());
        assertThat(serviceInfo.spec().taskTemplate().containerSpec().command(), is(Arrays.asList("cat", "/etc/hosts", "/etc/group")));
        Thread.sleep(7000);
        List<Container> containers = docker.listContainers(DockerClient.ListContainersParam.withLabel("com.docker.swarm.service.name", service.name()), DockerClient.ListContainersParam.allContainers());
        String logs = docker.logs(containers.get(0).id(), DockerClient.LogsParam.stdout()).readFully();
        assertThat(logs, containsString("127.0.0.1")); // from /etc/hosts
        assertThat(logs, containsString("floppy:x:19:")); // from /etc/group
    }

    @Test
    public void shouldStartContainerWithCorrectMemoryLimit() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");
        properties.put("Memory", "512MB");

        DockerService service = DockerService.create(new CreateAgentRequest("key", properties, "prod"), createSettings(), docker);
        services.add(service.name());
        Service serviceInfo = docker.inspectService(service.name());
        assertThat(serviceInfo.spec().taskTemplate().resources().limits().memoryBytes(), is(512*1024*1024L));
    }

    @Test
    public void shouldTerminateAnExistingService() throws Exception {
        DockerService dockerService = DockerService.create(request, createSettings(), docker);
        services.add(dockerService.name());

        dockerService.terminate(docker);

        assertServiceDoesNotExist(dockerService.name());
    }

    @Test
    public void shouldFindAnExistingService() throws Exception {
        DockerService service = DockerService.create(request, createSettings(), docker);
        services.add(service.name());

        DockerService dockerService = DockerService.fromService(docker.inspectService(service.name()));

        assertEquals(service, dockerService);
    }
}
