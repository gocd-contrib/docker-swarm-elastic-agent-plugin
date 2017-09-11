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

package cd.go.contrib.elasticagents.dockerswarm.elasticagent;

import com.google.gson.Gson;
import com.spotify.docker.client.messages.swarm.Secret;
import com.spotify.docker.client.messages.swarm.SecretBind;
import com.spotify.docker.client.messages.swarm.SecretFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;
import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util.splitIntoLinesAndTrimSpaces;
import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.*;

public class DockerSecrets extends ArrayList<DockerSecrets.DockerSecret> {
    private static final Gson GSON = new Gson();

    public static DockerSecrets fromString(String content) {
        final List<Map<String, String>> collect = splitIntoLinesAndTrimSpaces(content).stream()
                .filter(line -> isNotBlank(line))
                .map(line -> lineToMap(line))
                .collect(toList());

        return GSON.fromJson(GSON.toJson(collect), DockerSecrets.class);
    }

    private static Map<String, String> lineToMap(String content) {
        final String[] properties = content.split(",");
        final HashMap<String, String> map = new HashMap<>();
        for (String property : properties) {
            if (property.contains("=")) {
                final String[] parts = property.split("=", 2);
                map.put(stripToEmpty(parts[0]).toLowerCase(), stripToEmpty(parts[1]));
            }
        }

        if (isBlank(map.get("src"))) {
            throw new RuntimeException(format("Invalid secret specification `{0}`. Must specify property `src` with value.", content));
        }

        return map;
    }

    public List<SecretBind> toSecretBind(List<Secret> secrets) {
        final Map<String, Secret> secretMap = secrets.stream().collect(Collectors.toMap(o -> o.secretSpec().name(), o -> o));
        final List<SecretBind> secretBinds = new ArrayList<>();

        for (DockerSecret dockerSecret : this) {
            final Secret secret = secretMap.get(dockerSecret.src);

            if (secret == null) {
                throw new RuntimeException(format("Secret with name `{0}` does not exist.", dockerSecret.name()));
            }

            LOG.debug(format("Using secret `{0}` with id `{1}`.", dockerSecret.name(), secret.id()));
            final SecretFile secretFile = SecretFile.builder()
                    .name(dockerSecret.file())
                    .uid(dockerSecret.uid())
                    .gid(dockerSecret.gid())
                    .mode(dockerSecret.mode())
                    .build();

            secretBinds.add(SecretBind.builder()
                    .secretId(secret.id())
                    .secretName(dockerSecret.name())
                    .file(secretFile)
                    .build());
        }

        return secretBinds;
    }

    static class DockerSecret {
        private String src;
        private String target;
        private String uid;
        private String gid;
        private String mode;

        public String name() {
            return src;
        }

        public String file() {
            return isNotBlank(target) ? target : src;
        }

        public String uid() {
            return isNotBlank(uid) ? uid : "0";
        }

        public String gid() {
            return isNotBlank(gid) ? gid : "0";
        }

        public Long mode() {
            try {
                return isNotBlank(mode) ? Long.parseLong(mode, 8) : 0444L;
            } catch (NumberFormatException e) {
                throw new RuntimeException(format("Invalid mode value `{0}` for secret `{1}`. Mode value must be provided in octal.", mode, src));
            }
        }
    }
}
