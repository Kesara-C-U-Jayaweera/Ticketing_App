package com.ticketsystem.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.List;

public class TicketSystemApplication extends Application {
    private TextArea logArea;
    private Configuration currentConfig;
    private TicketPool ticketPool;
    private TextField totalTicketsField;
    private TextField maxCapacityField;
    private TextField retrievalTimeField;
    private TextField buyingTimeField;
    private TextField vendorsField;
    private TextField customersField;
    private Button startButton;
    private Button stopButton;
    private Label statusLabel;
    private boolean systemRunning = false;
    private List<Vendor> activeVendors;
    private List<Customer> activeCustomers;

    @Override
    public void start(Stage primaryStage) {
        initializeFields();
        primaryStage.setTitle("Ticket Distribution System");

        // Create main layout
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f4f4f4;");

        // Add header
        Label headerLabel = new Label("Ticket Distribution System");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        headerLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Add status indicator
        statusLabel = new Label("System Status: Idle");
        statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        statusLabel.setStyle("-fx-text-fill: #7f8c8d;");

        // Configuration Section
        TitledPane configPane = createConfigurationSection();

        // System Control Section
        TitledPane controlPane = createSystemControlSection();

        // Log Section
        TitledPane logPane = createLogSection();

        mainLayout.getChildren().addAll(headerLabel, statusLabel, configPane, controlPane, logPane);

        Scene scene = new Scene(mainLayout, 900, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize the configurations directory
        initializeConfigDirectory();

        // Handle application close
        primaryStage.setOnCloseRequest(e -> {
            if (systemRunning) {
                stopTicketSystem();
            }
            Platform.exit();
            System.exit(0);
        });
    }

    private void initializeFields() {
        activeVendors = new ArrayList<>();
        activeCustomers = new ArrayList<>();
    }

    private void initializeConfigDirectory() {
        File configDir = new File("configurations");
        if (!configDir.exists()) {
            if (configDir.mkdir()) {
                log("Created configurations directory");
            } else {
                log("Failed to create configurations directory");
            }
        }
    }

    private TitledPane createConfigurationSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");

        // Configuration Fields
        totalTicketsField = createStyledTextField("10");
        maxCapacityField = createStyledTextField("20");
        retrievalTimeField = createStyledTextField("1000");
        buyingTimeField = createStyledTextField("1000");

        addLabeledField(grid, "Total Tickets (10-1000):", totalTicketsField, 0);
        addLabeledField(grid, "Max Capacity (10-1000):", maxCapacityField, 1);
        addLabeledField(grid, "Retrieval Time (500-10000ms):", retrievalTimeField, 2);
        addLabeledField(grid, "Buying Time (500-10000ms):", buyingTimeField, 3);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button loadButton = createStyledButton("Load Configuration");
        Button saveButton = createStyledButton("Save Configuration");
        Button createButton = createStyledButton("Create Configuration");

        buttonBox.getChildren().addAll(loadButton, saveButton, createButton);
        grid.add(buttonBox, 0, 4, 2, 1);

        // Button Actions
        loadButton.setOnAction(e -> loadConfiguration());
        saveButton.setOnAction(e -> saveConfiguration());
        createButton.setOnAction(e -> createConfiguration());

        TitledPane configPane = new TitledPane("Configuration Settings", grid);
        configPane.setCollapsible(false);
        return configPane;
    }

    private TitledPane createSystemControlSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");

        // Control Fields
        vendorsField = createStyledTextField("5");
        customersField = createStyledTextField("10");

        addLabeledField(grid, "Number of Vendors (1-50):", vendorsField, 0);
        addLabeledField(grid, "Number of Customers (1-200):", customersField, 1);

        // Control Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        startButton = createStyledButton("Start System");
        stopButton = createStyledButton("Stop System");
        stopButton.setDisable(true);

        buttonBox.getChildren().addAll(startButton, stopButton);
        grid.add(buttonBox, 0, 2, 2, 1);

        // Button Actions
        startButton.setOnAction(e -> startTicketSystem());
        stopButton.setOnAction(e -> stopTicketSystem());

