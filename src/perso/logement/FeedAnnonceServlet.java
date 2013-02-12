package perso.logement;

import static com.google.appengine.labs.repackaged.com.google.common.collect.Sets.newHashSet;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Short.parseShort;

import java.io.ByteArrayOutputStream;
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

import perso.logement.client.SeLogerUtils;
import perso.logement.core.Annonce;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@SuppressWarnings("serial")
public class FeedAnnonceServlet extends HttpServlet {

  private static final int MAX_PAGE_NUMBER = 29;
  private static final String QUARTIER = "quartier";
  private static final String ARRONDISSEMENT = "arrondissement";

  private static final Logger log = Logger.getLogger(FeedAnnonceServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    short arrondissement = parseShort(req.getParameter(ARRONDISSEMENT));
    String quartier = req.getParameter(QUARTIER);
    Integer nbPieces = null;
    if (req.getParameter("nbPieces") != null) {
      nbPieces = parseInt(req.getParameter("nbPieces"));
    }
    int nbAnnonceFeeded = startProcess(arrondissement, quartier, nbPieces);
    resp.setContentType("text/plain");
    resp.getWriter().println(nbAnnonceFeeded + " annonces ont été enregistrées");
  }

  // idq correspond au quartier...
  private static String selogerUrl =
      "http://www.seloger.com/recherche.htm?ci=7501{arrondissement}&idq={quartier}&idqfix=1&idtt=2&idtypebien=1&pxbtw=NaN%2fNaN&surfacebtw=NaN%2fNaN&tri=a_px&BCLANNpg=";

  public int startProcess(short arrondissement, String quartier, Integer nbPiecesSpecifiedByRequest) throws IOException {
    log.info("startProcess (arrondissement=" + arrondissement + ", quartier=" + quartier + ",nbPieces="
        + nbPiecesSpecifiedByRequest + ")");
    Document firstPage = null;
    while (firstPage == null) {
      firstPage = download(1, arrondissement, quartier, nbPiecesSpecifiedByRequest);
    }
    int nbAnnonceFed = 0;
    if (nbPiecesSpecifiedByRequest == null && doesRequestContainTooManyAnnonces(firstPage)) {
      /*
       * We arrive here when :
       * - the request doesn't specify a nbPiecesParameter
       * - the request contains too many announces and we must split it into several little request.
       * In such a case it's likely the whole request will fail because of the google timeout (1 minute...)
       */
      for (int nbPieces = 1; nbPieces <= 5; nbPieces++) {
        nbAnnonceFed += processRequest(arrondissement, quartier, nbPieces);
      }
    } else {
      /* We can arrive here in 2 cases :
       * - nbPiecesSpecifiedByUser is null and the request doesn't contain too many announces
       * - the request specifies an nbPieces parameter  
       */
      nbAnnonceFed += processRequest(arrondissement, quartier, nbPiecesSpecifiedByRequest);
    }
    return nbAnnonceFed;
  }

  /**
   * nbPieces may be null while other parameters can't.
   * @param arrondissement
   * @param quartier
   * @param nbPieces
   * @return
   * @throws IOException
   */
  private static int processRequest(short arrondissement, String quartier, Integer nbPieces) throws IOException {
    log.info("arrondissement : " + arrondissement);
    log.info("quartier : " + quartier);
    log.info("nbPieces : " + nbPieces);
    int currentPage = 0;
    int nbAnnonces = 0;
    Set<Annonce> annonces = newHashSet();
    do {
      currentPage++;
      log.info("Parsing page " + currentPage);
      Document doc = null;
      while (doc == null) {
        doc = download(currentPage, arrondissement, quartier, nbPieces);
      }
      annonces = parsePage(doc, arrondissement, quartier);
      nbAnnonces += annonces.size();
      persistAnnonces(annonces);
      log.info("nb annonces in page " + currentPage + ": " + annonces.size());
      for (Annonce annonce : annonces) {
        log.info(annonce.toString());
      }
    } while (!annonces.isEmpty());
    return nbAnnonces;
  }

  private static void persistAnnonces(Set<Annonce> annonces) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      for (Annonce annonce : annonces) {
        log.info("persisting annonce " + annonce);
        pm.makePersistent(annonce);
      }
    } finally {
      pm.close();
    }
  }

  private static boolean doesRequestContainTooManyAnnonces(Document firstPage) {
    Elements nbPagesElement = firstPage.getElementsByClass("rech_nbpage");
    String nbPagesAsString = nbPagesElement.get(0).ownText();
    log.info("nbPagesAsString = " + nbPagesAsString);
    int nbPages = 1;
    if (nbPagesAsString.indexOf("sur") != -1) {
      nbPages = parseInt(nbPagesAsString.substring(nbPagesAsString.indexOf("sur") + 4));
    }
    // There's too many pages we must add research criteria
    boolean doesPageContainTooManyAnnonces = nbPages >= MAX_PAGE_NUMBER;
    log.info("doesRequestContainTooManyAnnonces = " + doesPageContainTooManyAnnonces);
    return doesPageContainTooManyAnnonces;
  }

  public static Set<Annonce> parsePage(Document doc, short arrondissement, String quartier) {
    Set<Annonce> annonces = newHashSet();

    Elements annonceElements = doc.getElementsByClass("ann_ann");
    for (Element annonceElement : annonceElements) {
      double superficie = getSuperficice(annonceElement);
      double prix = getPrix(annonceElement);
      String text = annonceElement.getElementsByClass("rech_desc_right_photo").get(0).ownText(); // TODO : make sure there's only one
      String reference = annonceElement.getElementsByClass("rech_majref").get(0).ownText().substring(13); // TODO : make sure there's only one
      Annonce annonce =
          new Annonce(reference, text, prix, superficie, new Date(), arrondissement, SeLogerUtils.arrondissements.get(
              arrondissement).get(quartier));
      annonces.add(annonce);
    }
    return annonces;
  }

  private static double getPrix(Element annonceElement) {
    String prixAsString = annonceElement.getElementsByClass("mea2").text();
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
      if (tempSuperficie > 6) { // to avoid 2 in '2 pi�ces for example'
        superficie = tempSuperficie;
      }
    }
    return superficie;
  }

  private static Document download(int currentPage, short arrondissement, String quartier, Integer nbPieces)
      throws IOException {
    log.info("downloading page " + currentPage);

    URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

    String pageUrl = selogerUrl.replaceAll("\\{quartier\\}", quartier) + currentPage;
    pageUrl = pageUrl.replaceAll("\\{arrondissement\\}", "" + arrondissement);
    if (nbPieces != null) {
      pageUrl = pageUrl + "&nb_pieces=" + nbPieces;
    }
    URL url = new URL(pageUrl);
    log.info("page url : " + pageUrl);
    try {
      HTTPResponse response = fetcher.fetch(url);
      byte[] content = response.getContent();
      // if redirects are followed, this returns the final URL we are redirected to
      //URL finalUrl = response.getFinalUrl();
      log.info("page successfully retrieved");
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
