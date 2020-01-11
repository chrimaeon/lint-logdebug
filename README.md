# Android Lint for log output [![CircleCI](https://circleci.com/gh/chrimaeon/lint-logdebug.svg?style=svg)](https://circleci.com/gh/chrimaeon/lint-logdebug)

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=for-the-badge)](https://www.apache.org/licenses/LICENSE-2.0)
[![Bintray](https://img.shields.io/bintray/v/chrimaeon/maven/com.cmgapps.lint%3Alint-logdebug.svg?style=for-the-badge)](https://bintray.com/chrimaeon/maven/com.cmgapps.lint%3Alint-logdebug)

Check your code for missing conditional surrounding your log output.

The `BuildConfig` class provides a constant, `DEBUG`, which indicates whether the code is being built in release mode
or in debug mode. In release mode, you typically want to strip out all the logging calls. Since the compiler will
automatically remove all code which is inside a `if (false)` check, surrounding your logging calls with a check for 
`BuildConfig.DEBUG` is a good idea.

If you *really* intend for the logging to be present in release mode, you can suppress this warning with a `@SuppressLint`
annotation for the intentional logging calls.

## Usage

Add this to your dependencies in the modules `build.gradle`

```kotlin
dependencies {
    lintChecks("com.cmgapps.lint:lint-log:<version>")
}
```
For the latest version, please check [Bintray](https://bintray.com/chrimaeon/maven/com.cmgapps.lint%3Alint-log/_latestVersion)

## License

```text
Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>

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
