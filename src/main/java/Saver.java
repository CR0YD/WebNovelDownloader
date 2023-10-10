package main.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Saver {

	public static void saveChapterInFile(LinkedList<String> chapter, String filePath, String bookTitle, int chapterId,
			String fileExtension, boolean append) throws Exception {
		if (fileExtension.equalsIgnoreCase("txt")) {
			saveInTxt(chapter, new File(filePath + ".txt"), append);
			return;
		}
		if (fileExtension.equalsIgnoreCase("docx")) {
			saveInDocx(chapter, filePath, append);
			return;
		}
		if (fileExtension.equalsIgnoreCase("epub")) {
			saveInEpub(chapter, filePath, bookTitle, chapterId, append);
			return;
		}
	}

	public static void saveCover(String coverPath, String filePath, String bookTitle, String fileExtension)
			throws Exception {
		if (fileExtension.equalsIgnoreCase("epub")) {
			File baseDir = new File(filePath);
			baseDir.mkdir();

			FileUtils.copyDirectory(new File("resources/fileBlueprints/epub/folder"), baseDir);
			writeToXmlFile(baseDir + "\\OEBPS\\toc.xhtml", "head", "<title>" + bookTitle + "</title>\n");
			writeToXmlFile(baseDir + "\\OEBPS\\toc.ncx", "docTitle", "<title>" + bookTitle + "</title>\n");

			writeToXmlFile(baseDir + "\\OEBPS\\content.opf", "metadata", "<meta name=\"cover\" content=\"cover\"/>");
			writeToXmlFile(baseDir + "\\OEBPS\\content.opf", "manifest",
					"<item id=\"titlepage\" href=\"titlepage.xhtml\" media-type=\"application/xhtml+xml\"/>");
			writeToXmlFile(baseDir + "\\OEBPS\\content.opf", "manifest",
					"<item id=\"cover\" href=\"cover.jpg\" media-type=\"image/jpeg\"/>");
			writeToXmlFile(baseDir + "\\OEBPS\\content.opf", "spine", "<itemref idref=\"titlepage\"/>");

			writeToXmlFile(baseDir + "\\OEBPS\\content.opf", "manifest",
					"<item id=\"chapters\" href=\"toc.xhtml\" media-type=\"application/xhtml+xml\"/>");
			writeToXmlFile(baseDir + "\\OEBPS\\content.opf", "spine", "<itemref idref=\"chapters\"/>");

			FileUtils.copyFile(new File("resources/fileBlueprints/epub/singleFiles/titlepage.xhtml"),
					new File(baseDir + "\\OEBPS\\titlepage.xhtml"), false);

			URL url = new URL(coverPath);
			
			System.out.println(url);
			
			ImageIO.write(ImageIO.read(url), "jpg", new File(baseDir + "\\OEBPS\\cover.jpg"));
		}
	}

	private static void saveInTxt(LinkedList<String> chapter, File file, boolean append) throws Exception {
		file.createNewFile();
		FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8, append);

		chapter.forEach(line -> {
			if (line.equals(chapter.getLast())) {
				return;
			}
			try {
				if (line.equals(chapter.getFirst())) {
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
			FileUtils.copyDirectory(new File("resources/fileBlueprints/docx"), baseDir);

		FileWriter out = new FileWriter(baseDir + "\\word\\document.xml", StandardCharsets.UTF_8, true);

		chapter.forEach(line -> {
			if ((line.equals(chapter.getFirst()) && chapter.getFirst().equals(chapter.get(1)))
					|| line.equals(chapter.getLast())) {
				return;
			}

			try {
				out.write(
						"<w:p w:rsidR=\"00745BCB\" w:rsidRDefault=\"00745BCB\" w:rsidP=\"00AE4728\"><w:pPr><w:pStyle w:val=\"KeinLeerraum\" /></w:pPr><w:r><w:t>"
								+ line.replace("<", "\u2770").replace(">", "\u2771").replace("&", "&amp;")
										.replace("\"", "&quot;").replace("'", "&apos;")
								+ "</w:t></w:r></w:p>");
				out.write(
						"<w:p w:rsidR=\"00745BCB\" w:rsidRDefault=\"00745BCB\" w:rsidP=\"00AE4728\"><w:pPr><w:pStyle w:val=\"KeinLeerraum\" /></w:pPr></w:p>");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

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

	private static void saveInEpub(LinkedList<String> chapter, String filePath, String bookTitle, int chapterId,
			boolean append) throws Exception {
		File baseDir = new File(filePath);
		baseDir.mkdir();

		if (!new File(filePath + "\\mimetype").exists()) {
			FileUtils.copyDirectory(new File("resources/fileBlueprints/epub/folder"), baseDir);
			writeToXmlFile(baseDir + "\\OEBPS\\toc.xhtml", "head", "<title>" + bookTitle + "</title>\n");
			writeToXmlFile(baseDir + "\\OEBPS\\toc.ncx", "docTitle", "<title>" + bookTitle + "</title>\n");
			writeToXmlFile(baseDir + "\\OEBPS\\content.opf", "manifest",
					"<item id=\"chapters\" href=\"toc.xhtml\" media-type=\"application/xhtml+xml\"/>");
			writeToXmlFile(baseDir + "\\OEBPS\\content.opf", "spine", "<itemref idref=\"chapters\"/>");
		}

		FileUtils.copyFile(new File("resources/fileBlueprints/epub/singleFiles/chapter.xhtml"),
				new File(baseDir + "\\OEBPS\\chapter" + chapterId + ".xhtml"), false);

		writeToXmlFile(baseDir + "\\OEBPS\\chapter" + chapterId + ".xhtml", "head",
				"<title>" + bookTitle + "</title>\n");
		writeToXmlFile(baseDir + "\\OEBPS\\chapter" + chapterId + ".xhtml", "body", chapter);

		writeToXmlFile(baseDir + "\\OEBPS\\toc.xhtml", "div",
				"<p><a href=\"chapter" + chapterId + ".xhtml\">" + chapter.getFirst() + "</a></p>\n");

		writeToXmlFile(baseDir + "\\OEBPS\\content.opf", "manifest", "<item id=\"chapter" + chapterId
				+ "\" href=\"chapter" + chapterId + ".xhtml\" media-type=\"application/xhtml+xml\"/>");

		writeToXmlFile(baseDir + "\\OEBPS\\content.opf", "spine", "<itemref idref=\"chapter" + chapterId + "\"/>");

		writeToXmlFile(baseDir + "\\OEBPS\\toc.ncx", "navMap",
				"<navPoint id=\"num_" + chapterId + "\" playOrder=\"" + chapterId
						+ "\"><navLabel><text>Chapters</text></navLabel><content src=\"chapter" + chapterId
						+ ".xhtml\"/></navPoint>");
	}

	private static String getXmlString(String path) throws Exception {
		Scanner scanner = new Scanner(new File(path));
		LinkedList<String> lines = new LinkedList<>();
		while (scanner.hasNextLine()) {
			lines.add(scanner.nextLine() + "\n");
		}
		scanner.close();
		
		StringBuilder result = new StringBuilder();
		lines.forEach(result::append);
		
		return result.toString();
	}

	private static void writeToXmlFile(String path, String tag, String content) throws Exception {
		Document doc = Jsoup.parse(FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8), org.jsoup.parser.Parser.xmlParser());
		doc.getElementsByTag(tag).append(content);

		FileWriter writer = new FileWriter(path, StandardCharsets.UTF_8);
		writer.write(doc.outerHtml());
		writer.flush();
		writer.close();
	}

	private static void writeToXmlFile(String path, String tag, LinkedList<String> content) throws Exception {
		Document doc = Jsoup.parse(getXmlString(path), org.jsoup.parser.Parser.xmlParser());
		for (int i = 0; i < content.size() - 1; i++) {
			if (i == 0) {
				doc.getElementsByTag(tag).append("<h3>" + content.get(i).replace("<", "\u2770").replace(">", "\u2771") + "</h3>\n");
				continue;
			}
			doc.getElementsByTag(tag).append("<p>" + content.get(i).replace("<", "\u2770").replace(">", "\u2771") + "</p>\n");
		}

		FileWriter writer = new FileWriter(path, StandardCharsets.UTF_8);
		writer.write(doc.outerHtml());
		writer.flush();
		writer.close();
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

		if (fileExtension.equalsIgnoreCase("epub")) {
			File baseDir = new File(filePath);

			writeToXmlFile(baseDir + "\\OEBPS\\content.opf", "manifest",
					"<item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>");

			FileOutputStream fos = new FileOutputStream(baseDir.getParent() + "\\" + baseDir.getName() + ".epub");
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
