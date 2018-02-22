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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.PluginRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.reports.SwarmCluster;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.reports.StatusReportGenerationErrorHandler;
import com.google.gson.JsonObject;
import com.spotify.docker.client.DockerClient;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;

import java.io.IOException;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;

public class StatusReportExecutor {
    private final PluginRequest pluginRequest;
    private final DockerClientFactory dockerClientFactory;
    private final PluginStatusReportViewBuilder statusReportViewBuilder;

    public StatusReportExecutor(PluginRequest pluginRequest) throws IOException {
        this(pluginRequest, DockerClientFactory.instance(), PluginStatusReportViewBuilder.instance());
    }

    StatusReportExecutor(PluginRequest pluginRequest, DockerClientFactory dockerClientFactory, PluginStatusReportViewBuilder statusReportViewBuilder) {
        this.pluginRequest = pluginRequest;
        this.dockerClientFactory = dockerClientFactory;
        this.statusReportViewBuilder = statusReportViewBuilder;
    }

    public GoPluginApiResponse execute() {
        try {
            LOG.info("[status-report] Generating status report");

            final DockerClient dockerClient = dockerClientFactory.docker(pluginRequest.getPluginSettings());
            final SwarmCluster swarmCluster = new SwarmCluster(dockerClient);
            final Template template = statusReportViewBuilder.getTemplate("status-report.template.ftlh");
            final String statusReportView = statusReportViewBuilder.build(template, swarmCluster);

            JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        } catch (Exception e) {
            return StatusReportGenerationErrorHandler.handle(statusReportViewBuilder, e);
        }
    }
}
