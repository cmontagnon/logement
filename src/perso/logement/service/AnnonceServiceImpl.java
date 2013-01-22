package perso.logement.service;

import static com.google.appengine.labs.repackaged.com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.parseInt;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static org.datanucleus.util.StringUtils.isEmpty;
import static perso.logement.client.SeLogerUtils.arrondissements;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import perso.logement.client.AnnonceService;
import perso.logement.client.dto.AnnonceDto;
import perso.logement.core.Annonce;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AnnonceServiceImpl extends RemoteServiceServlet implements AnnonceService {

  private static final long serialVersionUID = 1L;

  private static final Logger log = Logger.getLogger(AnnonceServiceImpl.class.getName());

  private static final EntityManagerFactory emfInstance = Persistence
      .createEntityManagerFactory("transactions-optional");

  @Override
  public List<AnnonceDto> getAnnonces(String startDate, String arrondissement, String quartier, String queryType) {
    log.info("getAnnonces called");
    List<AnnonceDto> resultList = newArrayList();
    // Query database
    if (!isEmpty(startDate)) {
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
        Calendar cal = Calendar.getInstance();
        cal.set(YEAR, parseInt(startDate.substring(0, 4)));
        cal.set(MONTH, parseInt(startDate.substring(5, 7)) - 1);
        cal.set(DAY_OF_MONTH, parseInt(startDate.substring(8, 10)));
        cal.set(HOUR_OF_DAY, 0);
        cal.set(MINUTE, 0);
        cal.set(MILLISECOND, 0);
        query.setParameter("startDate", cal.getTime());
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
            resultList.add(new AnnonceDto(annonce.getReference(), annonce.getText(), annonce.getPrix(), annonce
                .getSuperficie(), annonce.getDate(), annonce.getArrondissement(), annonce.getQuartier()));
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
}
