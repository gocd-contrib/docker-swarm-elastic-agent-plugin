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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.spotify.docker.client.messages.Volume;
import com.spotify.docker.client.messages.mount.Mount;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DockerMountsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldBuildVolumeMountFromString() throws Exception {
        final DockerMounts mounts = DockerMounts.fromString("source=namedVolume, target=/path/in/container");

        assertNotNull(mounts);
        assertThat(mounts, hasSize(1));

        assertThat(mounts.get(0).type(), is("volume"));
        assertThat(mounts.get(0).source(), is("namedVolume"));
        assertThat(mounts.get(0).target(), is("/path/in/container"));
    }

    @Test
    public void shouldBuildBindMountFromString() throws Exception {
        final DockerMounts mounts = DockerMounts.fromString("type=bind, source=/path/in/host, target=/path/in/container");

        assertNotNull(mounts);
        assertThat(mounts, hasSize(1));

        assertThat(mounts.get(0).type(), is("bind"));
        assertThat(mounts.get(0).source(), is("/path/in/host"));
        assertThat(mounts.get(0).target(), is("/path/in/container"));
    }

    @Test
    public void shouldSkipEmptyLine() throws Exception {
        final DockerMounts dockerMounts = DockerMounts.fromString("type=volume, source=namedVolume, target=/path/in/container\n\ntype=bind, source=/path/in/host, target=/path/in/container2");

        assertNotNull(dockerMounts);
        assertThat(dockerMounts, hasSize(2));

        assertThat(dockerMounts.get(0).type(), is("volume"));
        assertThat(dockerMounts.get(1).type(), is("bind"));
    }

    @Test
    public void shouldBuildMountFromDockerMount() throws Exception {
        final DockerMounts dockerMounts = DockerMounts.fromString("source=namedVolume, target=/path/in/container\ntype=bind, src=/path/in/host, target=/path/in/container2, readonly");
        final Volume volume = mock(Volume.class);

        when(volume.name()).thenReturn("namedVolume");

        final List<Mount> mounts = dockerMounts.toMount(asList(volume));

        assertThat(mounts, hasSize(2));
        assertThat(mounts.get(0).type(), is("volume"));
        assertThat(mounts.get(0).source(), is("namedVolume"));
        assertThat(mounts.get(0).target(), is("/path/in/container"));
        assertThat(mounts.get(0).readOnly(), is(false));

        assertThat(mounts.get(1).type(), is("bind"));
        assertThat(mounts.get(1).source(), is("/path/in/host"));
        assertThat(mounts.get(1).target(), is("/path/in/container2"));
        assertThat(mounts.get(1).readOnly(), is(true));
    }

    @Test
    public void shouldErrorOutWhenVolumeDoesNotExist() throws Exception {
        final DockerMounts dockerMounts = DockerMounts.fromString("source=namedVolume, target=/path/in/container\nsource=namedVolume2, target=/path/in/container2");
        final Volume volume = mock(Volume.class);

        when(volume.name()).thenReturn("namedVolume");

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Volume with name `namedVolume2` does not exist.");

        dockerMounts.toMount(asList(volume));
    }
}
