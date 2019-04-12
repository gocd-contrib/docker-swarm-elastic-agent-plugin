package cd.go.contrib.elasticagents.dockerswarm.elasticagent.validator;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors.Metadata;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ClusterProfileValidateRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors.GetClusterProfileMetadataExecutor.*;
import static org.apache.commons.lang.StringUtils.isBlank;

public class PrivateDockerRegistrySettingsValidator {

    public List<Map<String, String>> validate(ClusterProfileValidateRequest request) {
        final List<Map<String, String>> result = new ArrayList<>();
        final boolean useDockerAuthInfo = Boolean.valueOf(request.getProperties().get(ENABLE_PRIVATE_REGISTRY_AUTHENTICATION.getKey()));
        if (!useDockerAuthInfo) {
            return result;
        }

        validate(PRIVATE_REGISTRY_SERVER, request, result);
        validate(PRIVATE_REGISTRY_USERNAME, request, result);
        validate(PRIVATE_REGISTRY_PASSWORD, request, result);
        return result;
    }

    private void validate(Metadata field, ClusterProfileValidateRequest request, List<Map<String, String>> errorResult) {
        if (isBlank(request.getProperties().get(field.getKey()))) {
            Map<String, String> result = new HashMap<>();
            result.put("key", field.getKey());
            result.put("message", field.getKey()+ " must not be blank.");
            errorResult.add(result);
        }
    }
}
