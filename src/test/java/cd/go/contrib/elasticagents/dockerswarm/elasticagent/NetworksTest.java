package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.swarm.NetworkAttachmentConfig;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NetworksTest {
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
        assertThatThrownBy(() -> Networks.fromString("frontend", Collections.emptyList()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Network with name `frontend` does not exist.");
    }
}
