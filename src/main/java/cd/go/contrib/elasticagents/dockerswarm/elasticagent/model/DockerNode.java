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

import com.spotify.docker.client.messages.swarm.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DockerNode {
    private final String id;
    private final String name;
    private final String hostname;
    private final String role;
    private final String availability;
    private final Date createdAt;

    private final String os;
    private final String engineVersion;
    private final String architecture;
    private final Long memory;
    private final Long cpus;
    private final String state;
    private final String nodeIP;
    private final Boolean leader;
    private final String reachability;
    private final Date updatedAt;
    private final List<DockerContainer> containers = new ArrayList<>();

    public DockerNode(Node node) {
        id = node.id();
        name = node.spec().name();
        hostname = node.description().hostname();
        availability = node.spec().availability();
        createdAt = node.createdAt();
        updatedAt = node.updatedAt();
        state = node.status().state();
        nodeIP = node.status().addr();
        role = node.spec().role();
        leader = node.managerStatus().leader();
        reachability = node.managerStatus().reachability();

        engineVersion = node.description().engine().engineVersion();
        architecture = node.description().platform().architecture();
        os = node.description().platform().os();

        memory = node.description().resources().memoryBytes() / 1024;
        cpus = node.description().resources().nanoCpus() / 1000000000;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public Date getCreatedAt() {
        return createdAt;
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

    public Long getMemory() {
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

    public Boolean getLeader() {
        return leader;
    }

    public String getReachability() {
        return reachability;
    }

    public void add(DockerContainer dockerContainer) {
        this.containers.add(dockerContainer);
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public List<DockerContainer> getContainers() {
        return Collections.unmodifiableList(containers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerNode that = (DockerNode) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (hostname != null ? !hostname.equals(that.hostname) : that.hostname != null) return false;
        if (role != null ? !role.equals(that.role) : that.role != null) return false;
        if (availability != null ? !availability.equals(that.availability) : that.availability != null) return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        if (os != null ? !os.equals(that.os) : that.os != null) return false;
        if (engineVersion != null ? !engineVersion.equals(that.engineVersion) : that.engineVersion != null)
            return false;
        if (architecture != null ? !architecture.equals(that.architecture) : that.architecture != null) return false;
        if (memory != null ? !memory.equals(that.memory) : that.memory != null) return false;
        if (cpus != null ? !cpus.equals(that.cpus) : that.cpus != null) return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;
        if (nodeIP != null ? !nodeIP.equals(that.nodeIP) : that.nodeIP != null) return false;
        if (leader != null ? !leader.equals(that.leader) : that.leader != null) return false;
        if (reachability != null ? !reachability.equals(that.reachability) : that.reachability != null) return false;
        if (updatedAt != null ? !updatedAt.equals(that.updatedAt) : that.updatedAt != null) return false;
        return containers != null ? containers.equals(that.containers) : that.containers == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (availability != null ? availability.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (os != null ? os.hashCode() : 0);
        result = 31 * result + (engineVersion != null ? engineVersion.hashCode() : 0);
        result = 31 * result + (architecture != null ? architecture.hashCode() : 0);
        result = 31 * result + (memory != null ? memory.hashCode() : 0);
        result = 31 * result + (cpus != null ? cpus.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (nodeIP != null ? nodeIP.hashCode() : 0);
        result = 31 * result + (leader != null ? leader.hashCode() : 0);
        result = 31 * result + (reachability != null ? reachability.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        result = 31 * result + (containers != null ? containers.hashCode() : 0);
        return result;
    }
}
