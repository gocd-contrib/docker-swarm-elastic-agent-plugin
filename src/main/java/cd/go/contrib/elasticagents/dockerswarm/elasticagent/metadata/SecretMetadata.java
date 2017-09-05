package cd.go.contrib.elasticagents.dockerswarm.elasticagent.metadata;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors.Metadata;

public class SecretMetadata extends Metadata {
    public SecretMetadata() {
        super("Secrets", false, false);
    }
}
