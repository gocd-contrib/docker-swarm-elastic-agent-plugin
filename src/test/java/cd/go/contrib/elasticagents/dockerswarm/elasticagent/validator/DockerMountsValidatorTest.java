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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationError;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationResult;
import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.messages.Volume;
import com.spotify.docker.client.messages.VolumeList;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DockerMountsValidatorTest {

    @Test
    public void shouldValidateValidVolumeMountConfiguration() {
        final HashMap<String, String> properties = new HashMap<>();
        final VolumeList volumeList = mock(VolumeList.class);
        properties.put("Image", "alpine");
        properties.put("Mounts", "src=Foo, target=Bar");

        when(volumeList.volumes()).thenReturn(new ImmutableList.Builder<Volume>().add(Volume.builder().name("Foo").build()).build());

        ValidationResult validationResult = new DockerMountsValidator().validate(properties);

        assertFalse(validationResult.hasErrors());
    }

    @Test
    public void shouldValidateInvalidVolumeMountConfiguration() {
        final HashMap<String, String> properties = new HashMap<>();
        final VolumeList volumeList = mock(VolumeList.class);
        properties.put("Image", "alpine");
        properties.put("Mounts", "src=Foo");

        when(volumeList.volumes()).thenReturn(new ImmutableList.Builder<Volume>().add(Volume.builder().name("Foo").build()).build());

        ValidationResult validationResult = new DockerMountsValidator().validate(properties);

        assertTrue(validationResult.hasErrors());
        assertThat(validationResult.allErrors(), contains(new ValidationError("Mounts", "Invalid mount target specification `src=Foo`. `target` has to be specified.")));
    }
}
