package perso.logement;

import static com.google.appengine.labs.repackaged.com.google.common.collect.Lists.newArrayList;
import static com.google.appengine.labs.repackaged.com.google.common.collect.Maps.newHashMap;
import static com.google.appengine.labs.repackaged.com.google.common.collect.Sets.newHashSet;

import java.io.IOException;
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

import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class StatLogementServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(StatLogementServlet.class.getName());

  private static final EntityManagerFactory emfInstance = Persistence
      .createEntityManagerFactory("transactions-optional");

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
      }

      if (req.getParameter("quartier") != null) {
        queryString.append(" " + (whereClauseCreated ? "and" : "where") + " quartier=");
        queryString.append(req.getParameter("quartier"));
        whereClauseCreated = true;
      }

      if (req.getParameter("reference") != null) {
        queryString.append(" " + (whereClauseCreated ? "and" : "where") + " reference=");
        queryString.append(req.getParameter("reference"));
      }

      //queryString.append(" group by reference, superficie, arrondissement, quartier");
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
      resp.getWriter().println("</head>");

      resp.getWriter().println("<body>");

      resp.getWriter().println("Il y a actuellement " + annonces.size() + " annonces<br/>");
      Map<AnnonceKey, List<Double>> prixByAnnonce = newHashMap();
      for (Annonce annonce : annonces) {
        if (prixByAnnonce.containsKey(annonce.getKey())) {
          prixByAnnonce.get(annonce.getKey()).add(annonce.getPrix());
        } else {
          List<Double> prices = Lists.newArrayList();
          prices.add(annonce.getPrix());
          prixByAnnonce.put(new AnnonceKey(annonce.getReference(), annonce.getSuperficie(),
              annonce.getArrondissement(), annonce.getQuartier()), prices);
        }
      }
      resp.getWriter().println("<table id=\"table-3\">");
      resp.getWriter().println("<thead>");
      resp.getWriter().println("<th><b>Reference</b></th>");
      resp.getWriter().println("<th><b>Superficie</b></th>");
      resp.getWriter().println("<th><b>Arrondissement</b></th>");
      resp.getWriter().println("<th><b>Quartier</b></th>");
      resp.getWriter().println("<th><b>Prix</b></th>");
      resp.getWriter().println("</thead>");

      resp.getWriter().println("<tbody>");
      for (AnnonceKey key : prixByAnnonce.keySet()) {
        Set<Double> uniquePrices = newHashSet();
        List<Double> prices = prixByAnnonce.get(key);
        uniquePrices.addAll(prices);
        if (uniquePrices.size() > 1) {
          double lastPrice = prices.get(prices.size() - 1);
          double beforeLastPrice = prices.get(prices.size() - 2);
          resp.getWriter().println("<tr class=\"greenCell\">");

        } else {
          resp.getWriter().println("<tr>");
        }
        resp.getWriter().println("<td>" + key.getReference() + "</td>");
        resp.getWriter().println("<td>" + key.getSuperficie() + "</td>");
        resp.getWriter().println("<td>" + key.getArrondissement() + "</td>");
        resp.getWriter().println("<td>" + key.getQuartier() + "</td>");
        resp.getWriter().println("<td>" + prices + "</td>");
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
