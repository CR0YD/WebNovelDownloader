package main.java;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlParser;

public class SourceManager {

	private static final String SOURCES_FOLDER_PATH = "resources/sourceConfigs";

	public static String[] getSourceNames() {
		Config sourceRegister = new TomlParser().parse(new File(SOURCES_FOLDER_PATH + "/SourceRegister.toml"),
				FileNotFoundAction.THROW_ERROR, StandardCharsets.UTF_8);

		return sourceRegister.valueMap().keySet().toArray(new String[0]);
	}

	public static LinkedList<String> getNovelNames(String source) throws Exception {
		File sourceFile = new File(SOURCES_FOLDER_PATH + "/" + getSourceConfigs(source).valueMap().get("sourcePath"));

		Map<String, Object> sourceFileConfigMap = new TomlParser()
				.parse(sourceFile, FileNotFoundAction.THROW_ERROR, Charset.forName("utf-8")).valueMap();

		File novelList = new File(sourceFile.getParent() + "/"
				+ ((Config) sourceFileConfigMap.get("novelList")).valueMap().get("novelListPath"));

		if (!novelList.exists()) {
			return new LinkedList<>();
		}

		LinkedList<String> novelNames = new LinkedList<>();

		getNovelsFromFile(novelList, "").forEach(entry -> {
			novelNames.add(entry.get("title").toString());
		});

		return novelNames;
	}

