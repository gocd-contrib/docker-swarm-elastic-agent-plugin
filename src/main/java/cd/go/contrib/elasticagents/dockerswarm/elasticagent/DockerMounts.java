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

import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerPlugin.LOG;
import static cd.go.contrib.elasticagents.dockerswarm.elasticagent.utils.Util.splitIntoLinesAndTrimSpaces;
import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.stripToEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.spotify.docker.client.messages.Volume;
import com.spotify.docker.client.messages.mount.Mount;

public class DockerMounts extends ArrayList<DockerMounts.DockerMount> {
    private static final Gson GSON = new Gson();

    public static DockerMounts fromString(String mountsConfig) {
        final List<Map<String, String>> mounts = splitIntoLinesAndTrimSpaces(mountsConfig).stream()
                .filter(line -> isNotBlank(line))
                .map(line -> lineToMap(line))
                .collect(toList());

        return GSON.fromJson(GSON.toJson(mounts), DockerMounts.class);
    }

    private static Map<String, String> lineToMap(String line) {
        final HashMap<String, String> map = new HashMap<>();
        final String[] properties = line.split(",");

        for (String property : properties) {
            if (stripToEmpty(property).toLowerCase().equals("readonly")) {
                map.put("readOnly", "true");
            } else if (property.contains("=")) {
                final String[] parts = property.split("=", 2);
                switch (stripToEmpty(parts[0]).toLowerCase()) {
                    case "type":
                        map.put("type", stripToEmpty(parts[1]));
                        break;
                    case "source": case "src":
                        map.put("source", stripToEmpty(parts[1]));
                        break;
                    case "target": case "destination": case "dst":
                        map.put("target", stripToEmpty(parts[1]));
                        break;
                    default:
                        throw new RuntimeException(format("Invalid mount specification `{0}`. Option `{1}` not implemented.", line, parts[0]));
                }
            } else {
                throw new RuntimeException(format("Invalid mount specification `{0}`. Option `{1}` not implemented.", line, property));
            }
        }

        final List<String> mountTypes = Arrays.asList("bind", "volume");
        if (map.containsKey("type") && mountTypes.stream().noneMatch(map.get("type")::contains)) {
            throw new RuntimeException(format("Invalid mount type specification `{0}`. Available options are `{1}`.", line, mountTypes));
        }

        if (map.containsKey("type") && map.get("type").equals("bind") && isBlank(map.get("source"))) {
            throw new RuntimeException(format("Invalid mount source specification `{0}`. Bind mounts have to specify `source`.", line));
        }

        if (isBlank(map.get("target"))) {
            throw new RuntimeException(format("Invalid mount target specification `{0}`. `target` has to be specified.", line));
        }

        return map;
    }

    public List<Mount> toMount(List<Volume> volumes) {
        final Map<String, Volume> volumeMap = volumes.stream().collect(Collectors.toMap(o -> o.name(), o -> o));
        final List<Mount> mounts = new ArrayList<>();

        for (DockerMount dockerMount : this) {
            if (dockerMount.type().equals("volume")) {
                final Volume volume = volumeMap.get(dockerMount.source);

                if (volume == null) {
                    throw new RuntimeException(format("Volume with name `{0}` does not exist.", dockerMount.source()));
                }

                LOG.debug(format("Using volume `{0}`.", dockerMount.source()));
            }

            final Mount mount = Mount.builder()
                    .type(dockerMount.type())
                    .source(dockerMount.source())
                    .target(dockerMount.target())
                    .readOnly(dockerMount.readOnly())
                    .build();

            mounts.add(mount);
        }

        return mounts;
    }

    static class DockerMount {
        private String type;
        private String source;
        private String target;
        private String readOnly;

        public String type() {
            return isNotBlank(type) ? type : "volume";
        }

        public String source() {
            return source;
        }

        public String target() {
            return target;
        }

        public Boolean readOnly() {
            return isNotBlank(readOnly);
        }
    }
}
