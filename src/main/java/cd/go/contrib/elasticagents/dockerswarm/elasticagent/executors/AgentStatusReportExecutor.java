package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.Constants;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.PluginRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.reports.agent.DockerServiceElasticAgent;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.reports.agent.ExceptionMessage;
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
    private final DockerClientFactory dockerClientFactory;
    private final PluginStatusReportViewBuilder builder;

    public AgentStatusReportExecutor(AgentStatusReportRequest request, PluginRequest pluginRequest) throws IOException {
        this(request, pluginRequest, DockerClientFactory.instance(), PluginStatusReportViewBuilder.instance());
    }

    public AgentStatusReportExecutor(AgentStatusReportRequest request, PluginRequest pluginRequest, DockerClientFactory dockerClientFactory, PluginStatusReportViewBuilder builder) {
        this.request = request;
        this.pluginRequest = pluginRequest;
        this.dockerClientFactory = dockerClientFactory;
        this.builder = builder;
    }

    public GoPluginApiResponse execute() throws Exception {
        String elasticAgentId = request.getElasticAgentId();
        JobIdentifier jobIdentifier = request.getJobIdentifier();
        LOG.info(String.format("[status-report] Generating status report for agent: %s with job: %s", elasticAgentId, jobIdentifier));
        final DockerClient dockerClient = dockerClientFactory.docker(pluginRequest.getPluginSettings());

        try {
            Service dockerService;

            if (StringUtils.isNotBlank(elasticAgentId)) {
                dockerService = findPodUsingElasticAgentId(elasticAgentId, dockerClient);
            } else {
                dockerService = findPodUsingJobIdentifier(jobIdentifier, dockerClient);
            }

            DockerServiceElasticAgent elasticAgent = DockerServiceElasticAgent.fromService(dockerService, dockerClient);
            final String statusReportView = builder.build(builder.getTemplate("agent-status-report.template.ftlh"), elasticAgent);

            JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        } catch (Exception e) {
            final String statusReportView = builder.build(builder.getTemplate("error.template.ftlh"), new ExceptionMessage(e));

            JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        }
    }

    private Service findPodUsingJobIdentifier(JobIdentifier jobIdentifier, DockerClient client) {
        try {
            return client.listServices(Service.Criteria.builder().addLabel(Constants.JOB_IDENTIFIER_LABEL_KEY, jobIdentifier.toJson()).build()).get(0);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Can not find a running service for the provided job identifier '%s'", jobIdentifier));
        }
    }

    private Service findPodUsingElasticAgentId(String elasticAgentId, DockerClient client) throws Exception {
        for (Service service : client.listServices()) {
            if (service.spec().name().equals(elasticAgentId) || service.id().equals(elasticAgentId)) {
                return service;
            }
        }

        throw new RuntimeException(String.format("Can not find a running service for the provided elastic agent id '%s'", elasticAgentId));
    }
}
