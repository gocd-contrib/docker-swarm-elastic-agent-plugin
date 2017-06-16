package cd.go.contrib.elasticagents.dockerswarm.elasticagent.validator;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors.Field;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ValidatePluginSettingsRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors.GetPluginConfigurationExecutor.*;
import static org.apache.commons.lang.StringUtils.isBlank;

public class PrivateDockerRegistrySettingsValidator {

    public List<Map<String, String>> validate(ValidatePluginSettingsRequest request) {
        final List<Map<String, String>> result = new ArrayList<>();
        final boolean useDockerAuthInfo = Boolean.valueOf(request.get(ENABLE_PRIVATE_REGISTRY_AUTHENTICATION.key()));

        if (!useDockerAuthInfo) {
            return result;
        }

        validate(PRIVATE_REGISTRY_SERVER, request, result);
        validate(PRIVATE_REGISTRY_USERNAME, request, result);
        validate(PRIVATE_REGISTRY_PASSWORD, request, result);

        return result;
    }

    private void validate(Field field, ValidatePluginSettingsRequest request, List<Map<String, String>> errorResult) {
        if (isBlank(request.get(field.key()))) {
            Map<String, String> result = new HashMap<>();
            result.put("key", field.key());
            result.put("message", field.displayName() + " must not be blank.");
            errorResult.add(result);
        }
    }
}
