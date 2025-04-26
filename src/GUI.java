import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.Optional;
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
            Dialog<String> typeDialog = new Dialog<>();
            typeDialog.setTitle("Choose Simulation Type");
            typeDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            ChoiceBox<String> typeChoice = new ChoiceBox<>();
            typeChoice.getItems().addAll("Random", "Locality-Based");
            typeChoice.setValue("Random");
            typeDialog.getDialogPane().setContent(typeChoice);

            typeDialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) return typeChoice.getValue();
                return null;
            });

            Optional<String> typeResult = typeDialog.showAndWait();
            if (!typeResult.isPresent()) return;  // user canceled

            Random rand = new Random();
            String simulationType = simulationTypeBox.getValue();

            if (simulationType.equals("Random")) {
                // Random Accesses
                Dialog<Pair<String,String>> randDialog = new Dialog<>();
                randDialog.setTitle("Random Simulation Parameters");
                randDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                GridPane randGrid = new GridPane();
                randGrid.setHgap(10);
                randGrid.setVgap(10);
                TextField numField     = new TextField();
                TextField maxField     = new TextField();
                randGrid.add(new Label("Num accesses:"),    0, 0);
                randGrid.add(numField,                       1, 0);
                randGrid.add(new Label("Max word address (exclusive):"), 0, 1);
                randGrid.add(maxField,                       1, 1);
                randDialog.getDialogPane().setContent(randGrid);

                randDialog.setResultConverter(btn -> {
                    if (btn == ButtonType.OK)
                        return new Pair<>(numField.getText(), maxField.getText());
                    return null;
                });

                Optional<Pair<String,String>> randRes = randDialog.showAndWait();
                if (!randRes.isPresent()) return;

                try {
                    int num     = Integer.parseInt(randRes.get().getKey());
                    int maxWord = Integer.parseInt(randRes.get().getValue());
                    for (int i = 0; i < num; i++) {
                        cache.accessWord(rand.nextInt(maxWord));
                    }
                    outputLabel.setText("Random simulation done: " + num + " accesses.");
                } catch (NumberFormatException ex) {
                    outputLabel.setText("Invalid input for random parameters.");
                }

            } else {
                    // Locality-Based Access
                    int num = 50;          // default
                    int maxWord = 100;     // default
                    int windowSize = 10;   
                    int clusterStart = rand.nextInt(maxWord - windowSize + 1);

                    for (int i = 0; i < num; i++) {
                        int addr = clusterStart + rand.nextInt(windowSize);
                        cache.accessWord(addr);
                        if (i % windowSize == 0) {
                            clusterStart = rand.nextInt(maxWord - windowSize + 1);
                        }
                    }
                    outputLabel.setText("Locality-based simulation done: " + num + " accesses.");
            }

                drawCacheGrid();
           
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
