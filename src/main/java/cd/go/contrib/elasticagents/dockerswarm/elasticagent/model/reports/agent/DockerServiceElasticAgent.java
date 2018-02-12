package cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.reports.agent;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.Resources;
import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.Task;
import com.spotify.docker.client.messages.swarm.TaskSpec;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.Constants.JOB_IDENTIFIER_LABEL_KEY;

public class DockerServiceElasticAgent {
    private String id;
    private String name;
    private Date createdAt;
    private String logs;
    private String limits;
    private String reservations;
    private String image;
    private String command;
    private String args;
    private JobIdentifier jobIdentifier;
    private String placementConstraints;
    private Map<String, String> environments;
    private String hosts;
    private String hostname;
    private List<TaskStatus> tasksStatus = new ArrayList<>();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getLogs() {
        return logs;
    }

    public String getLimits() {
        return limits;
    }

    public String getReservations() {
        return reservations;
    }

    public String getImage() {
        return image;
    }

    public String getCommand() {
        return command;
    }

    public String getArgs() {
        return args;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public String getPlacementConstraints() {
        return placementConstraints;
    }

    public Map<String, String> getEnvironments() {
        return environments;
    }

    public String getHosts() {
        return hosts;
    }

    public String getHostname() {
        return hostname;
    }

    public List<TaskStatus> getTasksStatus() {
        return tasksStatus;
    }

    public static DockerServiceElasticAgent fromService(Service service, DockerClient client) throws DockerException, InterruptedException {
        DockerServiceElasticAgent agent = new DockerServiceElasticAgent();

        agent.id = service.id();
        agent.name = service.spec().name();
        agent.createdAt = service.createdAt();
        agent.jobIdentifier = JobIdentifier.fromJson(service.spec().labels().get(JOB_IDENTIFIER_LABEL_KEY));

        LogStream logStream = client.serviceLogs(service.id(), DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr());
        agent.logs = logStream.readFully();
        logStream.close();

        TaskSpec taskSpec = service.spec().taskTemplate();

        agent.image = taskSpec.containerSpec().image();
        agent.hostname = taskSpec.containerSpec().hostname();
        agent.limits = resourceToString(taskSpec.resources().limits());
        agent.reservations = resourceToString(taskSpec.resources().reservations());
        agent.command = listToString(taskSpec.containerSpec().command());
        agent.args = listToString(taskSpec.containerSpec().args());
        agent.placementConstraints = listToString(taskSpec.placement().constraints());
        agent.environments = toMap(taskSpec);
        agent.hosts = listToString(taskSpec.containerSpec().hosts());

        final List<Task> tasks = client.listTasks(Task.Criteria.builder().serviceName(service.id()).build());
        if (!tasks.isEmpty()) {
            for (Task task : tasks) {
                agent.tasksStatus.add(new TaskStatus(task));
            }
        }

        return agent;
    }

    private static Map<String, String> toMap(TaskSpec taskSpec) {
        final List<String> envFromTask = taskSpec.containerSpec().env();
        Map<String, String> envs = new HashMap<>();
        if (envFromTask != null) {
            for (String env : envFromTask) {
                final String[] parts = env.split("=", 2);
                if (parts.length == 2) {
                    envs.put(parts[0], parts[1]);
                } else {
                    envs.put(parts[0], null);
                }
            }
        }
        return envs;
    }

    private static String listToString(List<String> stringList) {
        return (stringList == null || stringList.isEmpty()) ? "Not Specified" : StringUtils.join(stringList, "\n");
    }

    private static String resourceToString(Resources resource) {
        return (resource == null) ? "Not Specified" : Util.readableSize(resource.memoryBytes());
    }
}
