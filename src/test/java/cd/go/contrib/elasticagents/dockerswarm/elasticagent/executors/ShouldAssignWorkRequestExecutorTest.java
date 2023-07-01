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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.*;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.CreateAgentRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ShouldAssignWorkRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class ShouldAssignWorkRequestExecutorTest extends BaseTest {

    private DockerServices agentInstances;
    private DockerService instance;
    private final String environment = "production";
    private Map<String, String> properties = new HashMap<>();
    private JobIdentifier jobIdentifier;

    @BeforeEach
    public void setUp() throws Exception {
        jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);
        agentInstances = new DockerServices();
        ClusterProfileProperties clusterProfiles = createClusterProfiles();
        properties.put("foo", "bar");
        properties.put("Image", "alpine:latest");
        instance = agentInstances.create(new CreateAgentRequest(UUID.randomUUID().toString(), properties, environment, jobIdentifier, clusterProfiles), mock(PluginRequest.class));
        services.add(instance.name());
    }

    @Test
    public void shouldAssignWorkToContainerWithMatchingJobId() {
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(instance.name(), null, null, null), environment, properties, jobIdentifier, null);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances).execute();
        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("true"));
    }

    @Test
    public void shouldNotAssignWorkToContainerWithNotMatchingJobId() {
        JobIdentifier mismatchingJobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 999999L);
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(instance.name(), null, null, null), "FooEnv", properties, mismatchingJobIdentifier, null);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances).execute();
        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("false"));
    }
}
