/*
 * Copyright 2018 ThoughtWorks, Inc.
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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.ClusterProfileProperties;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerServices;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.reports.SwarmCluster;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ClusterStatusReportRequest;
import com.spotify.docker.client.DockerClient;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClusterStatusReportExecutorTest {

    private DockerClientFactory dockerClientFactory;
    private DockerClient dockerClient;
    private DockerServices dockerServices;
    private ClusterStatusReportRequest clusterStatusReportRequest;
    private ClusterProfileProperties profileProperties;

    @Before
    public void setUp() throws Exception {
        dockerClientFactory = mock(DockerClientFactory.class);
        dockerClient = mock(DockerClient.class);
        dockerServices = mock(DockerServices.class);
        clusterStatusReportRequest = mock(ClusterStatusReportRequest.class);
        profileProperties = new ClusterProfileProperties();
        when(clusterStatusReportRequest.getClusterProfile()).thenReturn(profileProperties);
        when(dockerClientFactory.docker(profileProperties)).thenReturn(dockerClient);
    }

    @Test
    public void shouldBuildStatusReportView() throws Exception {
        final PluginStatusReportViewBuilder builder = mock(PluginStatusReportViewBuilder.class);
        final Template template = mock(Template.class);

        when(builder.getTemplate("status-report.template.ftlh")).thenReturn(template);
        when(builder.build(eq(template), any(SwarmCluster.class))).thenReturn("status-report");
        final GoPluginApiResponse response = new ClusterStatusReportExecutor(clusterStatusReportRequest, dockerServices, dockerClientFactory, builder).execute();

        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("{\"view\":\"status-report\"}"));
    }
}