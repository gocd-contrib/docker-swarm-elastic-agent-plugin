package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ClusterProfileValidateRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ClusterProfileValidateRequestExecutorTest {

    @Test
    public void shouldValidateABadConfiguration() throws Exception {
        HashMap<String, String> settings = new HashMap<>();
        ClusterProfileValidateRequest request = new ClusterProfileValidateRequest(settings);
        GoPluginApiResponse response = new ClusterProfileValidateRequestExecutor(request).execute();

        assertThat(response.responseCode(), is(200));
        String expectedJSON = "[" +
                "{\"key\":\"go_server_url\",\"message\":\"Go Server URL must not be blank.\"}," +
                "{\"key\":\"auto_register_timeout\",\"message\":\"auto_register_timeout must not be blank.\"}," +
                "{\"key\":\"docker_uri\",\"message\":\"docker_uri must not be blank.\"}," +
                "{\"key\":\"max_docker_containers\",\"message\":\"max_docker_containers must not be blank.\"}]";
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }


    @Test
    public void shouldValidateAGoodConfiguration() throws Exception {
        HashMap<String, String> properties = new HashMap<>();
        ClusterProfileValidateRequest request = new ClusterProfileValidateRequest(properties);

        properties.put("max_docker_containers", "1");
        properties.put("docker_uri", "https://api.example.com");
        properties.put("docker_ca_cert", "some ca cert");
        properties.put("docker_client_key", "some client key");
        properties.put("docker_client_cert", "some client cert");
        properties.put("go_server_url", "https://ci.example.com/go");
        properties.put("auto_register_timeout", "10");
        properties.put("enable_private_registry_authentication", "false");
        GoPluginApiResponse response = new ClusterProfileValidateRequestExecutor(request).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[]", response.responseBody(), true);
    }

    @Test
    public void shouldValidateAConfigurationWithAllPrivateRegistryInfos() throws Exception {
        HashMap<String, String> properties = new HashMap<>();
        ClusterProfileValidateRequest request = new ClusterProfileValidateRequest(properties);
        properties.put("max_docker_containers", "1");
        properties.put("docker_uri", "https://api.example.com");
        properties.put("docker_ca_cert", "some ca cert");
        properties.put("docker_client_key", "some client key");
        properties.put("docker_client_cert", "sone client cert");
        properties.put("go_server_url", "https://ci.example.com/go");
        properties.put("enable_private_registry_authentication", "true");
        properties.put("private_registry_server", "server");
        properties.put("private_registry_username", "username");
        properties.put("private_registry_password", "password");
        properties.put("auto_register_timeout", "10");
        GoPluginApiResponse response = new ClusterProfileValidateRequestExecutor(request).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[]", response.responseBody(), true);
    }

    @Test
    public void shouldNotValidateAConfigurationWithInvalidPrivateRegistrySettings() throws Exception {
        HashMap<String, String> properties = new HashMap<>();
        ClusterProfileValidateRequest request = new ClusterProfileValidateRequest(properties);
        properties.put("max_docker_containers", "1");
        properties.put("docker_uri", "https://api.example.com");
        properties.put("docker_ca_cert", "some ca cert");
        properties.put("docker_client_key", "some client key");
        properties.put("docker_client_cert", "sone client cert");
        properties.put("go_server_url", "https://ci.example.com/go");
        properties.put("enable_private_registry_authentication", "true");
        properties.put("private_registry_server", "");
        properties.put("private_registry_username", "");
        properties.put("private_registry_password", "");
        properties.put("auto_register_timeout", "10");
        GoPluginApiResponse response = new ClusterProfileValidateRequestExecutor(request).execute();
        String expectedJSON = "[" +
                "{\"key\":\"private_registry_server\",\"message\":\"private_registry_server must not be blank.\"}," +
                "{\"key\":\"private_registry_password\",\"message\":\"private_registry_password must not be blank.\"}," +
                "{\"key\":\"private_registry_username\",\"message\":\"private_registry_username must not be blank.\"}" +
                "]";
        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }
}