	public static LinkedList<String> getNovelDetails(String source, String novelName) throws Exception {
		File sourceFile = new File(SOURCES_FOLDER_PATH + "/" + getSourceConfigs(source).valueMap().get("sourcePath"));

		Map<String, Object> sourceFileConfigMap = new TomlParser()
				.parse(sourceFile, FileNotFoundAction.THROW_ERROR, Charset.forName("utf-8")).valueMap();

		File novelList = new File(sourceFile.getParent() + "/"
				+ ((Config) sourceFileConfigMap.get("novelList")).valueMap().get("novelListPath"));

		if (!novelList.exists()) {
			return new LinkedList<>();
		}

		JSONObject novel = getNovelsFromFile(novelList, novelName).getFirst();

		LinkedList<String> result = new LinkedList<>();

		result.add(novelName);

		Document novelOverview = Jsoup.connect((String) novel.get("url")).get();

		String coverUrl = novelOverview.getAllElements()
				.select(((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("cover").toString())
				.attr(((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("cover-attr") == null ? "src"
						: (String) ((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("cover-attr"));
		
		String baseLink = ((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("baseLink") == null ? ""
				: ((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("baseLink").toString();

		result.add(verifyURL(coverUrl) ? coverUrl : baseLink + coverUrl);

		StringBuilder description = new StringBuilder();

		novelOverview.getAllElements()
				.select((String) ((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("description"))
				.forEach(e -> description.append(e.text() + "\n\n"));

		result.add(description.toString().substring(0, description.toString().length() - 2));

		return result;
	}
	
	private static boolean verifyURL(String url) {
		try {
	        new URL(url).toURI();
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}

	public static String getFirstChapterLink(String source, String novelName) throws Exception {
		File sourceFile = new File(SOURCES_FOLDER_PATH + "/" + getSourceConfigs(source).valueMap().get("sourcePath"));

		Map<String, Object> sourceFileConfigMap = new TomlParser()
				.parse(sourceFile, FileNotFoundAction.THROW_ERROR, Charset.forName("utf-8")).valueMap();

		File novelList = new File(sourceFile.getParent() + "/"
				+ ((Config) sourceFileConfigMap.get("novelList")).valueMap().get("novelListPath"));

		if (!novelList.exists()) {
			return "";
		}

		JSONObject novel = getNovelsFromFile(novelList, novelName).getFirst();

		Document novelOverview = Jsoup.connect((String) novel.get("url")).get();

		
		
		String firstChapterUrl = novelOverview.getAllElements()
				.select(((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("firstChapter").toString())
				.attr("href");
		
		String baseLink = ((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("baseLink") == null ? ""
				: ((Config) sourceFileConfigMap.get("novelOverview")).valueMap().get("baseLink").toString();
		
		return firstChapterUrl.startsWith(baseLink) ? firstChapterUrl : baseLink + firstChapterUrl;
	}

	public static String getNovelLink(String source, String novelName) throws Exception {
		File sourceFile = new File(SOURCES_FOLDER_PATH + "/" + getSourceConfigs(source).valueMap().get("sourcePath"));

		Map<String, Object> sourceFileConfigMap = new TomlParser()
				.parse(sourceFile, FileNotFoundAction.THROW_ERROR, Charset.forName("utf-8")).valueMap();

		File novelList = new File(sourceFile.getParent() + "/"
				+ ((Config) sourceFileConfigMap.get("novelList")).valueMap().get("novelListPath"));

		if (!novelList.exists()) {
			return "";
		}

		JSONObject novel = getNovelsFromFile(novelList, novelName).getFirst();

		return novel.get("url").toString();
	}

	private static LinkedList<JSONObject> getNovelsFromFile(File novelList, String titleFilter) throws Exception {
		Scanner reader = new Scanner(novelList, StandardCharsets.UTF_8);

		StringBuilder result = new StringBuilder();

		while (reader.hasNextLine()) {
			result.append(reader.nextLine());
		}

		reader.close();

		LinkedList<JSONObject> novels = new LinkedList<>();

		((JSONArray) new JSONObject(result.toString()).get("novels")).forEach(entry -> {
			if (titleFilter.isBlank() ? true
					: new JSONObject(entry.toString()).get("title").toString().equals(titleFilter)) {
				novels.add(new JSONObject(entry.toString()));
			}
		});

		return novels;
	}

	public static void fetchNovels(String source) throws Exception {

		Map<String, Object> sourceConfigMap = getSourceConfigs(source).valueMap();

		File sourceFile = new File(SOURCES_FOLDER_PATH + "/" + sourceConfigMap.get("sourcePath"));

		Map<String, Object> sourceFileConfigMap = new TomlParser()
				.parse(sourceFile, FileNotFoundAction.THROW_ERROR, Charset.forName("utf-8")).valueMap();

		Map<String, Object> novelCollection = ((Config) sourceFileConfigMap.get("novelCollection")).valueMap();

		String url = novelCollection.get("url").toString();

		// create new thread to execute the below code!!!!
		// maybe create some sort of loading indicator for the user
		JSONArray novels = new JSONArray();

		String baseLink = novelCollection.get("baseLink") == null ? "" : novelCollection.get("baseLink").toString();

		for (int i = 0; true; i++) {
			Document dom = Jsoup.connect(url).get();
			dom.getAllElements().select((String) novelCollection.get("novelElement")).forEach(element -> {
				novels.put(new JSONObject("{ title: \"" + element.text().replace("\\", "\\\\").replace("\"", "\\\"")
						+ "\", url: \"" + (element.attr("href").startsWith(baseLink) ? element.attr("href")
								: baseLink + element.attr("href"))
						+ "\"}"));
			});

			System.out.println(i + " done!");

			if ((dom.getAllElements().select((String) novelCollection.get("nextPage")).attr("href").startsWith(baseLink)
					? dom.getAllElements().select((String) novelCollection.get("nextPage")).attr("href")
					: baseLink + dom.getAllElements().select((String) novelCollection.get("nextPage")).attr("href"))
					.equals(url)) {
				break;
			}

			url = dom.getAllElements().select((String) novelCollection.get("nextPage")).attr("href");

			if (url.isBlank()) {
				break;
			}

			if (url.startsWith(baseLink)) {
				continue;
			}

			url = baseLink + url;
		}

		JSONObject result = new JSONObject();

		result.put("novels", novels);
		result.put("baseURL", sourceConfigMap.get("url"));

		File novelList = new File(sourceFile.getParent() + "/"
				+ ((Config) sourceFileConfigMap.get("novelList")).valueMap().get("novelListPath"));

		if (!novelList.exists()) {
			novelList.createNewFile();
		}

		FileWriter saver = new FileWriter(novelList, StandardCharsets.UTF_8);

		saver.write(result.toString());
		saver.flush();
		saver.close();
	}

	public static Config getSourceConfigs(String source) {
		Config sourceRegister = new TomlParser().parse(new File(SOURCES_FOLDER_PATH + "/SourceRegister.toml"),
				FileNotFoundAction.THROW_ERROR, StandardCharsets.UTF_8);

		for (String key : sourceRegister.valueMap().keySet()) {
			if (key.equals(source)) {
				return (Config) sourceRegister.valueMap().get(key);
			}
		}
		return null;
	}

}
