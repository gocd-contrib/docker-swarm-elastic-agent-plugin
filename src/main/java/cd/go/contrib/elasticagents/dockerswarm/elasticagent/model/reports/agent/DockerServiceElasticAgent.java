package cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.reports.agent;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.Resources;
import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.TaskSpec;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;

public class DockerServiceElasticAgent {
    private String id;
    private Date createdAt;
    private String logs;
    private String limits;
    private String reservations;
    private String image;
    private String command;
    private String args;

    public static DockerServiceElasticAgent fromService(Service service, DockerClient client) throws DockerException, InterruptedException {
        DockerServiceElasticAgent agent = new DockerServiceElasticAgent();

        agent.id = service.id();
        agent.createdAt = service.createdAt();

        LogStream logStream = client.serviceLogs(service.id(), DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr());
        agent.logs = logStream.readFully();
        logStream.close();

        TaskSpec taskSpec = service.spec().taskTemplate();

        Resources limits = taskSpec.resources().limits();
        Resources reservations = taskSpec.resources().reservations();

        agent.limits = (limits == null) ? "Not Specified" : Util.readableSize(limits.memoryBytes());
        agent.reservations = (reservations == null) ? "Not Specified" : Util.readableSize(reservations.memoryBytes());

        agent.image = taskSpec.containerSpec().image();
        List<String> command = taskSpec.containerSpec().command();
        List<String> args = taskSpec.containerSpec().args();

        agent.command = (command == null) ? "Not Specified" : StringUtils.join(command, "\n");
        agent.args = (args == null) ? "Not Specified" : StringUtils.join(args, "\n");

        return agent;
    }

    public String getId() {
        return id;
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
}
