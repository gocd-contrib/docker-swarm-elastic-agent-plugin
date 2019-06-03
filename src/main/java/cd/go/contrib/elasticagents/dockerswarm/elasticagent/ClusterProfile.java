package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Objects;

public class ClusterProfile {
    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("plugin_id")
    private String pluginId;

    @Expose
    @SerializedName("properties")
    private ClusterProfileProperties clusterProfileProperties;


    public ClusterProfile() {
    }

    public ClusterProfile(String id, String pluginId, PluginSettings pluginSettings) {
        this.id = id;
        this.pluginId = pluginId;
        setClusterProfileProperties(pluginSettings);
    }

    public static ClusterProfile fromJSON(String json) {
        return GSON.fromJson(json, ClusterProfile.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterProfile that = (ClusterProfile) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(clusterProfileProperties, that.clusterProfileProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pluginId, clusterProfileProperties);
    }

    @Override
    public String toString() {
        return "ClusterProfile{" +
                "id='" + id + '\'' +
                ", pluginId='" + pluginId + '\'' +
                ", clusterProfileProperties=" + clusterProfileProperties +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getPluginId() {
        return pluginId;
    }

    public ClusterProfileProperties getClusterProfileProperties() {
        return clusterProfileProperties;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }
    public void setClusterProfileProperties(ClusterProfileProperties clusterProfileProperties) {
        this.clusterProfileProperties = clusterProfileProperties;
    }

    public void setClusterProfileProperties(PluginSettings pluginSettings) {
        this.clusterProfileProperties = ClusterProfileProperties.fromConfiguration(GSON.fromJson(GSON.toJson(pluginSettings), HashMap.class));
    }

}
