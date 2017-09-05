package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.Constants.SWARM_SERVICE_NAME;
import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerSecret.NAME;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class DockerSecretTest {
    @Test
    public void shouldBuildSecretSpecsFromSecretString() throws Exception {
        final DockerSecret dockerSecret = new DockerSecret("foo-service", "Username:admin\nPassword:badger");

        assertThat(dockerSecret.secretSpecs(), hasSize(2));

        assertNotNull(dockerSecret.secretSpecs().get(0).name());
        assertThat(dockerSecret.secretSpecs().get(0).labels().get(NAME), is("Username"));
        assertThat(dockerSecret.secretSpecs().get(0).data(), is(Base64.getEncoder().encodeToString("admin".getBytes(StandardCharsets.UTF_8))));
        assertThat(dockerSecret.secretSpecs().get(0).labels().get(SWARM_SERVICE_NAME), is("foo-service"));


        assertNotNull(dockerSecret.secretSpecs().get(1).name());
        assertThat(dockerSecret.secretSpecs().get(1).labels().get(NAME), is("Password"));
        assertThat(dockerSecret.secretSpecs().get(1).data(), is(Base64.getEncoder().encodeToString("badger".getBytes(StandardCharsets.UTF_8))));
        assertThat(dockerSecret.secretSpecs().get(1).labels().get(SWARM_SERVICE_NAME), is("foo-service"));
    }

    @Test
    public void shouldBuildSecretSpecsFromSecretSpaceSeparatedString() throws Exception {
        final DockerSecret dockerSecret = new DockerSecret("foo-service", "Username admin\nPassword badger");

        assertThat(dockerSecret.secretSpecs(), hasSize(2));

        assertNotNull(dockerSecret.secretSpecs().get(0).name());
        assertThat(dockerSecret.secretSpecs().get(0).labels().get(NAME), is("Username"));
        assertThat(dockerSecret.secretSpecs().get(0).data(), is(Base64.getEncoder().encodeToString("admin".getBytes(StandardCharsets.UTF_8))));
        assertThat(dockerSecret.secretSpecs().get(0).labels().get(SWARM_SERVICE_NAME), is("foo-service"));


        assertNotNull(dockerSecret.secretSpecs().get(1).name());
        assertThat(dockerSecret.secretSpecs().get(1).labels().get(NAME), is("Password"));
        assertThat(dockerSecret.secretSpecs().get(1).data(), is(Base64.getEncoder().encodeToString("badger".getBytes(StandardCharsets.UTF_8))));
        assertThat(dockerSecret.secretSpecs().get(1).labels().get(SWARM_SERVICE_NAME), is("foo-service"));
    }
}