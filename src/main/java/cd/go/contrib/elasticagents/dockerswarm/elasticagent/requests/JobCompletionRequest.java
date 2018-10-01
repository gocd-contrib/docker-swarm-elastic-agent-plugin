package cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.AgentInstances;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerService;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.PluginRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors.JobCompletionRequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.JobIdentifier;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util.GSON;

public class JobCompletionRequest {
    @Expose
    @SerializedName("elastic_agent_id")
    private String elasticAgentId;
    @Expose
    @SerializedName("job_identifier")
    private JobIdentifier jobIdentifier;

    public JobCompletionRequest() {
    }

    public JobCompletionRequest(String elasticAgentId, JobIdentifier jobIdentifier) {
        this.elasticAgentId = elasticAgentId;
        this.jobIdentifier = jobIdentifier;
    }

    public static JobCompletionRequest fromJSON(String json) {
        JobCompletionRequest jobCompletionRequest = GSON.fromJson(json, JobCompletionRequest.class);
        return jobCompletionRequest;
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }

    public JobIdentifier jobIdentifier() {
        return jobIdentifier;
    }

    public RequestExecutor executor(AgentInstances<DockerService> agentInstances, PluginRequest pluginRequest) {
        return new JobCompletionRequestExecutor(this, agentInstances, pluginRequest);
    }

    @Override
    public String toString() {
        return "JobCompletionRequest{" +
                "elasticAgentId='" + elasticAgentId + '\'' +
                ", jobIdentifier=" + jobIdentifier +
                '}';
    }
}
