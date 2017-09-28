/*
 * Copyright 2016 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.PluginRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationResult;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ProfileValidateRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.validator.DockerMountsValidator;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.validator.DockerSecretValidator;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.validator.Validatable;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileValidateRequestExecutor implements RequestExecutor {
    private final ProfileValidateRequest request;
    private List<Validatable> validators = new ArrayList<>();

    public ProfileValidateRequestExecutor(ProfileValidateRequest request, PluginRequest pluginRequest) {
        this.request = request;
        validators.add(new DockerSecretValidator(pluginRequest));
        validators.add(new DockerMountsValidator(pluginRequest));
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        final List<String> knownFields = new ArrayList<>();
        final ValidationResult validationResult = new ValidationResult();

        for (Metadata field : GetProfileMetadataExecutor.FIELDS) {
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

        for (Validatable validatable : validators) {
            validationResult.merge(validatable.validate(request.getProperties()));
        }

        return DefaultGoPluginApiResponse.success(validationResult.toJSON());
    }
}
