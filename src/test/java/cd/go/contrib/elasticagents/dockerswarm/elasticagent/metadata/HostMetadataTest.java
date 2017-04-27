package cd.go.contrib.elasticagents.dockerswarm.elasticagent.metadata;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class HostMetadataTest {

    @Test
    public void shouldValidateHostConfig() throws Exception {
        assertTrue(new HostMetadata("Hosts", false, false).validate("10.0.0.1 hostname").isEmpty());

        Map<String, String> validationResult = new HostMetadata("Hosts", false, false)
                .validate("some-config");

        assertThat(validationResult.size(), is(2));
        assertThat(validationResult, hasEntry("message", "Host entry `some-config` is invalid."));
        assertThat(validationResult, hasEntry("key", "Hosts"));
    }

    @Test
    public void shouldValidateHostConfigWhenRequireField() throws Exception {
        Map<String, String> validate = new HostMetadata("Hosts", true, false).validate(null);
        assertThat(validate.size(), is(2));
        assertThat(validate, hasEntry("message", "Hosts must not be blank."));
        assertThat(validate, hasEntry("key", "Hosts"));
    }
}