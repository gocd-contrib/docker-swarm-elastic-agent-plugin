package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class GetClusterProfileViewRequestExecutorTest {

    @Test
    public void shouldRenderTheTemplateInJSON() throws Exception {
        GoPluginApiResponse response = new GetClusterProfileViewRequestExecutor().execute();
        assertThat(response.responseCode(), is(200));
        final Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> hashSet = new Gson().fromJson(response.responseBody(), type);
        assertThat(hashSet, hasEntry("template", Util.readResource("/plugin-settings.template.html")));
    }

    @Test
    public void allFieldsShouldBePresentInView() {
        String template = Util.readResource("/plugin-settings.template.html");

        for (Metadata field : GetClusterProfileMetadataExecutor.FIELDS) {
            assertThat(template, containsString("ng-model=\"" + field.getKey() + "\""));
            assertThat(template, containsString("<span class=\"form_error\" ng-show=\"GOINPUTNAME[" + field.getKey() +
                    "].$error.server\">{{GOINPUTNAME[" + field.getKey() +
                    "].$error.server}}</span>"));
        }
    }
}