        TitledPane controlPane = new TitledPane("System Control", grid);
        controlPane.setCollapsible(false);
        return controlPane;
    }

    private TitledPane createLogSection() {
        VBox logBox = new VBox(10);
        logBox.setPadding(new Insets(20));
        logBox.setStyle("-fx-background-color: white;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(10);
        logArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        Button clearButton = createStyledButton("Clear Log");
        clearButton.setOnAction(e -> logArea.clear());

        logBox.getChildren().addAll(logArea, clearButton);

        TitledPane logPane = new TitledPane("System Log", logBox);
        logPane.setCollapsible(false);
        return logPane;
    }

    private TextField createStyledTextField(String defaultValue) {
        TextField field = new TextField(defaultValue);
        field.setStyle("-fx-background-radius: 5; -fx-border-radius: 5;");
        return field;
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-border-radius: 5;");
        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-border-radius: 5;"));
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-border-radius: 5;"));
        return button;
    }

    private void addLabeledField(GridPane grid, String labelText, TextField field, int row) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold;");
        grid.add(label, 0, row);
        grid.add(field, 1, row);
    }

    private void loadConfiguration() {
        File folder = new File("configurations");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            showAlert("No Configurations", "No saved configurations found.", Alert.AlertType.INFORMATION);
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Load Configuration");
        dialog.setHeaderText("Select a configuration to load:");
        for (File file : files) {
            dialog.getItems().add(file.getName().replace(".txt", ""));
        }

        dialog.showAndWait().ifPresent(name -> {
            String filename = "configurations/" + name + ".txt";
            currentConfig = Configuration.loadFromFile(filename);
            if (currentConfig != null) {
                updateConfigurationFields();
                log("Configuration loaded: " + name);
            }
        });
    }

    private void saveConfiguration() {
        if (currentConfig == null) {
            showAlert("No Configuration", "Please create a configuration first.", Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Save Configuration");
        dialog.setHeaderText("Enter configuration name:");
        dialog.showAndWait().ifPresent(name -> {
            String filename = "configurations/" + name + ".txt";
            currentConfig.saveToFile(filename);
            log("Configuration saved as: " + name);
        });
    }

    private void createConfiguration() {
        try {
            int totalTickets = Integer.parseInt(totalTicketsField.getText());
            int maxCapacity = Integer.parseInt(maxCapacityField.getText());
            int retrievalTime = Integer.parseInt(retrievalTimeField.getText());
            int buyingTime = Integer.parseInt(buyingTimeField.getText());

            if (validateConfiguration(totalTickets, maxCapacity, retrievalTime, buyingTime)) {
                currentConfig = new Configuration(totalTickets, maxCapacity, retrievalTime, buyingTime);
                log("New configuration created successfully");
                showAlert("Success", "Configuration created successfully!", Alert.AlertType.INFORMATION);
            }
        } catch (NumberFormatException ex) {
            showAlert("Invalid Input", "Please enter valid numbers in all fields.", Alert.AlertType.ERROR);
        }
    }

    private void updateConfigurationFields() {
        if (currentConfig != null) {
            totalTicketsField.setText(String.valueOf(currentConfig.getTotalTickets()));
            maxCapacityField.setText(String.valueOf(currentConfig.getMaxTicketCapacity()));
            retrievalTimeField.setText(String.valueOf(currentConfig.getTicketRetrievalTime()));
            buyingTimeField.setText(String.valueOf(currentConfig.getTicketBuyingTime()));
        }
    }

    private void startTicketSystem() {
        if (currentConfig == null) {
            showAlert("No Configuration", "Please create or load a configuration first.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int numVendors = Integer.parseInt(vendorsField.getText());
            int numCustomers = Integer.parseInt(customersField.getText());

            if (validateSystemParameters(numVendors, numCustomers)) {
                systemRunning = true;
                updateSystemStatus(true);
                ticketPool = new TicketPool(currentConfig.getMaxTicketCapacity(), currentConfig.getTotalTickets());

                // Clear previous lists
                activeVendors = new ArrayList<>();
                activeCustomers = new ArrayList<>();

                // Start vendors
                for (int i = 0; i < numVendors; i++) {
                    Vendor vendor = new Vendor(i + 1, ticketPool, currentConfig.getTicketRetrievalTime());
                    activeVendors.add(vendor);
                    new Thread(vendor).start();
                }

                // Start customers
                for (int i = 0; i < numCustomers; i++) {
                    Customer customer = new Customer(i + 1, ticketPool, currentConfig.getTicketBuyingTime());
                    activeCustomers.add(customer);
                    new Thread(customer).start();
                }

                log("System started with " + numVendors + " vendors and " + numCustomers + " customers");
            }
        } catch (NumberFormatException ex) {
            showAlert("Invalid Input", "Please enter valid numbers for vendors and customers.", Alert.AlertType.ERROR);
        }
    }

    private void stopTicketSystem() {
        systemRunning = false;

        // Stop all vendors
        if (activeVendors != null) {
            for (Vendor vendor : activeVendors) {
                vendor.stop();
            }
        }

        // Stop all customers
        if (activeCustomers != null) {
            for (Customer customer : activeCustomers) {
                customer.stop();
            }
        }

        // Shutdown ticket pool
        if (ticketPool != null) {
            ticketPool.shutdown();
        }

        updateSystemStatus(false);
        log("System stopped manually");
    }

    private void updateSystemStatus(boolean running) {
        systemRunning = running;
        startButton.setDisable(running);
        stopButton.setDisable(!running);
        statusLabel.setText("System Status: " + (running ? "Running" : "Idle"));
        statusLabel.setStyle("-fx-text-fill: " + (running ? "#27ae60" : "#7f8c8d") + ";");
    }

    private boolean validateConfiguration(int totalTickets, int maxCapacity, int retrievalTime, int buyingTime) {
        if (totalTickets < 10 || totalTickets > 1000) {
            showAlert("Invalid Input", "Total tickets must be between 10 and 1000", Alert.AlertType.ERROR);
            return false;
        }
        if (maxCapacity < totalTickets || maxCapacity > 1000) {
            showAlert("Invalid Input", "Max capacity must be between total tickets and 1000", Alert.AlertType.ERROR);
            return false;
        }
        if (retrievalTime < 500 || retrievalTime > 10000) {
            showAlert("Invalid Input", "Retrieval time must be between 500 and 10000ms", Alert.AlertType.ERROR);
            return false;
        }
        if (buyingTime < 500 || buyingTime > 10000) {
            showAlert("Invalid Input", "Buying time must be between 500 and 10000ms", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private boolean validateSystemParameters(int numVendors, int numCustomers) {
        if (numVendors < 1 || numVendors > 50) {
            showAlert("Invalid Input", "Number of vendors must be between 1 and 50", Alert.AlertType.ERROR);
            return false;
        }
        if (numCustomers < 1 || numCustomers > 200) {
            showAlert("Invalid Input", "Number of customers must be between 1 and 200", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void log(String message) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.appendText("[" + timestamp + "] " + message + "\n");
            logArea.setScrollTop(Double.MAX_VALUE); // Auto-scroll to bottom
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}