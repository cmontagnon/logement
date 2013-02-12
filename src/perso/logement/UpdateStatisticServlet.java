package perso.logement;

import static org.datanucleus.util.StringUtils.isEmpty;
import static perso.logement.client.SeLogerUtils.arrondissements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import perso.logement.core.Annonce;
import perso.logement.core.AnnonceKey;
import perso.logement.core.MonthStatistic;

@SuppressWarnings("serial")
public class UpdateStatisticServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(UpdateStatisticServlet.class.getName());
  private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

  private static final EntityManagerFactory emfInstance = Persistence
      .createEntityManagerFactory("transactions-optional");

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String year = req.getParameter("year");
    String month = req.getParameter("month");
    String quartier = req.getParameter("quartier");
    String arrondissement = req.getParameter("arrondissement");

    EntityManager em = emfInstance.createEntityManager();
    try {
      Date startDate = formatter.parse(year + "-" + month + "-01");
      Date endDate = formatter.parse(year + "-" + (month + 1) + "-01");
      StringBuilder queryString = new StringBuilder();
      queryString.append("select annonce");
      queryString.append(" from Annonce annonce");
      queryString.append(" where date>:startDate and date<:endDate");
      if (!isEmpty(arrondissement)) {
        queryString.append(" and arrondissement=" + arrondissement);
        if (!isEmpty(quartier)) {
          queryString.append(" and quartier=\"" + arrondissements.get(new Short(arrondissement)).get(quartier) + "\"");
        }
      }
      queryString.append(" order by date asc");

      Query query = em.createQuery(queryString.toString());
      query.setParameter("startDate", startDate);
      query.setParameter("endDate", endDate);
      List<Annonce> resultList = query.getResultList();

      // We retain only one version of every annonce
      Map<AnnonceKey, Annonce> annoncesWithoutNullPriceByKey = new HashMap<AnnonceKey, Annonce>();
      int nbAnnoncesWithNullPrice = 0;
      for (Annonce annonce : resultList) {
        if (annonce.getSuperficie() > 0) {
          annoncesWithoutNullPriceByKey.put(annonce.getKey(), annonce); // On écrase par l'annonce la plus récente
        } else {
          nbAnnoncesWithNullPrice++;
        }
      }
      log.info("nbAnnoncesWithNullPrice : " + nbAnnoncesWithNullPrice);

      int nbAnnonces = resultList.size();
      log.info("nbAnnonces : " + nbAnnonces);
      int priceByMeterSquare = 0;
      for (Annonce annonceWithoutNullPrice : annoncesWithoutNullPriceByKey.values()) {
        priceByMeterSquare += annonceWithoutNullPrice.getPrix() / annonceWithoutNullPrice.getSuperficie();
      }
      priceByMeterSquare = priceByMeterSquare / (annoncesWithoutNullPriceByKey.size());
      log.info("priceByMeterSquare : " + priceByMeterSquare);

      // We insert/update the corresponding MonthStatistic
      Query monthStatQuery =
          em.createQuery("select stat from MonthStatistic stat where year=" + year + " and month=" + month
              + " and arrondissement" + (isEmpty(arrondissement) ? " is null" : "=" + arrondissement) + " and quartier"
              + (isEmpty(quartier) ? " is null" : "='" + quartier + "'"));
      List<MonthStatistic> monthStatistics = monthStatQuery.getResultList();

      if (!monthStatistics.isEmpty()) {
        MonthStatistic monthStatistic = monthStatistics.get(0);
        monthStatistic.setNbAnnonces(nbAnnonces);
        monthStatistic.setNbAnnoncesWithNullPrice(nbAnnoncesWithNullPrice);
        monthStatistic.setPriceByMeterSquare(priceByMeterSquare);
        log.info("Updating  MonthStatistic");
        em.merge(monthStatistic);
      } else {
        MonthStatistic monthStatistic =
            new MonthStatistic(Integer.parseInt(year), Integer.parseInt(month), nbAnnonces, nbAnnoncesWithNullPrice,
                priceByMeterSquare, Short.parseShort(arrondissement), quartier);
        log.info("Inserting  MonthStatistic");
        em.persist(monthStatistic);
      }

    } catch (NumberFormatException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    } finally {
      em.close();
    }
    resp.setContentType("text/plain");
    resp.getWriter().println("Hello, world");
  }
}
