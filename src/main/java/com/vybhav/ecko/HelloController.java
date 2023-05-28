package com.vybhav.ecko;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class HelloController {
    @FXML
    private TextField folderName;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> searchResults;

    @FXML
    private ListView<String> folderList;

    @FXML
    private ListView<String> fileList;

    @FXML
    private Label totalPageCountLabel;

    @FXML
    private Button logoutButton;

    private static final String ROOT_DIR = "C:/folderManager/";
    private static final String PAGE_COUNT_FILE = "pageCount.txt";
    private String currentFolderPath;
    private int totalPageCount;

    @FXML
    private void initialize() {

        Image folderIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/folder.png")));
        Image fileIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pdf.png")));

        folderList.setCellFactory(listView -> new ListCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(24);
                imageView.setFitWidth(24);
            }

            @Override
            public void updateItem(String folderName, boolean empty) {
                super.updateItem(folderName, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    imageView.setImage(folderIcon);
                    setText(folderName);
                    setGraphic(imageView);
                }
            }
        });

        fileList.setCellFactory(listView -> new ListCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(24);
                imageView.setFitWidth(24);
            }

            @Override
            public void updateItem(String fileName, boolean empty) {
                super.updateItem(fileName, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    imageView.setImage(fileIcon);
                    setText(fileName);
                    setGraphic(imageView);
                }
            }
        });

        searchResults.setCellFactory(listView -> new ListCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(24);
                imageView.setFitWidth(24);
            }

            @Override
            public void updateItem(String fileName, boolean empty) {
                super.updateItem(fileName, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    imageView.setImage(fileIcon);
                    setText(fileName);
                    setGraphic(imageView);
                }
            }
        });

        refreshFolderList();
        folderList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                currentFolderPath = ROOT_DIR + newSelection;
                displayFilesInFolder(newSelection);
            }
        });
        loadPageCount();
        updatePageCountLabel();
        logoutButton.setOnAction(e -> logout());
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
    private void createFolder(ActionEvent event) {
        try {
            String name = folderName.getText();
            Files.createDirectories(Paths.get(ROOT_DIR + name));
            refreshFolderList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void uploadPDF(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File selectedFile = fileChooser.showOpenDialog(new Stage());
            if (selectedFile != null && currentFolderPath != null) {
                FileUtils.copyFileToDirectory(selectedFile, new File(currentFolderPath));
                displayFilesInFolder(currentFolderPath.substring(ROOT_DIR.length()));

                // Count the number of pages and update total
                try (PDDocument document = PDDocument.load(selectedFile)) {
                    int numPages = document.getNumberOfPages();
                    totalPageCount += numPages;
                    savePageCount();
                    updatePageCountLabel();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void searchFile(ActionEvent event) {
        String regex = searchField.getText();
        Pattern pattern = Pattern.compile(regex);
        List<File> results = searchInDirectory(new File(ROOT_DIR), pattern);
        searchResults.getItems().clear();
        for (File result : results) {
            searchResults.getItems().add(result.getName());
        }
    }

    private List<File> searchInDirectory(File directory, Pattern pattern) {
        List<File> result = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && pattern.matcher(file.getName()).find()) {
                    result.add(file);
                } else if (file.isDirectory()) {
                    result.addAll(searchInDirectory(file, pattern));
                }
            }
        }
        return result;
    }

    private void refreshFolderList() {
        File[] directories = new File(ROOT_DIR).listFiles(File::isDirectory);
        if (directories != null) {
            folderList.getItems().clear();
            for (File dir : directories) {
                folderList.getItems().add(dir.getName());
            }
        }
    }

    private void displayFilesInFolder(String folderName) {
        File[] files = new File(ROOT_DIR + folderName).listFiles(File::isFile);
        if (files != null) {
            fileList.getItems().clear();
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.endsWith(".pdf")) {
                    try (PDDocument document = PDDocument.load(file)) {
                        int pageCount = document.getNumberOfPages();
                        fileName += " (" + pageCount + " pages)";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                fileList.getItems().add(fileName);
            }
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

    private void savePageCount() {
        try (PrintWriter out = new PrintWriter(new FileWriter(PAGE_COUNT_FILE))) {
            out.println(totalPageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePageCountLabel() {
        totalPageCountLabel.setText("Total pages: " + totalPageCount);
    }

    public void login(ActionEvent actionEvent) {
        HelloApplication.showLogin();
    }
}