package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import java.util.Map;
import java.util.Objects;

public class ClusterProfileProperties extends PluginSettings {
    public static ClusterProfileProperties fromJSON(String json) {
        return GSON.fromJson(json, ClusterProfileProperties.class);
    }

    public static ClusterProfileProperties fromConfiguration(Map<String, String> clusterProfileProperties) {
        return GSON.fromJson(GSON.toJson(clusterProfileProperties), ClusterProfileProperties.class);
    }

    public String uuid() {
        return Integer.toHexString(Objects.hash(this));
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
