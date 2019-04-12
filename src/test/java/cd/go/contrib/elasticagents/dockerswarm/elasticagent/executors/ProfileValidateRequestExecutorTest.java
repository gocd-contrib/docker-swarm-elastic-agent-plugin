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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.PluginRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.PluginSettings;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.PluginSettingsNotConfiguredException;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ProfileValidateRequest;
import com.spotify.docker.client.DockerClient;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProfileValidateRequestExecutorTest {

    private DockerClientFactory dockerClientFactory;
    private PluginRequest pluginRequest;
    private PluginSettings pluginSettings;
    private DockerClient dockerClient;

    @Before
    public void setUp() throws Exception {
        dockerClientFactory = mock(DockerClientFactory.class);
        pluginRequest = mock(PluginRequest.class);
        pluginSettings = mock(PluginSettings.class);
        dockerClient = mock(DockerClient.class);

        when(pluginRequest.getPluginSettings()).thenReturn(pluginSettings);
        when(dockerClientFactory.docker(pluginSettings)).thenReturn(dockerClient);
    }

    @Test
    public void shouldBarfWhenUnknownKeysArePassed() throws Exception {
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(Collections.singletonMap("foo", "bar")), null);
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Image must not be blank.\",\"key\":\"Image\"},{\"key\":\"foo\",\"message\":\"Is an unknown property.\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidateMandatoryKeys() throws Exception {
        ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(Collections.<String, String>emptyMap()), null);
        String json = executor.execute().responseBody();
        JSONAssert.assertEquals("[{\"message\":\"Image must not be blank.\",\"key\":\"Image\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void dockerVolumeMountValidatorShouldErrorOutWhenPluginSettingsNotConfigured() throws Exception {
        final HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine");
        properties.put("Mounts", "src=Foo, target=Bar");

        when(pluginRequest.getPluginSettings()).thenThrow(new PluginSettingsNotConfiguredException());

        final GoPluginApiResponse response = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties), pluginRequest).execute();

        JSONAssert.assertEquals("[{\"message\":\"Plugin settings is not configured.\",\"key\":\"Mounts\"}]", response.responseBody(), true);
    }

}
