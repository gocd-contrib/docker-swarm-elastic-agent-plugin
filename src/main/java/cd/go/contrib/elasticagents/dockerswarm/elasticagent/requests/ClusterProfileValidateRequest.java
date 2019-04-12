package cd.go.contrib.elasticagents.dockerswarm.elasticagent.requests;

import cd.go.contrib.elasticagents.dockerswarm.elasticagent.RequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.elasticagent.executors.ClusterProfileValidateRequestExecutor;
import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ClusterProfileValidateRequest extends HashMap<String, String>{
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private Map<String, String> properties;

    public ClusterProfileValidateRequest(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public static ClusterProfileValidateRequest fromJSON(String json) {
        final Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        final Map<String, String> properties = GSON.fromJson(json, type);
        return new ClusterProfileValidateRequest(properties);
    }

    public RequestExecutor executor() {
        return new ClusterProfileValidateRequestExecutor(this);
    }}
