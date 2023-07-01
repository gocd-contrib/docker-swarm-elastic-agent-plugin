package cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ClusterStatusReportRequestTest {
    @Test
    public void shouldDeserializeFromJSON() {
        JsonObject jsonObject = new JsonObject();
        JsonObject clusterJSON = new JsonObject();
        clusterJSON.addProperty("go_server_url", "https://go-server/go");
        jsonObject.add("cluster_profile_properties", clusterJSON);

        ClusterStatusReportRequest clusterStatusReportRequest = ClusterStatusReportRequest.fromJSON(jsonObject.toString());

        ClusterStatusReportRequest expected = new ClusterStatusReportRequest(Collections.singletonMap("go_server_url", "https://go-server/go"));
        assertThat(clusterStatusReportRequest, is(expected));
    }
}