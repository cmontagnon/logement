package perso.logement.client.dto;

import static perso.logement.core.AnnonceEvolution.DOWN;
import static perso.logement.core.AnnonceEvolution.NONE;
import static perso.logement.core.AnnonceEvolution.UP;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import perso.logement.core.AnnonceEvolution;

public class AnnonceAggregateDto implements Serializable {

  private static final long serialVersionUID = 1L;
  private List<AnnonceDto> annonces = new ArrayList<AnnonceDto>();
  private int queryMeanPriceBySquareMeter;

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

  public List<Double> getPrices() {
    List<Double> prices = new ArrayList<Double>();
    for (AnnonceDto annonce : annonces) {
      prices.add(annonce.getPrix());
    }
    return prices;
  }

  public String getPricesAsString() {
    StringBuilder prices = new StringBuilder();
    for (AnnonceDto annonce : annonces) {
      prices.append(annonce.getPrix());
      prices.append(";");
    }
    return prices.toString();
  }

  public String getPricesBySquareMeterAsString() {
    StringBuilder pricesBySquareMeter = new StringBuilder();
    for (AnnonceDto annonce : annonces) {
      if (annonce.getSuperficie() != 0) {
        pricesBySquareMeter.append(Math.round(annonce.getPrix() / annonce.getSuperficie()));
        pricesBySquareMeter.append(";");
      } else {
        pricesBySquareMeter.append("NA;");
      }
    }
    return pricesBySquareMeter.toString();
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

  public void setQueryMeanPriceBySquareMeter(int queryMeanPriceBySquareMeter) {
    this.queryMeanPriceBySquareMeter = queryMeanPriceBySquareMeter;
  }

  public int getQueryMeanPriceBySquareMeter() {
    return queryMeanPriceBySquareMeter;
  }
}
