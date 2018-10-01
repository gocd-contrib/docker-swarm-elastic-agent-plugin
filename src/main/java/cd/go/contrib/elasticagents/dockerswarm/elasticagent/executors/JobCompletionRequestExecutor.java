package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.*;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.JobCompletionRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Arrays;
import java.util.List;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;
import static java.text.MessageFormat.format;

public class JobCompletionRequestExecutor implements RequestExecutor {
    private final JobCompletionRequest jobCompletionRequest;
    private final AgentInstances<DockerService> agentInstances;
    private final PluginRequest pluginRequest;

    public JobCompletionRequestExecutor(JobCompletionRequest jobCompletionRequest, AgentInstances<DockerService> agentInstances, PluginRequest pluginRequest) {
        this.jobCompletionRequest = jobCompletionRequest;
        this.agentInstances = agentInstances;
        this.pluginRequest = pluginRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        PluginSettings pluginSettings = pluginRequest.getPluginSettings();

        String elasticAgentId = jobCompletionRequest.getElasticAgentId();

        Agent agent = new Agent();
        agent.elasticAgentId(elasticAgentId);

        LOG.info(format("[Job Completion] Terminating elastic agent with id {0} on job completion {1}.", agent.elasticAgentId(), jobCompletionRequest.jobIdentifier()));

        List<Agent> agents = Arrays.asList(agent);
        pluginRequest.disableAgents(agents);
        agentInstances.terminate(agent.elasticAgentId(), pluginSettings);
        pluginRequest.deleteAgents(agents);

        return DefaultGoPluginApiResponse.success("");
    }
}
