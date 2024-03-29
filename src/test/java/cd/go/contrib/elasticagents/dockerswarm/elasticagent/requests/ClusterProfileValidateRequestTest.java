package cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests;


import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClusterProfileValidateRequestTest {
    @Test
    public void shouldDeserializeFromJSON() {

        String json ="{" +
                "   \"go_server_url\":\"http://localhost\"," +
                "   \"auto_register_timeout\":\"10\"," +
                "   \"username\":\"Bob\"," +
                "   \"password\":\"secret\"" +
                "}";
        ClusterProfileValidateRequest request = ClusterProfileValidateRequest.fromJSON(json);
        HashMap<String, String> expectedSettings = new HashMap<>();
        expectedSettings.put("go_server_url", "http://localhost");
        expectedSettings.put("auto_register_timeout", "10");
        expectedSettings.put("username", "Bob");
        expectedSettings.put("password", "secret");
        assertThat(request.getProperties(), equalTo(expectedSettings));
    }
}
