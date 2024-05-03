
# JavaFX Desktop Application Setup

## Overview
This JavaFX application provides a user interface for managing user logins and handling file uploads. It supports different user roles and integrates functionalities such as PDF file uploads.

## Prerequisites
- JDK 11 or newer
- JavaFX SDK (compatible with your JDK version)
- Maven (for dependency management)
- IDE that supports Java (e.g., IntelliJ IDEA, Eclipse)

## Required Libraries
- JavaFX SDK
- Apache PDFBox
- Apache Commons IO
- Firebase Admin SDK
- Google OAuth2

## Module Configuration
Ensure that the `module-info.java` file is configured with the necessary modules:
```java
module com.vybhav.ecko {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.io;
    requires org.apache.pdfbox;
    requires com.google.auth.oauth2;
    requires firebase.admin;
    requires com.google.auth;
    requires org.json;
    requires java.desktop;

    opens com.vybhav.ecko to javafx.fxml;
    exports com.vybhav.ecko;
}
```

## Setup Instructions

### 1. Clone the Repository
Clone the project repository to your local machine using the following command:
```
git clone <repository-url>
```
Replace `<repository-url>` with the actual URL of your Git repository.

### 2. Import the Project
Open your preferred IDE and import the project. Most IDEs have an option to import projects from existing sources.

### 3. Add JavaFX Library
Add the JavaFX SDK as a library in your project settings. Ensure that the path to the JavaFX `lib` folder is correctly set.

### 4. Configure VM Options
To run the application from your IDE, add the following VM options to your run configurations:
```
--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
```
Replace `/path/to/javafx-sdk/lib` with the actual path to the JavaFX library.

### 5. Build and Run
Build the project using your IDE's build tools and run the application. The main class to run is `App.java` or `HelloApplication.java`, depending on your project's entry point.

## Contributing
Contributions to this project are welcome. Please ensure that you follow the existing code structure and submit a pull request for review.
