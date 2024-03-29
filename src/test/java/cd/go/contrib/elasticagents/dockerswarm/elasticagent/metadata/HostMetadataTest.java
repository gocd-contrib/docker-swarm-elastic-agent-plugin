package cd.go.contrib.elasticagents.dockerswarm.elasticagent.metadata;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.ValidationError;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HostMetadataTest {

    @Test
    public void shouldValidateHostConfig() {
        assertNull(new HostMetadata("Hosts", false, false).validate("10.0.0.1 hostname"));

        ValidationError validationError = new HostMetadata("Hosts", false, false)
                .validate("some-config");

        assertNotNull(validationError);
        assertThat(validationError.key(), is("Hosts"));
        assertThat(validationError.message(), is("Host entry `some-config` is invalid. Must be in `IP-ADDRESS HOST-1 HOST-2...` format."));
    }

    @Test
    public void shouldValidateHostConfigWhenRequireField() {
        ValidationError validationError = new HostMetadata("Hosts", true, false).validate(null);

        assertNotNull(validationError);
        assertThat(validationError.key(), is("Hosts"));
        assertThat(validationError.message(), is("Hosts must not be blank."));
    }
}
