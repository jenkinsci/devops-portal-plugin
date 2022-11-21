# DevOps Portal Jenkins Plugin

A Jenkins Dashboard Plugin with many features :

1. Offer a specific **dashboard** to track 🚀 **RUN operations**
    - Environments **monitoring** (service availability)
    - HTTPS certificate validity and expiration monitoring
    - **Operations** tracking, like Deployments or Rollback (target environment, application and version, related run)
2. Offer a specific **dashboard** to bring together all applications 📦 **BUILD activities**
    - Display all built **applications with versions**
    - Link to the last build run
    - Last application **deployment** information are also displayed
    - Gather useful information in the same place: **artifacts** built and size of them, **unit tests** performed,
      **code quality** metrics, application **performance** metrics and published containers **images**

Current supported translations: 🇫🇷 🇬🇧

## ⚡ Manage Environments

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

## 🚀 Manage Run Operations

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

### Report a run operation using the Jenkins interfaces (GUI)

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

### Report run operation with pipeline script (DSL)

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

## 📦 Manage Build Activities

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
  - **Build artifacts**: artifact file size
  - **Unit test report**: number of passed/failed/skipped tests, coverage and score
  - **Code Quality & Security Audit**: designed to gather SonarQube metrics into Jenkins, it displays the Quality Gate
    status, number of bugs/vulnerabilities/hotspot, code duplication and code volume. Hence, it also
    displays scores according to the quality gate.
  - **Performance/load testing report**: score and Quality Gate status, number of load request and
  the average response time (in milliseconds)
  - **Released container image**: keep track of container images built and published to a registry

**Note**: you can filter applications to display on the dashboard using `Edit View`. Regular expressions are supported.

Once the dashboard is created, you can feed it using a **reporter**.

### Report a build activity using Jenkins Interface (GUI)

You can report build activities using a special build task.
In the `Configure` screen of a job, click on `Add Build Step` button and choose one among:

- Record a build report
- Record a UT report
- Record a quality audit
- Record a performance test
- Record an image release

### Report build activity with pipeline script (DSL)

The report can also be made using a Groovy Pipeline script using these commands:

```
reportBuild(
    applicationName: string,       // Name of application built
    applicationVersion: string,    // Version of application built
    applicationComponent: string   // Name of application component built
    artifactFileName: string       // Full path to generated artifact
)
```

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
```

```
reportDependenciesAnalysis(
    applicationName: string,       // Name of application built
    applicationVersion: string,    // Version of application built
    applicationComponent: string   // Name of application component built
    baseDirectory: string,         // Path to component source code folder (containing dependencies file)
    manager: string                // Choose amoung: 'MAVEN' or 'NPM'
)
```

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
- [x] ~~reportBuild()~~
- [ ] reportUnitTest() : remove testCoverage
- [ ] reportSurefireTest()
- [ ] reportQualityAudit()
- [ ] reportDependenciesAnalysis()
  - [ ] Maven
    - [x] ~~Dependencies~~
    - [ ] Vulnerabilities = ^(.+?)(\\.*?)? \((.*?)\) : (CVE.*)$
  - [ ] NPM
      - [ ] Dependencies
      - [ ] Vulnerabilities
- [ ] reportPerformanceTest()
- [ ] reportJmeterPerformanceTest()
- [x] ~~reportImageRelease()~~
- [x] ~~reportRunOperation()~~
