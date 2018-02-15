/*
 * Copyright 2017 ThoughtWorks, Inc.
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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.model;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.Task;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class SwarmCluster {
    private final List<DockerNode> nodes;

    public SwarmCluster(DockerClient dockerClient) throws DockerException, InterruptedException {
        nodes = dockerClient.listNodes().stream().map(node -> new DockerNode(node)).collect(toList());
        LOG.info("Running docker swarm nodes " + nodes.size());
        fetchTasks(dockerClient);
        sortNodes();
    }

    private void sortNodes() {
        nodes.sort((node1, node2) -> {
            final int leaderCompareResult = Boolean.compare(node2.isLeader(), node1.isLeader());
            if (leaderCompareResult == 0) {
                final int compareResult = Boolean.compare(node2.isManager(), node1.isManager());
                if (compareResult == 0) {
                    return node1.getHostname().compareTo(node2.getHostname());
                }
                return compareResult;
            }
            return leaderCompareResult;
        });
    }

    private void fetchTasks(DockerClient dockerClient) throws DockerException, InterruptedException {
        final Map<String, DockerNode> dockerNodeMap = nodes.stream().distinct().collect(toMap(DockerNode::getId, node -> node));
        final List<Task> tasks = dockerClient.listTasks();
        LOG.info("Running tasks " + tasks.size());
        final Map<String, Service> serviceIdToService = serviceIdToServiceMap(dockerClient);

        for (Task task : tasks) {
            final Service service = serviceIdToService.get(task.serviceId());
            if (service == null) {
                continue;
            }
            
            final DockerTask dockerTask = new DockerTask(task, service);
            final DockerNode dockerNode = dockerNodeMap.get(dockerTask.getNodeId());
            if (dockerNode != null) {
                dockerNode.add(dockerTask);
            }
        }
    }

    private Map<String, Service> serviceIdToServiceMap(DockerClient dockerClient) throws DockerException, InterruptedException {
        final List<Service> services = dockerClient.listServices();
        if (services == null || services.isEmpty()) {
            return Collections.emptyMap();
        }

        final HashMap<String, Service> serviceIdToService = new HashMap<>();
        for (Service service : services) {
            serviceIdToService.put(service.id(), service);
        }
        return serviceIdToService;
    }

    public List<DockerNode> getNodes() {
        return nodes;
    }
}
