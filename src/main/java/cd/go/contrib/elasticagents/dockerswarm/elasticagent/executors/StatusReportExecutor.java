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
import com.google.gson.JsonObject;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;

public class StatusReportExecutor {
    private final DockerClient dockerClient;
    private final PluginStatusReportViewBuilder statusReportViewBuilder;

    public StatusReportExecutor(DockerClient dockerClient) throws IOException {
        this(dockerClient, PluginStatusReportViewBuilder.instance());
    }

    StatusReportExecutor(DockerClient dockerClient, PluginStatusReportViewBuilder statusReportViewBuilder) {
        this.dockerClient = dockerClient;
        this.statusReportViewBuilder = statusReportViewBuilder;
    }

    public GoPluginApiResponse execute() throws DockerException, InterruptedException, IOException, TemplateException {
        LOG.info("[status-report] Generating status report");

        final SwarmCluster swarmCluster = new SwarmCluster(dockerClient);
        final Template template = statusReportViewBuilder.getTemplate("status-report.template.ftlh");
        final String statusReportView = statusReportViewBuilder.build(template, swarmCluster);

        JsonObject responseJSON = new JsonObject();
        responseJSON.addProperty("view", statusReportView);

        return DefaultGoPluginApiResponse.success(responseJSON.toString());
    }
}
