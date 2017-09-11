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

import com.spotify.docker.client.messages.Container;

public class DockerContainer {

    private final String id;
    private final String image;
    private final Long created;
    private final String imageId;
    private final String state;
    private final String status;
    private final String nodeId;
    private final String serviceId;
    private final String serviceName;
    private final String taskId;
    private final String taskName;
    private final String command;

    public DockerContainer(Container container) {
        id = container.id();
        image = container.image();
        created = container.created() * 1000L;
        imageId = container.imageId();
        state = container.state();
        status = container.status();
        command = container.command();
        nodeId = container.labels().get("com.docker.swarm.node.id");
        serviceId = container.labels().get("com.docker.swarm.service.id");
        serviceName = container.labels().get("com.docker.swarm.service.name");
        taskId = container.labels().get("com.docker.swarm.task.id");
        taskName = container.labels().get("com.docker.swarm.task.name");
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public Long getCreated() {
        return created;
    }

    public String getImageId() {
        return imageId;
    }

    public String getState() {
        return state;
    }

    public String getStatus() {
        return status;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerContainer that = (DockerContainer) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (image != null ? !image.equals(that.image) : that.image != null) return false;
        if (created != null ? !created.equals(that.created) : that.created != null) return false;
        if (imageId != null ? !imageId.equals(that.imageId) : that.imageId != null) return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (nodeId != null ? !nodeId.equals(that.nodeId) : that.nodeId != null) return false;
        if (serviceId != null ? !serviceId.equals(that.serviceId) : that.serviceId != null) return false;
        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        if (taskId != null ? !taskId.equals(that.taskId) : that.taskId != null) return false;
        if (taskName != null ? !taskName.equals(that.taskName) : that.taskName != null) return false;
        return command != null ? command.equals(that.command) : that.command == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (image != null ? image.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (imageId != null ? imageId.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (nodeId != null ? nodeId.hashCode() : 0);
        result = 31 * result + (serviceId != null ? serviceId.hashCode() : 0);
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        result = 31 * result + (taskId != null ? taskId.hashCode() : 0);
        result = 31 * result + (taskName != null ? taskName.hashCode() : 0);
        result = 31 * result + (command != null ? command.hashCode() : 0);
        return result;
    }
}
