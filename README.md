# GoCD Elastic agent plugin for Docker Swarm

Table of Contents
=================

  * [Building the code base](#building-the-code-base)
  * [Is this production ready?](#is-this-production-ready)
  * [Using your own docker image with elastic agents](#using-your-own-docker-image-with-elastic-agents)
     * [Using the GoCD agent, installed via .deb/.rpm](#using-the-gocd-agent-installed-via-debrpm)
     * [Use a custom bootstrapper](#use-a-custom-bootstrapper)
  * [Usage instructions](#usage-instructions)
  * [Troubleshooting](#troubleshooting)
  * [Credits](#credits)
  * [License](#license)

## Building the code base

To build the jar, run `./gradlew clean test assemble`

## Is this production ready?

We think so. We've been using it on https://build.gocd.org for a while now. You should know that this plugin terminates docker containers aggressively (within a minute or two of the agent being idle).

## Using your own docker image with elastic agents

The plugin executes the equivalent of the following docker command to start the agent —

```
docker run -e GO_EA_SERVER_URL=...
           -e GO_EA_AUTO_REGISTER_KEY=...
           -e GO_EA_AUTO_REGISTER_ENVIRONMENT=...
           -e GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID=...
           -e GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID=...
           ...
           IMAGE_ID
```

Your docker image is expected to contain a bootstrap program (to be executed via docker's `CMD`) that will create an [`autoregister.properties`](https://docs.gocd.io/current/advanced_usage/agent_auto_register.html) file using these variables. The `GO_EA_SERVER_URL` will point to the server url that the agent must communicate with.

Here is an example shell script to do this —

```bash
# write out autoregister.properties
(
cat <<EOF
agent.auto.register.key=${GO_EA_AUTO_REGISTER_KEY}
agent.auto.register.environments=${GO_EA_AUTO_REGISTER_ENVIRONMENT}
agent.auto.register.elasticAgent.agentId=${GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID}
agent.auto.register.elasticAgent.pluginId=${GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID}
EOF
) > /var/lib/go-agent/config/autoregister.properties
```

### Using the GoCD agent, installed via `.deb/.rpm`

See the bootstrap script and docker file here under [`contrib/scripts/bootstrap-via-installer`](contrib/scripts/bootstrap-via-installer).

### Use a custom bootstrapper

This method uses lesser memory and boots up the agent process and starts off a build quickly:

See the bootstrap script and docker file here under [`contrib/scripts/bootstrap-without-installed-agent`](contrib/scripts/bootstrap-without-installed-agent).

## Usage instructions

* Download and install Docker for your favorite OS from https://docs.docker.com/engine/installation/

If you already have it running it on a mac, make sure to restart it (see https://github.com/docker/for-mac/issues/17#mobyaccess). Time drift is known to cause the plugin to not work, because the timestamps returned by the docker API has drifted from the host.

A good way to know if there's a time drift is to run `docker ps` —

    ```
    CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
    e0754c9f4cdb        alpine:latest       "/bin/sh"           32 minutes ago      Up 17 seconds                           test
    809f310ba1e4        ubuntu:trusty       "/bin/bash"         33 minutes ago      Up About a minute                       reverent_raman
    ```

Notice how the `CREATED` and `STATUS` are several minutes apart for a recently created container.

* Download the latest GoCD installer from https://go.cd/download

    ```shell
    $ unzip go-server-VERSION.zip
    $ mkdir -p go-server-VERSION/plugins/external
    ```
* Download the docker plugin (https://github.com/gocd-contrib/docker-swarm-elastic-agents/releases)
* Copy the docker plugin to the go server directory

    ```
    $ cp build/libs/docker-swarm-elastic-agents-0.1-SNAPSHOT.jar /path/to/go-server-VERSION/plugins/external
    ```

* Start the server and configure the plugin (turn on debug logging to get more logs, they're not that noisy)

  On linux/mac

    ```shell
    $ GO_SERVER_SYSTEM_PROPERTIES='-Dplugin.cd.go.contrib.elastic-agent.docker-swarm.log.level=debug' ./server.sh
    ```

  On windows

    ```
    C:> set GO_SERVER_SYSTEM_PROPERTIES='-Dplugin.cd.go.contrib.elastic-agent.docker-swarm.log.level=debug'
    C:> server.cmd
    ```

To configure the plugin, navigate to the plugin settings page on your GoCD server http://localhost:8153/go/admin/plugins and setup the following settings for the docker plugin.

```
Go Server Host — https://YOUR_IP_ADDRESS:8154/go — do not use "localhost"
Docker URI (for mac and linux) — unix:///var/run/docker.sock
Auto register timeout - between 1-3 minutes
```

Now Let's configure the plugin —

## Plugin settings

In order to use the plugin user have to configure the plugin settings.

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Plugins_**

![Plugins][1]

2. Open a plugin settings for the `GoCD Elastic agent plugin for Docker Swarm`
    - Provide Go server url (`https://YOUR_HOST_OR_IP_ADDRESS:8154/go`). Server hostname/ip must resolve in your container. Don't use `localhost` or `127.0.0.1`.
    - Specify agent auto-register timeout(in minutes)
    - Specify maximum docker containers to run at any given point in time. Plugin will not create more container when running container count reached to specified limits.
    - Specify docker uri.
        - If your Go Server is running on local machine then use(for mac and linux) — `unix:///var/run/docker.sock` 
    - Save the plugin settings   
    
![Configure plugin settings][2]


## Create an elastic profile

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Elastic Agent Profiles_**

![Elastic Profiles][3]

2. Click on **_Add_** to create new elastic agent profile
    1. Specify `id` for profile.
    2. Select `GoCD Docker Swarm Elastic Agents` for **_Plugin id_**
    3. Specify GoCD elastic agent docker image name.
    4. Specify Soft memory limit. Container will start with memory specified here.
    5. Specify hard memory limit. The maximum amount of memory the container can use.
    6. Save your profile
    
![Create elastic profile][4]    

### Configure job to use an elastic agent profile

1. Click the gear icon on **_Pipeline_**

![Pipeline][5]

2. Click on **_Quick Edit_** button

![Quick edit][6]

3. Click on **_Stages_**
4. Create/Edit a job
5. Enter the `unique id` of an elastic profile in Job Settings

![Configure a job][7]

6. Save your changes

## Troubleshooting

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
Copyright 2016, ThoughtWorks, Inc.

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

[1]: images/plugins.png     "Plugins"
[2]: images/plugin-settings.png    "Configure plugin settings"
[3]: images/profiles_page.png  "Elastic profiles"
[4]: images/profile.png "Create elastic profile"
[5]: images/pipeline.png  "Pipeline"
[6]: images/quick-edit.png  "Quick edit"
[7]: images/configure-job.png  "Configure a job"