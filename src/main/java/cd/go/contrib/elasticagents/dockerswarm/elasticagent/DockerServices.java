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

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests.CreateAgentRequest;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ServiceNotFoundException;
import com.spotify.docker.client.messages.swarm.Service;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;

public class DockerServices implements AgentInstances<DockerService> {

    private final ConcurrentHashMap<String, DockerService> services = new ConcurrentHashMap<>();
    private boolean refreshed;
    private List<JobIdentifier> jobsWaitingForAgentCreation = new ArrayList<>();
    public Clock clock = Clock.DEFAULT;

    final Semaphore semaphore = new Semaphore(0, true);


    @Override
    public DockerService create(CreateAgentRequest request, PluginRequest pluginRequest) throws Exception {
        ClusterProfileProperties clusterProfileProperties = request.getClusterProfileProperties();
        final Integer maxAllowedContainers = clusterProfileProperties.getMaxDockerContainers();
        synchronized (services) {
            if (!jobsWaitingForAgentCreation.contains(request.jobIdentifier())) {
                jobsWaitingForAgentCreation.add(request.jobIdentifier());
            }
            doWithLockOnSemaphore(new SetupSemaphore(maxAllowedContainers, services, semaphore));
            List<Map<String, String>> messages = new ArrayList<>();

            if (semaphore.tryAcquire()) {
                pluginRequest.addServerHealthMessage(messages);
                DockerService dockerService = DockerService.create(request, clusterProfileProperties, docker(clusterProfileProperties));
                register(dockerService);
                jobsWaitingForAgentCreation.remove(request.jobIdentifier());
                return dockerService;
            } else {
                String maxLimitExceededMessage = "The number of containers currently running is currently at the maximum permissible limit (" + services.size() + "). Not creating any more containers.";
                Map<String, String> messageToBeAdded = new HashMap<>();
                messageToBeAdded.put("type", "warning");
                messageToBeAdded.put("message", maxLimitExceededMessage);
                messages.add(messageToBeAdded);
                pluginRequest.addServerHealthMessage(messages);
                LOG.info(maxLimitExceededMessage);
                return null;
            }
        }
    }

    private void doWithLockOnSemaphore(Runnable runnable) {
        synchronized (semaphore) {
            runnable.run();
        }
    }

    @Override
    public void terminate(String agentId, ClusterProfileProperties clusterProfileProperties) throws Exception {
        DockerService instance = services.get(agentId);
        if (instance != null) {
            instance.terminate(docker(clusterProfileProperties));
        } else {
            LOG.warn("Requested to terminate an instance that does not exist " + agentId);
        }

        doWithLockOnSemaphore(new Runnable() {
            @Override
            public void run() {
                semaphore.release();
            }
        });

        synchronized (services) {
            services.remove(agentId);
        }
    }

    @Override
    public void terminateUnregisteredInstances(ClusterProfileProperties clusterProfileProperties, Agents agents) throws Exception {
        DockerServices toTerminate = unregisteredAfterTimeout(clusterProfileProperties, agents);
        if (toTerminate.services.isEmpty()) {
            return;
        }

        LOG.warn("Terminating services that did not register " + toTerminate.services.keySet());
        for (DockerService dockerService : toTerminate.services.values()) {
            terminate(dockerService.name(), clusterProfileProperties);
        }
    }

    @Override
    public Agents instancesCreatedAfterTimeout(PluginSettings settings, Agents agents) {
        ArrayList<Agent> oldAgents = new ArrayList<>();
        for (Agent agent : agents.agents()) {
            DockerService instance = services.get(agent.elasticAgentId());
            if (instance == null) {
                continue;
            }

            if (clock.now().isAfter(instance.createdAt().plus(settings.getAutoRegisterPeriod()))) {
                oldAgents.add(agent);
            }
        }
        return new Agents(oldAgents);
    }

    private void refreshAgentInstances(ClusterProfileProperties pluginSettings) throws Exception {
        DockerClient dockerClient = docker(pluginSettings);
        List<Service> clusterSpecificServices = dockerClient.listServices();
        services.clear();
        for (Service service : clusterSpecificServices) {
            ImmutableMap<String, String> labels = service.spec().labels();
            if (labels != null && Constants.PLUGIN_ID.equals(labels.get(Constants.CREATED_BY_LABEL_KEY))) {
                register(DockerService.fromService(service));
            }
        }
        refreshed = true;
    }

    @Override
    public void refreshAll(ClusterProfileProperties pluginSettings, boolean forceRefresh) throws Exception {
        if (!refreshed || forceRefresh) {
            refreshAgentInstances(pluginSettings);
        }
    }

    @Override
    public void refreshAll(ClusterProfileProperties pluginSettings) throws Exception {
        if (!refreshed) {
            refreshAgentInstances(pluginSettings);
        }
    }

    public void register(DockerService service) {
        services.put(service.name(), service);
    }

    private DockerClient docker(ClusterProfileProperties clusterProfileProperties) throws Exception {
        return DockerClientFactory.instance().docker(clusterProfileProperties);
    }

    private DockerServices unregisteredAfterTimeout(ClusterProfileProperties clusterProfileProperties, Agents knownAgents) throws Exception {
        Period period = clusterProfileProperties.getAutoRegisterPeriod();
        DockerServices unregisteredContainers = new DockerServices();

        for (String serviceName : services.keySet()) {
            if (knownAgents.containsServiceWithId(serviceName)) {
                continue;
            }

            Service serviceInfo;
            try {
                serviceInfo = docker(clusterProfileProperties).inspectService(serviceName);
            } catch (ServiceNotFoundException e) {
                LOG.warn("The container " + serviceName + " could not be found.");
                continue;
            }
            DateTime dateTimeCreated = new DateTime(serviceInfo.createdAt());

            if (clock.now().isAfter(dateTimeCreated.plus(period))) {
                unregisteredContainers.register(DockerService.fromService(serviceInfo));
            }
        }
        return unregisteredContainers;
    }

    public boolean hasInstance(String agentId) {
        return services.containsKey(agentId);
    }

    @Override
    public DockerService find(String agentId) {
        return services.get(agentId);
    }

    // used by test
    protected boolean isEmpty() {
        return services.isEmpty();
    }

}
