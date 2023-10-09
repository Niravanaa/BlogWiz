# BlogWiz - A Simple Blog Management Tool

BlogWiz is a Java-based desktop application for managing your blog content hosted on a remote server. It allows you to connect to a server via SSH, navigate your blog's directory structure, view and edit blog post files, and perform basic file management operations such as creating folders and files.

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Features

- **SSH Authentication**: Securely connect to your remote server using SSH authentication.
- **File Explorer**: Browse and navigate your blog's directory structure.
- **Text Editor**: View and edit the content of your blog post files with a built-in text editor.
- **File Management**: Create new folders and files, update existing files, and delete files or folders.
- **Logging**: Real-time log display to keep track of your actions and any potential errors.
- **User-Friendly Interface**: A simple and intuitive user interface for effortless blog management.

## Getting Started

### Prerequisites

Before you begin, ensure you have met the following requirements:

- **Java Development Kit (JDK)**: Make sure you have Java 11 or later installed on your system.
- **JavaFX**: If you are using Java 11 or later, you'll need to install JavaFX separately. You can download it from the [OpenJFX website](https://openjfx.io/).
- **Maven**: You need Maven installed to build and manage the project dependencies.

### Installation

Follow these steps to set up and run BlogWiz:

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/Niravanaa/BlogWiz.git
   ```

2. **Navigate to the Project Directory**:

   ```bash
   cd BlogWiz
   ```

3. **Build the Project**:

   Use Maven to build the project and resolve dependencies:

   ```bash
   mvn spring-boot:run
   ```

4. **Run the Application**:

   After building the project, you can run the application using the following command:

   ```bash
   java -jar target/BlogWiz-1.0-SNAPSHOT.jar
   ```

   Replace `1.0-SNAPSHOT` with the actual version specified in your `pom.xml` file.

## Usage

1. **Launch the Application**: Run the application using the instructions provided in the Installation section.

2. **Connect to Your Server**:
   - Enter your SSH credentials (Username and Password) in the respective fields.
   - Click the "Connect" button to establish an SSH connection to your server.

3. **Browse Your Blog Directory**:
   - The left side of the application displays your blog's directory structure as a tree view.
   - Click on folders to expand or collapse them.
   - Select a folder or file by clicking on it.

4. **View and Edit Files**:
   - When you select a file, its content will be displayed in the text editor on the right.
   - Edit the content as needed.

5. **File Operations**:
   - Use the "Get" button to retrieve the content of a file.
   - Use the "Update" button to save changes to a file.
   - Use the "Create Folder" and "Create File" buttons to add new directories and files.
   - Use the "Delete" button to remove selected files or folders.

6. **Logging**:
   - The application provides a real-time log display at the bottom to keep track of your actions and any potential errors.

## Contributing

Contributions are welcome! If you'd like to contribute to this project, please follow these steps:

1. Fork the repository.
2. Create a new branch for your feature or bug fix: `git checkout -b feature/your-feature-name` or `bugfix/issue-number`.
3. Commit your changes: `git commit -m "Your descriptive commit message"`.
4. Push to your branch: `git push origin feature/your-feature-name` or `bugfix/issue-number`.
5. Open a Pull Request on GitHub.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.