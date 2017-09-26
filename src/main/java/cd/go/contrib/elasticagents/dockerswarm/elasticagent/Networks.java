package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;
import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util.splitIntoLinesAndTrimSpaces;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.swarm.NetworkAttachmentConfig;

public class Networks {
    public static List<NetworkAttachmentConfig> fromString(String networkConfig, List<Network> dockerNetworks) {
        if (isBlank(networkConfig)) {
            return Collections.emptyList();
        }

        final Map<String, Network> availableNetworks = dockerNetworks.stream().collect(Collectors.toMap(o -> o.name(), o -> o));

        final List<NetworkAttachmentConfig> serviceNetworks = new ArrayList<>();
        final Collection<String> networkEntries = splitIntoLinesAndTrimSpaces(networkConfig);
        networkEntries.forEach(networkEntry -> {
            final Network availableNetwork = availableNetworks.get(networkEntry);

            if (availableNetwork == null) {
                throw new RuntimeException(format("Network with name `{0}` does not exist.", networkEntry));
            }

            LOG.debug(format("Using network `{0}` with id `{1}`.", networkEntry, availableNetwork.id()));
            serviceNetworks.add(NetworkAttachmentConfig.builder().target(networkEntry).build());
        });

        return serviceNetworks;
    }
}
