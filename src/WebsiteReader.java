import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlParser;

public class WebsiteReader {

	public static void main(String[] args) {
		try {
			LinkedList<String> chapter = readNovelFromUrl(
					"https://www.novelpub.com/novel/the-academys-weakest-became-a-demon-limited-hunter-1632/chapter-1");
			chapter.forEach(element -> System.out.println(element));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static LinkedList<String> readNovelFromUrl(String url) throws Exception {
		Config sourceRegister = new TomlParser().parse(new File("sources/SourceRegister.toml"),
				FileNotFoundAction.THROW_ERROR, Charset.forName("utf-8"));

		for (String key : sourceRegister.valueMap().keySet()) {
			if (new URL(sourceRegister.get(key + ".url"))
					.sameFile(new URL(url.replace(new URL(url).getPath(), "")))) {
				Config sourceFile = new TomlParser().parse(
						new File("sources/" + sourceRegister.get(key + ".sourcePath")), FileNotFoundAction.THROW_ERROR,
						Charset.forName("utf-8"));

				Map<String, Object> sourceFileValues = sourceFile.valueMap();

				return read(((Config) sourceFileValues.get("titleElement")).valueMap(),
						((Config) sourceFileValues.get("contentElement")).valueMap(),
						((Config) sourceFileValues.get("nextLinkElement")).valueMap(), url);
			}
		}

		throw new Exception("No source for the url " + url + "was found");
	}

	private static LinkedList<String> read(Map<String, Object> titleElement, Map<String, Object> contentElement,
			Map<String, Object> nextLinkElement, String url) throws Exception {
		Document doc = Jsoup.connect(url).get();
		doc = Jsoup.parse(doc.html(), "UTF-8");

		LinkedList<String> chapter = new LinkedList<>();

		// parsing chapter title
		chapter.add(getTitle(doc, titleElement));

		// parsing chapter content
		chapter.addAll(getChapter(doc, contentElement));

		// parsing next chapter link
		chapter.add(getNextChapterLink(doc, nextLinkElement));

		return chapter;
	}

	private static String getTitle(Document doc, Map<String, Object> titleElement) throws Exception {
		if (!titleElement.containsKey("id") && !titleElement.containsKey("class")) {
			throw new Exception("There has to be either an \"class\" or an \"id\" key to find the title element");
		}

		Element element = findElement(doc, (String) titleElement.get("id"), (String) titleElement.get("class"));

		if (element == null) {
			throw new Exception("No title element was found");
		}

		return element.text();
	}

	private static LinkedList<String> getChapter(Document doc, Map<String, Object> contentElement) throws Exception {
		if (!contentElement.containsKey("id") && !contentElement.containsKey("class")) {
			throw new Exception("There has to be either an \"class\" or an \"id\" key to find the chapter element");
		}

		Element element = findElement(doc, (String) contentElement.get("id"), (String) contentElement.get("class"));

		if (element == null) {
			throw new Exception("No title element was found");
		}

		LinkedList<String> chapter = new LinkedList<>();

		for (Element child : element.children()) {
			if (!child.text().isBlank()) {
				chapter.add(child.text());
			}
		}
		return chapter;
	}

	private static String getNextChapterLink(Document doc, Map<String, Object> nextLinkElement) throws Exception {
		if (!nextLinkElement.containsKey("id") && !nextLinkElement.containsKey("class")) {
			throw new Exception("There has to be either an \"class\" or an \"id\" key to find the next chapter link");
		}

		Element element = findElement(doc, (String) nextLinkElement.get("id"), (String) nextLinkElement.get("class"));

		if (element == null) {
			return "";
		}

		return nextLinkElement.get("baseLink") == null ? element.attr("href")
						: (String) nextLinkElement.get("baseLink") + element.attr("href");
	}

	private static Element findElement(Document doc, String elementId, String elementClass) {
		Element[] elements = new Element[0];

		elements = doc.getAllElements().toArray(elements);

		for (Element element : elements) {
			if (elementId != null ? element.id().equals(elementId)
					: true && elementClass != null ? element.className().equals(elementClass) : true) {
				return element;
			}
		}

		return null;
	}

}
