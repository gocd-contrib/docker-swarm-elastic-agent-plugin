package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import com.spotify.docker.client.messages.swarm.SecretSpec;
import org.glassfish.jersey.internal.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.Constants.SWARM_SERVICE_NAME;
import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

public class DockerSecret {
    public static final String NAME = "Name";
    private final String serviceName;
    private final List<SecretSpec> secretSpecs;

    public DockerSecret(String serviceName, String secretSpecs) {
        this.serviceName = serviceName;
        this.secretSpecs = unmodifiableList(toSecretSpecs(secretSpecs));
    }

    public List<SecretSpec> secretSpecs() {
        return secretSpecs;
    }

    private List<SecretSpec> toSecretSpecs(String content) {
        if (isBlank(content)) {
            return Collections.emptyList();
        }

        final Properties properties = toProperties(content);
        return properties.stringPropertyNames().stream()
                .map(secretName -> createSecretSpec(secretName, properties.getProperty(secretName)))
                .collect(Collectors.toList());

    }

    private Properties toProperties(String content) {
        try {
            final Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(content.getBytes()));
            return properties;
        } catch (IOException e) {
            LOG.error("Failed to parse docker secret", e);
            throw new RuntimeException(e);
        }
    }

    private SecretSpec createSecretSpec(String secretName, String secretValue) {
        LOG.debug("Secret detected from profile: " + secretName);
        final String trimmedValue = trimToEmpty(secretValue);

        return SecretSpec.builder()
                .data(Base64.encodeAsString(trimmedValue.getBytes()))
                .name(UUID.randomUUID().toString().replaceAll("-", ""))
                .labels(buildLabels(secretName))
                .build();
    }

    private Map<String, String> buildLabels(String secretName) {
        final HashMap<String, String> labels = new HashMap<>();
        labels.put(NAME, secretName);
        labels.put(SWARM_SERVICE_NAME, serviceName);
        return labels;
    }
}
