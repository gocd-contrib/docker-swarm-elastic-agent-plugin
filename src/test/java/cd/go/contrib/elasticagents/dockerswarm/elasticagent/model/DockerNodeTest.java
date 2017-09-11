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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.model;

import com.spotify.docker.client.messages.swarm.*;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DockerNodeTest {

    @Test
    public void shouldCreateDockerNodeFromNodeObject() throws Exception {
        final Node node = mock(Node.class);

        when(node.id()).thenReturn("node-id");

        final NodeStatus nodeStatus = mock(NodeStatus.class);
        when(node.status()).thenReturn(nodeStatus);
        when(nodeStatus.state()).thenReturn("ready");
        when(nodeStatus.addr()).thenReturn("192.168.65.2");

        mockNodeDescription(node, "moby", "17.09.0-ce-rc1", "x86_64", "linux");

        when(node.spec()).thenReturn(NodeSpec.builder().name("node-name").availability("active").role("manager").build());

        final ManagerStatus managerStatus = mock(ManagerStatus.class);
        when(node.managerStatus()).thenReturn(managerStatus);
        when(managerStatus.leader()).thenReturn(true);
        when(managerStatus.reachability()).thenReturn("reachable");
        when(managerStatus.addr()).thenReturn("192.168.65.2:2377");

        final DateTime now = DateTime.now();
        final Date createdAt = now.minusHours(5).toDate();
        final Date updatedAt = now.minusHours(3).toDate();
        when(node.createdAt()).thenReturn(createdAt);
        when(node.updatedAt()).thenReturn(updatedAt);


        final DockerNode dockerNode = new DockerNode(node);

        assertThat(dockerNode.getId(), is("node-id"));
        assertThat(dockerNode.getName(), is("node-name"));
        assertThat(dockerNode.getRole(), is("manager"));
        assertThat(dockerNode.getAvailability(), is("active"));

        assertThat(dockerNode.getState(), is("ready"));
        assertThat(dockerNode.getNodeIP(), is("192.168.65.2"));

        assertThat(dockerNode.getHostname(), is("moby"));
        assertThat(dockerNode.getEngineVersion(), is("17.09.0-ce-rc1"));
        assertThat(dockerNode.getArchitecture(), is("x86_64"));
        assertThat(dockerNode.getOs(), is("linux"));
        assertThat(dockerNode.getCpus(), is(4L));
        assertThat(dockerNode.getMemory(), is(2046756L));

        assertThat(dockerNode.getReachability(), is("reachable"));

        assertThat(dockerNode.getCreatedAt(), is(createdAt));
        assertThat(dockerNode.getUpdatedAt(), is(updatedAt));

    }

    private void mockNodeDescription(Node node, String hostname, String dockerVersion, String architecture, String os) {
        final NodeDescription nodeDescription = mock(NodeDescription.class);
        when(nodeDescription.hostname()).thenReturn(hostname);

        final Platform platform = mock(Platform.class);
        when(nodeDescription.platform()).thenReturn(platform);
        when(platform.architecture()).thenReturn(architecture);
        when(platform.os()).thenReturn(os);

        final Resources resources = mock(Resources.class);
        when(nodeDescription.resources()).thenReturn(resources);
        when(resources.nanoCpus()).thenReturn(4000000000L);
        when(resources.memoryBytes()).thenReturn(2095878144L);

        final EngineConfig engineConfig = mock(EngineConfig.class);
        when(nodeDescription.engine()).thenReturn(engineConfig);
        when(engineConfig.engineVersion()).thenReturn(dockerVersion);

        when(node.description()).thenReturn(nodeDescription);
    }
}