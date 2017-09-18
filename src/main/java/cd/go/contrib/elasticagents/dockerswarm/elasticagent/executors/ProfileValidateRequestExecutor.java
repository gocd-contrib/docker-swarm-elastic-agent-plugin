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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerSecrets;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ProfileValidateRequest;
import com.google.gson.Gson;
import com.spotify.docker.client.DockerClient;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.*;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util.dockerApiVersionAtLeast;

public class ProfileValidateRequestExecutor implements RequestExecutor {
    private final ProfileValidateRequest request;
    private final DockerClient dockerClient;
    private static final Gson GSON = new Gson();

    public ProfileValidateRequestExecutor(ProfileValidateRequest request, DockerClient dockerClient) {
        this.request = request;
        this.dockerClient = dockerClient;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        final List<Map<String, String>> result = new ArrayList<>();
        final List<String> knownFields = new ArrayList<>();

        for (Metadata field : GetProfileMetadataExecutor.FIELDS) {
            knownFields.add(field.getKey());
            Map<String, String> validationError = field.validate(request.getProperties().get(field.getKey()));

            if (!validationError.isEmpty()) {
                result.add(validationError);
            }
        }


        final Set<String> set = new HashSet<>(request.getProperties().keySet());
        set.removeAll(knownFields);

        if (!set.isEmpty()) {
            for (String key : set) {
                LinkedHashMap<String, String> validationError = new LinkedHashMap<>();
                validationError.put("key", key);
                validationError.put("message", "Is an unknown property");
                result.add(validationError);
            }
        }

        validateDockerSecrets(result);

        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }

    private void validateDockerSecrets(List<Map<String, String>> result) {
        try {
            final DockerSecrets dockerSecrets = DockerSecrets.fromString(request.getProperties().get("Secrets"));
            if (!dockerSecrets.isEmpty()) {
                if (!dockerApiVersionAtLeast(dockerClient, "1.26")) {
                    throw new RuntimeException("Docker secret requires api version 1.26 or higher.");
                }
                dockerSecrets.toSecretBind(dockerClient.listSecrets());
            }
        } catch (Exception e) {
            LinkedHashMap<String, String> validationError = new LinkedHashMap<>();
            validationError.put("key", "Secrets");
            validationError.put("message", e.getMessage());
            result.add(validationError);
        }
    }
}
