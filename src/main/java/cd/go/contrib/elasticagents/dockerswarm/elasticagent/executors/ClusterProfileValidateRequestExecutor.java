package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationError;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationResult;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ClusterProfileValidateRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.validator.PrivateDockerRegistrySettingsValidator;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.*;

public class ClusterProfileValidateRequestExecutor implements RequestExecutor {
    private ClusterProfileValidateRequest request;

    public ClusterProfileValidateRequestExecutor(ClusterProfileValidateRequest request) {
        this.request = request;
    }

    public GoPluginApiResponse execute() {
        final List<String> knownFields = new ArrayList<>();
        final ValidationResult validationResult = new ValidationResult();

        for (Metadata field : GetClusterProfileMetadataExecutor.FIELDS) {
            knownFields.add(field.getKey());
            validationResult.addError(field.validate(request.getProperties().get(field.getKey())));
        }
        final Set<String> set = new HashSet<>(request.getProperties().keySet());
        set.removeAll(knownFields);

        if (!set.isEmpty()) {
            for (String key : set) {
                validationResult.addError(key, "Is an unknown property.");
            }
        }
        List<Map<String, String>> validateErrors = new PrivateDockerRegistrySettingsValidator().validate(request);
        validateErrors.forEach(error -> validationResult.addError(new ValidationError(error.get("key"), error.get("message"))));
        return DefaultGoPluginApiResponse.success(validationResult.toJSON());
    }

}
