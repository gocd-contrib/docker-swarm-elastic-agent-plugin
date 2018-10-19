## 4.0.0 - 2018-10-19
- Added support to terminate container once job is finished.

**_NOTE:_** *_Requires GoCD version 18.10.0 or higher. Plugin will not work with the older version of GoCD._*

## 3.0.3 - 2018-10-12

- Upgraded some dependant libraries to improve compatibility with newer docker versions.

## 3.0.2 - 2018-09-04

- Removed additional margin from status report pages

## 3.0.1 - 2018-04-17

## Improved

- The plugin status report will now render all swarm nodes expanded to make it easier to search for things.

## 3.0.0 - 2018-03-08

### Added

- Support for agent status report and plugin status report.
- Plugin now uses's job identifier to tag the docker services. This allows the plugin to decide to which agent it should assign work.

#### Known issues

- Logs are not shown on agent status report[(#68)](https://github.com/gocd-contrib/docker-swarm-elastic-agents/issues/68).
- Better message when a container is not created for the job or docker service is killed[(#71)](https://github.com/gocd-contrib/docker-swarm-elastic-agents/issues/71).

**_Note:_** *Requires GoCD version 18.2.0 or higher. Plugin will not work with the older version of GoCD.*

## 2.2.0 - 2017-10-25

### Added

- Support to specify constraints information in elastic profile.

## 2.1.0 - 2017-09-28

### Added

- Support to start service with volume mount.
- Support to start a service with an existing network.
- UI improvement for status report. 

## 2.0.0 - 2017-09-20

### Added

- Support for private docker registry support
- Tooltip help text on elastic profile fields 
- Support for binding secrets with containers
- Plugin uses elastic agent extension v2 and supports status report

**_Note:_** *_This requires GoCD version `17.9.0` or above_* 

## 1.1.4 - 2017-04-27

### Added

- Added support to start docker containers with host mapping.
  * User can now provide mapping for host entries in `IP-ADDRESS   HOSTNAME-1  HOSTNAME-2...` format, which is the standard format for `/etc/hosts` file.

```
10.0.0.1   host-x
10.0.0.2   host-y   host-z
```

## 1.1.3 - 2017-04-26

### Fixed

- Fixed issue with docker version 17.04. Docker 17.04 have removed `UpdateStatus` from the `service inspect` JSON response on new services.

## 1.1.2 - 2016-12-11

### Changed

- Changed the `go.cd.elastic-agent.get-icon` call to use underscore instead of hyphens.

## 1.1.1 - 2016-11-24

### Fixed

- Added `MaxMemory` and `ReservedMemory` configuration in the profile view. This was always supported in the XML, but was missing in the view.

## 1.1.0 - 2016-11-20

### Added

- Added support for a few additional calls required by the GoCD server.
  * `go.cd.elastic-agent.get-profile-metadata`
  * `go.cd.elastic-agent.get-profile-view`
  * `go.cd.elastic-agent.validate-profile`
  * `go.cd.elastic-agent.get-icon`

## 1.0.0 - 2016-10-07

### Changed

- Renamed the `Memory` property to `MaxMemory`

### Added

- The memory reservation can now be specified using the `ReservedMemory` property -

    ```xml
    <profile pluginId="cd.go.contrib.elastic-agent.docker" id="foo">
      <property>
        <key>ReservedMemory</key>
        <!-- You may use prefix B, KB, MB, GB -->
        <value>1024MB</value>
      </property>
    </profile>
    ```


## 0.3.1 - 2016-10-07

### Fixed

- Fixed an issue with `Memory` property that did not allow for using over 2GB.

## 0.3.0 - 2016-10-07

### Added

- The memory limit can now be specified using the `Memory` property -

    ```xml
    <profile pluginId="cd.go.contrib.elastic-agent.docker" id="foo">
      <property>
        <key>Memory</key>
        <!-- You may use prefix B, KB, MB, GB -->
        <value>1024MB</value>
      </property>
    </profile>
    ```

## 0.2.0 - 2016-10-06

### Fixed

- Fix some synchronization issues that allowed more number of containers than the settings permitted

## 0.1.0 - 2016-10-01

Initial release of plugin
