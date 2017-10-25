# GoCD Elastic agent plugin for Docker Swarm

Table of Contents
=================

  * [Installation](#installation)
  * [Building the code base](#building-the-code-base)
  * [Is this production ready?](#is-this-production-ready)
  * [Using your own docker image with elastic agents](#using-your-own-docker-image-with-elastic-agents)
  * [Troubleshooting](#troubleshooting)
  * [License](#license)
  
## Installation

Documentation for installation is available [here](install.md)  

## Building the code base

To build the jar, run `./gradlew clean test assemble`

## Is this production ready?

We think so. We've been using it on https://build.gocd.org for a while now. You should know that this plugin terminates docker containers aggressively (within a minute or two of the agent being idle).

## Using your own docker image with elastic agents

More information to build custom GoCD agent docker image is available [here](https://github.com/gocd/docker-gocd-agent)

## Troubleshooting

If you already have it running it on a mac, make sure to restart it (see https://github.com/docker/for-mac/issues/17#mobyaccess). Time drift is known to cause the plugin to not work, because the timestamps returned by the docker API has drifted from the host.

A good way to know if there's a time drift is to run `docker ps` —

    ```
    CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
    e0754c9f4cdb        alpine:latest       "/bin/sh"           32 minutes ago      Up 17 seconds                           test
    809f310ba1e4        ubuntu:trusty       "/bin/bash"         33 minutes ago      Up About a minute                       reverent_raman
    ```

Notice how the `CREATED` and `STATUS` are several minutes apart for a recently created container.

### Enabling debug level logging

Enabling debug level logging can help you troubleshoot an issue with the elastic agent plugin. To enable debug level logs, edit the `/etc/default/go-server` (for Linux) to add:

```bash
export GO_SERVER_SYSTEM_PROPERTIES="$GO_SERVER_SYSTEM_PROPERTIES -Dplugin.cd.go.contrib.elastic-agent.docker-swarm.log.level=debug"
```

If you're running the server via `./server.sh` script —

```
$ GO_SERVER_SYSTEM_PROPERTIES="-Dplugin.cd.go.contrib.elastic-agent.docker-swarm.log.level=debug" ./server.sh
```

## License

```plain
Copyright 2017, ThoughtWorks, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```