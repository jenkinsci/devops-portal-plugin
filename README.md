# DevOps Portal Jenkins Plugin

A Jenkins Dashboard Plugin with many features :

1. Offer a specific **dashboard** to track 🚀 **[RUN operations](#section-run)**
    - Environments **monitoring** (service availability)
    - HTTPS certificate validity and expiration monitoring
    - **Operations** tracking, like Deployments or Rollback (target environment, application and version, related run)
2. Offer a specific **dashboard** to bring together all applications 📦 **[BUILD activities](#section-build)**
    - Display all built **applications with versions**
    - Link to the last build run
    - Last application **deployment** information are also displayed
    - Gather useful information in the same place: **artifacts** built and size of them, **unit tests** performed,
      **code quality** metrics, application **performance** metrics and published containers **images**

Current supported translations: 🇫🇷 🇬🇧

## <a name="section-manage"></a> ⚡ Manage Environments

In Jenkins Administration, a link allows to configure managed environments:

![xxxx](.doc/PluginManagementLink.png)

Then you can configure each environment to manage:

![xxxx](.doc/ServiceConfiguration.png)

You have to provide:
- An unique label
- A category (like production, staging, ...)
- An optional monitoring URL
- A time interval (in minutes) between two monitoring checks
- A flag to accept invalid certificates (for monitoring URL)

## <a name="section-run"></a> 🚀 Manage Run Operations

### Dashboard

Since you configured your environments, you can create a dashboard.

Create new dashboard using: `View` > `+ button` > View type: `Run Dashboard`

Example dashboard :

![Run Dashboard](.doc/RunDashboard.png)

The dashboard provides some information:

- Display all environments grouped by categories
- Display a status icon according to monitoring result

|                        Icon                         | Meaning                                                        |
|:---------------------------------------------------:|----------------------------------------------------------------|
| ![Icon Success](.doc/MonitoringStatusAvailable.png) | Successful connection                                          |
|  ![Icon Failure](.doc/MonitoringStatusFailure.png)  | Connection failure                                             |
|    ![Icon Alert](.doc/MonitoringStatusAlert.png)    | HTTPS configuration issue (expired or self signed certificate) |
| ![Icon Disabled](.doc/MonitoringStatusDisabled.png) | Monitoring is disabled                                         |

- Show the certificate expiration (if the given monitoring URL is HTTPS)
- Display the last deployment information: application, version and jenkins run
- Also display the deployment tags, which allows to describe the deployment process
  (Eg. `ansible`, `ssh`, `ftp`)

**Note**: you can filter environment categories to display on the dashboard using `Edit View`. Regular expressions are supported.

Once the dashboard is created, you can feed it using a **reporter**.

#### Report a run operation using the Jenkins interfaces (GUI)

You can report run operations, such as Deployment, using a special build task.
In the `Configure` screen of a job, click on `Add Build Step` button and choose
`Record a run operation`.

![Run Operation Reporter](.doc/RunOperationReporter.png)

You have to fill in:

- The target environment name (declared previously in `Manage Environments`)
- The name of concerned application
- The version of concerned application
- The performed operation:
  - `DEPLOYMENT`
  - `ROLLBACK`
- A success/failure status
- Optionally, you can add tags to describe the operation (comma-separated)

#### Report run operation with pipeline script (DSL)

The report can also be made using a Groovy Pipeline script using this command:

```
reportRunOperation(
    targetService: string,      // Name for target environnement to deploy to
    applicationName: string,    // Name of application deployed
    applicationVersion: string, // Version of application deployed
    operation: string,          // Operation name
    status: boolean,            // Status
    tags?: string               // Optional: comma-separated list
)
```

## <a name="section-build"></a> 📦 Manage Build Activities

This plugin also allow to track many metrics of the software development.

Create new dashboard using: `View` > `+ button` > View type: `Build Dashboard`

Example Dashboard :

![Build Dashboard](.doc/BuildDashboard.png)

The dashboard bring together much information:

- List all applications and versions
- Display last run with status
- If possible, display related VCS branch and commit (only GIT actually)
- Display the last deployment target environment
- Also, it can display a lot of activities:
  - **[Build artifacts](#activity-build)**: artifact file size
  - **[Unit test report](#activity-ut)**: number of passed/failed/skipped tests, coverage and score
  - **[Code Quality & Security Audit](#activity-quality)**: designed to gather SonarQube metrics into Jenkins,
  - it displays the Quality Gate status, number of bugs/vulnerabilities/hotspot, code duplication and code
  - volume. Hence, it also displays scores according to the quality gate.
  - **[Dependencies Analysis report](#activity-dependencies)**: score and Quality Gate status, number of load request and
  - **[Performance/load testing report](#activity-performance)**: score and Quality Gate status, number of load request and
  the average response time (in milliseconds)
  - **[Released container image](#activity-release)**: keep track of container images built and published to a registry

**Note**: you can filter applications to display on the dashboard using `Edit View`. Regular expressions are supported.

Once the dashboard is created, you can feed it using a **reporter**.

### <a name="activity-build"></a> Activity: Artifact build

You can report build activities using a special build step.
In the `Configure` screen of a job, click on `Add Build Step` button and choose one among:

| Build step              |
|-------------------------|
| `Record a build report` |

With pipeline script (DSL):

```
reportBuild(
    applicationName: string,       // Name of application built
    applicationVersion: string,    // Version of application built
    applicationComponent: string   // Name of application component built
    artifactFileName: string       // Full path to generated artifact
)
```

Dashboard preview:

![ActivityBuild](.doc/ActivityBuild.png)

### <a name="activity-ut"></a> Activity: Unit Test

You can report build activities using a special build step.
In the `Configure` screen of a job, click on `Add Build Step` button and choose one among:

| Build step                    |
|-------------------------------|
| `Record a UT report manually` |
| `Record a Surefire UT report` |

With pipeline script (DSL):

```
reportUnitTest(
    applicationName: string,       // Name of application built
    applicationVersion: string,    // Version of application built
    applicationComponent: string   // Name of application component built
    testCoverage?: float,          // Optional: coverage ratio (between 0-1)
    testsPassed?: int,             // Optional: number of passed tests
    testsFailed?: int,             // Optional: number of failed tests
    testsIgnored?: int             // Optional: number of skipped tests
)

reportSurefireTest(
    applicationName: string,       // Name of application built
    applicationVersion: string,    // Version of application built
    applicationComponent: string   // Name of application component built
    surefireReportPath: string     // Path to the Surefire report file
)
```

Dashboard preview:

![ActivityUnitTest](.doc/ActivityUnitTest.png)

###  <a name="activity-quality"></a> Activity: Code Quality audit

You can report build activities using a special build step.
In the `Configure` screen of a job, click on `Add Build Step` button and choose one among:

| Build step               |
|--------------------------|
| `Record a quality audit` |

With pipeline script (DSL):

⛔ TODO

Dashboard preview:

⛔ TODO

###  <a name="activity-dependencies"></a> Activity: Dependency analysis

You can report build activities using a special build step.
In the `Configure` screen of a job, click on `Add Build Step` button and choose one among:

| Build step                       |
|----------------------------------|
| `Record a dependencies analysis` |

With pipeline script (DSL):

```
reportDependenciesAnalysis(
    applicationName: string,       // Name of application built
    applicationVersion: string,    // Version of application built
    applicationComponent: string   // Name of application component built
    manager: string,               // Only 'MAVEN' is supported actually
    manifestFile: string,          // Path to project manifest file (pom.xml)
    managerCommand?: string        // Optional: shell command to run the manifest
                                   // If not provided, the plugin will try to guess it 
)
```

Dashboard preview:

![ActivityDependenciesAnalysis](.doc/ActivityDependenciesAnalysis.png)

###  <a name="activity-performance"></a> Activity: Performance test

You can report build activities using a special build step.
In the `Configure` screen of a job, click on `Add Build Step` button and choose one among:

| Build step                  |
|-----------------------------|
| `Record a performance test` |

With pipeline script (DSL):

⛔ TODO

Dashboard preview:

⛔ TODO

###  <a name="activity-release"></a> Activity: Container image release

You can report build activities using a special build step.
In the `Configure` screen of a job, click on `Add Build Step` button and choose one among:

| Build step                |
|---------------------------|
| `Record an image release` |

With pipeline script (DSL):

```
reportImageRelease(
    applicationName: string,       // Name of application built
    applicationVersion: string,    // Version of application built
    applicationComponent: string   // Name of application component built
    registryName?: string,         // Optional: registry server name
    imageName?: string,            // Optional: image name released
    tags?: string                  // Optional: comma-separated list of image's tags
)
```

Dashboard preview:

![ActivityImageRelease](.doc/ActivityImageRelease.png)

## Setup as Developer

1. Checkout from: https://github.com/rbello/jenkins-plugin-devops-portal.git
2. Recommended IDE is **Intellij IDEA**
3. JDK 11 is preferred (newer JDK may introduce serialization issues)
4. The minimal Jenkins version is: 2.346.1
5. Run locally with: `mvn hpi:run -Djetty.port=5000`
6. Create HPI package with: `mvn hpi:hpi`
7. Suggest any change by Forking the project and opening a Pull Request
8. Release with: `mvn release:prepare release:perform -Dusername=****** -Dpassword=******`

## Author & Licence

This plugin is provided by Rémi BELLO \
https://github.com/rbello/jenkins-plugin-devops-portal

**Licence** \
GNU GENERAL PUBLIC LICENSE \
Version 3, 29 June 2007

## TODO

- [ ] Encode FR translations characters
- [ ] Ensure JenkinsUtils.getBuild() works with:
  - [ ] Folders
  - [ ] Multibranch Pipelines
- [ ] TU
- [ ] Dark theme compatibility
- [ ] Synchronize I/O methods
- [ ] Build dashboard: delete entry
- [x] ~~reportBuild()~~
- [x] ~~reportUnitTest()~~
- [x] ~~reportSurefireTest()~~
- [ ] reportQualityAudit()
- [x] ~~reportDependenciesAnalysis()~~
  - [x] ~~Maven~~
    - [x] ~~Dependencies~~
    - [x] ~~Vulnerabilities~~
  - [ ] NPM
      - [ ] Dependencies
      - [ ] Vulnerabilities
- [x] ~~reportPerformanceTest()~~
- [ ] reportJmeterPerformanceTest()
- [x] ~~reportImageRelease()~~
- [x] ~~reportRunOperation()~~
