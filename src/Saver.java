import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class Saver {

	public static void saveChapterInFile(LinkedList<String> chapter, String filePath, String fileExtension, boolean append) throws Exception {
		if (fileExtension.equalsIgnoreCase("txt")) {
			saveInTxt(chapter, new File(filePath + ".txt"), append);
			return;
		}
	}
	
	private static void saveInTxt(LinkedList<String> chapter, File file, boolean append) throws Exception {
		file.createNewFile();
		FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8, append);
		
		chapter.forEach(line -> {
			if (line.equals(chapter.getFirst()) || line.equals(chapter.getLast())) {
				return;
			}
			try {
				if (line.equals(chapter.get(1))) {
					fileWriter.write(line);
					return;
				}
				fileWriter.write("\n\n" +line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		fileWriter.flush();
		fileWriter.close();
	}
}
