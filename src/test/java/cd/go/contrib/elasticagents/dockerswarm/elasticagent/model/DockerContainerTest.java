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

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.messages.Container;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DockerContainerTest {
    @Test
    public void shouldCreateDockerContainerFromContainerObject() throws Exception {
        final Container container = mock(Container.class);

        when(container.id()).thenReturn("container-id");
        when(container.created()).thenReturn(1L);
        when(container.image()).thenReturn("gocd-agent:latest");
        when(container.imageId()).thenReturn("image-id");
        when(container.state()).thenReturn("running");
        when(container.status()).thenReturn("Up 3 minutes");
        when(container.command()).thenReturn("make cross");
        when(container.labels()).thenReturn(labels());

        final DockerContainer dockerContainer = new DockerContainer(container);

        assertThat(dockerContainer.getId(), is("container-id"));
        assertThat(dockerContainer.getCreated(), is(1000L));
        assertThat(dockerContainer.getImage(), is("gocd-agent:latest"));
        assertThat(dockerContainer.getImageId(), is("image-id"));
        assertThat(dockerContainer.getState(), is("running"));
        assertThat(dockerContainer.getStatus(), is("Up 3 minutes"));
        assertThat(dockerContainer.getCommand(), is("make cross"));

        assertThat(dockerContainer.getNodeId(), is("node-id"));
        assertThat(dockerContainer.getServiceId(), is("service-id"));
        assertThat(dockerContainer.getServiceName(), is("service-name"));
        assertThat(dockerContainer.getTaskId(), is("task-id"));
        assertThat(dockerContainer.getTaskName(), is("task-name"));
    }

    private ImmutableMap<String, String> labels() {
        return new ImmutableMap.Builder<String, String>()
                .put("com.docker.swarm.node.id", "node-id")
                .put("com.docker.swarm.service.id", "service-id")
                .put("com.docker.swarm.service.name", "service-name")
                .put("com.docker.swarm.task.id", "task-id")
                .put("com.docker.swarm.task.name", "task-name")
                .build();
    }
}