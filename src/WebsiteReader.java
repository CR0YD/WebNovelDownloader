import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WebsiteReader {

	public static List<String> readNovelFromUrl(String url) throws Exception {
		Document doc = Jsoup.connect(url).get();
		doc = Jsoup.parse(doc.html(), "UTF-8");

		List<String> chapter = new List<>();

		chapter.add(doc.getElementsByClass("chapter-title").text());
		for (Element child : doc.getElementById("chapter-container").getElementsByTag("p")) {
			chapter.add(child.text());
		}
		chapter.add(doc.getElementsByClass("button nextchap ").attr("href").isBlank() ? ""
				: "https://www.novelpub.com" + doc.getElementsByClass("button nextchap ").attr("href"));

		return chapter;
	}

}
