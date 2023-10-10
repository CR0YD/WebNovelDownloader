package main.java;

import java.io.File;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlParser;

public class WebsiteReader {

	private static final String SOURCES_FOLDER_PATH = "resources/sourceConfigs";
	
	public static LinkedList<String> readNovelFromUrl(String url, String source) throws Exception {
		File sourceFile = new File(SOURCES_FOLDER_PATH + "/" + SourceManager.getSourceConfigs(source).valueMap().get("sourcePath"));

		Map<String, Object> sourceFileValues = new TomlParser()
				.parse(sourceFile, FileNotFoundAction.THROW_ERROR, Charset.forName("utf-8")).valueMap();

		return read(((Config) sourceFileValues.get("novelChapter")).valueMap(), url);
	}

	public static String readCoverFromUrl(String url, String source) throws Exception {
		File sourceFile = new File(SOURCES_FOLDER_PATH + "/" + SourceManager.getSourceConfigs(source).valueMap().get("sourcePath"));

		Map<String, Object> sourceFileConfigMap = new TomlParser()
				.parse(sourceFile, FileNotFoundAction.THROW_ERROR, Charset.forName("utf-8")).valueMap();

		Document novelOverview = Jsoup.connect(url).get();
		
		String coverUrl = novelOverview.getAllElements()
				.select(((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("cover").toString())
				.attr(((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("cover-attr") == null ? "src"
						: (String) ((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("cover-attr"));
		
		String baseLink = ((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("baseLink") == null ? "" : ((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("baseLink").toString();
		
		return coverUrl.startsWith(baseLink) ? coverUrl : baseLink + coverUrl;
	}

	private static LinkedList<String> read(Map<String, Object> novelChapter, String url) throws Exception {
		Document doc = Jsoup.connect(url).get();

		LinkedList<String> chapter = new LinkedList<>();

		// parsing chapter title
		chapter.add(doc.getAllElements().select(novelChapter.get("title").toString()).first().text());

		// parsing chapter content
		doc.getAllElements().select(novelChapter.get("content").toString()).forEach(element -> {
			if (!element.text().isBlank()) {
				chapter.add(element.text());
			}
		});

		// parsing next chapter link
		String nextChapterLink = doc.getAllElements().select(novelChapter.get("nextChapter").toString()).attr("href");
		
		if (nextChapterLink.isBlank()) {
			chapter.add("");
			return chapter;
		}
		
		String baseLink = novelChapter.get("baseLink") == null ? "" : novelChapter.get("baseLink").toString();
		String endLink = novelChapter.get("endLink") == null ? "" : novelChapter.get("endLink").toString();

		if (endLink.isBlank()) {
			chapter.add(nextChapterLink.isBlank() ? ""
					: (nextChapterLink.startsWith(baseLink) ? nextChapterLink : baseLink + nextChapterLink));
			return chapter;
		}

		chapter.add(nextChapterLink.equals(endLink) ? ""
				: (nextChapterLink.startsWith(baseLink) ? nextChapterLink : baseLink + nextChapterLink));
		return chapter;
	}

}
