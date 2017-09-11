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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.SwarmCluster;
import com.spotify.docker.client.DockerClient;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatusReportExecutorTest {

    @Test
    public void shouldBuildStatusReportView() throws Exception {
        final DockerClient dockerClient = mock(DockerClient.class);
        final PluginStatusReportViewBuilder builder = mock(PluginStatusReportViewBuilder.class);
        final Template template = mock(Template.class);

        when(builder.getTemplate("status-report.template.ftlh")).thenReturn(template);
        when(builder.build(eq(template), any(SwarmCluster.class))).thenReturn("status-report");

        final GoPluginApiResponse response = new StatusReportExecutor(dockerClient, builder).execute();

        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("{\"view\":\"status-report\"}"));
    }
}