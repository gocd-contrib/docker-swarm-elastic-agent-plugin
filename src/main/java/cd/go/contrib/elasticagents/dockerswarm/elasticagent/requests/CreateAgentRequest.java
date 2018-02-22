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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.AgentInstances;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.Constants;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.PluginRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors.CreateAgentRequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.JobIdentifier;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class CreateAgentRequest {
    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private String autoRegisterKey;
    private Map<String, String> properties;
    private String environment;
    private JobIdentifier jobIdentifier;

    public CreateAgentRequest() {
    }

    public CreateAgentRequest(String autoRegisterKey, Map<String, String> properties, String environment, JobIdentifier jobIdentifier) {
        this.autoRegisterKey = autoRegisterKey;
        this.properties = properties;
        this.environment = environment;
        this.jobIdentifier = jobIdentifier;
    }

    public String autoRegisterKey() {
        return autoRegisterKey;
    }

    public Map<String, String> properties() {
        return properties;
    }

    public String environment() {
        return environment;
    }

    public JobIdentifier jobIdentifier() {
        return jobIdentifier;
    }

    public static CreateAgentRequest fromJSON(String json) {
        return GSON.fromJson(json, CreateAgentRequest.class);
    }

    public RequestExecutor executor(AgentInstances agentInstances, PluginRequest pluginRequest) {
        return new CreateAgentRequestExecutor(this, agentInstances, pluginRequest);
    }

    public Collection<String> autoregisterPropertiesAsEnvironmentVars(String elasticAgentId) {
        ArrayList<String> vars = new ArrayList<>();
        if (isNotBlank(autoRegisterKey)) {
            vars.add("GO_EA_AUTO_REGISTER_KEY=" + autoRegisterKey);
        }
        if (isNotBlank(environment)) {
            vars.add("GO_EA_AUTO_REGISTER_ENVIRONMENT=" + environment);
        }
        vars.add("GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID=" + elasticAgentId);
        vars.add("GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID=" + Constants.PLUGIN_ID);
        return vars;
    }
}
