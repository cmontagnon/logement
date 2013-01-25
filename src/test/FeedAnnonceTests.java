package test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class FeedAnnonceTests {

  @Test
  public void feedAnnonce() {
    String baseUrlString =
        "http://127.0.0.1:8888/addAnnonce?reference={reference}&text={text}&prix={prix}&superficie={superficie}&date={date}&arrondissement={arrondissement}&quartier={quartier}&gwt.codesvr=127.0.0.1:9997";
    InputStream webStream = null;
    for (int i = 0; i < 40; i++) {
      try {
        String urlString = baseUrlString;
        urlString = urlString.replaceAll("\\{reference\\}", "ref" + (i / 10));
        urlString = urlString.replaceAll("\\{text\\}", "text" + (i % 5));
        urlString = urlString.replaceAll("\\{prix\\}", "" + (100000 + 30000 * Math.random()));
        urlString = urlString.replaceAll("\\{superficie\\}", "" + (50 + (i / 10) * 5));
        urlString = urlString.replaceAll("\\{date\\}", "2013-01-15");
        urlString = urlString.replaceAll("\\{arrondissement\\}", "10");
        urlString = urlString.replaceAll("\\{quartier\\}", "Louis&nbsp;Blanc/Aqueduc");
        URL url = new URL(urlString);
        HttpURLConnection conn = null;
        conn = (HttpURLConnection) url.openConnection();
        webStream = conn.getInputStream();
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          webStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
