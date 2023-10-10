package main.java;
import java.io.File;
import java.util.LinkedList;

import main.java.FileSystem.AdjustmentType;

public class ReaderThread extends Thread {

	private GUI gui;
	private String url, bookTitle, bookTitleCorrected, path, fileExtension, source;
	private boolean append;

	public ReaderThread(String source, GUI gui, String url, String bookTitle, String path, String fileExtension, boolean append) {
		this.source = source;
		this.gui = gui;
		this.url = url;
		this.bookTitle = bookTitle;
		this.bookTitleCorrected = bookTitle.replace(" ", "-").replace("\\", "").replace("/", "").replace(":", "")
				.replace("*", "").replace("?", "").replace("\"", "").replace("<", "").replace(">", "").replace("|", "");
		this.path = path + "\\" + bookTitleCorrected;
		this.fileExtension = fileExtension;
		this.append = append;
	}

	@Override
	public void run() {
		super.run();
		try {
			LinkedList<String> list;
			
			File directory = FileSystem.createDirectory(path);
			
			Saver.saveCover(SourceManager.getNovelDetails(source, bookTitle).get(1), path, bookTitle, fileExtension);
			
			url = SourceManager.getFirstChapterLink(source, bookTitle);
			
			for (int i = 0; true; i++) {
				list = WebsiteReader.readNovelFromUrl(url, source);
				Saver.saveChapterInFile(list, append ? path : path + "\\" + i, bookTitle, i, fileExtension, append);
				gui.addProgressTextAreaText("Chapter " + i + " saved");
				url = list.getLast();
				if (url.isBlank())
					break;
			}
			
			Saver.finishSaveFiles(path, fileExtension, append);
			
			if (!append)
				FileSystem.adjustFileNameLength(directory, "0", AdjustmentType.PREFIX);
			
			gui.addProgressTextAreaText("Done!");
		} catch (Exception exception) {
			gui.addProgressTextAreaText(exception.toString());
			exception.printStackTrace();
		}
	}
}
