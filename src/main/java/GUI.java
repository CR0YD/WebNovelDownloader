package main.java;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;

public class GUI {

	private Scene scene;

	private ComboBox<String> sourceSelection, typeSelection;
	private Button loadNovels, pathSelectButton, download;
	private ListView<String> novelList;
	private Label novelTitle;
	private ImageView novelCover;
	private TextArea novelDescription;
	private TextField pathTextField;
	private CheckBox singleFileCheck;

	public GUI(Parent root) {
		scene = new Scene(root);
		configureComponents();

	}

	public Scene getScene() {
		return scene;
	}

	@SuppressWarnings("unchecked")
	private void configureComponents() {
		sourceSelection = (ComboBox<String>) scene.lookup("#sourceSelection");
		modifySourceSelection();

		loadNovels = (Button) scene.lookup("#loadNovels");
		modifyLoadNovels();

		novelList = (ListView<String>) scene.lookup("#novelList");
		modifyNovelList();

		novelTitle = (Label) scene.lookup("#novelTitle");
		novelCover = (ImageView) scene.lookup("#novelCover");
		novelDescription = (TextArea) scene.lookup("#novelDescription");

		typeSelection = (ComboBox<String>) scene.lookup("#typeSelection");
		modifyTypeSelection();

		pathTextField = (TextField) scene.lookup("#pathTextField");

		pathSelectButton = (Button) scene.lookup("#pathSelectButton");
		modifyPathSelectButton();

		singleFileCheck = (CheckBox) scene.lookup("#singleFileCheck");

		download = (Button) scene.lookup("#download");
		modifyDownload();
	}

	private void modifySourceSelection() {
		ObservableList<String> sourceOptions = FXCollections
				.observableArrayList(Arrays.stream(SourceManager.getSourceNames()).toList());

		sourceSelection.setItems(sourceOptions);
		sourceSelection.setVisibleRowCount(5);

		sourceSelection.valueProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				try {
					updateNovelList(newValue);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
	}

	private void modifyLoadNovels() {
		loadNovels.pressedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					try {
						String source = sourceSelection.valueProperty().getValue().toString();
						SourceManager.fetchNovels(source);
						updateNovelList(source);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		});
	}

	private void modifyNovelList() {
		novelList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue == null) {
					return;
				}
				try {
					LinkedList<String> novelOverview = SourceManager
							.getNovelDetails(sourceSelection.valueProperty().getValue().toString(), newValue);

					novelTitle.setText(novelOverview.get(0));

					Image cover = new Image((String) novelOverview.get(1));
					novelCover.setLayoutX((639 - cover.getWidth()) / 2);
					novelCover.setImage(cover);

					novelDescription.setText(novelOverview.get(2));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void updateNovelList(String source) throws Exception {
		novelList.getSelectionModel().clearSelection();
		LinkedList<String> novelNames = SourceManager.getNovelNames(source);

		novelNames.sort(Comparator.naturalOrder());

		novelList.setItems(FXCollections.observableList(novelNames));
	}

	private void modifyTypeSelection() {
		ObservableList<String> sourceOptions = FXCollections.observableArrayList("EPUB", "TXT", "DOCX");

		typeSelection.setItems(sourceOptions);
		typeSelection.setVisibleRowCount(5);
	}

	private void modifyPathSelectButton() {
		pathSelectButton.pressedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					DirectoryChooser directoryChooser = new DirectoryChooser();
					File directory = directoryChooser.showDialog(scene.getWindow());
					if (directory != null) {
						pathTextField.setText(directory.getAbsolutePath());
					}
				}
			}

		});
	}

	private void modifyDownload() {
		GUI gui = this;
		download.pressedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					if (typeSelection.itemsProperty().getValue() == null) {
						System.out.println("Please select a type!");
						return;
					}
					if (pathTextField.getText().isBlank()) {
						System.out.println("Please select a destination!");
						return;
					}
					try {
						ReaderThread reader = new ReaderThread(sourceSelection.valueProperty().getValue().toString(), gui,
								SourceManager.getNovelLink(sourceSelection.valueProperty().getValue().toString(),
										novelList.getSelectionModel().getSelectedItem()),
								novelList.getSelectionModel().getSelectedItem(), pathTextField.getText(),
								typeSelection.valueProperty().getValue(), singleFileCheck.isSelected());
						reader.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		});
	}

	public void addProgressTextAreaText(String text) {
		System.out.println(text);
	}

}
