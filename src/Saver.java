import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

public class Saver {

	public static void saveChapterInFile(LinkedList<String> chapter, String filePath, String fileExtension,
			boolean append) throws Exception {
		if (fileExtension.equalsIgnoreCase("txt")) {
			saveInTxt(chapter, new File(filePath + ".txt"), append);
			return;
		}
		if (fileExtension.equalsIgnoreCase("docx")) {
			saveInDocx(chapter, filePath, append);
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
				fileWriter.write("\n\n" + line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		fileWriter.flush();
		fileWriter.close();
	}

	private static void saveInDocx(LinkedList<String> chapter, String filePath, boolean append) throws Exception {
		File baseDir = new File(filePath);
		baseDir.mkdir();

		if (!new File(filePath + "\\[Content_Types].xml").exists())
			FileUtils.copyDirectory(new File("blueprints/docx"), baseDir);

		//File document = new File(baseDir + "\\word\\document.xml");

		/*
		 * SAXReader sr = new SAXReader(); Document doc = sr.read(document);
		 */

		FileWriter out = new FileWriter(baseDir + "\\word\\document.xml", StandardCharsets.UTF_8, true);

		chapter.forEach(line -> {
			if ((line.equals(chapter.getFirst()) && chapter.getFirst().equals(chapter.get(1)))
					|| line.equals(chapter.getLast())) {
				return;
			}

			try {
				out.write("<w:p><w:r><w:t>" + line.replace("<", "\u2770").replace(">", "\u2771").replace("&", "&amp;")
						.replace("\"", "&quot;").replace("'", "&apos;") + "</w:t></w:r></w:p>");
				out.write("<w:p><w:r><w:t></w:t></w:r></w:p>");
			} catch (IOException e) {
				e.printStackTrace();
			}
			/*
			 * Element paragraphRoot =
			 * doc.getRootElement().element("body").addElement("w:p");
			 * paragraphRoot.addElement("w:pPr").addElement("w:pStyle").addAttribute(
			 * "w:val", "KeinLeerraum");
			 * paragraphRoot.addElement("w:r").addElement("w:t").addText(line);
			 * paragraphRoot = doc.getRootElement().element("body").addElement("w:p");
			 * paragraphRoot.addElement("w:pPr").addElement("w:pStyle").addAttribute(
			 * "w:val", "KeinLeerraum");
			 * paragraphRoot.addElement("w:r").addElement("w:t").addText("");
			 */
		});

		/*
		 * FileWriter out = new FileWriter(baseDir + "\\word\\document.xml",
		 * StandardCharsets.UTF_8); out.write(doc.asXML()); out.flush(); out.close();
		 */

		if (append) {
			out.flush();
			out.close();
			return;
		}

		out.write("</w:body></w:document>");
		out.flush();
		out.close();

		// creating the actual .docx
		FileOutputStream fos = new FileOutputStream(baseDir.getParent() + "\\" + baseDir.getName() + ".docx");
		ZipOutputStream zipOut = new ZipOutputStream(fos);

		for (String name : baseDir.list()) {
			zipFile(new File(baseDir + "\\" + name), name, zipOut);
		}

		zipOut.close();
		fos.close();

		deleteFile(baseDir);
	}

	public static void finishSaveFiles(String filePath, String fileExtension, boolean append) throws Exception {
		if (fileExtension.equalsIgnoreCase("docx")) {
			if (!append) {
				return;
			}

			File baseDir = new File(filePath);

			FileWriter out = new FileWriter(baseDir + "\\word\\document.xml", StandardCharsets.UTF_8, true);
			out.write("</w:body></w:document>");
			out.flush();
			out.close();

			FileOutputStream fos = new FileOutputStream(baseDir.getParent() + "\\" + baseDir.getName() + ".docx");
			ZipOutputStream zipOut = new ZipOutputStream(fos);

			for (String name : baseDir.list()) {
				zipFile(new File(baseDir + "\\" + name), name, zipOut);
			}

			zipOut.close();
			fos.close();

			deleteFile(baseDir);

			return;
		}
	}

	private static void deleteFile(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}
		for (String name : file.list()) {
			deleteFile(new File(file.getAbsolutePath() + "\\" + name));
		}
		file.delete();
	}

	private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			if (fileName.endsWith("/")) {
				zipOut.putNextEntry(new ZipEntry(fileName));
				zipOut.closeEntry();
			} else {
				zipOut.putNextEntry(new ZipEntry(fileName + "/"));
				zipOut.closeEntry();
			}
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}
}
