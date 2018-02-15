package cd.go.contrib.elasticagents.dockerswarm.elasticagent.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util.GSON;
import static java.text.MessageFormat.format;

public class JobIdentifier {
    @Expose
    @SerializedName("pipeline_name")
    private String pipelineName;

    @Expose
    @SerializedName("pipeline_counter")
    private Long pipelineCounter;

    @Expose
    @SerializedName("pipeline_label")
    private String pipelineLabel;

    @Expose
    @SerializedName("stage_name")
    private String stageName;

    @Expose
    @SerializedName("stage_counter")
    private String stageCounter;

    @Expose
    @SerializedName("job_name")
    private String jobName;

    @Expose
    @SerializedName("job_id")
    private Long jobId;

    private String representation;

    public JobIdentifier(Long jobId) {
        this.jobId = jobId;
    }

    public JobIdentifier() {
    }

    public JobIdentifier(String pipelineName, Long pipelineCounter, String pipelineLabel, String stageName, String stageCounter, String jobName, Long jobId) {
        this.pipelineName = pipelineName;
        this.pipelineCounter = pipelineCounter;
        this.pipelineLabel = pipelineLabel;
        this.stageName = stageName;
        this.stageCounter = stageCounter;
        this.jobName = jobName;
        this.jobId = jobId;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public Long getPipelineCounter() {
        return pipelineCounter;
    }

    public String getPipelineLabel() {
        return pipelineLabel;
    }

    public String getStageName() {
        return stageName;
    }

    public String getStageCounter() {
        return stageCounter;
    }

    public String getRepresentation() {
        if (StringUtils.isBlank(representation)) {
            this.representation = format("{0}/{1}/{2}/{3}/{4}", pipelineName, pipelineCounter, stageName, stageCounter, jobName);
        }
        return representation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobIdentifier)) return false;

        JobIdentifier that = (JobIdentifier) o;

        if (pipelineCounter != that.pipelineCounter) return false;
        if (jobId != that.jobId) return false;
        if (pipelineName != null ? !pipelineName.equals(that.pipelineName) : that.pipelineName != null) return false;
        if (pipelineLabel != null ? !pipelineLabel.equals(that.pipelineLabel) : that.pipelineLabel != null)
            return false;
        if (stageName != null ? !stageName.equals(that.stageName) : that.stageName != null) return false;
        if (stageCounter != null ? !stageCounter.equals(that.stageCounter) : that.stageCounter != null) return false;
        return jobName != null ? jobName.equals(that.jobName) : that.jobName == null;
    }

    @Override
    public int hashCode() {
        int result = pipelineName != null ? pipelineName.hashCode() : 0;
        result = 31 * result + (int) (pipelineCounter ^ (pipelineCounter >>> 32));
        result = 31 * result + (pipelineLabel != null ? pipelineLabel.hashCode() : 0);
        result = 31 * result + (stageName != null ? stageName.hashCode() : 0);
        result = 31 * result + (stageCounter != null ? stageCounter.hashCode() : 0);
        result = 31 * result + (jobName != null ? jobName.hashCode() : 0);
        result = 31 * result + (int) (jobId ^ (jobId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "JobIdentifier{" +
                "pipelineName='" + pipelineName + '\'' +
                ", pipelineCounter=" + pipelineCounter +
                ", pipelineLabel='" + pipelineLabel + '\'' +
                ", staqeName='" + stageName + '\'' +
                ", stageCounter='" + stageCounter + '\'' +
                ", jobName='" + jobName + '\'' +
                ", jobId=" + jobId +
                '}';
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static JobIdentifier fromJson(String json) {
        return GSON.fromJson(json, JobIdentifier.class);
    }
}
