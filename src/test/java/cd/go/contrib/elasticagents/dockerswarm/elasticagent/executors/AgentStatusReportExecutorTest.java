/*
 * Copyright 2018 ThoughtWorks, Inc.
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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerServices;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.PluginRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.PluginSettings;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.AgentStatusReportRequest;
import com.google.gson.reflect.TypeToken;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.swarm.*;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.Constants.JOB_IDENTIFIER_LABEL_KEY;
import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util.GSON;
import static com.spotify.docker.client.DockerClient.LogsParam.stderr;
import static com.spotify.docker.client.DockerClient.LogsParam.stdout;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AgentStatusReportExecutorTest {
    @Mock
    private AgentStatusReportRequest statusReportRequest;
    @Mock
    private PluginRequest pluginRequest;
    @Mock
    private DockerClientFactory dockerClientFactory;
    @Mock
    private DockerClient client;
    @Mock
    private PluginSettings pluginSettings;
    @Mock
    private DockerServices dockerServices;

    private AgentStatusReportExecutor executor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        executor = new AgentStatusReportExecutor(statusReportRequest, pluginRequest, dockerServices, dockerClientFactory, PluginStatusReportViewBuilder.instance());
        when(dockerClientFactory.docker(pluginSettings)).thenReturn(client);
        when(pluginRequest.getPluginSettings()).thenReturn(pluginSettings);
    }

    @Test
    public void shouldReturnAgentStatusReportBasedOnProvidedElasticAgentId() throws Exception {
        final Service service = mockedService("elastic-agent-id", "abcd-xyz");
        when(statusReportRequest.getJobIdentifier()).thenReturn(null);
        when(statusReportRequest.getElasticAgentId()).thenReturn("elastic-agent-id");
        when(client.listServices()).thenReturn(Arrays.asList(service));
        when(client.serviceLogs("abcd-xyz", stdout(), stderr())).thenReturn(new StubbedLogStream("some-logs"));

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        final Map<String, String> responseMap = GSON.fromJson(response.responseBody(), new TypeToken<Map<String, String>>() {
        }.getType());
        assertTrue(responseMap.containsKey("view"));

        final Document document = Jsoup.parse(responseMap.get("view"));
        assertServiceDetails(service, document);
        assertServiceLog(document, "some-logs");
        assertTrue(hasEnvironmentVariable(document, "Foo", "Bar"));
        assertTrue(hasEnvironmentVariable(document, "Baz", null));
    }

    @Test
    public void shouldNotPrintAutoRegisterKey() throws Exception {
        final Service service = mockedService("elastic-agent-id", "abcd-xyz");
        when(statusReportRequest.getJobIdentifier()).thenReturn(null);
        when(statusReportRequest.getElasticAgentId()).thenReturn("elastic-agent-id");
        when(client.listServices()).thenReturn(Arrays.asList(service));
        when(client.serviceLogs("abcd-xyz", stdout(), stderr())).thenReturn(new StubbedLogStream("some-logs"));

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        final Map<String, String> responseMap = GSON.fromJson(response.responseBody(), new TypeToken<Map<String, String>>() {
        }.getType());
        assertTrue(responseMap.containsKey("view"));

        final Document document = Jsoup.parse(responseMap.get("view"));
        assertServiceDetails(service, document);
        assertServiceLog(document, "some-logs");
        assertTrue(hasEnvironmentVariable(document, "Foo", "Bar"));
        assertTrue(hasEnvironmentVariable(document, "Baz", null));
        assertFalse(hasEnvironmentVariable(document, "GO_EA_AUTO_REGISTER_KEY", null));
    }

    @Test
    public void shouldPrintMessageWhenLogIsNotAvailable() throws Exception {
        final Service service = mockedService("elastic-agent-id", "abcd-xyz");
        when(statusReportRequest.getJobIdentifier()).thenReturn(null);
        when(statusReportRequest.getElasticAgentId()).thenReturn("elastic-agent-id");
        when(client.listServices()).thenReturn(Arrays.asList(service));
        when(client.serviceLogs("abcd-xyz", stdout(), stderr())).thenReturn(new StubbedLogStream(""));

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        final Map<String, String> responseMap = GSON.fromJson(response.responseBody(), new TypeToken<Map<String, String>>() {
        }.getType());
        assertTrue(responseMap.containsKey("view"));

        final Document document = Jsoup.parse(responseMap.get("view"));
        assertThat(document.select(".service-logs").text(), is("Logs not available for this agent."));
    }

    private boolean hasEnvironmentVariable(Document document, String name, String value) {
        final Elements elements = document.select(MessageFormat.format(".environments .name-value .name-value_pair label:contains({0})", name));
        if (elements.isEmpty()) {
            return false;
        }

        final String envValueSpanText = StringUtils.stripToNull(elements.get(0).parent().select("span").text());
        return StringUtils.equals(value, envValueSpanText);
    }

    private void assertServiceLog(Document document, String logs) {
        final Elements logDetails = document.select(".service-logs").select("textarea");
        assertThat(logDetails.val(), is(logs));
    }

    private void assertServiceDetails(Service service, Document document) {
        final Elements serviceDetails = document.select(".tab-content").attr("ng-show", "currenttab == 'service-details'");
        final String serviceDetailsText = serviceDetails.text();

        assertThat(serviceDetailsText, containsString(service.id()));
        assertThat(serviceDetailsText, containsString(service.spec().name()));
        assertThat(serviceDetailsText, containsString(service.spec().taskTemplate().containerSpec().image()));
    }

    private Service mockedService(String serviceName, String serviceId) {
        final ContainerSpec containerSpec = ContainerSpec.builder()
                .hosts(Arrays.asList("10.0.0.1 foo.bar.com", "10.0.0.1 abx.baz.com"))
                .hostname("some-hostname")
                .image("gocd/gocd-docker-agent:v18.2.0")
                .command("")
                .env("Foo=Bar", "Baz", "GO_EA_AUTO_REGISTER_KEY=auto-register-key")
                .args("")
                .build();

        final TaskSpec template = TaskSpec.builder()
                .containerSpec(containerSpec)
                .resources(ResourceRequirements.builder().build())
                .placement(Placement.create(Arrays.asList("")))
                .build();

        final ServiceSpec serviceSpec = ServiceSpec.builder()
                .addLabel(JOB_IDENTIFIER_LABEL_KEY, new JobIdentifier().toJson())
                .name(serviceName)
                .taskTemplate(template).build();

        return new StubbedService(serviceId, serviceSpec);
    }

    class StubbedLogStream implements LogStream {
        private final String logs;

        public StubbedLogStream(String logs) {

            this.logs = logs;
        }

        @Override
        public String readFully() {
            return logs;
        }

        @Override
        public void attach(OutputStream stdout, OutputStream stderr) throws IOException {

        }

        @Override
        public void attach(OutputStream stdout, OutputStream stderr, boolean closeAtEof) throws IOException {

        }

        @Override
        public void close() {

        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public LogMessage next() {
            return null;
        }
    }

    class StubbedService extends Service {
        private final String serviceId;
        private final ServiceSpec serviceSpec;

        public StubbedService(String serviceId, ServiceSpec serviceSpec) {
            this.serviceId = serviceId;
            this.serviceSpec = serviceSpec;
        }

        @Override
        public String id() {
            return serviceId;
        }

        @Override
        public Version version() {
            return null;
        }

        @Override
        public Date createdAt() {
            return new Date();
        }

        @Override
        public Date updatedAt() {
            return new Date();
        }

        @Override
        public ServiceSpec spec() {
            return serviceSpec;
        }

        @Override
        public Endpoint endpoint() {
            return null;
        }

        @Override
        public UpdateStatus updateStatus() {
            return null;
        }
    }
}