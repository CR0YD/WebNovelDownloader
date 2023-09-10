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
					"https://noveltrust.com/book/shadow-slave/chapter1-nightmare-begins");
			chapter.forEach(element -> System.out.println(element));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static LinkedList<String> readNovelFromUrl(String url) throws Exception {
		Config sourceRegister = new TomlParser().parse(new File("sources/SourceRegister.toml"),
				FileNotFoundAction.THROW_ERROR, Charset.forName("utf-8"));

		LinkedList<String> chapter = new LinkedList<>();

		sourceRegister.valueMap().forEach((String name, Object value) -> {

			try {
				if (new URL(sourceRegister.get(name + ".url") + "")
						.sameFile(new URL(url.replace(new URL(url).getPath(), "")))) {
					Config sourceFile = new TomlParser().parse(
							new File("sources/" + sourceRegister.get(name + ".sourcePath")),
							FileNotFoundAction.THROW_ERROR, Charset.forName("utf-8"));

					Map<String, Object> sourceFileValues = sourceFile.valueMap();

					read(((Config) sourceFileValues.get("titleElement")).valueMap(),
							((Config) sourceFileValues.get("contentElement")).valueMap(),
							((Config) sourceFileValues.get("nextLinkElement")).valueMap(), url, chapter);

					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		});
		return chapter;
	}

	private static LinkedList<String> read(Map<String, Object> titleElement, Map<String, Object> contentElement,
			Map<String, Object> nextLinkElement, String url, LinkedList<String> chapter) throws Exception {
		Document doc = Jsoup.connect(url).get();
		doc = Jsoup.parse(doc.html(), "UTF-8");

		// parsing chapter title
		chapter.add(getTitle(doc, titleElement));

		// parsing chapter content
		Map<String, Object> contentElementExcpetions = contentElement.get("exceptions") != null
				? ((Config) contentElement.get("exceptions")).valueMap()
				: null;
		if (((String) contentElement.get("class")) == null && ((String) contentElement.get("id")) == null) {
			throw new Exception("There has to be a content element");
		}
		if (((String) contentElement.get("class")) != null && ((String) contentElement.get("id")) == null) {
			for (Element element : doc.getElementsByClass(((String) contentElement.get("class")))) {
				for (Element child : element.children()) {
					if (!child.text().isBlank()) {
						if (contentElementExcpetions == null) {
							chapter.add(child.text());
							continue;
						}
					}
				}
			}
		}
		if (((String) contentElement.get("class")) == null && ((String) contentElement.get("id")) != null) {
			for (Element child : doc.getElementById(((String) contentElement.get("id"))).children()) {
				if (!child.text().isBlank()) {
					if (contentElementExcpetions == null) {
						chapter.add(child.text());
						continue;
					}
				}
			}
		}
		if (((String) contentElement.get("class")) != null && ((String) contentElement.get("id")) != null) {
			for (Element element : doc.getElementsByClass(((String) contentElement.get("class")))) {
				if (element.id().equals(((String) contentElement.get("id")))) {
					if (!element.text().isBlank()) {
						if (contentElementExcpetions == null) {
							chapter.add(element.text());
							continue;
						}
					}
				}
			}
		}

		// parsing next chapter link
		chapter.add(getNextChapterLink(doc, nextLinkElement));

		return chapter;
	}

	private static String getTitle(Document doc, Map<String, Object> titleElement) throws Exception {
		if (!titleElement.containsKey("id") && !titleElement.containsKey("class")) {
			throw new Exception("There has to be either an \"class\" or an \"id\" key to find the title element");
		}
		
		Element[] elements = new Element[0];
		
		elements = doc.getAllElements().toArray(elements);
		
		for (Element element : elements) {
			if ((String) titleElement.get("id") != null ? element.id().equals((String) titleElement.get("id"))
					: true && (String) titleElement.get("class") != null
							? element.className().equals((String) titleElement.get("class"))
							: true) {
				return element.text();
			}
		}

		throw new Exception("No title element was found");
	}

	private static String getNextChapterLink(Document doc, Map<String, Object> nextLinkElement) throws Exception {
		if (!nextLinkElement.containsKey("id") && !nextLinkElement.containsKey("class")) {
			throw new Exception("There has to be either an \"class\" or an \"id\" key to find the next chapter link");
		}
		
		Element[] elements = new Element[0];
		
		elements = doc.getAllElements().toArray(elements);
		
		for (Element element : elements) {
			if ((String) nextLinkElement.get("id") != null ? element.id().equals((String) nextLinkElement.get("id"))
					: true && (String) nextLinkElement.get("class") != null
							? element.className().equals((String) nextLinkElement.get("class"))
							: true) {
				return element.attr("href").isBlank() ? ""
						: (nextLinkElement.get("baseLink") == null) ? element.attr("href")
								: (String) nextLinkElement.get("baseLink") + element.attr("href");
			}
		}

		throw new Exception("No next chapter link element was found");
	}

}
