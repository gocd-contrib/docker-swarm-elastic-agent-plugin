package cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.ClusterProfile;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.ElasticAgentProfile;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.PluginSettings;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors.MigrateConfigurationRequestExecutor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class MigrateConfigurationRequest {
    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    @Expose
    @SerializedName("plugin_settings")
    private PluginSettings pluginSettings;

    @Expose
    @SerializedName("cluster_profiles")
    private List<ClusterProfile> clusterProfiles;

    @Expose
    @SerializedName("elastic_agent_profiles")
    private List<ElasticAgentProfile> elasticAgentProfiles;

    public MigrateConfigurationRequest() {
    }

    public MigrateConfigurationRequest(PluginSettings pluginSettings, List<ClusterProfile> clusterProfiles, List<ElasticAgentProfile> elasticAgentProfiles) {
        this.pluginSettings = pluginSettings;
        this.clusterProfiles = clusterProfiles;
        this.elasticAgentProfiles = elasticAgentProfiles;
    }

    public static MigrateConfigurationRequest fromJSON(String requestBody) {
        MigrateConfigurationRequest request = GSON.fromJson(requestBody, MigrateConfigurationRequest.class);
        return request;
    }

    public String toJSON() {
        return GSON.toJson(this);
    }

    public MigrateConfigurationRequestExecutor executor() {
        return new MigrateConfigurationRequestExecutor(this);
    }

    public PluginSettings getPluginSettings() {
        return pluginSettings;
    }

    public void setPluginSettings(PluginSettings pluginSettings) {
        this.pluginSettings = pluginSettings;
    }

    public List<ClusterProfile> getClusterProfiles() {
        return clusterProfiles;
    }

    public void setClusterProfiles(List<ClusterProfile> clusterProfiles) {
        this.clusterProfiles = clusterProfiles;
    }

    public List<ElasticAgentProfile> getElasticAgentProfiles() {
        return elasticAgentProfiles;
    }

    public void setElasticAgentProfiles(List<ElasticAgentProfile> elasticAgentProfiles) {
        this.elasticAgentProfiles = elasticAgentProfiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MigrateConfigurationRequest that = (MigrateConfigurationRequest) o;
        return Objects.equals(pluginSettings, that.pluginSettings) &&
                Objects.equals(clusterProfiles, that.clusterProfiles) &&
                Objects.equals(elasticAgentProfiles, that.elasticAgentProfiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginSettings, clusterProfiles, elasticAgentProfiles);
    }

    @Override
    public String toString() {
        return "MigrateConfigurationRequest{" +
                "pluginSettings=" + pluginSettings +
                ", clusterProfiles=" + clusterProfiles +
                ", elasticAgentProfiles=" + elasticAgentProfiles +
                '}';
    }

}
