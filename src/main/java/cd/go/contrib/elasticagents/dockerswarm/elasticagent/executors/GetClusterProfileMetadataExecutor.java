package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.RequestExecutor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.List;

public class GetClusterProfileMetadataExecutor implements RequestExecutor {
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static final Metadata GO_SERVER_URL = new GoServerURLMetadata();
    public static final Metadata ENVIRONMENT_VARIABLES = new Metadata("environment_variables", false, false);
    public static final Metadata MAX_DOCKER_CONTAINERS = new Metadata("max_docker_containers", true, false);
    public static final Metadata DOCKER_URI = new Metadata("docker_uri", true, false);
    public static final Metadata AUTO_REGISTER_TIMEOUT = new Metadata("auto_register_timeout", true, false);
    public static final Metadata DOCKER_CA_CERT = new Metadata("docker_ca_cert", false, true);
    public static final Metadata DOCKER_CLIENT_KEY = new Metadata("docker_client_key", false, true);
    public static final Metadata DOCKER_CLIENT_CERT = new Metadata("docker_client_cert", false, true);
    public static final Metadata ENABLE_PRIVATE_REGISTRY_AUTHENTICATION = new Metadata("enable_private_registry_authentication", false, false);
    public static final Metadata PRIVATE_REGISTRY_SERVER = new Metadata("private_registry_server", false, false);
    public static final Metadata PRIVATE_REGISTRY_USERNAME = new Metadata("private_registry_username", false, false);
    public static final Metadata PRIVATE_REGISTRY_PASSWORD = new Metadata("private_registry_password", false, true);

    public static final List<Metadata> FIELDS = new ArrayList<>();

    static {
        FIELDS.add(GO_SERVER_URL);
        FIELDS.add(ENVIRONMENT_VARIABLES);
        FIELDS.add(MAX_DOCKER_CONTAINERS);
        FIELDS.add(DOCKER_URI);
        FIELDS.add(AUTO_REGISTER_TIMEOUT);

        // certs
        FIELDS.add(DOCKER_CA_CERT);
        FIELDS.add(DOCKER_CLIENT_KEY);
        FIELDS.add(DOCKER_CLIENT_CERT);

        FIELDS.add(ENABLE_PRIVATE_REGISTRY_AUTHENTICATION);
        FIELDS.add(PRIVATE_REGISTRY_SERVER);
        FIELDS.add(PRIVATE_REGISTRY_USERNAME);
        FIELDS.add(PRIVATE_REGISTRY_PASSWORD);
    }

    @Override
    public GoPluginApiResponse execute() {
        return new DefaultGoPluginApiResponse(200, GSON.toJson(FIELDS));
    }
}
