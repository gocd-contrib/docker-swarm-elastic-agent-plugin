/*
 * Copyright 2016 ThoughtWorks, Inc.
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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.CreateAgentRequest;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Size;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util;
import com.google.gson.Gson;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ServiceNotFoundException;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.swarm.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.util.*;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.Constants.*;
import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;
import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util.dockerApiVersionAtLeast;
import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util.splitIntoLinesAndTrimSpaces;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerService {
    private static final Gson GSON = new Gson();
    private final DateTime createdAt;
    private final Map<String, String> properties;
    private final String environment;
    private String jobId;
    private String name;

    public DockerService(String name, Date createdAt, Map<String, String> properties, String environment, String JobId) {
        this.name = name;
        this.createdAt = new DateTime(createdAt);
        this.properties = properties;
        this.environment = environment;
        jobId = JobId;
    }

    public String name() {
        return name;
    }

    public DateTime createdAt() {
        return createdAt;
    }

    public String environment() {
        return environment;
    }

    public Map<String, String> properties() {
        return properties;
    }

    public String jobId() {
        return jobId;
    }

    public void terminate(DockerClient docker) throws DockerException, InterruptedException {
        try {
            LOG.debug("Terminating service " + this.name());
            docker.removeService(name);
        } catch (ServiceNotFoundException ignore) {
            LOG.warn("Cannot terminate a service that does not exist " + name);
        }
    }

    public static DockerService fromService(Service service) {
        Map<String, String> labels = service.spec().labels();
        return new DockerService(service.spec().name(),
                service.createdAt(),
                GSON.fromJson(labels.get(CONFIGURATION_LABEL_KEY), HashMap.class),
                labels.get(ENVIRONMENT_LABEL_KEY),
                labels.get(JOB_ID_LABEL_KEY));
    }

    public static DockerService create(CreateAgentRequest request, PluginSettings settings, DockerClient docker) throws InterruptedException, DockerException {
        String serviceName = UUID.randomUUID().toString();

        HashMap<String, String> labels = labelsFrom(request);
        String imageName = image(request.properties());
        String[] env = environmentFrom(request, settings, serviceName);

        final ContainerSpec.Builder containerSpecBuilder = ContainerSpec.builder()
                .image(imageName)
                .env(env);

        if (StringUtils.isNotBlank(request.properties().get("Command"))) {
            containerSpecBuilder.command(splitIntoLinesAndTrimSpaces(request.properties().get("Command")).toArray(new String[]{}));
        }

        if (dockerApiVersionAtLeast(docker, "1.26")) {
            containerSpecBuilder.hosts(new Hosts().hosts(request.properties().get("Hosts")));
            final DockerMounts dockerMounts = DockerMounts.fromString(request.properties().get("Mounts"));
            containerSpecBuilder.mounts(dockerMounts.toMount(docker.listVolumes().volumes()));
            final DockerSecrets dockerSecrets = DockerSecrets.fromString(request.properties().get("Secrets"));
            containerSpecBuilder.secrets(dockerSecrets.toSecretBind(docker.listSecrets()));
        } else {
            LOG.warn(format("Detected docker version and api version is {0} and {1} respectively. Docker with api version 1.26 or above is required to use volume mounts, secrets and host file entries. Please refer https://docs.docker.com/engine/api/v1.32/#section/Versioning for more information about docker release.", docker.version().version(), docker.version().apiVersion()));
        }

        TaskSpec taskSpec = TaskSpec.builder()
                .containerSpec(containerSpecBuilder.build())
                .resources(requirements(request))
                .placement(Placement.create(Util.linesToList(request.properties().get("Constraints"))))
                .build();

        ServiceSpec serviceSpec = ServiceSpec.builder()
                .name(serviceName)
                .labels(labels)
                .taskTemplate(taskSpec)
                .networks(Networks.fromString(request.properties().get("Networks"), docker.listNetworks()))
                .build();

        ServiceCreateResponse service = docker.createService(serviceSpec);

        String id = service.id();

        Service serviceInfo = docker.inspectService(id);

        LOG.debug("Created service " + serviceInfo.spec().name());
        return new DockerService(serviceName,
                serviceInfo.createdAt(),
                request.properties(),
                request.environment(),
                String.valueOf(request.jobIdentifier().getJobId()));
    }

    private static ResourceRequirements requirements(CreateAgentRequest request) {
        ResourceRequirements resourceRequirements = null;
        Resources.Builder limits = null;
        Resources.Builder reservations = null;

        if (request.properties().containsKey("MaxMemory")) {
            long maxMemory = Size.parse(request.properties().get("MaxMemory")).toBytes();
            limits = Resources.builder()
                    .memoryBytes(maxMemory);
        }

        if (request.properties().containsKey("ReservedMemory")) {
            long reservedMemory = Size.parse(request.properties().get("ReservedMemory")).toBytes();
            reservations = Resources.builder()
                    .memoryBytes(reservedMemory);

        }

        if (limits != null || reservations != null) {
            ResourceRequirements.Builder resourceRequirementsBuilder = ResourceRequirements.builder();

            if (limits != null) {
                resourceRequirementsBuilder.limits(limits.build());
            }

            if (reservations != null) {
                resourceRequirementsBuilder.reservations(reservations.build());
            }

            resourceRequirements = resourceRequirementsBuilder.build();
        }
        return resourceRequirements;
    }

    private static String[] environmentFrom(CreateAgentRequest request, PluginSettings settings, String containerName) {
        Set<String> env = new HashSet<>();

        env.addAll(settings.getEnvironmentVariables());
        if (StringUtils.isNotBlank(request.properties().get("Environment"))) {
            env.addAll(splitIntoLinesAndTrimSpaces(request.properties().get("Environment")));
        }

        env.addAll(Arrays.asList(
                "GO_EA_MODE=" + mode(),
                "GO_EA_SERVER_URL=" + settings.getGoServerUrl(),
                "GO_EA_GUID=" + "docker-swarm." + containerName
        ));

        env.addAll(request.autoregisterPropertiesAsEnvironmentVars(containerName));

        return env.toArray(new String[env.size()]);
    }

    private static HashMap<String, String> labelsFrom(CreateAgentRequest request) {
        HashMap<String, String> labels = new HashMap<>();

        labels.put(CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID);
        labels.put(JOB_ID_LABEL_KEY, String.valueOf(request.jobIdentifier().getJobId()));
        if (StringUtils.isNotBlank(request.environment())) {
            labels.put(ENVIRONMENT_LABEL_KEY, request.environment());
        }
        labels.put(CONFIGURATION_LABEL_KEY, GSON.toJson(request.properties()));
        return labels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerService that = (DockerService) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    private static String mode() {
        if ("false".equals(System.getProperty("rails.use.compressed.js"))) {
            return "dev";
        }

        if ("true".equalsIgnoreCase(System.getProperty("rails.use.compressed.js"))) {
            return "prod";
        }

        return "";
    }

    private static String image(Map<String, String> properties) {
        String image = properties.get("Image");

        if (isBlank(image)) {
            throw new IllegalArgumentException("Must provide `Image` attribute.");
        }

        if (!image.contains(":")) {
            return image + ":latest";
        }
        return image;
    }

}
