package main.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class App extends Application {
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		
		GUI gui = new GUI(FXMLLoader.load(getClass().getResource("/NovelView.fxml")));

		stage.setTitle("WebNovelDownloader");
		stage.setScene(gui.getScene());
		stage.setResizable(false);
		stage.show();
	}
	
}
