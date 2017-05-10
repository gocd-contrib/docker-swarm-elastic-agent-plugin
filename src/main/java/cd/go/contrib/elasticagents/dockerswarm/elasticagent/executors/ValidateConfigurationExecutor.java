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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ValidatePluginSettingsRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.validator.PrivateDockerRegistrySettingsValidator;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.Map;

public class ValidateConfigurationExecutor implements RequestExecutor {
    private static final Gson GSON = new Gson();

    private final ValidatePluginSettingsRequest request;

    public ValidateConfigurationExecutor(ValidatePluginSettingsRequest request) {
        this.request = request;
    }

    public GoPluginApiResponse execute() {
        ArrayList<Map<String, String>> result = new ArrayList<>();

        for (Map.Entry<String, Field> entry : GetPluginConfigurationExecutor.FIELDS.entrySet()) {
            Field field = entry.getValue();
            Map<String, String> validationError = field.validate(request.get(entry.getKey()));

            if (!validationError.isEmpty()) {
                result.add(validationError);
            }
        }

        result.addAll(new PrivateDockerRegistrySettingsValidator().validate(request));

        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }
}
