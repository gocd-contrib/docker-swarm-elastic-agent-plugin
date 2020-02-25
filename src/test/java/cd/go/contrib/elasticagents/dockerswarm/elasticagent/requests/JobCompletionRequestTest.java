package cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.model.JobIdentifier;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class JobCompletionRequestTest {

    @Test
    public void shouldDeserializeFromJSON() {

        String json = "{\n" +
                "  \"elastic_agent_id\": \"ea1\",\n" +
                "  \"job_identifier\": {\n" +
                "    \"pipeline_name\": \"test-pipeline\",\n" +
                "    \"pipeline_counter\": 1,\n" +
                "    \"pipeline_label\": \"Test Pipeline\",\n" +
                "    \"stage_name\": \"test-stage\",\n" +
                "    \"stage_counter\": \"1\",\n" +
                "    \"job_name\": \"test-job\",\n" +
                "    \"job_id\": 100\n" +
                "  }\n" +
                "}";

        JobCompletionRequest request = JobCompletionRequest.fromJSON(json);

        JobIdentifier expectedJobIdentifier = new JobIdentifier("test-pipeline", 1L, "Test Pipeline", "test-stage", "1", "test-job", 100L);
        JobIdentifier actualJobIdentifier = request.jobIdentifier();

        assertThat(actualJobIdentifier, is(expectedJobIdentifier));

        assertThat(request.getElasticAgentId(), is("ea1"));
    }


}
