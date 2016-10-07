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
