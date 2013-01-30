package perso.logement.client;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static perso.logement.core.AnnonceEvolution.DOWN;
import static perso.logement.core.AnnonceEvolution.NONE;
import static perso.logement.core.AnnonceEvolution.UP;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import perso.logement.client.dto.AnnonceDto;
import perso.logement.client.resources.LogementResources;
import perso.logement.core.AnnonceEvolution;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;

public class AnnonceAggregate implements Serializable {

  private static final long serialVersionUID = 1L;
  private List<AnnonceDto> annonces = new ArrayList<AnnonceDto>();
  private int queryMeanPriceBySquareMeter;

  private static LogementResources resources = GWT.create(LogementResources.class);

  public void addAnnonce(AnnonceDto annonce) {
    annonces.add(annonce);
  }

  public List<AnnonceDto> getAnnonces() {
    return annonces;
  }

  public String getReference() {
    return annonces.get(0).getReference();
  }

  public short getArrondissement() {
    return annonces.get(0).getArrondissement();
  }

  public double getSuperficie() {
    return annonces.get(0).getSuperficie();
  }

  public String getQuartier() {
    return annonces.get(0).getQuartier();
  }

  public Double getLastPrice() {
    return annonces.get(annonces.size() - 1).getPrix();
  }

  public List<Double> getPrices() {
    List<Double> prices = new ArrayList<Double>();
    for (AnnonceDto annonce : annonces) {
      prices.add(annonce.getPrix());
    }
    return prices;
  }

  public Long getLastPriceBySquareMeter() {
    AnnonceDto lastAnnonce = annonces.get(annonces.size() - 1);
    return Math.round(lastAnnonce.getPrix() / lastAnnonce.getSuperficie());
  }

  public List<Long> getPricesBySquareMeter() {
    List<Long> prices = new ArrayList<Long>();
    for (AnnonceDto annonce : annonces) {
      prices.add(Math.round(annonce.getPrix() / annonce.getSuperficie()));
    }
    return prices;
  }

  public boolean hasMultiplePrices() {
    Set<Double> prices = new HashSet<Double>();
    for (AnnonceDto annonce : annonces) {
      prices.add(annonce.getPrix());
    }
    return prices.size() > 1;
  }

  public AnnonceEvolution getAnnonceEvolution() {
    List<Double> prices = getPrices();
    Set<Double> distinctPrices = new HashSet<Double>(prices);
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

  public double getMeanPriceDifference() {
    List<Long> pricesBySquareMeter = getPricesBySquareMeter();
    double meanTagPercentage = 0;
    if (!pricesBySquareMeter.isEmpty()) { // It may be empty when the superficie is equal to 0
      Long lastPriceBySquareMeter = pricesBySquareMeter.get(pricesBySquareMeter.size() - 1);
      if (getQueryMeanPriceBySquareMeter() != 0) {
        meanTagPercentage =
            round(100 * new Double(lastPriceBySquareMeter - getQueryMeanPriceBySquareMeter())
                / getQueryMeanPriceBySquareMeter(), 2, ROUND_HALF_UP);
      } else {
        meanTagPercentage = 0;
      }
    }
    return meanTagPercentage;
  }

  public ImageResource getMeanPriceDifferenceImage() {
    return getMeanPriceDifference() >= 0 ? resources.green() : resources.red();
  }

  public String getSeLogerLink() {
    return "<a href=\"http://www.seloger.com/recherche.htm?ci=7501" + getArrondissement()
        + "&idtt=2&org=advanced_search&refannonce=" + getReference() + "\">seloger</a>";
  }

  public ImageResource getEvolutionImage() {
    AnnonceEvolution annonceEvolution = getAnnonceEvolution();
    switch (annonceEvolution) {
      case DOWN:
        return resources.down();
      case UP:
        return resources.up();
      default:
        break;
    }
    return null;
  }

  private static double round(double x, int scale, int roundingMethod) {
    try {
      return (new BigDecimal(Double.toString(x)).setScale(scale, roundingMethod)).doubleValue();
    } catch (NumberFormatException ex) {
      if (Double.isInfinite(x)) {
        return x;
      } else {
        return Double.NaN;
      }
    }
  }

  public void setQueryMeanPriceBySquareMeter(int queryMeanPriceBySquareMeter) {
    this.queryMeanPriceBySquareMeter = queryMeanPriceBySquareMeter;
  }

  public int getQueryMeanPriceBySquareMeter() {
    return queryMeanPriceBySquareMeter;
  }

  public String getKey() {
    return getReference() + "_" + getSuperficie() + "_" + getArrondissement() + "_" + getQuartier();
  }
}
