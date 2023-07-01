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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.reports;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util;
import com.spotify.docker.client.messages.swarm.ManagerStatus;
import com.spotify.docker.client.messages.swarm.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

public class DockerNode {
    private final String id;
    private final String hostname;
    private final String role;
    private final String availability;

    private final String os;
    private final String engineVersion;
    private final String architecture;
    private final String memory;
    private final Long cpus;
    private final String state;
    private final String nodeIP;
    private final String managerStatus;
    private final List<DockerTask> tasks = new ArrayList<>();

    public DockerNode(Node node) {
        id = node.id();
        hostname = node.description().hostname();
        availability = capitalize(node.spec().availability());
        state = capitalize(node.status().state());
        nodeIP = node.status().addr();
        role = capitalize(node.spec().role());

        final ManagerStatus managerStatus = node.managerStatus();
        this.managerStatus = getManagerStatus(managerStatus);

        engineVersion = node.description().engine().engineVersion();
        architecture = node.description().platform().architecture();
        os = node.description().platform().os();

        memory = Util.readableSize(node.description().resources().memoryBytes());
        cpus = node.description().resources().nanoCpus() / 1000000000;
    }

    private String getManagerStatus(ManagerStatus managerStatus) {
        if (managerStatus == null) {
            return null;
        }

        if ("manager".equalsIgnoreCase(role)) {
            if (managerStatus.leader() != null && Boolean.valueOf(managerStatus.leader())) {
                return "Leader";
            } else {
                return capitalize(managerStatus.reachability());
            }
        }
        return null;
    }

    public boolean isManager() {
        return equalsIgnoreCase("manager", role);
    }

    public boolean isLeader() {
        return equalsIgnoreCase("Leader", managerStatus);
    }

    public String getId() {
        return id;
    }

    public String getHostname() {
        return hostname;
    }

    public String getRole() {
        return role;
    }

    public String getAvailability() {
        return availability;
    }

    public String getOs() {
        return os;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getMemory() {
        return memory;
    }

    public Long getCpus() {
        return cpus;
    }

    public String getState() {
        return state;
    }

    public String getNodeIP() {
        return nodeIP;
    }

    public String getManagerStatus() {
        return managerStatus;
    }

    public void add(DockerTask dockerTask) {
        this.tasks.add(dockerTask);
    }

    public List<DockerTask> getTasks() {
        return Collections.unmodifiableList(tasks);
    }
}
