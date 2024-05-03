package com.vybhav.ecko;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.awt.Desktop;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


public class HelloController {
    @FXML
    public Button uploadButton;

    @FXML
    private TextField searchField;

    @FXML
    private Label totalPageCountLabel;

    @FXML
    private Label totalA4PageCountLabel;

    @FXML
    private Label totalA3PageCountLabel;

    @FXML
    private Label totalLegalPageCountLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private TreeView<File> fileTree;

    private static final String ROOT_DIR = "C:/менеджерпапок/";
    private static final String PAGE_COUNT_FILE = "pageCount.txt";
    private static final String A4_PAGE_COUNT_FILE = "a4PageCount.txt";

    private static final String A3_PAGE_COUNT_FILE = "a3PageCount.txt";
    private static final String LEGAL_PAGE_COUNT_FILE = "legalPageCount.txt";
    private int totalPageCount;
    private int totalA4PageCount;
    private int totalA3PageCount;
    private int totalLegalPageCount;

    private boolean uploadInProgress = false;

    private List<String> expandedPaths = new ArrayList<>();

    @FXML
    private void initialize() {

        Image folderIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/folder-new.png")));
        Image fileIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pdf.png")));

        fileTree.setCellFactory(tv -> new TreeCell<>() {
            private final ImageView imageView = new ImageView();
            private final ContextMenu contextMenu = new ContextMenu();

            {
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);

                MenuItem newFolderMenuItem = new MenuItem("New Folder");
                newFolderMenuItem.setOnAction(event -> createNewFolder(getTreeItem().getValue()));
                contextMenu.getItems().add(newFolderMenuItem);
            }

            @Override
            public void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                } else {
                    imageView.setImage(item.isDirectory() ? folderIcon : fileIcon);
                    setText(item.getName());
                    setGraphic(imageView);
                    if (item.isDirectory()) {
                        setContextMenu(contextMenu);
                    } else {
                        setContextMenu(null);
                    }
                }
            }
        });

        fileTree.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                TreeItem<File> selectedItem = fileTree.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    File file = selectedItem.getValue();
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                        openPDFFile(file);
                    }
                }
            }
        });

