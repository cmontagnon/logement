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
    resp.getWriter().println("</head>");

    resp.getWriter().println("<body>");

    resp.getWriter().println("<form action=\"/stats\" method=\"get\">");
    // start date filter
    resp.getWriter().println(
        "<div>Date de début :<input name=\"startDate\" value=\""
            + (startDateParameter == null ? "" : startDateParameter) + "\"/></div>");

    // arrondissement filter
    resp.getWriter().println(
        "<div>Arrondissement :<select name=\"arrondissement\" value=\"" + arrondissementParameter + "\">");
    resp.getWriter().println("<option/>");
    for (Short arrondissement : SeLogerUtils.arrondissements.keySet()) {
      resp.getWriter().println(
          "<option value=\"" + arrondissement + "\" + "
              + (arrondissementParameter.equals(arrondissement.toString()) ? "selected=\"selected\"" : "") + ">"
              + arrondissement + "</option>");
    }
    resp.getWriter().println("</select>");

    // quartier filter
    resp.getWriter().println(
        "<div>Quartier :<input name=\"quartier\" value=\"" + (quartierParameter == null ? "" : quartierParameter)
            + "\"/></div>");
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
        resp.getWriter().println("<table id=\"table-3\">");
        resp.getWriter().println("<thead>");
        resp.getWriter().println("<th><b>Reference</a></b></th>");
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
        resp.getWriter().println("</html></body>");
      } finally {
        em.close();
      }
    }
  }
}
