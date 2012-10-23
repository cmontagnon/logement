package perso.logement;

import static com.google.appengine.labs.repackaged.com.google.common.collect.Lists.newArrayList;
import static com.google.appengine.labs.repackaged.com.google.common.collect.Maps.newHashMap;
import static java.lang.Integer.parseInt;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static org.datanucleus.util.StringUtils.isEmpty;
import static perso.logement.SeLogerUtils.arrondissements;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class StatAnnonceServlet extends HttpServlet {

  private static final EntityManagerFactory emfInstance = Persistence
      .createEntityManagerFactory("transactions-optional");

  @SuppressWarnings({"cast", "unchecked"})
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String startDateParameter = req.getParameter("startDate");
    String arrondissementParameter = req.getParameter("arrondissement");
    String quartierParameter = req.getParameter("quartier");

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
    resp.getWriter().println(
        "<div>Date de début :<input name=\"startDate\" value=\""
            + (startDateParameter == null ? "" : startDateParameter) + "\"/></div>");

    // arrondissement filter
    resp.getWriter().println("<div>Arrondissement :");
    resp.getWriter().println(
        "<select id=\"arrondissement\" name=\"arrondissement\""
            + (arrondissementParameter == null ? "" : " value=\"" + arrondissementParameter + "\"")
            + " onchange=\"arrondissementChange();\">");
    resp.getWriter().println("<option></option>");
    for (Short arrondissement : SeLogerUtils.arrondissements.keySet()) {
      boolean isArrondissementSelected =
          arrondissementParameter != null && arrondissementParameter.equals(arrondissement.toString());
      resp.getWriter().println(
          "<option value=\"" + arrondissement + "\"" + (isArrondissementSelected ? " selected=\"selected\"" : "") + ">"
              + arrondissement + "</option>");
    }
    resp.getWriter().println("</select>");
    resp.getWriter().println("</div>");

    // quartier filter
    resp.getWriter().println(
        "<div>Quartier :<select id=\"quartier\" name=\"quartier\""
            + (quartierParameter == null ? "" : " value=\"" + quartierParameter + "\"") + "></select></div>");
    resp.getWriter().println("<div><input type=\"submit\" value=\"Submit\" /></div>");
    resp.getWriter().println("</form>");

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
        for (Annonce annonce : resultList) {
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
        }
        // We remove annonces with only one price
        Map<AnnonceKey, List<Double>> filteredPrixByAnnonce = newHashMap();
        for (AnnonceKey annonceKey : prixByAnnonce.keySet()) {
          if (prixByAnnonce.get(annonceKey).size() > 1) {
            filteredPrixByAnnonce.put(annonceKey, prixByAnnonce.get(annonceKey));
          }
        }
        resp.getWriter().println("<table id=\"table-3\" class=\"sortable\">");
        resp.getWriter().println("<thead>");
        resp.getWriter().println("<th><b>Reference</b></th>");
        resp.getWriter().println("<th><b>Superficie</b></th>");
        resp.getWriter().println("<th><b>Arrondissement</b></th>");
        resp.getWriter().println("<th><b>Quartier</b></th>");
        resp.getWriter().println("<th><b>Prix</b></th>");
        resp.getWriter().println("<th><b>Prix/m²</b></th>");
        resp.getWriter().println("</thead>");

        resp.getWriter().println("<tbody>");
        for (AnnonceKey key : filteredPrixByAnnonce.keySet()) {
          List<Double> prices = filteredPrixByAnnonce.get(key);
          List<Long> pricesByQuareMeter = newArrayList();
          for (Double price : prices) {
            pricesByQuareMeter.add(Math.round(price / key.getSuperficie()));
          }
          resp.getWriter().println("<tr class=\"greenCell\">");
          resp.getWriter().println(
              "<td><a href=\"/query?reference=" + key.getReference() + "\">" + key.getReference() + "</a></td>");
          resp.getWriter().println("<td>" + key.getSuperficie() + "</td>");
          resp.getWriter().println("<td>" + key.getArrondissement() + "</td>");
          resp.getWriter().println("<td>" + key.getQuartier() + "</td>");
          resp.getWriter().println("<td>" + prices + "</td>");
          resp.getWriter().println("<td>" + pricesByQuareMeter + "</td>");
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

  private static void addComboboxJavaScript(HttpServletResponse resp, String quartierParameter) throws IOException {
    resp.getWriter().println("<script type=\"text/javascript\">");
    resp.getWriter().println("function arrondissementChange() {");

    for (Short arrondissement : arrondissements.keySet()) {
      Map<String, String> quartiersMap = arrondissements.get(arrondissement);
      resp.getWriter().println("var seLoger" + arrondissement + "=new Array();");
      resp.getWriter().println("var humanReadable" + arrondissement + "=new Array();");
      int i = 0;
      for (String selogerQuartier : quartiersMap.keySet()) {
        resp.getWriter().println("seLoger" + arrondissement + "[" + i + "] = \"" + selogerQuartier + "\";");
        resp.getWriter().println(
            "humanReadable" + arrondissement + "[" + i + "] = \"" + quartiersMap.get(selogerQuartier) + "\";");
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
