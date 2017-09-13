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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ProfileValidateRequest;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Version;
import com.spotify.docker.client.messages.swarm.Secret;
import com.spotify.docker.client.messages.swarm.SecretSpec;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Collections;
import java.util.HashMap;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProfileValidateRequestExecutorTest {
    @Test
    public void shouldBarfWhenUnknownKeysArePassed() throws Exception {
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(Collections.singletonMap("foo", "bar")), null);
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Image must not be blank.\",\"key\":\"Image\"},{\"key\":\"foo\",\"message\":\"Is an unknown property\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidateMandatoryKeys() throws Exception {
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(Collections.<String, String>emptyMap()), null);
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Image must not be blank.\",\"key\":\"Image\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidateInvalidDockerSecretsConfiguration() throws Exception {
        final DockerClient dockerClient = mock(DockerClient.class);
        final Version version = mock(Version.class);
        final HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine");
        properties.put("Secrets", "Foo");

        when(version.apiVersion()).thenReturn("1.27");
        when(dockerClient.version()).thenReturn(version);

        GoPluginApiResponse response = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties), dockerClient).execute();

        assertThat(response.responseCode(), is(200));

        final String expectedJson = "[\n" +
                "  {\n" +
                "    \"key\": \"Secrets\",\n" +
                "    \"message\": \"Invalid secret specification `Foo`. Must specify property `src` with value.\"\n" +
                "  }\n" +
                "]";

        JSONAssert.assertEquals(expectedJson, response.responseBody(), true);
    }


    @Test
    public void shouldValidateValidSecretConfiguration() throws Exception {
        final DockerClient dockerClient = mock(DockerClient.class);
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

        GoPluginApiResponse response = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties), dockerClient).execute();

        assertThat(response.responseCode(), is(200));

        JSONAssert.assertEquals("[]", response.responseBody(), true);
    }

    @Test
    public void shouldValidateDockerApiVersionForDockerSecretSupport() throws Exception {
        final DockerClient dockerClient = mock(DockerClient.class);
        final Version version = mock(Version.class);
        final HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine");
        properties.put("Secrets", "src=Foo");

        when(version.apiVersion()).thenReturn("1.25");
        when(dockerClient.version()).thenReturn(version);

        GoPluginApiResponse response = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties), dockerClient).execute();

        assertThat(response.responseCode(), is(200));

        final String expectedJson = "[\n" +
                "  {\n" +
                "    \"key\": \"Secrets\",\n" +
                "    \"message\": \"Docker secret requires api version 1.26 or higher.\"\n" +
                "  }\n" +
                "]";

        JSONAssert.assertEquals(expectedJson, response.responseBody(), true);
    }
}