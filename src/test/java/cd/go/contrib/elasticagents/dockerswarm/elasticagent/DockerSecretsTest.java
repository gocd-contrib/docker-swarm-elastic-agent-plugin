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

import com.spotify.docker.client.messages.swarm.Secret;
import com.spotify.docker.client.messages.swarm.SecretBind;
import com.spotify.docker.client.messages.swarm.SecretSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DockerSecretsTest {
    @Test
    public void shouldBuildDockerSecretFromString() {
        final DockerSecrets dockerSecrets = DockerSecrets.fromString("src=Username, target=Foo, uid=uid,gid=gid, mode=640");

        assertNotNull(dockerSecrets);
        assertThat(dockerSecrets, hasSize(1));

        assertThat(dockerSecrets.get(0).name(), is("Username"));
        assertThat(dockerSecrets.get(0).file(), is("Foo"));
        assertThat(dockerSecrets.get(0).uid(), is("uid"));
        assertThat(dockerSecrets.get(0).gid(), is("gid"));
        assertThat(dockerSecrets.get(0).mode(), is(0640L));
    }

    @Test
    public void shouldSkipEmptyLine() {
        final DockerSecrets dockerSecrets = DockerSecrets.fromString("src=Username, target=Foo, uid=UID\n\nsrc=Password, target=Bar");

        assertNotNull(dockerSecrets);
        assertThat(dockerSecrets, hasSize(2));

        assertThat(dockerSecrets.get(0).name(), is("Username"));
        assertThat(dockerSecrets.get(1).name(), is("Password"));
    }

    @Test
    public void shouldBuildSecretBindFromDockerSecret() {
        final DockerSecrets dockerSecrets = DockerSecrets.fromString("src=Username, target=username, uid=uid, gid=gid, mode=0640\nsrc=Password, target=passwd, uid=uid, gid=gid, mode=0640");
        final Secret secretForUsername = mock(Secret.class);
        final Secret secretForPassword = mock(Secret.class);

        when(secretForUsername.secretSpec()).thenReturn(SecretSpec.builder().name("Username").build());
        when(secretForUsername.id()).thenReturn("username-secret-id");

        when(secretForPassword.secretSpec()).thenReturn(SecretSpec.builder().name("Password").build());
        when(secretForPassword.id()).thenReturn("password-secret-id");

        final List<SecretBind> secretBinds = dockerSecrets.toSecretBind(asList(secretForUsername, secretForPassword));

        assertThat(secretBinds, hasSize(2));
        assertThat(secretBinds.get(0).secretName(), is("Username"));
        assertThat(secretBinds.get(0).secretId(), is("username-secret-id"));
        assertThat(secretBinds.get(0).file().name(), is("username"));
        assertThat(secretBinds.get(0).file().uid(), is("uid"));
        assertThat(secretBinds.get(0).file().gid(), is("gid"));
        assertThat(secretBinds.get(0).file().mode(), is(0640L));

        assertThat(secretBinds.get(1).secretName(), is("Password"));
        assertThat(secretBinds.get(1).secretId(), is("password-secret-id"));
        assertThat(secretBinds.get(1).file().name(), is("passwd"));
        assertThat(secretBinds.get(1).file().uid(), is("uid"));
        assertThat(secretBinds.get(1).file().gid(), is("gid"));
        assertThat(secretBinds.get(1).file().mode(), is(0640L));
    }

    @Test
    public void shouldBuildSecretBindFromDockerSecretAndUseDefaultsWhenNotProvided() {
        final DockerSecrets dockerSecrets = DockerSecrets.fromString("src=Username");
        final Secret secret = mock(Secret.class);

        when(secret.secretSpec()).thenReturn(SecretSpec.builder().name("Username").build());
        when(secret.id()).thenReturn("secret-id");

        final List<SecretBind> secretBinds = dockerSecrets.toSecretBind(asList(secret));

        assertThat(secretBinds, hasSize(1));
        assertThat(secretBinds.get(0).secretName(), is("Username"));
        assertThat(secretBinds.get(0).secretId(), is("secret-id"));

        assertThat(secretBinds.get(0).file().name(), is("Username"));
        assertThat(secretBinds.get(0).file().uid(), is("0"));
        assertThat(secretBinds.get(0).file().gid(), is("0"));
        assertThat(secretBinds.get(0).file().mode(), is(0444L));
    }

    @Test
    public void shouldErrorOutWhenSecretDoesNotExist() {
        final DockerSecrets dockerSecrets = DockerSecrets.fromString("src=Username\nsrc=Password");
        final Secret secret = mock(Secret.class);

        when(secret.secretSpec()).thenReturn(SecretSpec.builder().name("Username").build());
        when(secret.id()).thenReturn("secret-id");

        assertThatThrownBy(() -> dockerSecrets.toSecretBind(asList(secret)))
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessage("Secret with name `Password` does not exist.");
    }

    @Test
    public void shouldErrorOutWhenSecretNameIsNotProvided() {
        assertThatThrownBy(() -> DockerSecrets.fromString("target=Username"))
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessage("Invalid secret specification `target=Username`. Must specify property `src` with value.");
    }

    @Test
    public void shouldErrorOutWhenModeIsInvalid() {
        assertThatThrownBy(() -> DockerSecrets.fromString("src=Username, mode=0898").get(0).mode())
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessage("Invalid mode value `0898` for secret `Username`. Mode value must be provided in octal.");
    }
}
