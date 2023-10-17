package BlogWiz.program;
	
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.*;

import BlogWiz.program.service.FileService;
import com.jcraft.jsch.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlogWizGUI extends Application {
    private TextField usernameField;
    private PasswordField passwordField;
	private TextField serverField;
	private TextField portField;
    private Button connectButton;
    private TextArea logTextArea;
    private TextArea sourceCodeTextArea;
    private HBox centerBox;
    private FileExplorerTreeView fileExplorerTreeView = new FileExplorerTreeView();

    private ExecutorService executorService;
    private SimpleDateFormat dateFormat;
    
    private AuthenticationManager authManager;
    
    private Button getButton;
    private Button updateButton;
    private Button createFolderButton;
    private Button createFileButton;
    private Button deleteButton;
    private FileService fileService;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("BlogWiz Tool");

        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();

		Label serverLabel = new Label("Server:");
        serverField = new TextField();
		
		Label portLabel = new Label("Port:");
        portField = new TextField();
		
        connectButton = new Button("Connect");
        connectButton.setOnAction(e -> handleConnect());

        logTextArea = new TextArea();
		
        logTextArea.setEditable(false);

        sourceCodeTextArea = new TextArea();
        sourceCodeTextArea.setWrapText(true);

	     // Create layout
        HBox topBox = new HBox(10); // Top container for username, password, and connect button
        topBox.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, serverLabel, serverField, portLabel, portField, connectButton);

        // Set Hgrow policy for sourceCodeTextArea and fileExplorerTreeView
		HBox.setHgrow(sourceCodeTextArea, Priority.ALWAYS);
		HBox.setHgrow(fileExplorerTreeView, Priority.ALWAYS);

		centerBox = new HBox(40); // Center container for source code and file explorer
		centerBox.getChildren().addAll(sourceCodeTextArea, fileExplorerTreeView);

		// Add constraints to make them fill the width
		HBox.setHgrow(sourceCodeTextArea, Priority.ALWAYS);
		HBox.setHgrow(fileExplorerTreeView, Priority.ALWAYS);

        VBox mainLayout = new VBox(10); // Main container for topBox, centerBox, and logTextArea
        mainLayout.setPadding(new Insets(10));
        
        // Create buttons
        getButton = new Button("Get");
        updateButton = new Button("Update");
        createFolderButton = new Button("Create Folder");
        createFileButton = new Button("Create File");
        deleteButton = new Button("Delete");

        // Set button actions
        getButton.setOnAction(e -> handleGet());
        updateButton.setOnAction(e -> handleUpdate());
        createFolderButton.setOnAction(e -> handleCreate(false));
        createFileButton.setOnAction(e -> handleCreate(true));
        deleteButton.setOnAction(e -> handleDelete());

        // Create button container
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(getButton, updateButton, createFolderButton, createFileButton, deleteButton);

        // Add button container to the main layout
        mainLayout.getChildren().addAll(topBox, centerBox, buttonBox, logTextArea);

        Scene scene = new Scene(mainLayout);
        primaryStage.setScene(scene);

        primaryStage.setResizable(false);

        primaryStage.show();

        executorService = Executors.newCachedThreadPool();

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ");
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
    }

    private String getSelectedPath(TreeItem<String> selectedItem) {
    	String username = usernameField.getText();
        String firstLetter = username.substring(0, 1);
        String directory = String.format("../../../www/home/%s/%s/", firstLetter, username);
        
        StringBuilder path = new StringBuilder();

        while (selectedItem != null && !selectedItem.getValue().equals("Root")) {
            if (!path.toString().isEmpty()) {
                path.insert(0, "/");
            }
            path.insert(0, selectedItem.getValue());
            selectedItem = selectedItem.getParent();
        }

        return directory + path.toString();
    }

    private void handleGet() {
        TreeItem<String> selectedItem = fileExplorerTreeView.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            appendLog("No file selected.");
            return;
        }

        String selectedPath = getSelectedPath(selectedItem);
                
        CompletableFuture<Void> getFileFuture = CompletableFuture.runAsync(() -> {
            try {
                String fileContent = fileService.getFileContent(selectedPath);

                Platform.runLater(() -> {
                    sourceCodeTextArea.setText(fileContent);
                    appendLog("File content retrieved.");
                });
            } catch (Exception e) {
                e.printStackTrace(); // Handle the exception appropriately, e.g., show an error message
            }
        }, executorService);

        getFileFuture.exceptionally(ex -> {
            Platform.runLater(() -> {
                appendLog("Error during file retrieval.");
                ex.printStackTrace();
            });
            return null;
        });
    }

    private void handleUpdate() {
        TreeItem<String> selectedItem = fileExplorerTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String selectedPath = getSelectedPath(selectedItem);
            if (selectedPath != null) {
                String newContent = sourceCodeTextArea.getText();
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        fileService.updateFile(selectedPath, newContent);
                        Platform.runLater(() -> {
                            appendLog("File updated successfully.");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            appendLog("Error updating file.");
                            e.printStackTrace();
                        });
                    }
                }, executorService);
            }
        }
    }

    private void handleCreate(boolean isFile) {
        FileExplorerTreeItem selectedDirectory = (FileExplorerTreeItem) fileExplorerTreeView.getSelectionModel().getSelectedItem();
        if (selectedDirectory != null) {
            String directoryPath = getSelectedPath(selectedDirectory);
            if (directoryPath != null) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Create File");
                dialog.setHeaderText("Enter file name:");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(fileName -> {
                    // Create a final array to hold the modified path
                    final String[] finalDirectoryPath = {directoryPath};

                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            // Remove everything after the last backslash
                            int lastBackslashIndex = finalDirectoryPath[0].lastIndexOf('/');
                            if (lastBackslashIndex != -1 && !selectedDirectory.isDirectory) {
                                finalDirectoryPath[0] = finalDirectoryPath[0].substring(0, lastBackslashIndex + 1); // Include the backslash
                            }

                            // Create the file inside the selected directory
                            fileService.createFile(finalDirectoryPath[0] + '/' + fileName, isFile);
                            Platform.runLater(() -> {
                                appendLog("File created successfully.");
                                // Refresh the file explorer
                                populateFileExplorer(authManager);
                            });
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                appendLog("Error creating file.");
                                e.printStackTrace();
                            });
                        }
                    }, executorService);
                });
            }
        }
    }

    private void handleDelete() {
        FileExplorerTreeItem selectedItem = (FileExplorerTreeItem) fileExplorerTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String selectedPath = getSelectedPath(selectedItem);
            System.out.println(selectedPath);
            boolean directoryIs = selectedItem.isDirectory;
            if (selectedPath != null) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        fileService.deleteFile(selectedPath, directoryIs);

                        Platform.runLater(() -> {
                            appendLog(directoryIs ? "Directory deleted successfully." : "File deleted successfully.");
                            // Refresh the file explorer
                            populateFileExplorer(authManager);
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            appendLog(directoryIs ? "Error deleting directory." : "Error deleting file.");
                            e.printStackTrace();
                        });
                    }
                }, executorService);
            }
        }
    }

    private void handleConnect() {
        // Get the username and password from the UI
        String username = usernameField.getText();
        String password = passwordField.getText();
		String server = serverField.getText();
		int port = Integer.parseInt(portField.getText());

        // Perform authentication asynchronously
        CompletableFuture<Void> authenticationFuture = CompletableFuture.runAsync(() -> {
            try {
                authManager = new AuthenticationManager();

                if (connectButton.getText().equals("Connect")) {
                    authManager.connect(server, port, username, password);

                    // Update UI on the JavaFX application thread
                    Platform.runLater(() -> {
                        appendLog("Connected successfully.");
                    	
                        fileService = new FileService(authManager);
                        
                        connectButton.setText("Disconnect"); // Toggle button text
                    });

                    // Populate the file explorer
                    if (fileExplorerTreeView != null) {
                        populateFileExplorer(authManager);
                    }
                } else {
                    authManager.disconnect();
					
                    // Update UI on the JavaFX application thread
                    Platform.runLater(() -> {
						fileExplorerTreeView = new FileExplorerTreeView();
						sourceCodeTextArea.clear();
                        appendLog("Disconnected.");
                        connectButton.setText("Connect"); // Toggle button text
                    });
                }
            } catch (JSchException e) {
                // Update UI on the JavaFX application thread
                Platform.runLater(() -> {
                    appendLog("Authentication failed.");
                    e.printStackTrace();
                });
            }
        }, executorService);

        authenticationFuture.exceptionally(ex -> {
            // Update UI on the JavaFX application thread
            Platform.runLater(() -> {
                appendLog("Error during authentication.");
                ex.printStackTrace();
            });
            return null;
        });
    }

    private void populateFileExplorer(AuthenticationManager authManager) {
        try {
            String username = usernameField.getText();
            String firstLetter = username.substring(0, 1);
            String directory = String.format("../../../www/home/%s/%s", firstLetter, username);
            String commandOutput = authManager.executeShellCommand(directory, authManager.session);

            // Create a new root node for the file explorer
            TreeItem<String> rootItem = new TreeItem<>("Root");

            // Use Platform.runLater to update the UI components from the JavaFX Application Thread
            Platform.runLater(() -> {
                fileExplorerTreeView.setRoot(rootItem);

                // Split the command output into lines
                String[] lines = commandOutput.split("\n");

                for (String line : lines) {
                    // Trim any leading or trailing whitespace
                    line = line.trim();

                    if (line.startsWith("Directory:")) {
                        // If it's a directory entry, create a new node
                        String prefix = String.format("Directory: ../../../www/home/%s/%s/", firstLetter, username);

                        if (line.startsWith(prefix)) {
                            String directoryName = line.substring(prefix.length()).trim();
                            String[] directoryParts = directoryName.split("/");

                            TreeItem<String> currentNode = rootItem;
                            boolean excludeInitial = true;

                            for (String part : directoryParts) {
                                // Exclude initial directories matching the root path
                                if (excludeInitial && part.equals(firstLetter)) {
                                    continue;
                                } else if (excludeInitial && part.equals(username)) {
                                    excludeInitial = false;
                                    continue;
                                }

                                // Check if the directory node already exists
                                boolean exists = false;
                                for (TreeItem<String> child : currentNode.getChildren()) {
                                    if (child.getValue().equals(part)) {
                                        currentNode = child;
                                        exists = true;
                                        break;
                                    }
                                }

                                if (!exists) {
                                    // Create a new directory node
                                    FileExplorerTreeItem newDirectory = new FileExplorerTreeItem(part, true);
                                    currentNode.getChildren().add(newDirectory);
                                    currentNode = newDirectory;
                                }
                            }
                        }
                    } else if (line.startsWith("File:")) {
                        // If it's a file entry, add it to the correct directory
                        String prefix = String.format("File: ../../../www/home/%s/%s/", firstLetter, username);

                        if (line.startsWith(prefix)) {
                            String fileName = line.substring(prefix.length()).trim();
                            String[] filePathParts = fileName.split("/");
                            String fileNameOnly = filePathParts[filePathParts.length - 1];

                            // Get the directory path excluding the file name
                            String directoryPath = fileName.substring(0, fileName.length() - fileNameOnly.length());

                            String[] directoryParts = directoryPath.split("/");
                            TreeItem<String> currentNode = rootItem;

                            for (String part : directoryParts) {
                                for (TreeItem<String> child : currentNode.getChildren()) {
                                    if (child.getValue().equals(part)) {
                                        currentNode = child;
                                        break;
                                    }
                                }
                            }

                            // Create a new file node
                            FileExplorerTreeItem newFile = new FileExplorerTreeItem(fileNameOnly, false);
                            currentNode.getChildren().add(newFile);
                        }
                    }
                }
            });
        } catch (JSchException e) {
            appendLog("Error during file explorer population.");
            e.printStackTrace();
        }
    }
    
    private void appendLog(String message) {
        String timestamp = dateFormat.format(new Date());
        Platform.runLater(() -> {
            logTextArea.appendText(timestamp + " " + message + "\n");
        });
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
