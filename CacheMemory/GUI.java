package CacheMemory;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GUI extends Application {

    private Cache cache = new Cache();
    private BorderPane borderPane;
    private Label title;
    private GridPane cacheGrid;

    private TextField cacheSizeField;
    private TextField wordsPerBlockField;
    private TextField mappingPolicyField;
    private TextField nWayField;

    @Override
    public void init() {
        this.borderPane = new BorderPane();
        this.title = new Label("Cache Memory");
        this.cacheGrid = new GridPane();
    }

    @Override
    public void start(Stage stage) {
        // Setup title
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");
        title.setAlignment(Pos.CENTER);
        borderPane.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        displayEmptyBoard(); // Initial display

        // Right side form (VBox)
        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.CENTER);

        cacheSizeField = new TextField();
        wordsPerBlockField = new TextField();
        mappingPolicyField = new TextField();
        nWayField = new TextField();

        Label cacheSizeLabel = new Label("Cache Size (Bytes):");
        Label wordsPerBlockLabel = new Label("Words per Block (1,2,4,8):");
        Label mappingPolicyLabel = new Label("Mapping Policy (DM/SA):");
        Label nWayLabel = new Label("N-Way (only if SA):");

        Button setupButton = new Button("Setup Cache");
        setupButton.setOnAction(e -> setupCacheAndDisplay());

        controlPanel.getChildren().addAll(
                cacheSizeLabel, cacheSizeField,
                wordsPerBlockLabel, wordsPerBlockField,
                mappingPolicyLabel, mappingPolicyField,
                nWayLabel, nWayField,
                setupButton
        );

        borderPane.setRight(controlPanel);

        borderPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(borderPane, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Cache Memory GUI");
        stage.show();
    }

    private void displayEmptyBoard() {
        cacheGrid.getChildren().clear();
        cacheGrid.setGridLinesVisible(true);
        cacheGrid.setAlignment(Pos.CENTER);
        borderPane.setCenter(cacheGrid);
    }

    private void setupCacheAndDisplay() {
        try {
            int cacheSize = Integer.parseInt(cacheSizeField.getText());
            int wordsPerBlock = Integer.parseInt(wordsPerBlockField.getText());
            String mappingPolicy = mappingPolicyField.getText();
            int nWay = mappingPolicy.equalsIgnoreCase("SA") ? Integer.parseInt(nWayField.getText()) : 1;

            cache.setupCache(cacheSize, wordsPerBlock, mappingPolicy, nWay);
            title.setText("Cache Setup Complete");
            drawCacheGrid();

        } catch (Exception ex) {
            title.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void drawCacheGrid() {
        cacheGrid.getChildren().clear();
        cacheGrid.setGridLinesVisible(true);
        cacheGrid.setAlignment(Pos.CENTER);

        int sets = cache.getSets();
        int nWay = cache.getNWay();

        // Headers
        cacheGrid.add(new Label("Set \\ Block"), 0, 0);
        for (int j = 0; j < nWay; j++) {
            Label blockLabel = new Label("Block " + j);
            blockLabel.setStyle("-fx-text-fill: black;");
            cacheGrid.add(blockLabel, j + 1, 0);
        }

        for (int i = 0; i < sets; i++) {
            Label setLabel = new Label("Set " + i);
            setLabel.setStyle("-fx-text-fill: black;");
            cacheGrid.add(setLabel, 0, i + 1);

            for (int j = 0; j < nWay; j++) {
                CacheBlock block = cache.getBlock(i, j);
                Label cell = new Label();
                cell.setPrefSize(80, 40);
                cell.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-alignment: center;");

                if (block.valid) {
                    cell.setText("Valid\nTag: " + block.tag);
                    cell.setStyle(cell.getStyle() + "-fx-background-color: green; -fx-text-fill: black;");
                } else {
                    cell.setText("Invalid");
                    cell.setStyle(cell.getStyle() + "-fx-background-color: gray; -fx-text-fill: black;");
                }

                cacheGrid.add(cell, j + 1, i + 1);
            }
        }

        borderPane.setCenter(cacheGrid);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
