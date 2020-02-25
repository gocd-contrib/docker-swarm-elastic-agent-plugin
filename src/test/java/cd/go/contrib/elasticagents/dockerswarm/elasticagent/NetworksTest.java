package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.swarm.NetworkAttachmentConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NetworksTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldReturnEmptyListWhenNetworkConfigIsNotProvided() {
        assertThat(Networks.fromString(null, Collections.emptyList()), hasSize(0));
        assertThat(Networks.fromString("", Collections.emptyList()), hasSize(0));
    }

    @Test
    public void shouldReturnNetworkAttachmentConfigListFromString() {
        final Network swarmNetwork = mock(Network.class);
        when(swarmNetwork.name()).thenReturn("frontend");

        final List<NetworkAttachmentConfig> serviceNetworks = Networks.fromString("frontend", asList(swarmNetwork));

        assertNotNull(serviceNetworks);
        assertThat(serviceNetworks, hasSize(1));

        assertThat(serviceNetworks.get(0).target(), is("frontend"));
    }

    @Test
    public void shouldErrorOutWhenNetworkDoesNotExist() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Network with name `frontend` does not exist.");

        final List<NetworkAttachmentConfig> serviceNetworks = Networks.fromString("frontend", Collections.emptyList());
    }
}
