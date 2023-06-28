package Client.View.Gui.Scene;

import Client.View.Gui.GuiApplication;
import Enumeration.TurnPhase;
import Utils.*;
import Utils.Cell;
import Utils.MockObjects.MockCommonGoal;
import Utils.MockObjects.MockModel;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Client.ClientApp.*;

public class LivingRoom extends Scene {
    private static GuiApplication app;
    private ImageView boardImage;
    private static GridPane gridBoard;
    private static GridPane pGoalGrid;
    private static MockModel mockModel;
    private static final HBox hBoxMyShelfAndCG = new HBox();
    private static VBox vBoxShelves;
    private static final List<ImageView> selectedTilesImg = new ArrayList<>();
    private static final List<Coordinates> selectedTiles = new ArrayList<>();
    private static final List<GridPane> othersShelves = new ArrayList<>();
    private static ScrollPane chatTextArea;
    private static final VBox chatTextAreaVbox = new VBox();
    private static GridPane highlightBoard;
    private static int numPlayers;
    private static StackPane cg1StackPane;
    private static StackPane cg2StackPane;
    private static int peekCg1;
    private static int peekCg2;
    private static Pane boardPane;
    private static final List<Pane> selectTilesPane = new ArrayList<>();
    private static final List<Integer> orderTiles = new ArrayList<>();
    private static int column = -1;

