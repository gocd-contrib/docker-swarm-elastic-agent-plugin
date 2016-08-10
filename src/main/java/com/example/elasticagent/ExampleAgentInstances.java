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

package com.example.elasticagent;

import com.example.elasticagent.requests.CreateAgentRequest;

public class ExampleAgentInstances implements AgentInstances<ExampleInstance> {

    public Clock clock = Clock.DEFAULT;

    @Override
    public ExampleInstance create(CreateAgentRequest request, PluginSettings settings) throws Exception {
        // TODO: Implement me!
        throw new UnsupportedOperationException();
    }

    @Override
    public void terminate(String agentId, PluginSettings settings) throws Exception {
        // TODO: Implement me!
        throw new UnsupportedOperationException();
    }

    @Override
    public void terminateUnregisteredInstances(PluginSettings pluginSettings, Agents agents) {
        // TODO: Implement me!
        throw new UnsupportedOperationException();
    }

    @Override
    public Agents instancesCreatedAfterTimeout(PluginSettings settings, Agents agents) {
        // TODO: Implement me!
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshAll(PluginRequest pluginRequest) throws Exception {
        // TODO: Implement me!
        throw new UnsupportedOperationException();
    }

    @Override
    public ExampleInstance find(String agentId) {
        return null;
    }

    // used by tests
    public boolean hasContainer(String containerId) {
        return false;
    }
}
