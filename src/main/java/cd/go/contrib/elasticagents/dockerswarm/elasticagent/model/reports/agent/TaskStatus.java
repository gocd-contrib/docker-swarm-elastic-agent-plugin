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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.reports.agent;

import com.spotify.docker.client.messages.swarm.Task;
import org.apache.commons.lang.StringUtils;

public class TaskStatus {

    private final String id;
    private final String state;
    private final String message;
    private String containerId;
    private Long exitCode;
    private String pid;

    public TaskStatus(Task task) {
        id = task.id();
        state = task.status().state();
        if ("failed".equals(state)) {
            message = task.status().err();
        } else {
            message = task.status().message();
        }

        if (task.status().containerStatus() != null) {
            if (StringUtils.isNotBlank(task.status().containerStatus().containerId())) {
                containerId = task.status().containerStatus().containerId().substring(0, 12);
            }
            exitCode = task.status().containerStatus().exitCode();

            if (task.status().containerStatus().pid() == null) {
                pid = "-";
            } else {
                pid = task.status().containerStatus().pid().toString();
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }

    public String getContainerId() {
        return containerId;
    }

    public Long getExitCode() {
        return exitCode;
    }

    public String getPid() {
        return pid;
    }
}
