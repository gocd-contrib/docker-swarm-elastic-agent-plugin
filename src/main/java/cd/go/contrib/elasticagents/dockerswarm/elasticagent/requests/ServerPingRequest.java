package cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.ClusterProfileProperties;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerServices;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.PluginRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors.ServerPingRequestExecutor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerPingRequest {
    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private List<ClusterProfileProperties> allClusterProfileProperties = new ArrayList<>();

    public ServerPingRequest() {
    }

    public ServerPingRequest(List<Map<String, String>> allClusterProfileProperties) {
        this.allClusterProfileProperties = allClusterProfileProperties.stream()
                .map(ClusterProfileProperties::fromConfiguration)
                .collect(Collectors.toList());
    }

    public List<ClusterProfileProperties> allClusterProfileProperties() {
        return allClusterProfileProperties;
    }

    public static ServerPingRequest fromJSON(String json) {
        return GSON.fromJson(json, ServerPingRequest.class);
    }

    @Override
    public String toString() {
        return "ServerPingRequest{" +
                "allClusterProfileProperties=" + allClusterProfileProperties +
                '}';
    }

    public ServerPingRequestExecutor executor(Map<String, DockerServices> clusterSpecificAgentInstances, PluginRequest pluginRequest) {
        return new ServerPingRequestExecutor(this, clusterSpecificAgentInstances, pluginRequest);
    }

}
