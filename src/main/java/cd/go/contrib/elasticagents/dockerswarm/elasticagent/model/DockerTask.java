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

import com.spotify.docker.client.messages.swarm.Task;

import java.util.Date;

import static org.apache.commons.lang.StringUtils.capitalize;

public class DockerTask {
    private final String id;
    private final String image;
    private final Date created;
    private final String state;
    private final String nodeId;
    private final String serviceId;

    public DockerTask(Task task) {
        id = task.id();
        image = task.spec().containerSpec().image();
        nodeId = task.nodeId();
        serviceId = task.serviceId();
        created = task.createdAt();
        state = capitalize(task.status().state());
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public Date getCreated() {
        return created;
    }

    public String getState() {
        return state;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerTask that = (DockerTask) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (image != null ? !image.equals(that.image) : that.image != null) return false;
        if (created != null ? !created.equals(that.created) : that.created != null) return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;
        if (nodeId != null ? !nodeId.equals(that.nodeId) : that.nodeId != null) return false;
        return serviceId != null ? serviceId.equals(that.serviceId) : that.serviceId == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (image != null ? image.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (nodeId != null ? nodeId.hashCode() : 0);
        result = 31 * result + (serviceId != null ? serviceId.hashCode() : 0);
        return result;
    }
}
