# EBU6304 Group 17 TA Recruitment System

This package contains the final software submission for Group 17.

## Contents

- `source_code/` - Java source code, resources, Maven project file, sample JSON data, and supporting project documents.
- `test_programs/` - automated test source files and the latest Maven Surefire test reports.
- `code_documentation/` - generated JavaDocs and additional implementation documentation.
- `user_manual/` - user manual with screenshots for the main application frames.

## Prerequisites

- JDK 21 or later.
- Maven 3.6 or later.
- Internet access for the first Maven run if dependencies are not already cached.

## Setup

1. Extract `Software_group17.zip`.
2. Open a terminal in the extracted `source_code/` directory.
3. Install dependencies and compile the software:

```bash
mvn test
```

## Running the Software

The Maven JavaFX plugin is configured to launch the TA position-list interface:

```bash
mvn javafx:run
```

To launch the full login workflow, run `LoginScreen.LoginMain` from an IDE such as IntelliJ IDEA/Eclipse, or update the `mainClass` value in `pom.xml` to:

```xml
<mainClass>LoginScreen.LoginMain</mainClass>
```

Then run:

```bash
mvn javafx:run
```

## Sample Accounts

| Role | Account | Password | Notes |
|------|---------|----------|-------|
| TA | `TAPan` | `123456` | Sample TA profile |
| MO | `MOPan` | `123456` | Sample module organiser |
| Admin | `Admin001` | `123456` | Admin registration code: `BUPTAdmin` |

## Running Tests

From `source_code/`:

```bash
mvn test
```

The latest test reports are also included under `test_programs/surefire-reports/`.

## Documentation

- Open `code_documentation/apidocs/index.html` for generated JavaDocs.
- Open `user_manual/User_Manual.md` for the user guide and screenshots.
