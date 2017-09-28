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
