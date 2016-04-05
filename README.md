# GoCD Elastic agent plugin skeleton

This is merely a skeleton plugin that plugin developers can fork to get quickly 
started with writing elastic agent plugins for GoCD.

## Getting started

* Edit the file `build.gradle`
* Edit the `GetPluginConfigurationExecutor.java` class to add any configuration fields that should be shown in the view.
* Edit the `plugin-settings.template.html` file which contains the view for the plugin settings page of your plugin.
* Edit the `PluginSettings.java` file which contains the model for your settings.
* Implement the `ExampleAgentInstances.java` class to get a really basic elastic agent plugin working.

## Building the code base

To build the jar, run `./gradlew clean test assemble`

## License

```plain
Copyright 2016 ThoughtWorks, Inc.

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
