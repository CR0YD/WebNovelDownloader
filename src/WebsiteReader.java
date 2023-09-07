import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WebsiteReader {
	
	public static void main(String[] args) {
		try {
			List l = readNovelFromUrl("https://allnovelfull.net/overgeared/chapter-1.html");
			for (int i = 0; i < l.length(); i++) {
				System.out.println(l.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> readNovelFromUrl(String url) throws Exception {
		if (url.startsWith("https://www.novelpub.com")) {
			return readNovelPubCom(url);
		}
		if (url.startsWith("https://allnovelfull.net")) {
			return readAllNovelFullNet(url);
		}
		throw new Exception("Website was not found or is currently not supported.");
	}
	
	private static List<String> readNovelPubCom(String url) throws Exception {
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
	
	private static List<String> readAllNovelFullNet(String url) throws Exception {
		Document doc = Jsoup.connect(url).get();
		doc = Jsoup.parse(doc.html(), "UTF-8");

		List<String> chapter = new List<>();

		chapter.add(doc.getElementsByClass("truyen-title").text());
		/*for (Element child : doc.getElementById("chapter-content").getElementsByTag("p")) {
			chapter.add(child.text());
		}*/
		for (Element child : doc.getElementById("chapter-content").children()) {
			if (child.text().isBlank())
				continue;
			chapter.add(child.text());
		}
		chapter.add(doc.getElementById("next_chap").attr("href").isBlank() ? ""
				: "https://allnovelfull.net" + doc.getElementById("next_chap").attr("href"));

		return chapter;
	}

}
