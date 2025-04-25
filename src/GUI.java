import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Random;

public class GUI extends Application {

    private Cache cache = new Cache();
    private GridPane cacheGrid = new GridPane();
    private Label outputLabel = new Label();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Cache Simulator GUI");

        // Input fields
        TextField cacheSizeField = new TextField();
        TextField wordsPerBlockField = new TextField();
        TextField mappingPolicyField = new TextField();
        TextField nWayField = new TextField();
        TextField wordAddrField = new TextField();

        outputLabel.setWrapText(true);

        Button setupButton = new Button("Setup Cache");
        Button accessButton = new Button("Access Word");
        Button simulateButton = new Button("Simulate Access");
        Button statsButton = new Button("Print Stats");
        Button logButton = new Button("Print Log");
        Button clearButton = new Button("Clear Cache");

        ChoiceBox<String> simulationTypeBox = new ChoiceBox<>();
        simulationTypeBox.getItems().addAll("Random", "Locality-Based");
        simulationTypeBox.setValue("Random"); // default


        // Setup button
        setupButton.setOnAction(e -> {
            try {
                int cacheSize = Integer.parseInt(cacheSizeField.getText());
                int wpb = Integer.parseInt(wordsPerBlockField.getText());
                String mapping = mappingPolicyField.getText();
                int nway = mapping.equalsIgnoreCase("SA") ? Integer.parseInt(nWayField.getText()) : 1;

                cache.setupCache(cacheSize, wpb, mapping, nway);
                outputLabel.setText("Cache setup completed.");
                drawCacheGrid();

            } catch (Exception ex) {
                outputLabel.setText("Invalid input for cache setup.");
            }
        });

        accessButton.setOnAction(e -> {
            try {
                int wordAddr = Integer.parseInt(wordAddrField.getText());
                boolean hit = cache.accessWord(wordAddr);
                Cache.AccessRecord last = cache.getLastAccessRecord();
                if (last != null) {
                    outputLabel.setText(last.toString());
                }
                drawCacheGrid();

            } catch (Exception ex) {
                outputLabel.setText("Invalid word address.");
            }
        });

        simulateButton.setOnAction(e -> {
            try {
                int num = 50; // Number of accesses
                int maxWord = 100; // Max word address space

                Random random = new Random();
                String simulationType = simulationTypeBox.getValue();

                if (simulationType.equals("Random")) {
                    // Random Accesses
                    for (int i = 0; i < num; i++) {
                        cache.accessWord(random.nextInt(maxWord));
                    }
                    outputLabel.setText("Random simulation completed. 50 accesses done.");

                } else if (simulationType.equals("Locality-Based")) {
                    // Locality-Based Access
                    int clusterStart = random.nextInt(maxWord - 10); // Pick a base region
                    for (int i = 0; i < num; i++) {
                        int localizedWord = clusterStart + random.nextInt(10); // access within a window
                        cache.accessWord(localizedWord);
                        if (i % 10 == 0) {
                            clusterStart = random.nextInt(maxWord - 10); // shift cluster occasionally
                        }
                    }
                    outputLabel.setText("Locality-based simulation completed. 50 accesses done.");
                }

                drawCacheGrid();
            } catch (Exception ex) {
                outputLabel.setText("Simulation failed.");
            }
        });


        statsButton.setOnAction(e -> {
            cache.printStatsTo(outputLabel);
        });

        logButton.setOnAction(e -> {
            cache.printAccessLogTo(outputLabel);
        });

        clearButton.setOnAction(e -> {
            cache.clearCache();
            outputLabel.setText("Cache cleared.");
            drawCacheGrid();
        });

        // Input layout
        GridPane inputGrid = new GridPane();
        inputGrid.setPadding(new Insets(10));
        inputGrid.setHgap(10);
        inputGrid.setVgap(8);

        inputGrid.add(new Label("Cache Size:"), 0, 0);
        inputGrid.add(cacheSizeField, 1, 0);
        inputGrid.add(new Label("Words/Block:"), 0, 1);
        inputGrid.add(wordsPerBlockField, 1, 1);
        inputGrid.add(new Label("Mapping (DM/SA):"), 0, 2);
        inputGrid.add(mappingPolicyField, 1, 2);
        inputGrid.add(new Label("N-Way (if SA):"), 0, 3);
        inputGrid.add(nWayField, 1, 3);
        inputGrid.add(setupButton, 1, 4);

        inputGrid.add(new Label("Word Addr:"), 0, 5);
        inputGrid.add(wordAddrField, 1, 5);
        inputGrid.add(accessButton, 1, 6);

        HBox controlButtons = new HBox(10, simulateButton, statsButton, logButton, clearButton);
        controlButtons.setAlignment(Pos.CENTER);
        controlButtons.setPadding(new Insets(10));

        HBox simulateType = new HBox(10, simulationTypeBox );
        simulateType.setAlignment(Pos.CENTER);
        simulateType.setPadding(new Insets(10));

        ScrollPane outputScrollPane = new ScrollPane(outputLabel);
        outputScrollPane.setFitToWidth(true);   // make label stretch horizontally
        outputScrollPane.setPrefHeight(150);    // fixed height for scroll area

        ScrollPane cacheScrollPane = new ScrollPane(cacheGrid);
        cacheScrollPane.setFitToWidth(true);
        cacheScrollPane.setPrefHeight(400);   // adjust this depending on your cache size

        VBox root = new VBox(10, inputGrid, controlButtons, outputScrollPane, cacheScrollPane);
        root.setPadding(new Insets(15));

        primaryStage.setScene(new Scene(root, 700, 700));
        primaryStage.show();
    }

    private void drawCacheGrid() {
        cacheGrid.getChildren().clear();
        cacheGrid.setGridLinesVisible(true);
        cacheGrid.setAlignment(Pos.CENTER);
        cacheGrid.setPadding(new Insets(10));
        cacheGrid.setHgap(5);
        cacheGrid.setVgap(5);

        if (cache.getSets() == 0 || cache.getNWay() == 0) return;

        // Headers
        cacheGrid.add(new Label("Set \\ Block"), 0, 0);
        for (int j = 0; j < cache.getNWay(); j++) {
            Label blockHeader = new Label("Block " + j);
            cacheGrid.add(blockHeader, j + 1, 0);
        }

        // Cache cells
        for (int i = 0; i < cache.getSets(); i++) {
            Label setLabel = new Label("Set " + i);
            cacheGrid.add(setLabel, 0, i + 1);

            for (int j = 0; j < cache.getNWay(); j++) {
                CacheBlock block = cache.getBlock(i, j);
                Label cell = new Label();
                cell.setPrefSize(120, 60);
                cell.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-alignment: center;");

                if (!block.accessed || block.content == null) {
                    // Not yet accessed — show "0"
                    cell.setText("0");
                } else {
                    // Accessed — show the content (B<tag>(Wwords))
                    cell.setText(block.content);
                }



                cacheGrid.add(cell, j + 1, i + 1);
            }
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
