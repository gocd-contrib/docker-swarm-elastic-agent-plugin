## 1.1.0 - 2016-11-19

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
