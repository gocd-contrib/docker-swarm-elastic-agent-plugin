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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.validator;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.*;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationError;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationResult;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.CreateAgentRequest;
import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Version;
import com.spotify.docker.client.messages.Volume;
import com.spotify.docker.client.messages.VolumeList;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DockerSecretValidatorTest {
    private DockerClientFactory dockerClientFactory;
    private CreateAgentRequest createAgentRequest;
    private DockerClient dockerClient;

    @Before
    public void setUp() throws Exception {
        dockerClientFactory = mock(DockerClientFactory.class);
        createAgentRequest = mock(CreateAgentRequest.class);
        ClusterProfileProperties clusterProfileProperties = mock(ClusterProfileProperties.class);
        dockerClient = mock(DockerClient.class);

        when(createAgentRequest.getClusterProfileProperties()).thenReturn(clusterProfileProperties);
        when(dockerClientFactory.docker(clusterProfileProperties)).thenReturn(dockerClient);
    }

    @Test
    public void shouldValidateValidVolumeMountConfiguration() throws Exception {
        final Version version = mock(Version.class);
        final HashMap<String, String> properties = new HashMap<>();
        final VolumeList volumeList = mock(VolumeList.class);
        properties.put("Image", "alpine");
        properties.put("Mounts", "src=Foo, target=Bar");

        when(version.apiVersion()).thenReturn("1.27");
        when(dockerClient.version()).thenReturn(version);
        when(dockerClient.listVolumes()).thenReturn(volumeList);
        when(volumeList.volumes()).thenReturn(new ImmutableList.Builder<Volume>().add(Volume.builder().name("Foo").build()).build());

        ValidationResult validationResult = new DockerMountsValidator(createAgentRequest, dockerClientFactory).validate(properties);

        assertFalse(validationResult.hasErrors());
    }

    @Test
    public void shouldValidateDockerApiVersionForDockerMountSupport() throws Exception {
        final Version version = mock(Version.class);
        final HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine");
        properties.put("Mounts", "src=Foo, target=Bar");

        when(version.apiVersion()).thenReturn("1.25");
        when(dockerClient.version()).thenReturn(version);

        ValidationResult validationResult = new DockerMountsValidator(createAgentRequest, dockerClientFactory).validate(properties);

        assertTrue(validationResult.hasErrors());
        assertThat(validationResult.allErrors(), contains(new ValidationError("Mounts", "Docker volume mount requires api version 1.26 or higher.")));
    }

    @Test
    public void shouldValidateInvalidDockerSecretsConfiguration() throws Exception {
        final Version version = mock(Version.class);
        final HashMap<String, String> properties = new HashMap<>();
        final VolumeList volumeList = mock(VolumeList.class);
        properties.put("Image", "alpine");
        properties.put("Mounts", "src=Foo");

        when(version.apiVersion()).thenReturn("1.27");
        when(dockerClient.version()).thenReturn(version);
        when(dockerClient.listVolumes()).thenReturn(volumeList);
        when(volumeList.volumes()).thenReturn(new ImmutableList.Builder<Volume>().add(Volume.builder().name("Foo").build()).build());

        ValidationResult validationResult = new DockerMountsValidator(createAgentRequest, dockerClientFactory).validate(properties);

        assertTrue(validationResult.hasErrors());
        assertThat(validationResult.allErrors(), contains(new ValidationError("Mounts", "Invalid mount target specification `src=Foo`. `target` has to be specified.")));
    }

    @Test
    public void shouldValidateErrorOutWhenPluginSettingsNotConfigured() {
        final HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine");
        properties.put("Mounts", "src=Foo, target=Bar");

        when(createAgentRequest.getClusterProfileProperties()).thenThrow(new PluginSettingsNotConfiguredException());

        ValidationResult validationResult = new DockerMountsValidator(createAgentRequest, dockerClientFactory).validate(properties);

        assertTrue(validationResult.hasErrors());
        assertThat(validationResult.allErrors(), contains(new ValidationError("Mounts", "Plugin settings is not configured.")));
    }
}
