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

package com.example.elasticagent.executors;

import com.example.elasticagent.*;
import org.joda.time.Period;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static com.example.elasticagent.Agent.ConfigState.Disabled;
import static org.mockito.Mockito.*;

public class ServerPingRequestExecutorTest extends BaseTest {

    @Test
    public void testShouldDisableIdleAgents() throws Exception {
        String agentId = UUID.randomUUID().toString();
        final Agents agents = new Agents(Arrays.asList(new Agent(agentId, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled)));
        AgentInstances agentInstances = mock(AgentInstances.class);

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());
        when(pluginRequest.listAgents()).thenReturn(agents);
        verifyNoMoreInteractions(pluginRequest);

        final Collection<Agent> values = agents.agents();
        new ServerPingRequestExecutor(agentInstances, pluginRequest).execute();
        verify(pluginRequest).disableAgents(argThat(collectionMatches(values)));
    }

    private ArgumentMatcher<Collection<Agent>> collectionMatches(final Collection<Agent> values) {
        return new ArgumentMatcher<Collection<Agent>>() {
            @Override
            public boolean matches(Object argument) {
                return new ArrayList<>((Collection) argument).equals(new ArrayList(values));
            }
        };
    }

    @Test
    public void testShouldTerminateDisabledAgents() throws Exception {
        String agentId = UUID.randomUUID().toString();
        final Agents agents = new Agents(Arrays.asList(new Agent(agentId, Agent.AgentState.Idle, Agent.BuildState.Idle, Disabled)));
        AgentInstances agentInstances = mock(AgentInstances.class);

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());
        when(pluginRequest.listAgents()).thenReturn(agents);
        verifyNoMoreInteractions(pluginRequest);

        new ServerPingRequestExecutor(agentInstances, pluginRequest).execute();
        final Collection<Agent> values = agents.agents();
        verify(pluginRequest).deleteAgents(argThat(collectionMatches(values)));
    }

    @Test
    public void testShouldTerminateInstancesThatNeverAutoRegistered() throws Exception {
        PluginSettings settings = spy(createSettings());
        when(settings.getAutoRegisterPeriod()).thenReturn(new Period().withMinutes(0));

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(settings);
        when(pluginRequest.listAgents()).thenReturn(new Agents());
        verifyNoMoreInteractions(pluginRequest);

        AgentInstances agentInstances = mock(AgentInstances.class);

        ServerPingRequestExecutor serverPingRequestExecutor = new ServerPingRequestExecutor(agentInstances, pluginRequest);
        serverPingRequestExecutor.execute();
        verify(agentInstances).terminateUnregisteredInstances(settings, new Agents());
        verifyNoMoreInteractions(agentInstances);
    }
}
