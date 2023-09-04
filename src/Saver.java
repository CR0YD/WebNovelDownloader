import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

public class Saver {

	public static void saveChapterInFile(List<String> chapter, String filePath, String fileExtension, boolean append) throws Exception {
		if (fileExtension.equalsIgnoreCase("txt")) {
			saveInTxt(chapter, new File(filePath + ".txt"), append);
			return;
		}
	}
	
	private static void saveInTxt(List<String> chapter, File file, boolean append) throws Exception {
		file.createNewFile();
		FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8, append);
		
		for (int i = 0; i < chapter.length() - 1; i++) {
			fileWriter.write(chapter.get(i));
			if (i < chapter.length() - 2) {
				fileWriter.write("\n\n");
			}
		}
		
		fileWriter.flush();
		fileWriter.close();
	}
}
