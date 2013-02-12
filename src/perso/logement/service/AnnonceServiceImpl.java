package perso.logement.service;

import static com.google.appengine.labs.repackaged.com.google.common.collect.Lists.newArrayList;
import static org.datanucleus.util.StringUtils.isEmpty;
import static perso.logement.client.SeLogerUtils.arrondissements;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import perso.logement.client.AnnonceService;
import perso.logement.client.dto.AnnonceDto;
import perso.logement.client.dto.MonthStatisticDto;
import perso.logement.core.Annonce;
import perso.logement.core.MonthStatistic;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AnnonceServiceImpl extends RemoteServiceServlet implements AnnonceService {

  private static final long serialVersionUID = 1L;

  private static final Logger log = Logger.getLogger(AnnonceServiceImpl.class.getName());

  private static final EntityManagerFactory emfInstance = Persistence
      .createEntityManagerFactory("transactions-optional");

  @Override
  public List<AnnonceDto> getAnnonces(Date startDate, String arrondissement, String quartier, Double surfaceMin,
      Double surfaceMax, Double priceMin, Double priceMax, String queryType) {
    log.info("getAnnonces called");
    List<AnnonceDto> resultList = newArrayList();
    // Query database
    if (startDate != null) {
      EntityManager em = emfInstance.createEntityManager();
      try {
        StringBuilder queryString = new StringBuilder();
        queryString.append("select annonce");
        queryString.append(" from Annonce annonce");
        queryString.append(" where date>:startDate");
        if (!isEmpty(arrondissement)) {
          queryString.append(" and arrondissement=" + arrondissement);
          if (!isEmpty(quartier)) {
            queryString
                .append(" and quartier=\'" + arrondissements.get(new Short(arrondissement)).get(quartier) + "\'");
          }
        }
        queryString.append(" order by date asc");

        log.info("QueryString : " + queryString);

        Query query = em.createQuery(queryString.toString());
        query.setParameter("startDate", startDate);
        query.setMaxResults(1000);
        log.info("Query : " + query);
        int index = 0;
        List result = null;
        do {
          query.setFirstResult(index * 1000);
          result = query.getResultList();
          log.info("Sub query returned " + result.size() + " annonces");
          for (Object object : result) {
            Annonce annonce = (Annonce) object;

            boolean isSurfaceOk = isSurfaceOk(surfaceMin, surfaceMax, annonce);
            boolean isPriceOk = isPriceOk(priceMin, priceMax, annonce);

            if (isSurfaceOk && isPriceOk) {
              resultList.add(new AnnonceDto(annonce.getReference(), annonce.getText(), annonce.getPrix(), annonce
                  .getSuperficie(), annonce.getDate(), annonce.getArrondissement(), annonce.getQuartier()));
            }
          }
          index++;
        } while (!result.isEmpty());
      } finally {
        em.close();
      }
    }
    log.info("Query returned " + resultList.size() + " annonces");
    return resultList;
  }

  private static boolean isSurfaceOk(Double surfaceMin, Double surfaceMax, Annonce annonce) {
    boolean isSurfaceOk = true;
    if (surfaceMin != null) {
      isSurfaceOk &= annonce.getSuperficie() >= surfaceMin;
    }
    if (surfaceMax != null) {
      isSurfaceOk &= annonce.getSuperficie() < surfaceMax;
    }
    return isSurfaceOk;
  }

  private static boolean isPriceOk(Double priceMin, Double priceMax, Annonce annonce) {
    boolean isPriceOk = true;
    if (priceMin != null) {
      isPriceOk &= annonce.getPrix() >= priceMin;
    }
    if (priceMax != null) {
      isPriceOk &= annonce.getPrix() < priceMax;
    }
    return isPriceOk;
  }

  @Override
  public Map<Date, Double> getPriceEvolution(Date startDate, String arrondissement, String quartier) {
    log.info("getPriceEvolution called");
    // TODO :
    return null;
  }

  @Override
  public List<MonthStatisticDto> getStatistic(Date fromDate, String arrondissement, String quartier) {
    log.info("getStatistic called");
    List<MonthStatisticDto> resultList = newArrayList();
    // Query database
    if (fromDate != null) {
      EntityManager em = emfInstance.createEntityManager();
      try {
        StringBuilder queryString = new StringBuilder();
        queryString.append("select monthStat");
        queryString.append(" from MonthStatistic monthStat");
        queryString.append(" where arrondissement=" + arrondissement);
        if (!isEmpty(quartier)) {
          queryString.append(" and quartier=\'" + quartier + "\'");
        } else {
          queryString.append(" and quartier is null");
        }
        queryString.append(" order by year asc, month asc");

        log.info("QueryString : " + queryString);

        Query query = em.createQuery(queryString.toString());
        query.setParameter("fromDate", fromDate);
        log.info("Query : " + query);
        List result = null;
        result = query.getResultList();
        log.info("Query returned " + result.size() + " statistics");
        for (Object object : result) {
          MonthStatistic monthStatistic = (MonthStatistic) object;

          resultList.add(new MonthStatisticDto(monthStatistic.getYear(), monthStatistic.getMonth(), monthStatistic
              .getNbAnnonces(), monthStatistic.getNbAnnoncesWithNullPrice(), monthStatistic.getPriceByMeterSquare(),
              monthStatistic.getArrondissement(), monthStatistic.getQuartier()));
        }
      } finally {
        em.close();
      }
    }
    log.info("Query returned " + resultList.size() + " statistics");
    return resultList;
  }
}
