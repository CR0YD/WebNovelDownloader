import java.io.File;
import java.util.LinkedList;

public class ReaderThread extends Thread {

	private GUI gui;
	private String url, bookTitle, bookTitleCorrected, path, fileExtension;
	private boolean append;

	public ReaderThread(GUI gui, String url, String bookTitle, String path, String fileExtension, boolean append) {
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
			
			createDirectory(gui, path);
			
			for (int i = 0; true; i++) {
				list = WebsiteReader.readNovelFromUrl(url);
				Saver.saveChapterInFile(list, append ? path : path + "\\" + i, fileExtension, append);
				gui.addProgressTextAreaText("Chapter " + i + " saved");
				url = list.getLast();
				if (url.isBlank())
					break;
			}
			
			Saver.finishSaveFiles(path, fileExtension, append);
			
			if (!append)
				correctFileNames(path);
			
			gui.addProgressTextAreaText("Done!");
		} catch (Exception exception) {
			gui.addProgressTextAreaText(exception.toString());
			exception.printStackTrace();
		}
	}

	private static void correctFileNames(String path) {
		File storyDirectory = new File(path);
		
		if (!storyDirectory.exists() || storyDirectory.listFiles().length < 11) {
			return;
		}
		
		File[] files = storyDirectory.listFiles();
		int maxFileNameLength = ("" + (files.length - 1)).length();
		
		for (File file : files) {
			if (file.getName().substring(0, file.getName().indexOf(".")).length() >= maxFileNameLength) {
				continue;
			}
			file.renameTo(new File(path + "\\"
					+ "0".repeat(maxFileNameLength
							- file.getName().substring(0, file.getName().indexOf(".")).length())
					+ file.getName()));
		}
	}

	private static void createDirectory(GUI gui, String path) throws Exception {
		File storyDirectory = new File(path);
		
		if (!storyDirectory.exists()) {
			storyDirectory.mkdir();
		}
	}
}
