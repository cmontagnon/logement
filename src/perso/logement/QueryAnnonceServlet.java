package perso.logement;

import static com.google.appengine.labs.repackaged.com.google.common.collect.Lists.newArrayList;
import static com.google.appengine.labs.repackaged.com.google.common.collect.Maps.newHashMap;
import static org.datanucleus.util.StringUtils.isEmpty;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
public class QueryAnnonceServlet extends HttpServlet {

  private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private static final EntityManagerFactory emfInstance = Persistence
      .createEntityManagerFactory("transactions-optional");

  @Override
  @SuppressWarnings({"unchecked", "cast"})
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");
    EntityManager em = emfInstance.createEntityManager();
    try {
      List<Annonce> annonces = newArrayList();
      StringBuilder queryString = new StringBuilder();
      queryString.append("select annonce from Annonce annonce");

      boolean whereClauseCreated = false;
      if (req.getParameter("arrondissement") != null) {
        queryString.append(" where arrondissement=");
        queryString.append(req.getParameter("arrondissement"));
        whereClauseCreated = true;
        if (req.getParameter("quartier") != null) {
          queryString.append(" " + (whereClauseCreated ? "and" : "where") + " quartier='");
          queryString.append(SeLogerUtils.arrondissements.get(new Short(req.getParameter("arrondissement"))).get(
              req.getParameter("quartier")));
          queryString.append("'");
          whereClauseCreated = true;
        }
      }

      String referenceParameter = req.getParameter("reference");
      if (!isEmpty(referenceParameter)) {
        queryString.append(" " + (whereClauseCreated ? "and" : "where") + " reference='");
        queryString.append(referenceParameter);
        queryString.append("'");
      }

      queryString.append(" order by date asc");
      Query query = em.createQuery(queryString.toString());
      query.setMaxResults(1000);
      List<Annonce> resultList = null;
      int counter = 0;
      while (resultList == null || resultList.size() == 1000) {
        query.setFirstResult(counter * 1000);
        resultList = (List<Annonce>) query.getResultList();
        annonces.addAll(resultList);
        counter++;
      }

      // HTML
      resp.setContentType("text/html");
      resp.getWriter().println("<html>");

      resp.getWriter().println("<head>");
      resp.getWriter().println("<link type=\"text/css\" rel=\"stylesheet\" href=\"/stylesheets/main.css\" />");
      if (!isEmpty(referenceParameter)) {
        resp.getWriter().println("<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>");
        resp.getWriter().println("<script type=\"text/javascript\">");

        resp.getWriter().println("google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});");
        resp.getWriter().println("google.setOnLoadCallback(drawChart);");
        resp.getWriter().println("function drawChart() {");
        resp.getWriter().println("var data = new google.visualization.DataTable();");
        resp.getWriter().println("data.addColumn('date', 'date');");
        resp.getWriter().println("data.addColumn('number', 'prix');");

        StringBuilder builder = new StringBuilder("[");
        for (Annonce annonce : annonces) {
          Calendar cal = Calendar.getInstance();
          cal.setTimeInMillis(annonce.getDate().getTime());
          int year = cal.get(Calendar.YEAR);
          int month = cal.get(Calendar.MONTH) + 1;
          int day = cal.get(Calendar.DAY_OF_MONTH);
          builder.append("[new Date(" + year + "," + month + "," + day + ")," + annonce.getPrix() + "],");
        }
        builder.append("]");
        resp.getWriter().println("data.addRows(" + builder.toString() + ");");
        resp.getWriter().println("var options = {width: 1000, height: 600};");
        resp.getWriter().println(
            "var chart = new google.visualization.LineChart(document.getElementById('chart_div'));");
        resp.getWriter().println("chart.draw(data, options);");
        resp.getWriter().println("}");

        resp.getWriter().println("</script>");
      }
      resp.getWriter().println("</head>");

      resp.getWriter().println("<body>");

      Map<AnnonceKey, List<Annonce>> prixByAnnonce = newHashMap();
      for (Annonce annonce : annonces) {
        if (prixByAnnonce.containsKey(annonce.getKey())) {
          prixByAnnonce.get(annonce.getKey()).add(annonce);
        } else {
          List<Annonce> relatedAnnonces = newArrayList();
          relatedAnnonces.add(annonce);
          prixByAnnonce.put(annonce.getKey(), relatedAnnonces);
        }
      }
      resp.getWriter().println("<table id=\"table-3\">");
      resp.getWriter().println("<thead>");
      resp.getWriter().println("<th><b>Reference</b></th>");
      resp.getWriter().println("<th><b>Superficie</b></th>");
      resp.getWriter().println("<th><b>Arrondissement</b></th>");
      resp.getWriter().println("<th><b>Quartier</b></th>");
      resp.getWriter().println("<th><b>Text</b></th>");
      resp.getWriter().println("<th><b>Prix</b></th>");
      resp.getWriter().println("</thead>");

      resp.getWriter().println("<tbody>");
      for (AnnonceKey key : prixByAnnonce.keySet()) {
        List<Annonce> relatedAnnonces = prixByAnnonce.get(key);
        List<Double> prices = newArrayList();
        List<String> priceString = newArrayList();
        Double previousPrice = null;
        for (Annonce annonce : relatedAnnonces) {
          prices.add(annonce.getPrix());
          if (previousPrice == null || !previousPrice.equals(annonce.getPrix())) {
            priceString.add(simpleDateFormat.format(annonce.getDate()) + "->" + annonce.getPrix() + "</br>");
          }
          previousPrice = annonce.getPrix();
        }
        resp.getWriter().println("<tr>");
        resp.getWriter().println("<td>" + key.getReference() + "</td>");
        resp.getWriter().println("<td>" + key.getSuperficie() + "</td>");
        resp.getWriter().println("<td>" + key.getArrondissement() + "</td>");
        resp.getWriter().println("<td>" + key.getQuartier() + "</td>");
        resp.getWriter().println("<td>" + prixByAnnonce.get(key).get(0).getText() + "</td>");
        resp.getWriter().println(
            "<td>" + priceString.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", "") + "</td>");
        resp.getWriter().println("</tr>");
      }
      resp.getWriter().println("</tbody>");
      resp.getWriter().println("</table>");
      if (!isEmpty(referenceParameter)) {
        resp.getWriter().println("<div id=\"chart_div\"></div> ");
      }
      resp.getWriter().println("</body></html>");
    } finally {
      em.close();
    }
  }
}
