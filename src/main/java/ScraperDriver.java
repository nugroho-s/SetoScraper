import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ScraperDriver {
    public static void main(String[] args){
        Validate.isTrue(args.length >= 1, "usage: supply url to fetch");
        Validate.isTrue(args.length == 2, "usage: supply how many time to fetch");
        String url = args[0];
        print("Fetching %s...", url);

        int limit = Integer.parseInt(args[1]);

        for(int i=0;i<limit;i++) {
            Document doc = null;
            try {
                doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36")
                        .header("Accept-Language", "en-US,en;q=0.8,id;q=0.6")
                        .header("Cache-Control", "max-age=0")
                        .header("Connection", "keep-alive")
                        .header("Host", "setomulyadi.ml")
                        .header("Upgrade-Insecure-Requests","1")
                        .header("Accept-Encoding", "gzip,deflate,sdch").get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Element content = doc.getElementsByClass("starter-template").first();
            String quote = content.getElementsByTag("h1").text();
            print(quote);
        }
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

}
