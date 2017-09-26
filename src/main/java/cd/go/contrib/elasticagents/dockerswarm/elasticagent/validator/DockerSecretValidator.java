/*
 * Copyright 2017 ThoughtWorks, Inc.
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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.validator;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerSecrets;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.PluginRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationResult;
import com.spotify.docker.client.DockerClient;

import java.util.Map;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util.dockerApiVersionAtLeast;

public class DockerSecretValidator implements Validatable {
    private final PluginRequest pluginRequest;
    private final DockerClientFactory dockerClientFactory;

    public DockerSecretValidator(PluginRequest pluginRequest) {
        this(pluginRequest, DockerClientFactory.instance());
    }

    DockerSecretValidator(PluginRequest pluginRequest, DockerClientFactory dockerClientFactory) {
        this.pluginRequest = pluginRequest;
        this.dockerClientFactory = dockerClientFactory;
    }

    @Override
    public ValidationResult validate(Map<String, String> elasticProfile) {
        final ValidationResult validationResult = new ValidationResult();

        try {
            final DockerSecrets dockerSecrets = DockerSecrets.fromString(elasticProfile.get("Secrets"));

            if (!dockerSecrets.isEmpty()) {
                DockerClient dockerClient = dockerClientFactory.docker(pluginRequest.getPluginSettings());
                if (!dockerApiVersionAtLeast(dockerClient, "1.26")) {
                    throw new RuntimeException("Docker secret requires api version 1.26 or higher.");
                }
                dockerSecrets.toSecretBind(dockerClient.listSecrets());
            }
        } catch (Exception e) {
            validationResult.addError("Secrets", e.getMessage());
        }

        return validationResult;
    }
}
