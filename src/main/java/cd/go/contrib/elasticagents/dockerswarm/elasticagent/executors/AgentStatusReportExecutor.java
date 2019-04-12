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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.*;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.reports.agent.DockerServiceElasticAgent;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.reports.StatusReportGenerationErrorHandler;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.reports.StatusReportGenerationException;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.AgentStatusReportRequest;
import com.google.gson.JsonObject;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.swarm.Service;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;

public class AgentStatusReportExecutor {
    private final AgentStatusReportRequest request;
    private final PluginRequest pluginRequest;
    private final AgentInstances<DockerService> agentInstances;
    private final DockerClientFactory dockerClientFactory;
    private final PluginStatusReportViewBuilder builder;

    public AgentStatusReportExecutor(AgentStatusReportRequest request, PluginRequest pluginRequest, AgentInstances<DockerService> agentInstances) throws IOException {
        this(request, pluginRequest, agentInstances, DockerClientFactory.instance(), PluginStatusReportViewBuilder.instance());
    }

    public AgentStatusReportExecutor(AgentStatusReportRequest request, PluginRequest pluginRequest, AgentInstances<DockerService> agentInstances, DockerClientFactory dockerClientFactory, PluginStatusReportViewBuilder builder) {
        this.request = request;
        this.pluginRequest = pluginRequest;
        this.agentInstances = agentInstances;
        this.dockerClientFactory = dockerClientFactory;
        this.builder = builder;
    }

    public GoPluginApiResponse execute() throws Exception {
        String elasticAgentId = request.getElasticAgentId();
        JobIdentifier jobIdentifier = request.getJobIdentifier();
        LOG.info(String.format("[status-report] Generating status report for agent: %s with job: %s", elasticAgentId, jobIdentifier));

        try {
            final DockerClient dockerClient = dockerClientFactory.docker(request.getClusterProfileProperties());
            Service dockerService = findService(elasticAgentId, jobIdentifier, dockerClient);

            DockerServiceElasticAgent elasticAgent = DockerServiceElasticAgent.fromService(dockerService, dockerClient);
            final String statusReportView = builder.build(builder.getTemplate("agent-status-report.template.ftlh"), elasticAgent);

            JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        } catch (Exception e) {
            return StatusReportGenerationErrorHandler.handle(builder, e);
        }
    }

    private Service findService(String elasticAgentId, JobIdentifier jobIdentifier, DockerClient dockerClient) throws Exception {
        Service dockerService;
        if (StringUtils.isNotBlank(elasticAgentId)) {
            dockerService = findServiceUsingElasticAgentId(elasticAgentId, dockerClient);
        } else {
            dockerService = findServiceUsingJobIdentifier(jobIdentifier, dockerClient);
        }
        return dockerService;
    }

    private Service findServiceUsingJobIdentifier(JobIdentifier jobIdentifier, DockerClient client) {
        try {
            return client.listServices(Service.Criteria.builder().addLabel(Constants.JOB_IDENTIFIER_LABEL_KEY, jobIdentifier.toJson()).build()).get(0);
        } catch (Exception e) {
            throw StatusReportGenerationException.noRunningService(jobIdentifier);
        }
    }

    private Service findServiceUsingElasticAgentId(String elasticAgentId, DockerClient client) throws Exception {
        for (Service service : client.listServices()) {
            if (service.spec().name().equals(elasticAgentId) || service.id().equals(elasticAgentId)) {
                return service;
            }
        }
        throw StatusReportGenerationException.noRunningService(elasticAgentId);
    }
}
