package com.example.ConnectFour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

	private static final int COLUMNS = 7;
	private static final int ROWS = 6;
	private static final int CIRCLE_DIAMETER = 80;
	private static final String discColor1 = "#24303E";
	private static final String discColor2 = "#4CAA88";
	int disc_count = 0; // to check if game is drawn.



	private boolean isPlayerOneTurn = true;

	private Disc[][] insertDiscArray = new Disc[ROWS][COLUMNS]; // for structural changes for dev...

	@FXML
	public GridPane rootGridPane;

	@FXML
	public Pane insertedDiscPane;

	@FXML
	public Label playerName;

	@FXML
	public TextField playerOne;

	@FXML
	public TextField playerTwo;

	@FXML
	public Button setPlayerName;

	private static String PLAYER_ONE = "Player 1";
	private static String PLAYER_TWO = "Player 2";

	private boolean isAllowedToinsert = true;

	public void createPlayground(){

		setPlayerName.setOnAction(event -> {
			PLAYER_ONE = playerOne.getText();
			PLAYER_TWO = playerTwo.getText();
			playerName.setText(PLAYER_ONE);
		});

		Shape rectangleWithHoles = createGameStructuralGrid();
		rootGridPane.add(rectangleWithHoles,0,1);

		List<Rectangle> rectangleList = createClickableColumns();

		for (Rectangle rectangle : rectangleList) {
			rootGridPane.add(rectangle,0,1);
		}



	}

	private Shape createGameStructuralGrid(){

		Shape rectangleWithHoles = new Rectangle((COLUMNS + 1) * CIRCLE_DIAMETER,(ROWS + 1) * CIRCLE_DIAMETER);

		for(int row = 0; row < ROWS ; row++){
			for(int col = 0 ; col < COLUMNS ; col++ ){

				Circle circle = new Circle();
				circle.setRadius(CIRCLE_DIAMETER / 2);
				circle.setCenterX(CIRCLE_DIAMETER/2);
				circle.setCenterY(CIRCLE_DIAMETER/2);
				circle.setSmooth(true);

				circle.setTranslateX(col * (CIRCLE_DIAMETER + 5 ) + CIRCLE_DIAMETER / 4) ;
				circle.setTranslateY(row * (CIRCLE_DIAMETER + 5)+ CIRCLE_DIAMETER / 4);

				rectangleWithHoles = Shape.subtract(rectangleWithHoles , circle);
			}
		}

		rectangleWithHoles.setFill(Color.WHITE);

		return rectangleWithHoles;
	}

	private List createClickableColumns(){

		List<Rectangle> rectangleList = new ArrayList<>();
		for(int col = 0; col < COLUMNS ; col++) {

			Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);

			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

			final int column = col;
			rectangle.setOnMouseClicked(event -> {
				if(isAllowedToinsert) {
					isAllowedToinsert = false; // When disc is being dropped then no more disc will be inserted
					insertDisc(new Disc(isPlayerOneTurn), column);
				}
			});

			rectangleList.add(rectangle);
		}
		return rectangleList;
	}

	private void insertDisc(Disc disc , int column){

		int row = ROWS - 1;
		while( row >= 0 ){
			if(getDiscIfPresent(row,column) == null)
				break;
			row--;
		}
		if(row < 0) // if it is full we cannot insert anymore disc
			return;

		insertDiscArray[row][column] = disc;
		insertedDiscPane.getChildren().add(disc);

		disc.setTranslateX(column *(CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);

		int currentRow = row;
		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5),disc);
		translateTransition.setToY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);
		translateTransition.setOnFinished(event -> {

			isAllowedToinsert = true; // finally when disc is dropped allow next player to insert disc
			if(gameEnded(currentRow,column)){
				gameOver();
				return;
			}
			isPlayerOneTurn = !isPlayerOneTurn;
			playerName.setText(isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO);
		});
		translateTransition.play();

	}

	private void gameOver() {

		String winner = isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO;
		System.out.println(winner);

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The Winner is " + winner);
		alert.setContentText("Want to play again ? ");

		ButtonType yesbtn = new ButtonType("Yes");
		ButtonType nobtn = new ButtonType("No, Exit");
		alert.getButtonTypes().setAll(yesbtn,nobtn);

		Platform.runLater(()->{
			Optional<ButtonType> btnClicked = alert.showAndWait();
			if(btnClicked .isPresent() && btnClicked.get() == yesbtn){
				resetGame();
			}else {
				Platform.exit();
				System.exit(0);
			}
		});
	}

	public void resetGame() {

		insertedDiscPane.getChildren().clear();
		disc_count = 0;
		for(int row = 0; row < ROWS;row++){
			for(int col =0 ; col < COLUMNS;col++){
				insertDiscArray[row][col] = null;
			}
		}
		isPlayerOneTurn = true;
		playerName.setText(PLAYER_ONE);

		createPlayground(); // prepare a fresh playground


	}

	private boolean gameEnded(int currentRow, int column) {

		// Vertical Points. A small example : Player has inserted his last disc at row = 2 ,column = 3



		List<Point2D> verticalPoints = IntStream.rangeClosed(currentRow - 3,currentRow + 3) // range of row values = 0,1,2,3,4,5
										.mapToObj(r -> new Point2D(r,column)) // index of each element present in column [row][column] : 0,3 1,3 2,3 3,3 4,3 5,3
										.collect(Collectors.toList());

		List<Point2D> horizontalPoints = IntStream.rangeClosed(column - 3,column + 3)
				.mapToObj(col -> new Point2D(currentRow , col))
				.collect(Collectors.toList());

		Point2D startPoint1 = new Point2D(currentRow - 3, column + 3);
		List<Point2D> diagonal1Points = IntStream.rangeClosed(0, 6)
				.mapToObj(i -> startPoint1.add(i, -i))
				.collect(Collectors.toList());

		Point2D startPoint2 = new Point2D(currentRow - 3, column - 3);
		List<Point2D> diagonal2Points = IntStream.rangeClosed(0, 6)
				.mapToObj(i -> startPoint2.add(i, i))
				.collect(Collectors.toList());

		boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints) || checkCombinations(diagonal1Points) || checkCombinations(diagonal2Points);



		disc_count++;
		//System.out.println(disc_count);

		if(disc_count >= 42){ // check if game is drawn
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Connect Four");
			alert.setHeaderText("Draw");
			alert.setContentText("Want to play again ? ");

			ButtonType yesbtn = new ButtonType("Yes");
			ButtonType nobtn = new ButtonType("No, Exit");
			alert.getButtonTypes().setAll(yesbtn,nobtn);

			Platform.runLater(()->{
				Optional<ButtonType> btnClicked = alert.showAndWait();
				if(btnClicked .isPresent() && btnClicked.get() == yesbtn){
					resetGame();
				}else {
					Platform.exit();
					System.exit(0);
				}
			});
		}

		return isEnded;
	}

	private boolean checkCombinations(List<Point2D> points) {

		int chain = 0;

		for (Point2D point : points) {

			int rowIndexForArray = (int) point.getX();
			int columnIndexForArray = (int) point.getY();

			Disc disc = getDiscIfPresent(rowIndexForArray, columnIndexForArray);

			if (disc != null && disc.isPlayerOneMove == isPlayerOneTurn) {

				chain++;

				if (chain == 4) {
					return true;
				}
			} else {
				chain = 0;
			}
		}

		return false;
	}

	private Disc getDiscIfPresent(int row,int column){ // To prevent ArrayOutOfBoundException

		if(row >= ROWS || row < 0 || column >= COLUMNS || column < 0){
			return null;
		}
		return insertDiscArray[row][column];
	}


	private static class Disc extends Circle{

		private final boolean isPlayerOneMove;

		public Disc(boolean isPlayerOneMove){

			this.isPlayerOneMove = isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER/2);
			setFill(isPlayerOneMove ? Color.valueOf(discColor1):Color.valueOf(discColor2));
			setCenterX(CIRCLE_DIAMETER/2);
			setCenterY(CIRCLE_DIAMETER/2);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
