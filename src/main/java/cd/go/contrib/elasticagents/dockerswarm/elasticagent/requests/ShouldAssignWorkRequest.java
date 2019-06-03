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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.*;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors.ShouldAssignWorkRequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.JobIdentifier;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

/**
 * Represents the {@link Request#REQUEST_SHOULD_ASSIGN_WORK} message.
 */
public class ShouldAssignWorkRequest {
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private Agent agent;
    private String environment;
    private Map<String, String> properties;
    private JobIdentifier jobIdentifier;
    private ClusterProfileProperties clusterProfileProperties;

    public ShouldAssignWorkRequest(Agent agent, String environment, Map<String, String> properties, JobIdentifier jobIdentifier, Map<String, String> clusterProfileProperties) {
        this.agent = agent;
        this.environment = environment;
        this.properties = properties;
        this.jobIdentifier = jobIdentifier;
        this.clusterProfileProperties = ClusterProfileProperties.fromConfiguration(clusterProfileProperties);
    }

    public ShouldAssignWorkRequest() {
    }

    public Agent agent() {
        return agent;
    }

    public String environment() {
        return environment;
    }

    public Map<String, String> properties() {
        return properties;
    }

    public JobIdentifier jobIdentifier() {
        return jobIdentifier;
    }

    public static ShouldAssignWorkRequest fromJSON(String json) {
        return GSON.fromJson(json, ShouldAssignWorkRequest.class);
    }

    public RequestExecutor executor(AgentInstances<DockerService> agentInstances, PluginRequest pluginRequest) {
        return new ShouldAssignWorkRequestExecutor(this, agentInstances);
    }

    public ClusterProfileProperties getClusterProfileProperties() {
        return clusterProfileProperties;
    }
}
