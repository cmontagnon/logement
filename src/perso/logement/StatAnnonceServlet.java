package perso.logement;

import static com.google.appengine.labs.repackaged.com.google.common.collect.Lists.newArrayList;
import static com.google.appengine.labs.repackaged.com.google.common.collect.Maps.newHashMap;
import static com.google.appengine.labs.repackaged.com.google.common.collect.Sets.newHashSet;
import static java.lang.Integer.parseInt;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static org.datanucleus.util.StringUtils.isEmpty;
import static perso.logement.client.SeLogerUtils.arrondissements;
import static perso.logement.core.AnnonceEvolution.DOWN;
import static perso.logement.core.AnnonceEvolution.NONE;
import static perso.logement.core.AnnonceEvolution.UP;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.math.util.MathUtils;

import perso.logement.client.SeLogerUtils;
import perso.logement.core.Annonce;
import perso.logement.core.AnnonceEvolution;
import perso.logement.core.AnnonceKey;

@SuppressWarnings("serial")
public class StatAnnonceServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(StatAnnonceServlet.class.getName());

  private static final String ANNONCES_WITH_PRICE_CHANGE_ONLY = "priceFallOnly";
  private static final String ALL_ANNONCES = "all";
  private static final EntityManagerFactory emfInstance = Persistence
      .createEntityManagerFactory("transactions-optional");
  private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

  @SuppressWarnings({"cast", "unchecked"})
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String startDateParameter = req.getParameter("startDate");
    String arrondissementParameter = req.getParameter("arrondissement");
    String quartierParameter = req.getParameter("quartier");
    String queryTypeParameter = req.getParameter("queryType");

    resp.setContentType("text/plain");
    resp.setContentType("text/html");
    resp.getWriter().println("<html>");
    resp.getWriter().println("<head>");
    resp.getWriter().println("<link type=\"text/css\" rel=\"stylesheet\" href=\"/stylesheets/main.css\" />");
    resp.getWriter().println("<script src=\"js/sorttable.js\"></script>");
    addComboboxJavaScript(resp, quartierParameter);
    resp.getWriter().println("</head>");

    resp.getWriter().println("<body onload='arrondissementChange()'>");

    resp.getWriter().println("<form action=\"/stats\" method=\"get\">");
    // start date filter
    addStartDateFilter(resp, startDateParameter);

    // arrondissement filter
    addArrondissementFilter(resp, arrondissementParameter);

    // quartier filter
    addQuartierFilter(resp, quartierParameter);

    // Query type filter
    addQueryTypeFilter(resp, queryTypeParameter);

    // Close the form
    resp.getWriter().println("<div><input type=\"submit\" value=\"Submit\" /></div>");
    resp.getWriter().println("</form>");

    // Query database
    if (!isEmpty(startDateParameter)) {
      EntityManager em = emfInstance.createEntityManager();
      try {
        StringBuilder queryString = new StringBuilder();
        queryString.append("select annonce");
        queryString.append(" from Annonce annonce");
        queryString.append(" where date>:startDate");
        if (!isEmpty(arrondissementParameter)) {
          queryString.append(" and arrondissement=" + arrondissementParameter);
          if (!isEmpty(quartierParameter)) {
            queryString.append(" and quartier=\""
                + arrondissements.get(new Short(arrondissementParameter)).get(quartierParameter) + "\"");
          }
        }
        queryString.append(" order by date asc");

        Query query = em.createQuery(queryString.toString());
        Calendar cal = Calendar.getInstance();
        cal.set(YEAR, parseInt(startDateParameter.substring(0, 4)));
        cal.set(MONTH, parseInt(startDateParameter.substring(5, 7)) - 1);
        cal.set(DAY_OF_MONTH, parseInt(startDateParameter.substring(8, 10)));
        cal.set(HOUR_OF_DAY, 0);
        cal.set(MINUTE, 0);
        cal.set(MILLISECOND, 0);
        query.setParameter("startDate", cal.getTime());
        List<Annonce> resultList = (List<Annonce>) query.getResultList();

        // HTML
        Map<AnnonceKey, List<Double>> prixByAnnonce = newHashMap();
        Map<AnnonceKey, List<Date>> datesByAnnonce = newHashMap();
        Date maxDate = null;
        for (Annonce annonce : resultList) {
          // prix
          if (prixByAnnonce.containsKey(annonce.getKey())) {
            List<Double> prices = prixByAnnonce.get(annonce.getKey());
            if (!prices.get(prices.size() - 1).equals(annonce.getPrix())) {
              prices.add(annonce.getPrix());
            }
          } else {
            List<Double> prices = newArrayList();
            prices.add(annonce.getPrix());
            prixByAnnonce.put(annonce.getKey(), prices);
          }
          // dates
          if (datesByAnnonce.containsKey(annonce.getKey())) {
            List<Date> dates = datesByAnnonce.get(annonce.getKey());
            if (!dates.get(dates.size() - 1).equals(annonce.getDate())) {
              dates.add(annonce.getDate());
            }
          } else {
            List<Date> dates = newArrayList();
            dates.add(annonce.getDate());
            datesByAnnonce.put(annonce.getKey(), dates);
          }
          // max date
          if (maxDate == null || maxDate.compareTo(annonce.getDate()) < 0) {
            maxDate = annonce.getDate();
          }
        }
        log.info("Max date : " + maxDate);
        Map<AnnonceKey, List<Double>> filteredPrixByAnnonce = newHashMap();
        Map<AnnonceKey, List<Date>> filteredDateByAnnonce = newHashMap();
        if (isEmpty(queryTypeParameter) || ANNONCES_WITH_PRICE_CHANGE_ONLY.equals(queryTypeParameter)) {
          // We remove annonces with only one price
          for (AnnonceKey annonceKey : prixByAnnonce.keySet()) {
            if (prixByAnnonce.get(annonceKey).size() > 1) {
              filteredPrixByAnnonce.put(annonceKey, prixByAnnonce.get(annonceKey));
              filteredDateByAnnonce.put(annonceKey, datesByAnnonce.get(annonceKey));
            }
          }
        } else {
          // We keep all annonces
          for (AnnonceKey annonceKey : prixByAnnonce.keySet()) {
            filteredPrixByAnnonce.put(annonceKey, prixByAnnonce.get(annonceKey));
            filteredDateByAnnonce.put(annonceKey, datesByAnnonce.get(annonceKey));
          }
        }

        // We compute the mean price by square meter value
        double currentPriceBySquareMeterSum = 0;
        int nbAnnoncesWithNullSuperficie = 0;
        for (AnnonceKey key : filteredPrixByAnnonce.keySet()) {
          List<Double> prices = filteredPrixByAnnonce.get(key);
          if (key.getSuperficie() != 0) {
            currentPriceBySquareMeterSum += (prices.get(prices.size() - 1)) / key.getSuperficie();
          } else {
            nbAnnoncesWithNullSuperficie++;
          }
        }
        int meanPriceBySquareMeter =
            (int) (currentPriceBySquareMeterSum / (filteredPrixByAnnonce.size() - nbAnnoncesWithNullSuperficie));

        resp.getWriter().println(
            "<div><span>Moyenne du prix au m² pour cette requête : " + meanPriceBySquareMeter + "</span></div>");
        // We build the HTML table
        resp.getWriter().println("<table id=\"table-3\" class=\"sortable\">");
        resp.getWriter().println("<thead>");
        resp.getWriter().println("<th><b>Reference</b></th>");
        resp.getWriter().println("<th><b>Superficie</b></th>");
        resp.getWriter().println("<th><b>Arrondissement</b></th>");
        resp.getWriter().println("<th><b>Quartier</b></th>");
        resp.getWriter().println("<th><b>Prix</b></th>");
        resp.getWriter().println("<th><b>Prix/m²</b></th>");
        resp.getWriter().println("<th><b>Différence à la moyenne (en %)</b></th>");
        resp.getWriter().println("<th><b>Lien</b></th>");
        resp.getWriter().println("</thead>");

        resp.getWriter().println("<tbody>");
        for (AnnonceKey key : filteredPrixByAnnonce.keySet()) {
          List<Double> prices = filteredPrixByAnnonce.get(key);
          List<Long> pricesBySquareMeter = newArrayList();
          for (Double price : prices) {
            if (key.getSuperficie() != 0) {
              pricesBySquareMeter.add(Math.round(price / key.getSuperficie()));
            }
          }
          String priceEvolImage = getPriceEvolImage(prices);

          String meanTagImage = "";
          double meanTagPercentage = 0;
          if (!pricesBySquareMeter.isEmpty()) { // It may be empty when the superficie is equal to 0
            Long lastPriceBySquareMeter = pricesBySquareMeter.get(pricesBySquareMeter.size() - 1);
            meanTagImage =
                meanPriceBySquareMeter >= lastPriceBySquareMeter ? "<img src=\"icons/green.png\">"
                    : "<img src=\"icons/red.png\">";
            if (meanPriceBySquareMeter != 0) {
              meanTagPercentage =
                  MathUtils.round(100 * new Double(lastPriceBySquareMeter - meanPriceBySquareMeter)
                      / meanPriceBySquareMeter, 2, ROUND_HALF_UP);
            } else {
              meanTagPercentage = 0;
            }
          }

          // is annonce stale?
          String lastAnnonceDate =
              dateFormatter.format(filteredDateByAnnonce.get(key).get(filteredDateByAnnonce.get(key).size() - 1));
          String annonceStatusImage = "";
          String maxDateAsString = dateFormatter.format(maxDate);
          if (!maxDateAsString.equals(lastAnnonceDate)) {
            log.info("Annonce " + key.getReference() + " is stale. LastAnnonceDate = " + lastAnnonceDate);
            // We may assume this annonce doesn't exist anymore
            annonceStatusImage = "<img src=\"icons/red.png\">";
          } else {
            annonceStatusImage = "<img src=\"icons/green.png\">";
          }

          resp.getWriter().println("<tr>");
          resp.getWriter().println(
              "<td><a href=\"/query?reference=" + key.getReference() + "\">" + key.getReference() + "</a></td>");
          resp.getWriter().println("<td>" + key.getSuperficie() + "</td>");
          resp.getWriter().println("<td>" + key.getArrondissement() + "</td>");
          resp.getWriter().println("<td>" + key.getQuartier() + "</td>");
          resp.getWriter().println("<td>" + prices + "&nbsp;" + priceEvolImage + "</td>");
          resp.getWriter().println("<td>" + pricesBySquareMeter + "</td>");
          resp.getWriter().println("<td>" + meanTagPercentage + "&nbsp;" + meanTagImage + "</td>");
          resp.getWriter().println(
              "<td><a href=\"http://www.seloger.com/recherche.htm?ci=7501" + arrondissementParameter
                  + "&idtt=2&org=advanced_search&refannonce=" + key.getReference() + "\">seloger</a>"
                  + annonceStatusImage + "</td>");
          resp.getWriter().println("</tr>");
        }
        resp.getWriter().println("</tbody>");
        resp.getWriter().println("</table>");
        resp.getWriter().println("</body>");
        resp.getWriter().println("</html>");
      } finally {
        em.close();
      }
    }
  }

  private static String getPriceEvolImage(List<Double> prices) {
    AnnonceEvolution annonceEvolution = getAnnonceEvolution(prices);
    String priceEvolImage = null;
    switch (annonceEvolution) {
      case DOWN:
        priceEvolImage = "<img src=\"icons/down.png\">";
        break;
      case UP:
        priceEvolImage = "<img src=\"icons/up.png\">";
        break;
      default:
        priceEvolImage = "";
    }
    return priceEvolImage;
  }

  private static void addStartDateFilter(HttpServletResponse resp, String startDateParameter) throws IOException {
    resp.getWriter().println(
        "<div>Date de début :<input name=\"startDate\" value=\""
            + (startDateParameter == null ? "" : startDateParameter) + "\"/></div>");
  }

  private static void addQueryTypeFilter(HttpServletResponse resp, String queryTypeParameter) throws IOException {
    resp.getWriter().println(
        "<div>Annonce évolution :<select id=\"queryType\" name=\"queryType\""
            + (queryTypeParameter == null ? "" : " value=\"" + queryTypeParameter + "\"") + ">");
    resp.getWriter()
        .println(
            "<option value='"
                + ANNONCES_WITH_PRICE_CHANGE_ONLY
                + "'"
                + (queryTypeParameter != null && queryTypeParameter.equals(ANNONCES_WITH_PRICE_CHANGE_ONLY) ? " selected='true'"
                    : "") + ">Seulement celles dont le prix change</option>");
    resp.getWriter().println(
        "<option value='" + ALL_ANNONCES + "'"
            + (queryTypeParameter != null && queryTypeParameter.equals(ALL_ANNONCES) ? " selected='true'" : "")
            + ">Toutes</option>");
    resp.getWriter().println("</select></div>");
  }

  private static void addQuartierFilter(HttpServletResponse resp, String quartierParameter) throws IOException {
    resp.getWriter().println(
        "<div>Quartier :<select id=\"quartier\" name=\"quartier\""
            + (quartierParameter == null ? "" : " value=\"" + quartierParameter + "\"") + "></select></div>");
  }

  private static void addArrondissementFilter(HttpServletResponse resp, String arrondissementParameter)
      throws IOException {
    resp.getWriter().println("<div>Arrondissement :");
    resp.getWriter().println(
        "<select id=\"arrondissement\" name=\"arrondissement\""
            + (arrondissementParameter == null ? "" : " value=\"" + arrondissementParameter + "\"")
            + " onchange=\"arrondissementChange();\">");
    resp.getWriter().println("<option></option>");
    List<Short> arrondissementsList = newArrayList(SeLogerUtils.arrondissements.keySet());
    Collections.sort(arrondissementsList);
    for (Short arrondissement : arrondissementsList) {
      boolean isArrondissementSelected =
          arrondissementParameter != null && arrondissementParameter.equals(arrondissement.toString());
      resp.getWriter().println(
          "<option value=\"" + arrondissement + "\"" + (isArrondissementSelected ? " selected=\"selected\"" : "") + ">"
              + arrondissement + "</option>");
    }
    resp.getWriter().println("</select>");
    resp.getWriter().println("</div>");
  }

  private static AnnonceEvolution getAnnonceEvolution(List<Double> prices) {
    Set<Double> distinctPrices = newHashSet(prices);
    if (distinctPrices.size() == 1) {
      return NONE;
    } else {
      double lastPrice = prices.get(prices.size() - 1);
      double lastButOnePrice = Double.NaN;
      for (int i = prices.size() - 2; i >= 0; i--) { // We start with the last but one price
        if (prices.get(i) != lastPrice) {
          lastButOnePrice = prices.get(i);
          break; // TODO : moche...
        }
      }
      if (lastPrice == lastButOnePrice) {
        return NONE;
      } else if (lastPrice > lastButOnePrice) {
        return UP;
      } else {
        return DOWN;
      }
    }
  }

  private static void addComboboxJavaScript(HttpServletResponse resp, String quartierParameter) throws IOException {
    resp.getWriter().println("<script type=\"text/javascript\">");
    resp.getWriter().println("function arrondissementChange() {");

    for (Short arrondissement : arrondissements.keySet()) {
      Map<String, String> quartiersMap = arrondissements.get(arrondissement);
      resp.getWriter().println("var seLoger" + arrondissement + "=new Array();");
      resp.getWriter().println("var humanReadable" + arrondissement + "=new Array();");
      int i = 0;
      List<String> selogerFormatQuartierList = newArrayList(quartiersMap.keySet());
      Collections.sort(selogerFormatQuartierList);
      for (String selogerFormatQuartier : selogerFormatQuartierList) {
        resp.getWriter().println("seLoger" + arrondissement + "[" + i + "] = \"" + selogerFormatQuartier + "\";");
        resp.getWriter().println(
            "humanReadable" + arrondissement + "[" + i + "] = \"" + quartiersMap.get(selogerFormatQuartier) + "\";");
        i++;
      }
    }
    resp.getWriter().println("");

    resp.getWriter().println("var selectedBoxValue = document.getElementById('arrondissement').value;");
    resp.getWriter().println("var i;");
    resp.getWriter().println("var j;");
    resp.getWriter().println("var nbQuartiers = eval('seLoger' + selectedBoxValue + '.length');");
    resp.getWriter().println("removeSelectboxOption();");
    resp.getWriter().println("for (i = 0; i < document.getElementById('arrondissement').options.length; i++) {");
    resp.getWriter().println("if (selectedBoxValue == document.getElementById('arrondissement').options[i].value) {");

    resp.getWriter().println(
        "document.getElementById('quartier').options[0] = new Option('All', '', false, "
            + (isEmpty(quartierParameter) ? "true" : false) + ");");

    resp.getWriter().println("for (j=0; j<nbQuartiers; j++) {");
    resp.getWriter().println("document.getElementById('quartier').options[j+1] = new Option(");
    resp.getWriter()
        .println(
            "eval('humanReadable' + selectedBoxValue)[j], eval('seLoger' + selectedBoxValue)[j], false, eval(eval('seLoger' + selectedBoxValue)[j]=='"
                + quartierParameter + "'))");
    resp.getWriter().println("}");
    resp.getWriter().println("}");
    resp.getWriter().println("}");
    resp.getWriter().println("}");
    resp.getWriter().println("function removeSelectboxOption() {");
    resp.getWriter().println("var i;");
    resp.getWriter().println("for (i = 0; i < document.getElementById('quartier').options.length; i++) {");
    resp.getWriter().println("document.getElementById('quartier').remove(i);");
    resp.getWriter().println("}");
    resp.getWriter().println("}");
    resp.getWriter().println("</script>");
  }
}
