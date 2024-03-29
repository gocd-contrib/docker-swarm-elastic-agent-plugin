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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.CreateAgentRequest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.spotify.docker.client.messages.Volume;
import com.spotify.docker.client.messages.mount.Mount;
import com.spotify.docker.client.messages.swarm.*;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DockerServiceElasticAgentTest extends BaseTest {

    private CreateAgentRequest request;

    private JobIdentifier jobIdentifier;
    private String environment;

    @BeforeEach
    public void setUp() {
        HashMap<String, String> elasticAgentProperties = new HashMap<>();
        HashMap<String, String> clusterProfileProperties = new HashMap<>();
        elasticAgentProperties.put("Image", "alpine:latest");
        jobIdentifier = new JobIdentifier(100L);
        environment = "production";
        request = new CreateAgentRequest("key", elasticAgentProperties, environment, jobIdentifier, clusterProfileProperties);
    }

    @Test
    public void shouldCreateService() throws Exception {
        DockerService dockerService = DockerService.create(request, createClusterProfiles(), docker);
        services.add(dockerService.name());
        assertServiceExist(dockerService.name());
    }

    @Test
    public void shouldCreateServiceForTheJobId() throws Exception {
        DockerService dockerService = DockerService.create(request, createClusterProfiles(), docker);
        services.add(dockerService.name());
        assertThat(dockerService.jobIdentifier(), is(jobIdentifier));
    }

    @Test
    public void shouldNotCreateServiceIfTheImageIsNotProvided() throws Exception {
        CreateAgentRequest request = new CreateAgentRequest("key", new HashMap<>(), "environment", new JobIdentifier(100L), new HashMap<>());

        assertThatThrownBy(() -> DockerService.create(request, createClusterProfiles(), docker))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Must provide `Image` attribute.");
    }

    @Test
    public void shouldStartServiceWithCorrectLabel() throws Exception {
        DockerService dockerService = DockerService.create(request, createClusterProfiles(), docker);
        services.add(dockerService.name());
        assertServiceExist(dockerService.name());

        Service serviceInfo = docker.inspectService(dockerService.name());
        ImmutableMap<String, String> labels = serviceInfo.spec().labels();

        assertThat(labels.get(Constants.JOB_IDENTIFIER_LABEL_KEY), is(jobIdentifier.toJson()));
        assertThat(labels.get(Constants.ENVIRONMENT_LABEL_KEY), is(environment));
        assertThat(labels.get(Constants.CREATED_BY_LABEL_KEY), is(Constants.PLUGIN_ID));
        assertThat(labels.get(Constants.CONFIGURATION_LABEL_KEY), is(new Gson().toJson(request.properties())));
    }

    @Test
    public void shouldStartServiceWithCorrectEnvironment() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine:latest");
        properties.put("Environment", "A=B\nC=D\r\nE=F\n\n\nX=Y");

        PluginSettings settings = createClusterProfiles();
        settings.setEnvironmentVariables("GLOBAL=something");
        DockerService service = DockerService.create(new CreateAgentRequest("key", properties, "prod", new JobIdentifier(100L), new HashMap<>()), settings, docker);
        services.add(service.name());

        Service serviceInfo = docker.inspectService(service.name());

        assertThat(serviceInfo.spec().taskTemplate().containerSpec().env(), hasItems("A=B", "C=D", "E=F", "X=Y", "GLOBAL=something"));
        DockerService dockerService = DockerService.fromService(serviceInfo);

        assertThat(dockerService.properties().get("Environment"), is(properties.get("Environment")));
    }

    @Test
    public void shouldStartContainerWithAutoregisterEnvironmentVariables() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine:latest");

        DockerService service = DockerService.create(new CreateAgentRequest("key", properties, "prod", new JobIdentifier(100L), new HashMap<>()), createClusterProfiles(), docker);
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
        properties.put("Image", "alpine:latest");
        List<String> command = Arrays.asList("/bin/sh", "-c", "cat /etc/hosts /etc/group");
        properties.put("Command", StringUtils.join(command, "\n"));

        DockerService service = DockerService.create(new CreateAgentRequest("key", properties, "prod", new JobIdentifier(100L), new HashMap<>()), createClusterProfiles(), docker);
        services.add(service.name());
        Service serviceInfo = docker.inspectService(service.name());

        assertThat(serviceInfo.spec().taskTemplate().containerSpec().command(), is(command));
    }

    @Test
    public void shouldStartContainerWithCorrectMemoryLimit() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine:latest");
        properties.put("MaxMemory", "512MB");
        properties.put("ReservedMemory", "100MB");

        DockerService service = DockerService.create(new CreateAgentRequest("key", properties, "prod", new JobIdentifier(100L), new HashMap<>()), createClusterProfiles(), docker);
        services.add(service.name());
        Service serviceInfo = docker.inspectService(service.name());
        assertThat(serviceInfo.spec().taskTemplate().resources().limits().memoryBytes(), is(512 * 1024 * 1024L));
        assertThat(serviceInfo.spec().taskTemplate().resources().reservations().memoryBytes(), is(100 * 1024 * 1024L));
    }

    @Test
    public void shouldStartContainerWithHostEntry() throws Exception {
        requireDockerApiVersionAtLeast("1.26", "Swarm host entry support");

        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine:latest");
        properties.put("Hosts", "127.0.0.1 foo bar\n 127.0.0.2 baz");

        DockerService service = DockerService.create(new CreateAgentRequest("key", properties, "prod", new JobIdentifier(100L), new HashMap<>()), createClusterProfiles(), docker);
        services.add(service.name());

        final Service inspectServiceInfo = docker.inspectService(service.name());
        assertThat(inspectServiceInfo.spec().taskTemplate().containerSpec().hosts(), contains("127.0.0.1 foo", "127.0.0.1 bar", "127.0.0.2 baz"));
    }

    @Test
    public void shouldStartContainerWithMountedVolume() throws Exception {
        requireDockerApiVersionAtLeast("1.26", "Docker volume mount.");

        final String volumeName = UUID.randomUUID().toString();

        final Volume volume = docker.createVolume(Volume.builder()
                .name(volumeName)
                .driver("local")
                .labels(Collections.singletonMap("cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin", ""))
                .build()
        );

        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine:latest");
        properties.put("Mounts", "source=" + volumeName + ", target=/path/in/container");

        DockerService service = DockerService.create(new CreateAgentRequest("key", properties, "prod", new JobIdentifier(100L), new HashMap<>()), createClusterProfiles(), docker);
        services.add(service.name());

        final Service inspectServiceInfo = docker.inspectService(service.name());
        final Mount mount = inspectServiceInfo.spec().taskTemplate().containerSpec().mounts().get(0);

        assertThat(mount.source(), is(volumeName));
        assertThat(mount.type(), is("volume"));
    }

    @Test
    public void shouldTerminateAnExistingService() throws Exception {
        DockerService dockerService = DockerService.create(request, createClusterProfiles(), docker);
        services.add(dockerService.name());

        dockerService.terminate(docker);

        assertServiceDoesNotExist(dockerService.name());
    }

    @Test
    public void shouldFindAnExistingService() throws Exception {
        DockerService service = DockerService.create(request, createClusterProfiles(), docker);
        services.add(service.name());

        DockerService dockerService = DockerService.fromService(docker.inspectService(service.name()));

        assertEquals(service, dockerService);
    }

    @Test
    public void shouldFindAnExistingServiceWithJobIdInformation() throws Exception {
        DockerService service = DockerService.create(request, createClusterProfiles(), docker);
        services.add(service.name());
        assertThat(service.jobIdentifier(), is(jobIdentifier));

        DockerService dockerService = DockerService.fromService(docker.inspectService(service.name()));

        assertThat(service.jobIdentifier(), is(jobIdentifier));
        assertEquals(service, dockerService);
    }

    @Test
    public void shouldStartContainerWithSecret() throws Exception {
        requireDockerApiVersionAtLeast("1.26", "Swarm secret support");

        final String secretName = UUID.randomUUID().toString();
        final SecretCreateResponse secret = docker.createSecret(SecretSpec.builder()
                .name(secretName)
                .data(Base64.getEncoder().encodeToString("some-random-junk".getBytes()))
                .labels(Collections.singletonMap("cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin", ""))
                .build()
        );

        final List<String> command = Arrays.asList("/bin/sh", "-c", "cat /run/secrets/" + secretName);
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine:latest");
        properties.put("Secrets", "src=" + secretName);
        properties.put("Command", StringUtils.join(command, "\n"));

        DockerService service = DockerService.create(new CreateAgentRequest("key", properties, "prod", new JobIdentifier(100L), new HashMap<>()), createClusterProfiles(), docker);
        services.add(service.name());

        final Service inspectService = docker.inspectService(service.name());
        final SecretBind secretBind = inspectService.spec().taskTemplate().containerSpec().secrets().get(0);

        assertThat(secretBind.secretName(), is(secretName));
        assertThat(secretBind.secretId(), is(secret.id()));
    }

    @Test
    public void shouldCreateServiceWithConstraints() throws Exception {
        final List<Node> nodes = docker.listNodes();
        final String nodeId = nodes.get(0).id();
        final Map<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine:latest");
        properties.put("Constraints", format("node.id == %s", nodeId));

        DockerService service = DockerService.create(new CreateAgentRequest("key", properties, "prod", new JobIdentifier(100L), new HashMap<>()), createClusterProfiles(), docker);
        services.add(service.name());

        final Service inspectService = docker.inspectService(service.name());
        final ImmutableList<String> constraints = inspectService.spec().taskTemplate().placement().constraints();

        assertThat(constraints, contains(format("node.id == %s", nodeId)));
    }
}
