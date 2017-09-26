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
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ServiceNotFoundException;
import com.spotify.docker.client.messages.swarm.Service;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;

public class DockerServices implements AgentInstances<DockerService> {

    private final ConcurrentHashMap<String, DockerService> services = new ConcurrentHashMap<>();
    private boolean refreshed;
    public Clock clock = Clock.DEFAULT;

    final Semaphore semaphore = new Semaphore(0, true);

    @Override
    public DockerService create(CreateAgentRequest request, PluginSettings settings) throws Exception {
        final Integer maxAllowedContainers = settings.getMaxDockerContainers();
        synchronized (services) {
            doWithLockOnSemaphore(new SetupSemaphore(maxAllowedContainers, services, semaphore));

            if (semaphore.tryAcquire()) {
                DockerService dockerService = DockerService.create(request, settings, docker(settings));
                register(dockerService);
                return dockerService;
            } else {
                LOG.info("The number of containers currently running is currently at the maximum permissible limit (" + services.size() + "). Not creating any more containers.");
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
    public void terminate(String agentId, PluginSettings settings) throws Exception {
        DockerService instance = services.get(agentId);
        if (instance != null) {
            instance.terminate(docker(settings));
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
    public void terminateUnregisteredInstances(PluginSettings settings, Agents agents) throws Exception {
        DockerServices toTerminate = unregisteredAfterTimeout(settings, agents);
        if (toTerminate.services.isEmpty()) {
            return;
        }

        LOG.warn("Terminating services that did not register " + toTerminate.services.keySet());
        for (DockerService dockerService : toTerminate.services.values()) {
            terminate(dockerService.name(), settings);
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

    @Override
    public void refreshAll(PluginRequest pluginRequest) throws Exception {
        if (!refreshed) {
            DockerClient docker = docker(pluginRequest.getPluginSettings());
            List<Service> services = docker.listServices();
            for (Service service : services) {
                if (Constants.PLUGIN_ID.equals(service.spec().labels().get(Constants.CREATED_BY_LABEL_KEY))) {
                    register(DockerService.fromService(service));
                }
            }
            refreshed = true;
        }
    }

    private void register(DockerService service) {
        services.put(service.name(), service);
    }

    private DockerClient docker(PluginSettings settings) throws Exception {
        return DockerClientFactory.instance().docker(settings);
    }

    private DockerServices unregisteredAfterTimeout(PluginSettings settings, Agents knownAgents) throws Exception {
        Period period = settings.getAutoRegisterPeriod();
        DockerServices unregisteredContainers = new DockerServices();

        for (String serviceName : services.keySet()) {
            if (knownAgents.containsServiceWithId(serviceName)) {
                continue;
            }

            Service serviceInfo;
            try {
                serviceInfo = docker(settings).inspectService(serviceName);
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
