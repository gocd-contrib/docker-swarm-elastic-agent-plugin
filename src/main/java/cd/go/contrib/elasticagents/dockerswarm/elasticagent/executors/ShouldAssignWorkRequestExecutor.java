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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.AgentInstances;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerService;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.ShouldAssignWorkRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;
import static java.text.MessageFormat.format;


public class ShouldAssignWorkRequestExecutor implements RequestExecutor {
    private final AgentInstances<DockerService> agentInstances;
    private final ShouldAssignWorkRequest request;

    public ShouldAssignWorkRequestExecutor(ShouldAssignWorkRequest request, AgentInstances<DockerService> agentInstances) {
        this.request = request;
        this.agentInstances = agentInstances;
    }

    @Override
    public GoPluginApiResponse execute() {
        DockerService instance = agentInstances.find(request.agent().elasticAgentId());

        if (instance == null) {
            LOG.info(format(format("[should-assign-work] Agent with id `{0}` not exists.", request.agent().elasticAgentId())));
            return DefaultGoPluginApiResponse.success("false");
        }

        if (request.jobIdentifier().equals(instance.jobIdentifier())) {
            LOG.info(format("[should-assign-work] Job with profile {0} can be assigned to an agent {1} with job id {2}", request.properties(), instance.name(), instance.jobIdentifier()));
            return DefaultGoPluginApiResponse.success("true");
        }

        LOG.info(format("[should-assign-work] Job with profile {0} can be assigned to an agent {1}", request.properties(), instance.name()));
        return DefaultGoPluginApiResponse.success("false");
    }
}
