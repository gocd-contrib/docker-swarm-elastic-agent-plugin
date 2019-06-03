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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetCapabilitiesExecutor {

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    private static final Map<String, Boolean> CAPABILITIES_RESPONSE = new LinkedHashMap<>();

    static {
        CAPABILITIES_RESPONSE.put("supports_plugin_status_report", false);
        CAPABILITIES_RESPONSE.put("supports_agent_status_report", true);
        CAPABILITIES_RESPONSE.put("supports_cluster_status_report", true);
    }
    public GoPluginApiResponse execute() {
        return DefaultGoPluginApiResponse.success(GSON.toJson(CAPABILITIES_RESPONSE));
    }
}
