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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent.model;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JobIdentifierTest {
    @Test
    public void shouldMatchJobIdentifier() {
        final JobIdentifier jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);

        final JobIdentifier deserializedJobIdentifier = JobIdentifier.fromJson(jobIdentifier.toJson());

        assertTrue(jobIdentifier.equals(deserializedJobIdentifier));
    }

    @Test
    public void shouldCreateRepresentationFromJobIdentifier() {
        final JobIdentifier jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);

        assertThat(jobIdentifier.getRepresentation(), is("up42/98765/stage_1/30000/job_1"));
    }

    @Test
    public void shouldCreatePipelineHistoryPageLink() {
        final JobIdentifier jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);

        assertThat(jobIdentifier.getPipelineHistoryPageLink(), is("/go/tab/pipeline/history/up42"));
    }

    @Test
    public void shouldCreateVSMPageLink() {
        final JobIdentifier jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);

        assertThat(jobIdentifier.getVsmPageLink(), is("/go/pipelines/value_stream_map/up42/98765"));
    }

    @Test
    public void shouldCreateStageDetailsPageLink() {
        final JobIdentifier jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);

        assertThat(jobIdentifier.getStageDetailsPageLink(), is("/go/pipelines/up42/98765/stage_1/30000"));
    }

    @Test
    public void shouldCreateJobDetailsPageLink() {
        final JobIdentifier jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);

        assertThat(jobIdentifier.getJobDetailsPageLink(), is("/go/tab/build/detail/up42/98765/stage_1/30000/job_1"));
    }
}