/*
 * Copyright 2022 Thoughtworks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GetProfileViewExecutorTest {
    @Test
    public void shouldRenderTheTemplateInJSON() throws Exception {
        GoPluginApiResponse response = new GetProfileViewExecutor().execute();
        assertThat(response.responseCode(), is(200));
        Map<String, String> hashSet = new Gson().fromJson(response.responseBody(), new TypeToken<Map<String, String>>() {
        }.getType());
        assertThat(hashSet, hasEntry("template", Util.readResource("/profile.template.html")));
    }

    @Test
    public void allFieldsShouldBePresentInView() {
        String template = Util.readResource("/profile.template.html");
        final Document document = Jsoup.parse(template);

        for (Metadata field : GetProfileMetadataExecutor.FIELDS) {
            final Elements inputFieldForKey = document.getElementsByAttributeValue("ng-model", field.getKey());
            assertThat(inputFieldForKey, hasSize(1));

            final Elements spanToShowError = document.getElementsByAttributeValue("ng-class", "{'is-visible': GOINPUTNAME[" + field.getKey() + "].$error.server}");
            assertThat(spanToShowError, hasSize(1));
            assertThat(spanToShowError.attr("ng-show"), is("GOINPUTNAME[" + field.getKey() + "].$error.server"));
            assertThat(spanToShowError.text(), is("{{GOINPUTNAME[" + field.getKey() + "].$error.server}}"));
        }

        final Elements inputs = document.select("textarea,input,select");
        assertThat(inputs, hasSize(GetProfileMetadataExecutor.FIELDS.size()));
    }

}
