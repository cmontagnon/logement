package perso.logement;

public class AnnonceKey {
  private String reference;
  private double superficie;
  private double arrondissement;
  private String quartier;

  public AnnonceKey(String reference, double superficie, double arrondissement, String quartier) {
    super();
    this.reference = reference;
    this.superficie = superficie;
    this.arrondissement = arrondissement;
    this.quartier = quartier;
  }

  @Override
  public String toString() {
    return "AnnonceKey [reference=" + reference + ", superficie=" + superficie + ", arrondissement=" + arrondissement
        + ", quartier=" + quartier + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(arrondissement);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((quartier == null) ? 0 : quartier.hashCode());
    result = prime * result + ((reference == null) ? 0 : reference.hashCode());
    temp = Double.doubleToLongBits(superficie);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AnnonceKey other = (AnnonceKey) obj;
    if (Double.doubleToLongBits(arrondissement) != Double.doubleToLongBits(other.arrondissement))
      return false;
    if (quartier == null) {
      if (other.quartier != null)
        return false;
    } else if (!quartier.equals(other.quartier))
      return false;
    if (reference == null) {
      if (other.reference != null)
        return false;
    } else if (!reference.equals(other.reference))
      return false;
    if (Double.doubleToLongBits(superficie) != Double.doubleToLongBits(other.superficie))
      return false;
    return true;
  }

  public String getReference() {
    return reference;
  }

  public double getSuperficie() {
    return superficie;
  }

  public double getArrondissement() {
    return arrondissement;
  }

  public String getQuartier() {
    return quartier;
  }
}
