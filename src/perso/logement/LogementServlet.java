package perso.logement;

import static com.google.appengine.labs.repackaged.com.google.common.collect.Sets.newHashSet;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Short.parseShort;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@SuppressWarnings("serial")
public class LogementServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(LogementServlet.class.getName());

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    short arrondissement = parseShort(req.getParameter("arrondissement"));
    String quartier = req.getParameter("quartier");
    Integer nbPieces = null;
    if (req.getParameter("nbPieces") != null) {
      nbPieces = parseInt(req.getParameter("nbPieces"));
    }
    startProcess(arrondissement, quartier, nbPieces);
    resp.setContentType("text/plain");
    resp.getWriter().println("Hello, world");
  }

  // idq correspond au quartier...
  private static String selogerUrl =
      "http://www.seloger.com/recherche.htm?ci=7501{arrondissement}&idq={quartier}&idqfix=1&idtt=2&idtypebien=1&pxbtw=NaN%2fNaN&surfacebtw=NaN%2fNaN&tri=a_px&BCLANNpg=";

  public void startProcess(short arrondissement, String quartier, Integer nbPiecesSpecifiedByUser) throws IOException {
    log.log(Level.INFO, "Feeding arrondissement " + arrondissement);
    log.log(Level.INFO, "Feeding quartier " + quartier);
    Document firstPage = download(1, arrondissement, quartier, nbPiecesSpecifiedByUser);
    if (nbPiecesSpecifiedByUser == null && doesPageContainTooManyAnnonces(firstPage)) {
      for (int nbPieces = 1; nbPieces <= 5; nbPieces++) {
        processOneType(arrondissement, quartier, nbPieces);
      }
    } else {
      processOneType(arrondissement, quartier, nbPiecesSpecifiedByUser);
    }

  }

  private static void processOneType(short arrondissement, String quartier, Integer nbPieces) throws IOException {
    boolean currentPageHasAnnonces = true;
    int currentPage = 0;
    while (currentPageHasAnnonces) {
      currentPage++;
      log.log(Level.INFO, "Parsing page " + currentPage);
      Set<Annonce> annonces = newHashSet();
      Document doc = null;
      while (doc == null) {
        doc = download(currentPage, arrondissement, quartier, nbPieces);
      }
      currentPageHasAnnonces = parsePage(doc, arrondissement, quartier, annonces);
      log.log(Level.INFO, "nb annonces in page " + currentPage + ": " + annonces.size());
      for (Annonce annonce : annonces) {
        log.log(Level.INFO, annonce.toString());
      }
    }
  }

  private static boolean doesPageContainTooManyAnnonces(Document firstPage) {
    Elements nbPagesElement = firstPage.getElementsByClass("rech_nbpage");
    String nbPagesAsString = nbPagesElement.get(0).ownText();
    int nbPages = parseInt(nbPagesAsString.substring(nbPagesAsString.indexOf("sur") + 4));
    // There's too many pages we must add research criteria
    return nbPages == 29; // TODO : magic number...
  }

  private static boolean parsePage(Document doc, short arrondissement, String quartier, Set<Annonce> annonces) {
    boolean currentPageHasAnnonces;

    Elements annonceElements = doc.getElementsByClass("ann_ann");
    currentPageHasAnnonces = annonceElements.size() > 0;
    for (Element annonceElement : annonceElements) {
      double superficie = getSuperficice(annonceElement);
      double prix = getPrix(annonceElement);
      String text = annonceElement.getElementsByClass("rech_desc_right_photo").get(0).ownText(); // TODO : make sure there's only one
      String reference = annonceElement.getElementsByClass("rech_majref").get(0).ownText().substring(13); // TODO : make sure there's only one
      Annonce annonce =
          new Annonce(reference, text, prix, superficie, new Date(), arrondissement,
              SeLogerUtils.humanReadableQuartier.get(quartier));
      annonces.add(annonce);
      PersistenceManager pm = PMF.get().getPersistenceManager();
      try {
        log.log(Level.INFO, "persisting annonce " + annonce);
        pm.makePersistent(annonce);
      } finally {
        pm.close();
      }
    }
    //}
    return currentPageHasAnnonces;
  }

  private static double getPrix(Element element) {
    String prixAsString = element.getElementsByClass("mea2").text();
    Pattern pattern = Pattern.compile("[\\d|\u00A0| ]+");
    Matcher matcher = pattern.matcher(prixAsString);
    Character.isWhitespace(prixAsString.codePointAt(2));
    double prix = 0;
    if (matcher.find()) {
      String prixWithoutSpaces = matcher.group().replaceAll("\u00A0", "").replaceAll(" ", "");
      double tempPrice = parseDouble(prixWithoutSpaces);
      prix = tempPrice;
    }
    return prix;
  }

  private static double getSuperficice(Element element) {
    String superficieAsString = element.getElementsByClass("mea1").text();
    Pattern pattern = Pattern.compile("[\\d|,]+");
    Matcher matcher = pattern.matcher(superficieAsString);
    double superficie = 0;
    while (matcher.find()) {
      double tempSuperficie = parseDouble(matcher.group().replaceAll(",", "."));
      if (tempSuperficie > 6) { // to avoid 2 in '2 pièces for example'
        superficie = tempSuperficie;
      }
    }
    return superficie;
  }

  public static void main(String[] args) throws IOException {
    Set<Annonce> annonces = newHashSet();
    Document doc = Jsoup.parse(new File("recherche.htm"), "UTF-8");
    parsePage(doc, new Short("1"), "133094", annonces);
  }

  private static Document download(int currentPage, short arrondissement, String quartier, Integer nbPieces)
      throws IOException {
    log.log(Level.INFO, "downloading page " + currentPage);

    URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

    String pageUrl = selogerUrl.replaceAll("\\{quartier\\}", quartier) + currentPage;
    pageUrl = pageUrl.replaceAll("\\{arrondissement\\}", "" + arrondissement);
    if (nbPieces != null) {
      pageUrl = pageUrl + "&nb_pieces=" + nbPieces;
    }
    URL url = new URL(pageUrl);
    log.log(Level.INFO, "page url : " + pageUrl);
    try {
      HTTPResponse response = fetcher.fetch(url);
      byte[] content = response.getContent();
      // if redirects are followed, this returns the final URL we are redirected to
      //URL finalUrl = response.getFinalUrl();

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      out.write(content);
      out.flush();
      out.close();
      return Jsoup.parse(out.toString(), "UTF-8");
    } catch (IOException e) {
      log.log(Level.SEVERE, "failed trying to get content from '" + url + "' : " + e.getMessage());
    } catch (Throwable t) {
      log.log(Level.SEVERE, "failed trying to get content from '" + url + "' : " + t.getMessage());
    }
    return null;
  }
}
