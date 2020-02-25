package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.util.Arrays;

import static org.apache.commons.lang.StringUtils.isBlank;

public class GoServerURLMetadata extends Metadata {
    private static String GO_SERVER_URL = "go_server_url";
    private static String GO_SERVER_URL_DISPLAY_VALUE = "Go Server URL";

    public GoServerURLMetadata() {
        super(GO_SERVER_URL, true, false);
    }

    @Override
    public String doValidate(String input) {
        if (isBlank(input)) {
            return GO_SERVER_URL_DISPLAY_VALUE + " must not be blank.";
        }

        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(input);
        } catch (URISyntaxException e) {
            return GO_SERVER_URL_DISPLAY_VALUE + " must be a valid URL (http://example.com:8153/go)";
        }

        if (isBlank(uriBuilder.getScheme())) {
            return GO_SERVER_URL_DISPLAY_VALUE + " must be a valid URL (http://example.com:8153/go)";
        }

        if (!Arrays.asList("http", "https").contains(uriBuilder.getScheme())) {
            return GO_SERVER_URL_DISPLAY_VALUE + " must use http or https protocol";
        }

        if (uriBuilder.getHost().equalsIgnoreCase("localhost") || uriBuilder.getHost().equalsIgnoreCase("127.0.0.1")) {
            return GO_SERVER_URL_DISPLAY_VALUE + " must not be localhost, since this gets resolved on the agents";
        }

        if (!(uriBuilder.getPath().endsWith("/go") || uriBuilder.getPath().endsWith("/go/"))) {
            return GO_SERVER_URL_DISPLAY_VALUE + " must be a valid URL ending with '/go' (http://example.com:8153/go)";
        }

        return null;
    }
}
