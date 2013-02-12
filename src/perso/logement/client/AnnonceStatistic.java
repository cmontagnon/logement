package perso.logement.client;

import java.io.Serializable;

public class AnnonceStatistic implements Serializable {
  private static final long serialVersionUID = 1L;
  private int nbAnnonces;
  private long priceBySquareMeter;

  public AnnonceStatistic(int nbAnnonces, long priceBySquareMeter) {
    super();
    this.nbAnnonces = nbAnnonces;
    this.priceBySquareMeter = priceBySquareMeter;
  }

  public int getNbAnnonces() {
    return nbAnnonces;
  }

  public long getPriceBySquareMeter() {
    return priceBySquareMeter;
  }
}