//        fileTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            // Handle selection changes here if needed
//        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTree(newValue.toLowerCase());
        });

        refreshFileTree();

        loadPageCount();
        loadA4PageCount();
        loadA3PageCount();
        loadLegalPageCount();

        updatePageCountLabel();
        updateA4PageCountLabel();
        updateA3PageCountLabel();
        updateLegalPageCountLabel();

        logoutButton.setOnAction(e -> logout());

    }

    private void filterTree(String searchText) {
        if (searchText.isEmpty()) {
            // Clear the filter and display all nodes
            TreeItem<File> root = createNode(new File(ROOT_DIR));
            fileTree.setRoot(root);
            fileTree.setShowRoot(false);
        } else {
            // Filter the tree nodes based on the search text
            TreeItem<File> filteredRoot = createFilteredFileTree(ROOT_DIR, searchText);
            fileTree.setRoot(filteredRoot);
        }
    }

    private TreeItem<File> createFilteredFileTree(String directoryPath, String searchText) {
        TreeItem<File> root = new TreeItem<>(new File(directoryPath));
        root.setExpanded(true);

        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    TreeItem<File> folderItem = createFilteredFileTree(file.getAbsolutePath(), searchText);
                    if (!folderItem.getChildren().isEmpty() || file.getName().toLowerCase().contains(searchText)) {
                        root.getChildren().add(folderItem);
                    }
                } else if (file.getName().toLowerCase().contains(searchText)) {
                    root.getChildren().add(new TreeItem<>(file));
                }
            }
        }

        return root;
    }

    private void openPDFFile(File file) {
        try {
            // Open the PDF file using the default application
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshFileTree() {
        TreeItem<File> root = createNode(new File(ROOT_DIR));
        fileTree.setRoot(root);
        fileTree.setShowRoot(false);
    }

    private TreeItem<File> createNode(final File file) {
        return new TreeItem<File>(file) {
            private boolean isLeaf;
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<File>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;

                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    isLeaf = file.isFile();
                }

                return isLeaf;
            }

            private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem) {
                File f = TreeItem.getValue();
                if (f != null && f.isDirectory()) {
                    File[] files = f.listFiles();
                    if (files != null) {
                        ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();

                        for (File childFile : files) {
                            children.add(createNode(childFile));
                        }

                        return children;
                    }
                }

                return FXCollections.emptyObservableList();
            }
        };
    }

    @FXML
    private void createNewFolderUnderRoot() {
        createNewFolder(new File(ROOT_DIR));
    }

    @FXML
    private void createNewFolderUnderSelected() {
        TreeItem<File> selectedItem = fileTree.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            createNewFolder(selectedItem.getValue());
        } else {
            // If no folder is selected, create the new folder under the root directory
            createNewFolder(new File(ROOT_DIR));
        }
    }

    private void createNewFolder(File parentDirectory) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("New Folder");
        dialog.setHeaderText("Create a new folder");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        Label label = new Label("Folder Name:");
        label.getStyleClass().add("dialog-label");

        TextField textField = new TextField();
        textField.getStyleClass().add("dialog-text-field");

        ImageView folderIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/folder.png"))));
        folderIcon.setFitHeight(30);
        folderIcon.setFitWidth(30);

        VBox vbox = new VBox(folderIcon, label, textField);
        vbox.setSpacing(10);
        dialogPane.setContent(vbox);

        ButtonType buttonTypeOk = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(buttonTypeOk);

        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return textField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            File newFolder = new File(parentDirectory, name);
            if (newFolder.exists()) {
                showAlert("A folder with the name \"" + name + "\" already exists in the selected directory.");
            } else {
                try {
                    Files.createDirectories(newFolder.toPath());
                    expandedPaths = getExpandedPaths(fileTree.getRoot());
                    refreshFileTree();
                    restoreExpandedState(fileTree.getRoot());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("WARNING");
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-pane");

        Label label = new Label(message);
        label.getStyleClass().add("alert-label");
        dialogPane.setContent(label);

        alert.showAndWait();

        ButtonType buttonTypeOk = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
        Button okButton = (Button) dialogPane.lookupButton(buttonTypeOk);
        if (okButton != null) {
            okButton.getStyleClass().add("alert-button");
        }
    }

    private void showAlertNEW(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        dialogPane.getStyleClass().add("alert-pane");

        Label label = new Label(message);
        label.getStyleClass().add("alert-label");
        dialogPane.setContent(label);

        alert.showAndWait();

        ButtonType buttonTypeOk = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
        Button okButton = (Button) dialogPane.lookupButton(buttonTypeOk);
        if (okButton != null) {
            okButton.getStyleClass().add("alert-button");
        }
    }

    private void showUploadSuccessPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Upload Success");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Customize the alert style
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        dialogPane.getStyleClass().add("alert-dialog");

        Label label = new Label(message);
        label.getStyleClass().add("alert-label");
        dialogPane.setContent(label);

        alert.showAndWait();

        ButtonType buttonTypeOk = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
        Button okButton = (Button) dialogPane.lookupButton(buttonTypeOk);
        if (okButton != null) {
            okButton.getStyleClass().add("alert-button");
        }
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root,800,650));
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void uploadPDF() {

        if (uploadInProgress) {
            showAlert("Cannot Upload PDFs while file Upload is in progress");
            return;
        }

        TreeItem<File> selectedItem = fileTree.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(new Stage());
            if (selectedFiles != null  && !selectedFiles.isEmpty()) {
                int totalFiles = selectedFiles.size();
                AtomicInteger successfulUploads = new AtomicInteger(0);
                uploadInProgress = true;
                uploadButton.setDisable(true);
                CompletableFuture.runAsync(() ->{
                    for (File selectedFile:selectedFiles){
                        try (PDDocument document = PDDocument.load(selectedFile)){
                            File destinationFile;
                            int numPages = document.getNumberOfPages();
                            String filename = FilenameUtils.removeExtension(selectedFile.getName());
                            String extension = FilenameUtils.getExtension(selectedFile.getName());
                            destinationFile = new File(selectedItem.getValue().getPath(), filename + "[" + numPages +" pages"+"]." + extension);
                            if (destinationFile.exists()) {
                                Platform.runLater(() -> showAlertNEW("File Exists", "File "+destinationFile.getName()+" already exists in the folder."));
                                continue; // Skip uploading the file
                            }
                            try {
                                Files.copy(selectedFile.toPath(), destinationFile.toPath());
                            } catch (NoSuchFileException e) {
                                e.printStackTrace();
                                showAlert("The destination folder does not exist.");
                                return;
                            }

                            int a4PageCount = 0;
                            int legalPageCount = 0;
                            int a3PageCount = 0;
                            int count = 1;
                            try (document) {
                                for (PDPage page : document.getPages()) {
                                    float width = page.getMediaBox().getWidth();
                                    float height = page.getMediaBox().getHeight();
                                     System.out.println("page size "+width+"and "+height+"count : "+count);
                                    count++;
                                    //doing +-10 for all width and height

                                    if (isA3Size(width, height)) {
                                        a3PageCount++;
                                    } else if (isLegalSize(width, height)) {
                                        legalPageCount++;
                                    } else if (isA4Size(width, height)) {
                                        a4PageCount++;
                                    } else  {
                                        a4PageCount++;
                                    }
                                }

                                if((a3PageCount+a4PageCount+legalPageCount)!=numPages){
                                    if((a3PageCount+a4PageCount+legalPageCount)<numPages){
                                        a4PageCount += numPages - (a3PageCount+a4PageCount+legalPageCount);
                                    }
                                }

                                totalPageCount += numPages;
                                totalA4PageCount += a4PageCount;
                                totalLegalPageCount += legalPageCount;
                                totalA3PageCount +=a3PageCount;

                                savePageCount();
                                saveA4PageCount();
                                saveA3PageCount();
                                saveLegalPageCount();

                                Platform.runLater(() -> {
                                    updatePageCountLabel();
                                    updateA4PageCountLabel();
                                    updateA3PageCountLabel();
                                    updateLegalPageCountLabel();
                                });
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            successfulUploads.incrementAndGet();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (successfulUploads.get() == totalFiles) {
                        if(totalFiles==1){
                            Platform.runLater(()->showUploadSuccessPopup("File uploaded successfully!"));
                        }else{
                            Platform.runLater(()->showUploadSuccessPopup("Files uploaded successfully!"));
                        }

                    } else {
                        Platform.runLater(() -> showAlertNEW("Upload Error", "Files failed to upload."));
                    }

                    uploadInProgress = false;

                    Platform.runLater(() -> {
                        uploadButton.setDisable(false);
                        expandedPaths = getExpandedPaths(fileTree.getRoot());
                        refreshFileTree();
                        restoreExpandedState(fileTree.getRoot());
                    });

                });
            }
        }
    }

    private List<String> getExpandedPaths(TreeItem<File> root) {
        List<String> expanded = new ArrayList<>();
        addExpandedPaths(root, expanded);
        return expanded;
    }

    private void addExpandedPaths(TreeItem<File> item, List<String> expanded) {
        if (item.isExpanded()) {
            File file = item.getValue();
            if (file != null) {
                String filePath = file.getAbsolutePath();
                expanded.add(filePath);
            }
            for (TreeItem<File> child : item.getChildren()) {
                addExpandedPaths(child, expanded);
            }
        }
    }

    private void restoreExpandedState(TreeItem<File> root) {
        for (TreeItem<File> item : root.getChildren()) {
            restoreExpandedStateRecursive(item);
        }
    }

    private void restoreExpandedStateRecursive(TreeItem<File> item) {
        File file = item.getValue();
        if (file != null) {
            String filePath = file.getAbsolutePath();
            if (expandedPaths.contains(filePath)) {
                item.setExpanded(true);
            }
        }
        for (TreeItem<File> child : item.getChildren()) {
            restoreExpandedStateRecursive(child);
        }
    }

    private void loadPageCount() {
        try {
            String pageCountStr = Files.readString(Paths.get(PAGE_COUNT_FILE)).trim();
            totalPageCount = Integer.parseInt(pageCountStr);
        } catch (Exception e) {
            e.printStackTrace();
            totalPageCount = 0;
        }
    }
    private void loadA4PageCount() {
        try {
            String pageCountStr = Files.readString(Paths.get(A4_PAGE_COUNT_FILE)).trim();
            totalA4PageCount = Integer.parseInt(pageCountStr);
        } catch (Exception e) {
            e.printStackTrace();
            totalA4PageCount = 0;
        }
    }

    private void loadA3PageCount() {
        try {
            String pageCountStr = Files.readString(Paths.get(A3_PAGE_COUNT_FILE)).trim();
            totalA3PageCount = Integer.parseInt(pageCountStr);
        } catch (Exception e) {
            e.printStackTrace();
            totalA3PageCount = 0;
        }
    }


    private void loadLegalPageCount() {
        try {
            String pageCountStr = Files.readString(Paths.get(LEGAL_PAGE_COUNT_FILE)).trim();
            totalLegalPageCount = Integer.parseInt(pageCountStr);
        } catch (Exception e) {
            e.printStackTrace();
            totalLegalPageCount = 0;
        }
    }

    private void savePageCount() {
        try (PrintWriter out = new PrintWriter(new FileWriter(PAGE_COUNT_FILE))) {
            out.println(totalPageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveA4PageCount() {
        try (PrintWriter out = new PrintWriter(new FileWriter(A4_PAGE_COUNT_FILE))) {
            out.println(totalA4PageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveA3PageCount() {
        try (PrintWriter out = new PrintWriter(new FileWriter(A3_PAGE_COUNT_FILE))) {
            out.println(totalA3PageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveLegalPageCount() {
        try (PrintWriter out = new PrintWriter(new FileWriter(LEGAL_PAGE_COUNT_FILE))) {
            out.println(totalLegalPageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePageCountLabel() {
        totalPageCountLabel.setText(String.valueOf(totalPageCount));
    }

    private void updateA4PageCountLabel() {
        totalA4PageCountLabel.setText(String.valueOf(totalA4PageCount));
    }

    private void updateA3PageCountLabel() {
        totalA3PageCountLabel.setText(String.valueOf(totalA3PageCount));
    }

    private void updateLegalPageCountLabel() {
        totalLegalPageCountLabel.setText(String.valueOf(totalLegalPageCount));
    }

    // Utility method to check if the page size is A3
    private boolean isA3Size(float width, float height) {
        // Assuming A3 size is in the range of 830-850 width and 1160-1180 height
        return (width >= 832f && width <= 852f && height >= 1181f && height <= 1201f) ||
                (height >= 832f && height <= 852f && width >= 1181f && width <= 1201f);
    }

    // Utility method to check if the page size is Legal
    private boolean isLegalSize(float width, float height) {
        // Assuming Legal size is in the range of 623-841 width and 1020-1190 height
        return (width >= 585f && width <= 605f && height >= 967f && height <= 987f) ||
                (height >= 585f && height <= 605f && width >= 967f && width <= 987f);
    }

    // Utility method to check if the page size is A4
    private boolean isA4Size(float width, float height) {
        // Assuming A4 size is in the range of 595-623 width and 841-1120 height
        return (width >= 585.276f && width <= 605.276f && height >= 832f && height <= 852f) ||
                (height >= 585.276f && height <= 605.276f && width >= 832f && width <= 852f);
    }

    @FXML
    private void moveFile() {
        TreeItem<File> selectedItem = fileTree.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getParent() != null) {
            File selectedFile = selectedItem.getValue();
            File selectedFolder = selectedItem.getParent().getValue();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Move File");
            File targetFolder = fileChooser.showSaveDialog(new Stage());

            if (targetFolder != null && targetFolder.isDirectory()) {
                Path sourceFilePath = Paths.get(selectedFile.getAbsolutePath());
                Path targetFilePath = Paths.get(targetFolder.getAbsolutePath(), selectedFile.getName());

                try {
                    Files.move(sourceFilePath, targetFilePath);
                    refreshFileTree(); // Refresh the file tree to reflect the move
                } catch (IOException e) {
                    e.printStackTrace();
                    showMoveFileFailureAlert("Failed to move the file.");
                }
            }
        }
    }

    private void showMoveFileFailureAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Move File Failed");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}