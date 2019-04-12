package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerServices;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.reports.SwarmCluster;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.reports.StatusReportGenerationErrorHandler;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ClusterStatusReportRequest;
import com.google.gson.JsonObject;
import com.spotify.docker.client.DockerClient;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;

import java.io.IOException;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;

public class ClusterStatusReportExecutor {

    private final ClusterStatusReportRequest clusterStatusReportRequest;
    private final DockerServices agentInstances;
    private final DockerClientFactory dockerClientFactory;
    private PluginStatusReportViewBuilder viewBuilder;

    public ClusterStatusReportExecutor(ClusterStatusReportRequest clusterStatusReportRequest, DockerServices agentInstances) throws IOException {
        this(clusterStatusReportRequest, agentInstances, DockerClientFactory.instance(), PluginStatusReportViewBuilder.instance());
    }

    ClusterStatusReportExecutor(ClusterStatusReportRequest clusterStatusReportRequest, DockerServices agentInstances, DockerClientFactory dockerClientFactory, PluginStatusReportViewBuilder viewBuilder) {
        this.clusterStatusReportRequest = clusterStatusReportRequest;
        this.agentInstances = agentInstances;
        this.dockerClientFactory = dockerClientFactory;
        this.viewBuilder = viewBuilder;
    }

    public GoPluginApiResponse execute() {
        try {
            LOG.debug("[status-report] Generating cluster status report.");
            final DockerClient dockerClient = dockerClientFactory.docker(clusterStatusReportRequest.getClusterProfile());
            final SwarmCluster swarmCluster = new SwarmCluster(dockerClient);
            final Template template = viewBuilder.getTemplate("status-report.template.ftlh");
            final String statusReportView = viewBuilder.build(template, swarmCluster);

            JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        } catch (Exception e) {
            return StatusReportGenerationErrorHandler.handle(viewBuilder, e);
        }
    }
}
