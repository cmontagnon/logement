package perso.logement.client;

import static perso.logement.client.AnnonceAggregate.NO_SURFACE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AnnonceAggregateStore implements Serializable {

  private static final long serialVersionUID = 1L;

  private Map<Long, Integer> nbAnnoncesByPrice = new TreeMap<Long, Integer>();

  public AnnonceAggregateStore() {
    super();
  }

  private void addAnnonceAggregate(AnnonceAggregate annonceAggregate) {
    Long lastPriceBySquareMeter = annonceAggregate.getLastPriceBySquareMeterRound();
    if (lastPriceBySquareMeter != NO_SURFACE) {
      if (nbAnnoncesByPrice.containsKey(lastPriceBySquareMeter)) {
        nbAnnoncesByPrice.put(lastPriceBySquareMeter, nbAnnoncesByPrice.get(lastPriceBySquareMeter) + 1);
      } else {
        nbAnnoncesByPrice.put(lastPriceBySquareMeter, 1);
      }
    }
  }

  public List<AnnonceStatistic> getAnnonceStatistics() {
    List<AnnonceStatistic> annonceStatistics = new ArrayList<AnnonceStatistic>();
    for (Long priceBySquareMeter : nbAnnoncesByPrice.keySet()) {
      annonceStatistics.add(new AnnonceStatistic(nbAnnoncesByPrice.get(priceBySquareMeter), priceBySquareMeter));
    }
    return annonceStatistics;
  }

  public void addAnnonceAggregates(Collection<AnnonceAggregate> annonceAggregates) {
    for (AnnonceAggregate annonceAggregate : annonceAggregates) {
      addAnnonceAggregate(annonceAggregate);
    }
  }

  public void clear() {
    nbAnnoncesByPrice.clear();
  }
}
