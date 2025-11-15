/*
 * Copyright 2022 Thoughtworks, Inc.
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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerMounts;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationResult;

import java.util.Map;

public class DockerMountsValidator implements Validatable {
    @Override
    public ValidationResult validate(Map<String, String> elasticProfile) {
        final ValidationResult validationResult = new ValidationResult();

        try {
            final DockerMounts dockerMounts = DockerMounts.fromString(elasticProfile.get("Mounts"));

            if (!dockerMounts.isEmpty()) {
                dockerMounts.toMount();
            }
        } catch (Exception e) {
            validationResult.addError("Mounts", e.getMessage());
        }

        return validationResult;
    }
}
