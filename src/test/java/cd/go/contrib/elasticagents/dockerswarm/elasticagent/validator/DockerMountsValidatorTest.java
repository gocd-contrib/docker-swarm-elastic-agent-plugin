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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.ClusterProfileProperties;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationError;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationResult;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.CreateAgentRequest;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Version;
import com.spotify.docker.client.messages.swarm.Secret;
import com.spotify.docker.client.messages.swarm.SecretSpec;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DockerMountsValidatorTest {

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
    public void shouldValidateValidSecretConfiguration() throws Exception {
        final Version version = mock(Version.class);
        final HashMap<String, String> properties = new HashMap<>();
        final Secret secret = mock(Secret.class);
        properties.put("Image", "alpine");
        properties.put("Secrets", "src=Foo");

        when(version.apiVersion()).thenReturn("1.27");
        when(dockerClient.version()).thenReturn(version);
        when(dockerClient.listSecrets()).thenReturn(asList(secret));
        when(secret.secretSpec()).thenReturn(SecretSpec.builder().name("Foo").build());
        when(secret.id()).thenReturn("service-id");

        ValidationResult validationResult = new DockerSecretValidator(createAgentRequest, dockerClientFactory).validate(properties);

        assertFalse(validationResult.hasErrors());
    }

    @Test
    public void shouldValidateInvalidDockerSecretsConfiguration() throws Exception {
        final Version version = mock(Version.class);
        final HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine");
        properties.put("Secrets", "Foo");

        when(version.apiVersion()).thenReturn("1.27");
        when(dockerClient.version()).thenReturn(version);
        when(dockerClientFactory.docker(any(ClusterProfileProperties.class))).thenReturn(dockerClient);

        ValidationResult validationResult = new DockerSecretValidator(createAgentRequest, null).validate(properties);

        assertTrue(validationResult.hasErrors());
        assertThat(validationResult.allErrors(), contains(new ValidationError("Secrets", "Invalid secret specification `Foo`. Must specify property `src` with value.")));
    }

}
