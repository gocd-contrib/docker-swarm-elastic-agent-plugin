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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.validator;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.ClusterProfileProperties;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationError;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationResult;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.CreateAgentRequest;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.swarm.Secret;
import com.spotify.docker.client.messages.swarm.SecretSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DockerSecretValidatorTest {
    private DockerClientFactory dockerClientFactory;
    private CreateAgentRequest createAgentRequest;
    private DockerClient dockerClient;

    @BeforeEach
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
        final HashMap<String, String> properties = new HashMap<>();
        final Secret secret = mock(Secret.class);
        properties.put("Image", "alpine");
        properties.put("Secrets", "src=Foo");

        when(dockerClient.listSecrets()).thenReturn(List.of(secret));
        when(secret.secretSpec()).thenReturn(SecretSpec.builder().name("Foo").build());
        when(secret.id()).thenReturn("service-id");

        ValidationResult validationResult = new DockerSecretValidator(createAgentRequest, dockerClientFactory).validate(properties);

        assertFalse(validationResult.hasErrors());
    }

    @Test
    public void shouldValidateInvalidDockerSecretsConfiguration() throws Exception {
        final HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine");
        properties.put("Secrets", "Foo");

        when(dockerClientFactory.docker(any(ClusterProfileProperties.class))).thenReturn(dockerClient);

        ValidationResult validationResult = new DockerSecretValidator(createAgentRequest, null).validate(properties);

        assertTrue(validationResult.hasErrors());
        assertThat(validationResult.allErrors(), contains(new ValidationError("Secrets", "Invalid secret specification `Foo`. Must specify property `src` with value.")));
    }
}