    public LivingRoom(GuiApplication app) {

        super(new Pane(), 1400, 768);
        setUserAgentStylesheet(STYLEPATH);

        LivingRoom.app = app;

        String boardImg = String.valueOf(GuiApplication.class.getResource("/img/boards/living-room.png"));
        boardImage = new ImageView(boardImg);
        boardStyle(boardImage);

        boardPane = new Pane();
        gridBoard = new GridPane();
        gridBoard.prefWidthProperty().bind(boardImage.fitWidthProperty());
        gridBoard.prefHeightProperty().bind(boardImage.fitWidthProperty());
        // gridBoard.setGridLinesVisible(true);

        for (int row = 0; row < 9; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPrefHeight(60);
            gridBoard.getRowConstraints().add(rowConstraints);
            gridBoard.addRow(row);
        }
        for (int col = 0; col < 9; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPrefWidth(60);
            gridBoard.getColumnConstraints().add(colConstraints);
            gridBoard.addColumn(col);
        }
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Pane paneBase = new Pane();
                gridBoard.add(paneBase, j, i);
            }
        }

        gridBoard.setLayoutX(41);
        gridBoard.setLayoutY(45);
        gridBoard.setHgap(10);
        gridBoard.setVgap(10);
        gridBoard.setDisable(false);

        highlightBoard = new GridPane();
        highlightBoard.prefWidthProperty().bind(boardImage.fitWidthProperty());
        highlightBoard.prefHeightProperty().bind(boardImage.fitWidthProperty());

        for (int row = 0; row < 9; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPrefHeight(60);
            highlightBoard.getRowConstraints().add(rowConstraints);
            highlightBoard.addRow(row);
        }
        for (int col = 0; col < 9; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPrefWidth(60);
            highlightBoard.getColumnConstraints().add(colConstraints);
            highlightBoard.addColumn(col);
        }
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Rectangle rectangle = new Rectangle(60, 60);
                rectangle.setFill(Color.TRANSPARENT);
                highlightBoard.add(rectangle, j, i);
            }
        }

        highlightBoard.setLayoutX(51);
        highlightBoard.setLayoutY(55);
        highlightBoard.setHgap(10);
        highlightBoard.setVgap(10);
        highlightBoard.setDisable(false);

        StackPane boards = new StackPane();
        boards.prefWidthProperty().bind(boardImage.fitWidthProperty());
        boards.prefHeightProperty().bind(boardImage.fitWidthProperty());
        boards.setLayoutX(41);
        boards.setLayoutY(45);
        boards.setDisable(false);
        boards.getChildren().addAll(gridBoard, highlightBoard);

        boardPane.getChildren().addAll(boardImage, boards);

        vBoxShelves = new VBox();
        vBoxShelves.setSpacing(10);

        HBox mainHBox = new HBox();
        mainHBox.setSpacing(10);
        mainHBox.getChildren().addAll(boardPane, vBoxShelves);

        highlightBoard.setOnMouseClicked(event -> {
            int tmp = 0;
            if (mockModel.getMockPlayers().size() == 2) tmp = 1;
            double mouseX = event.getX();
            double mouseY = event.getY();
            System.out.println("mouse x: " + mouseX + ", mouse y: " + mouseY);

            int colIndex = -1;
            int rowIndex = -1;

            double colStartX = 0;
            double colEndX = 70;
            for (int col = 0; col < 9; col++) {
                if (mouseX >= colStartX && mouseX <= colEndX) {
                    colIndex = col - tmp;
                    break;
                }
                colStartX += 70;
                colEndX += 70;
            }

            double rowStartY = 0;
            double rowEndY = 70;
            for (int row = 0; row < 9 + tmp; row++) {
                if (mouseY >= rowStartY && mouseY <= rowEndY) {
                    rowIndex = row - tmp;
                    break;
                }
                rowStartY += 70;
                rowEndY += 70;
            }

            if (colIndex >= 0 && rowIndex >= 0) {

                // rectangle to highlight
                Rectangle toHighlight = getRectangle(highlightBoard, colIndex + tmp, rowIndex + tmp);
                System.out.println("x: " + rowIndex + ", y: " + colIndex);

                // tile selected
                Coordinates tile = new Coordinates(rowIndex, colIndex);
                if (selectedTiles.contains(tile)) {
                    toHighlight.setStroke(Color.TRANSPARENT);
                    selectedTiles.remove(tile);
                } else {
                    toHighlight.setStroke(Color.BLACK);
                    toHighlight.setStrokeWidth(2);
                    selectedTiles.add(new Coordinates(rowIndex, colIndex));
                }

                // image handler
                Pane selectedPane = getPane(gridBoard, colIndex + tmp, rowIndex + tmp);
                if (selectedPane != null) {
                    ImageView selectedImageView = (ImageView) selectedPane.getChildren().get(0);
                    if (!selectedTilesImg.contains(selectedImageView) && selectedImageView != null) {
                        selectedTilesImg.add(selectedImageView);
                    } else selectedTilesImg.remove(selectedImageView);
                }
            }
        });
        setRoot(mainHBox);
    }

    private static Rectangle getRectangle(GridPane grid, int j, int i) {
        List<Node> kids = grid.getChildren();
        Rectangle res = null;
        for (Node n : kids) {
            if (GridPane.getColumnIndex(n) == j && GridPane.getRowIndex(n) == i) res = (Rectangle) n;
        }
        return res;
    }

    public static void toLobbySize() {
        Scene lobbySize = new LobbyScene(app);
        app.switchScene(lobbySize);
    }

    public static void updateMockModel(MockModel mockmodel) {
        mockModel = mockmodel;
    }

    public static void updateCommonGoal(int enumeration, Integer peek) {
        if (mockModel.getMockCommonGoal().get(0).getEnumeration() == enumeration) {
            if (peek != peekCg1) {
                ObservableList<Node> children = cg1StackPane.getChildren();
                if (!children.isEmpty()) children.remove(children.size() - 1);
            }
        } else {
            if (peek != peekCg2) {
                ObservableList<Node> children = cg2StackPane.getChildren();
                if (!children.isEmpty()) children.remove(children.size() - 1);
            }
        }
    }

    public static void lastRound() {

    }


    private void boardStyle(ImageView boardImage) {
        this.boardImage = boardImage;
        boardImage.setPreserveRatio(true);
        boardImage.setFitWidth(690);
        boardImage.setFitHeight(768);
        boardImage.setLayoutX(10);
        boardImage.setLayoutY(10);
        boardImage.setLayoutY(10);
    }

    public static void showBoard(Cell[][] board) {
        numPlayers = mockModel.getMockPlayers().size();
        int tmp = 0;
        if (numPlayers == 2) tmp = 1;
        fillBoard(board, tmp);
        Pane finalPointPain = new Pane();
        ImageView finalPointImg = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/scoring_tokens/end_game.jpg")));
        finalPointImg.setPreserveRatio(true);
        finalPointImg.setRotate(10);
        finalPointImg.setFitWidth(60);
        finalPointPain.getChildren().add(finalPointImg);
        finalPointPain.setLayoutX(575);
        finalPointPain.setLayoutY(495);
        boardPane.getChildren().add(finalPointPain);
    }

    private static Pane getPane(GridPane grid, int j, int i) {
        List<Node> kids = grid.getChildren();
        Pane res = null;
        for (Node n : kids) {
            if (GridPane.getColumnIndex(n) == j && GridPane.getRowIndex(n) == i) res = (Pane) n;
        }
        return res;
    }

    public static void showCommonAndShelves() {

        // common goals
        MockCommonGoal mockCommonGoal1 = mockModel.getMockCommonGoal().get(0);
        int numberCGoal1 = mockCommonGoal1.getEnumeration() + 1;
        MockCommonGoal mockCommonGoal2 = mockModel.getMockCommonGoal().get(1);
        int numberCGoal2 = mockCommonGoal2.getEnumeration() + 1;

        Pane cg1 = new Pane();
        Pane cg2 = new Pane();
        ImageView cg1img = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/common_goal_cards/" + numberCGoal1 + ".jpg")));
        ImageView cg2img = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/common_goal_cards/" + numberCGoal2 + ".jpg")));
        cg1img.setFitWidth(150);
        cg1img.setPreserveRatio(true);
        cg1img.setLayoutX(30);
        cg1img.setLayoutY(15);
        cg2img.setFitWidth(150);
        cg2img.setPreserveRatio(true);
        cg2img.setLayoutX(30);
        cg2img.setLayoutY(15);
        copyImageGenerator(cg1, cg1img);
        copyImageGenerator(cg2, cg2img);

        VBox commonGoal = new VBox();
        commonGoal.setLayoutY(30);
        commonGoal.getChildren().addAll(cg1, cg2);

        // images of common goals points
        // list 2-4-6-8
        // index 0-1-2-3
        List<ImageView> commonGoalsImg = new ArrayList<>();
        for (int i = 2; i <= 8; i += 2) {
            System.out.println("adding image to list");
            commonGoalsImg.add(new ImageView(String.valueOf(GuiApplication.class.getResource("/img/scoring_tokens/scoring_" + i + ".jpg"))));
        }
        switch (numPlayers) {
            case 2 -> {
                commonGoalsImg.remove(0);
                commonGoalsImg.remove(1);
            }
            case 3 -> commonGoalsImg.remove(0);
        }

        peekCg1 = 8;
        peekCg2 = 8;

        // positioning
        // stackPane -> pane -> img

        cg1StackPane = createStackPaneWithImages(commonGoalsImg);
        cg1.getChildren().add(cg1StackPane);

        cg2StackPane = createStackPaneWithImages(commonGoalsImg);
        cg2.getChildren().add(cg2StackPane);

        // personal goal

        Label personalID = new Label("Your shelf");
        personalID.setStyle("-fx-font-weight: bold;");
        personalID.getStyleClass().add("personal-shelf-label");

        Pane pGoalPane = new Pane();

        ImageView pGoalImg = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/boards/bookshelf.png")));
        pGoalImg.setPreserveRatio(true);
        pGoalImg.setFitWidth(250);
        pGoalImg.setFitHeight(300);

        pGoalGrid = new GridPane();
        personalGoalGrid(pGoalImg, pGoalGrid);

        ImageView image;

        GridPane wallpaper = new GridPane();
        personalGoalGrid(pGoalImg, wallpaper);
        Tile[][] personalGoal = mockModel.getPlayer(localPlayer).getPersonalGoal();
        for (int i = 0; i < personalGoal.length; i++) {
            for (int j = 0; j < personalGoal[0].length; j++) {
                if (personalGoal[i][j] != null) {
                    String colorString = personalGoal[i][j].color().getCode();
                    image = choseImage(colorString);
                    image.setPreserveRatio(true);
                    image.setOpacity(0.5);
                    Pane tmpPane = getPane(wallpaper, j, i);
                    image.fitWidthProperty().bind(tmpPane.widthProperty());
                    tmpPane.getChildren().add(image);
                }
            }
        }

        pGoalGrid.setOnMouseClicked(event -> {
            if (mockModel.getTurnPhase() == TurnPhase.PICKING) {
                try {
                    network.selectTiles(localPlayer, selectedTiles);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            } else if (mockModel.getTurnPhase() == TurnPhase.INSERTING) {
                printSelectedTiles();
            }
        });

        pGoalPane.getChildren().addAll(wallpaper, pGoalGrid, pGoalImg, personalID);

        // shelves

        HBox hBoxShelves = new HBox();

        for (int i = 0; i < numPlayers; i++) {

            // setting pane and image of shelves
            Pane playerShelfPane = new Pane();

            if (!localPlayer.equals(mockModel.getMockPlayers().get(i).getPlayerID())) {
                Label playerID = new Label(mockModel.getMockPlayers().get(i).getPlayerID() + "'s shelf.");
                playerID.getStyleClass().add("players-shelf-label");
                playerShelfPane.getChildren().add(playerID);


                ImageView shelfImg = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/boards/bookshelf.png")));
                shelfImg.setFitWidth(200);
                shelfImg.setFitHeight(250);
                shelfImg.setLayoutX(10);
                shelfImg.setLayoutY(20);
                shelfImg.setPreserveRatio(true);

                // binding the grid pane
                GridPane grid = new GridPane();
                grid.setGridLinesVisible(true);
                grid.setHgap(7);
                grid.setVgap(2);
                grid.setLayoutX(30);
                grid.setLayoutY(30);
                grid.prefWidthProperty().bind(shelfImg.fitWidthProperty());
                grid.prefHeightProperty().bind(shelfImg.fitWidthProperty());
                grid.getChildren().clear();
                for (int k = 0; k < 6; k++) {
                    for (int j = 0; j < 5; j++) {
                        Pane paneBase = new Pane();
                        paneBase.setPrefWidth(25);
                        paneBase.setPrefHeight(25);
                        grid.add(paneBase, j, k);
                        GridPane.setColumnIndex(paneBase, j);
                        GridPane.setRowIndex(paneBase, k);
                    }
                }
                othersShelves.add(grid);
                playerShelfPane.getChildren().addAll(grid, shelfImg);
                hBoxShelves.getChildren().add(playerShelfPane);
            }
        }


        vBoxShelves.getChildren().add(hBoxShelves);

        hBoxMyShelfAndCG.setSpacing(20);
        hBoxMyShelfAndCG.getChildren().addAll(pGoalPane, commonGoal);

        System.out.println("adding common and personal shelf");
        vBoxShelves.getChildren().add(hBoxMyShelfAndCG);

        // chat

        VBox chatLayout = new VBox();
        chatLayout.setSpacing(10);

        ComboBox<String> recipient = new ComboBox<>();
        recipient.getStyleClass().add("combo-box-chat");
        for (int i = 0; i < mockModel.getMockPlayers().size(); i++) {
            if (!localPlayer.equals(mockModel.getMockPlayers().get(i).getPlayerID()))
                recipient.getItems().add(mockModel.getMockPlayers().get(i).getPlayerID());
        }
        recipient.getItems().add("all");

        chatTextArea = new ScrollPane();
        chatTextArea.getStyleClass().add("chat-area");
        chatTextArea.setContent(chatTextAreaVbox);

        chatTextAreaVbox.getStyleClass().add("vBox-chat");

        TextField messageField = new TextField();
        messageField.getStyleClass().add("text-field-chat");

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("button-chat");

        HBox hBoxInputMessage = new HBox();
        hBoxInputMessage.setSpacing(10);
        hBoxInputMessage.setPrefWidth(600);
        hBoxInputMessage.setPrefHeight(25);

        hBoxInputMessage.getChildren().addAll(messageField, recipient, sendButton);

        chatLayout.getChildren().addAll(chatTextArea, hBoxInputMessage);
        vBoxShelves.getChildren().add(chatLayout);

        sendButton.setOnAction(event -> {
            String message = messageField.getText();
            String dest = recipient.getValue();
            if (Objects.equals(dest, "all")) dest = null;
            try {
                network.writeChat(localPlayer, message, dest);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            messageField.clear();
        });

        messageField.setOnAction(event -> {
            String message = messageField.getText();
            String dest = recipient.getValue();
            if (Objects.equals(dest, "all")) dest = null;
            try {
                network.writeChat(localPlayer, message, dest);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            messageField.clear();
        });

        // upgrading shelves in case of reconnection
        updateShelves();
    }

    private static void personalGoalGrid(ImageView pGoalImg, GridPane pGoalGrid) {
        pGoalGrid.setGridLinesVisible(true);
        pGoalGrid.setHgap(10);
        pGoalGrid.setVgap(2);
        pGoalGrid.setLayoutX(30);
        pGoalGrid.setLayoutY(20);
        pGoalGrid.prefWidthProperty().bind(pGoalImg.fitWidthProperty());
        pGoalGrid.prefHeightProperty().bind(pGoalImg.fitWidthProperty());

        pGoalGrid.getChildren().clear();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                Pane paneBase = new Pane();
                paneBase.setPrefWidth(30);
                paneBase.setPrefHeight(30);
                pGoalGrid.add(paneBase, j, i);
                GridPane.setColumnIndex(paneBase, j);
                GridPane.setRowIndex(paneBase, i);
            }
        }
    }

    private static StackPane createStackPaneWithImages(List<ImageView> images) {
        StackPane stackPane = new StackPane();
        for (ImageView image : images) {
            ImageView imageView = new ImageView(image.getImage());
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(40);
            imageView.setRotate(350);
            stackPane.getChildren().add(imageView);
        }
        stackPane.setLayoutX(120);
        stackPane.setLayoutY(40);
        return stackPane;
    }

    private static void copyImageGenerator(Pane cg1, ImageView cg1img) {
        Image copyCg1Image = cg1img.getImage();
        ImageView copyCg1 = new ImageView(copyCg1Image);
        copyCg1.setPreserveRatio(true);
        copyCg1.setFitWidth(250);
        cg1.getChildren().add(cg1img);
        cg1.setOnMouseClicked(e -> showCommonGoals(copyCg1, e.getScreenX(), e.getScreenY()));
    }

    private static void printSelectedTiles() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Selected Tiles");
        alert.setHeaderText("Selected tiles: ");

        // vBox -> hBox -> shelf

        VBox vBoxMain = new VBox();
        vBoxMain.setSpacing(10);

        HBox selectedTilesHBox = new HBox();
        selectedTilesHBox.setSpacing(10);
        for (int i = 0; i < selectedTilesImg.size(); i++) {
            Pane tilePane = new Pane();
            tilePane.setId(String.valueOf(i + 1));
            selectTilesPane.add(tilePane);
            tilePane.setOnMouseClicked(e -> handleClickTileOrder(tilePane.getId()));

            ImageView tileImg = selectedTilesImg.get(i);
            tileImg.setFitWidth(60);
            tileImg.setFitHeight(60);

            tilePane.getChildren().add(tileImg);
            selectedTilesHBox.getChildren().add(tilePane);
        }

        Pane personalGoalPane = new Pane();

        ImageView personalGoalImg = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/boards/bookshelf.png")));
        personalGoalImg.setPreserveRatio(true);
        personalGoalImg.setFitWidth(250);
        personalGoalImg.setFitHeight(300);

        GridPane personalGoalGrid = new GridPane();
        personalGoalGrid(personalGoalImg, personalGoalGrid);
        updatePersonalGoal(personalGoalGrid);

        Button insert = new Button("insert");
        insert.setOnAction(e -> {
            try {
                insertTiles();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
        insert.setPrefHeight(40);
        insert.setPrefWidth(60);

        personalGoalPane.getChildren().addAll(personalGoalGrid, personalGoalImg);

        HBox hBoxArrows = new HBox();
        hBoxArrows.setSpacing(10);
        for(int i=0;i<5;i++){
            Pane arrowPane = new Pane();
            arrowPane.setId(String.valueOf(i));

            ImageView arrowImg = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/misc/down-filled-triangular-arrow.png")));
            arrowImg.setPreserveRatio(true);
            arrowImg.setFitWidth(25);

            arrowPane.setOnMouseClicked(e-> chooseColumn(arrowPane, Integer.parseInt(arrowPane.getId())));

            arrowPane.getChildren().add(arrowImg);
            hBoxArrows.getChildren().add(arrowPane);
        }
        Pane firstPane = (Pane) hBoxArrows.getChildren().get(0);
        HBox.setMargin(firstPane, new Insets(0, 0, 0, 40));

        vBoxMain.getChildren().addAll(selectedTilesHBox,hBoxArrows, personalGoalPane, insert);


        alert.getDialogPane().setContent(vBoxMain);

        alert.showAndWait();
    }

    private static void chooseColumn(Pane arrowPane, int col) {
        ImageView imagePane = new ImageView();
        if(arrowPane.getChildren().size() > 0)
            imagePane = (ImageView) arrowPane.getChildren().get(0);
        if (col != column) {
            column = col;
            imagePane.setOpacity(0.5);
        } else {
            column = -1;
            imagePane.setOpacity(1);
        }
    }

    private synchronized static void printNumber(Pane tilePane, String id) {
        if (tilePane.getChildren().size() > 1)
            tilePane.getChildren().remove(1);

        if (!orderTiles.contains(Integer.parseInt(id))) {
            return;
        }

        int currentPos = orderTiles.indexOf(Integer.parseInt(id)) + 1;
        String currPosStr = String.valueOf(currentPos);
        Label toShow = new Label(currPosStr);
        toShow.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 30px");
        toShow.setLayoutX(25);
        toShow.setLayoutY(25);
        tilePane.getChildren().add(toShow);
    }

    private static void handleClickTileOrder(String id) {
        if (!orderTiles.contains(Integer.parseInt(id))) {
            orderTiles.add(Integer.parseInt(id));
        } else {
            orderTiles.remove(Integer.valueOf(Integer.parseInt(id)));
        }
        for (Pane pane : selectTilesPane)
            printNumber(pane, pane.getId());
    }

    public static void outcomeSelectTiles() {
        clearBoard();
        printSelectedTiles();
    }

    private static void clearBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Rectangle rt = getRectangle(highlightBoard, j, i);
                rt.setStroke(Color.TRANSPARENT);
            }
        }
    }

    public static void printError(String message) {
        clearBoard();
        selectedTilesImg.clear();
        selectedTiles.clear();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.showAndWait();
    }

    public static void insertTiles() throws RemoteException {
        selectedTiles.clear();
        selectedTilesImg.clear();
        System.out.println("insert tiles");

        network.insertTiles(localPlayer, orderTiles, column);
    }

    private static ImageView choseImage(String colorString) {
        ImageView image;
        switch (colorString) {
            case "\u001b[42;1m" -> {
                image = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/item_tiles/Cats1.png")));
                return cssTile(image);
            }
            case "\u001b[47;1m" -> {
                image = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/item_tiles/Books1.png")));
                return cssTile(image);
            }
            case "\u001b[43;1m" -> {
                image = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/item_tiles/Toys1.png")));
                return cssTile(image);
            }
            case "\u001b[44;1m" -> {
                image = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/item_tiles/Frames1.png")));
                return cssTile(image);
            }
            case "\u001b[46;1m" -> {
                image = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/item_tiles/Trophies1.png")));
                return cssTile(image);
            }
            case "\u001b[45;1m" -> {
                image = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/item_tiles/Plants1.png")));
                return cssTile(image);
            }
            default -> {
                image = new ImageView(String.valueOf(GuiApplication.class.getResource("/img/item_tiles/Plants2.png")));
                return cssTile(image);
            }
        }
    }

    private static ImageView cssTile(ImageView image) {
        image.setFitHeight(60);
        image.setFitWidth(60);
        image.setPreserveRatio(true);
        return image;
    }

    public static void updateShelves() {

        ImageView image;
        int grids = 0;
        // shelves
        for (int k = 0; k < mockModel.getMockPlayers().size(); k++) {
            if (!localPlayer.equals(mockModel.getMockPlayers().get(k).getPlayerID())) {
                System.out.println("Update other's shelves!");
                Tile[][] othersShelf = mockModel.getMockPlayers().get(k).getShelf();
                GridPane playerGrid = othersShelves.get(grids);
                grids++;
                // delete the old board
                for (int i = 0; i < othersShelf.length; i++) {
                    for (int j = 0; j < othersShelf[0].length; j++) {
                        Pane tmpPane = getPane(playerGrid, j, i);
                        if (tmpPane.getChildren().size() != 0) tmpPane.getChildren().remove(0);
                    }
                }

                // update
                for (int i = 0; i < othersShelf.length; i++) {
                    for (int j = 0; j < othersShelf[0].length; j++) {
                        if (othersShelf[i][j] != null) {
                            String colorString = othersShelf[i][j].color().getCode();
                            image = choseImage(colorString);
                            image.setPreserveRatio(true);
                            Pane tmpPane = getPane(playerGrid, j, i);
                            image.fitWidthProperty().bind(tmpPane.widthProperty());
                            tmpPane.getChildren().add(image);
                        }
                    }
                }
            } else {
                // personal goal
                updatePersonalGoal(pGoalGrid);
            }
        }
    }

    private static void updatePersonalGoal(GridPane grid) {
        ImageView image;
        Tile[][] personalShelf = mockModel.getPlayer(localPlayer).getShelf();
        // delete the old board
        for (int i = 0; i < personalShelf.length; i++) {
            for (int j = 0; j < personalShelf[0].length; j++) {
                Pane tmpPane = getPane(grid, j, i);
                if (tmpPane.getChildren().size() != 0) tmpPane.getChildren().remove(0);
            }
        }
        // adding tiles updated
        for (int i = 0; i < personalShelf.length; i++) {
            for (int j = 0; j < personalShelf[0].length; j++) {
                if (personalShelf[i][j] != null) {
                    String colorString = personalShelf[i][j].color().getCode();
                    image = choseImage(colorString);
                    image.setPreserveRatio(true);
                    Pane tmpPane = getPane(grid, j, i);
                    image.fitWidthProperty().bind(tmpPane.widthProperty());
                    tmpPane.getChildren().add(image);
                }
            }
        }
    }

    public static void updateBoard(Cell[][] board) {
        System.out.println("updating the board");
        int tmp = 0;
        if (numPlayers == 2) tmp = 1;

        // delete the old board
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                Pane tmpPane = getPane(gridBoard, j + tmp, i + tmp);
                if (tmpPane.getChildren().size() != 0) tmpPane.getChildren().remove(0);
            }
        }

        fillBoard(board, tmp);
    }

    private static void fillBoard(Cell[][] board, int tmp) {
        ImageView image;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j].getStatus() && board[i][j].getTile() != null) {
                    String colorString = board[i][j].getTile().color().getCode();
                    image = choseImage(colorString);
                    Pane tmpPane = getPane(gridBoard, j + tmp, i + tmp);
                    tmpPane.getChildren().add(image);
                }
            }
        }
    }

    public static void endGame(List<Rank> leaderboard) {
        EndGameScene endGameScene = new EndGameScene();
        EndGameScene.setRanks(leaderboard);
        app.switchScene(endGameScene);
    }

    private static void showCommonGoals(ImageView commonGoalImg, double mouseX, double mouseY) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("COMMON GOALS");
        alert.setHeaderText("COMMON GOALS: ");

        commonGoalImg.setFitWidth(250);
        commonGoalImg.setPreserveRatio(true);
        alert.getDialogPane().setContent(commonGoalImg);
        alert.getDialogPane().setPrefWidth(300);
        alert.getDialogPane().setPrefHeight(200);

        alert.setX(mouseX - 300.00);
        alert.setY(mouseY);

        alert.showAndWait();
    }

    public static void newMessageChat(ChatMessage message) {
        String from = "From " + message.from();
        String dest = " to " + message.to() + " : ";
        String toShow = from + dest + "'" + message.message() + "'";
        if (message.to() == null) {
            from += ": ";
            dest = "";
            toShow = from + dest + "'" + message.message() + "'";
        }
        Label tmp = new Label(toShow);
        chatTextAreaVbox.getChildren().add(tmp);
    }

    public static void writeInfos(String info) {
        if (chatTextArea != null) {
            Label tmp = new Label(info);
            tmp.getStyleClass().add("text-info-chat");
            chatTextAreaVbox.getChildren().add(tmp);
        }
    }

    public static void outcomeMessage(String message) {
        if (chatTextArea != null) {
            Label tmp = new Label(message);
            tmp.getStyleClass().add("text-info-chat");
            tmp.setTextFill(Color.GREEN);
            chatTextAreaVbox.getChildren().add(tmp);
        }
    }

    public static void refreshChat(List<ChatMessage> messageList) {
        for (ChatMessage message : messageList) {
            LivingRoom.newMessageChat(message);
        }
    }
}