package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import com.google.common.net.InetAddresses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util.splitIntoLinesAndTrimSpaces;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.isBlank;

public class Hosts {

    public List<String> hosts(String hostConfig) {
        if (isBlank(hostConfig)) {
            return Collections.emptyList();
        }

        List<String> hostMappings = new ArrayList<>();
        Collection<String> hostEntries = splitIntoLinesAndTrimSpaces(hostConfig);

        hostEntries.forEach(hostEntry -> {
            hostMappings.addAll(toHosts(hostEntry));
        });

        return hostMappings;
    }

    private List<String> toHosts(String hostEntry) {
        String[] parts = hostEntry.split("\\s+");

        final String ipAddess = parts[0];

        List<String> hosts = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            hosts.add(format("{0} {1}", ipAddess, parts[i]));
        }

        return hosts;
    }

    public List<String> validate(String hostConfig) {
        if (isBlank(hostConfig)) {
            return Collections.emptyList();
        }

        List<String> errors = new ArrayList<>();
        Collection<String> hostEntries = splitIntoLinesAndTrimSpaces(hostConfig);

        hostEntries.forEach(hostEntry -> {
            String[] parts = hostEntry.split("\\s+");
            if (parts.length < 2) {
                errors.add(format("Host entry `{0}` is invalid.", hostEntry));
            } else {
                validateIpAddress(errors, parts[0]);
            }
        });

        return errors;
    }

    private void validateIpAddress(List<String> errors, String part) {
        try {
            InetAddresses.forString(part);
        } catch (Exception e) {
            errors.add(e.getMessage());
        }
    }
}
