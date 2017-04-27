package cd.go.contrib.elasticagents.dockerswarm.elasticagent.metadata;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.Hosts;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors.Metadata;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class HostMetadata extends Metadata {
    public HostMetadata(String key, boolean required, boolean secure) {
        super(key, required, secure);
    }

    @Override
    protected String doValidate(String input) {
        final List<String> errors = new ArrayList<>();
        final String validate = super.doValidate(input);

        if (isNotBlank(validate)) {
            errors.add(validate);
        }

        errors.addAll(new Hosts().validate(input));

        if (errors.isEmpty()) {
            return null;
        } else {
            return StringUtils.join(errors, ". ");
        }
    }
}
