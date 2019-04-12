package cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.ClusterProfileProperties;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerServices;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors.ClusterStatusReportExecutor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class ClusterStatusReportRequest {
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    @Expose
    @SerializedName("cluster_profile_properties")
    private ClusterProfileProperties clusterProfile;

    public ClusterStatusReportRequest() {
    }

    public ClusterStatusReportRequest(Map<String, String> clusterProfileConfigurations) {
        this.clusterProfile = ClusterProfileProperties.fromConfiguration(clusterProfileConfigurations);
    }

    public ClusterProfileProperties getClusterProfile() {
        return clusterProfile;
    }

    public static ClusterStatusReportRequest fromJSON(String json) {
        return GSON.fromJson(json, ClusterStatusReportRequest.class);
    }

    public ClusterStatusReportExecutor executor(DockerServices dockerServices) throws IOException {
        return new ClusterStatusReportExecutor(this, dockerServices);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterStatusReportRequest that = (ClusterStatusReportRequest) o;
        return Objects.equals(clusterProfile, that.clusterProfile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterProfile);
    }

    @Override
    public String toString() {
        return "ClusterStatusReportRequest{" +
                "clusterProfile=" + clusterProfile +
                '}';
    }

}
