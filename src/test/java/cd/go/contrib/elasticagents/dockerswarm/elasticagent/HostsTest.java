package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class HostsTest {

    @Test
    public void shouldReturnEmptyListWhenHostConfigIsNotProvided() throws Exception {
        assertThat(new Hosts().hosts(null), hasSize(0));
        assertThat(new Hosts().hosts(""), hasSize(0));
    }

    @Test
    public void shouldReturnHostMappingForOneIpToOneHostnameMapping() throws Exception {
        final List<String> hosts = new Hosts().hosts("10.0.0.1 foo-host");
        assertThat(hosts, hasItem("10.0.0.1 foo-host"));
    }

    @Test
    public void shouldReturnHostMappingForOneIpToMAnyHostnameMapping() throws Exception {
        final List<String> hosts = new Hosts().hosts("10.0.0.1 foo-host bar-host");
        assertThat(hosts, hasItem("10.0.0.1 foo-host"));
        assertThat(hosts, hasItem("10.0.0.1 bar-host"));
    }

    @Test
    public void shouldErrorOutIfHostEntryIsInvalid() throws Exception {
        final List<String> errors = new Hosts().validate("foo-host 10.0.0.1");

        assertThat(errors, contains("'foo-host' is not an IP string literal."));
    }

    @Test
    public void shouldErrorOutIfHostEntryIsNotContainsHostName() throws Exception {
        final List<String> errors = new Hosts().validate("10.0.0.1");

        assertThat(errors, contains("Host entry `10.0.0.1` is invalid. Must be in `IP-ADDRESS HOST-1 HOST-2...` format."));
    }
}