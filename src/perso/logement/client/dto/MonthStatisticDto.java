package perso.logement.client.dto;

import java.io.Serializable;

import javax.persistence.Transient;

public class MonthStatisticDto implements Serializable {

  private static final long serialVersionUID = 1L;

  private int year;
  private int month;
  private int nbAnnonces;
  private int nbAnnoncesWithNullPrice;
  private int priceByMeterSquare;
  private short arrondissement;
  private String quartier;

  public MonthStatisticDto() {
    super();
  }

  public MonthStatisticDto(int year, int month, int nbAnnonces, int nbAnnoncesWithNullPrice, int priceByMeterSquare,
      short arrondissement, String quartier) {
    super();
    this.year = year;
    this.month = month;
    this.nbAnnonces = nbAnnonces;
    this.nbAnnoncesWithNullPrice = nbAnnoncesWithNullPrice;
    this.priceByMeterSquare = priceByMeterSquare;
    this.arrondissement = arrondissement;
    this.quartier = quartier;
  }

  @Transient
  public String getKey() {
    return year + "-" + month + "-" + arrondissement + "-" + quartier;
  }

  @Transient
  public String getPeriod() {
    return year + "-" + month;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getMonth() {
    return month;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public int getNbAnnonces() {
    return nbAnnonces;
  }

  public void setNbAnnonces(int nbAnnonces) {
    this.nbAnnonces = nbAnnonces;
  }

  public int getPriceByMeterSquare() {
    return priceByMeterSquare;
  }

  public void setPriceByMeterSquare(int priceByMeterSquare) {
    this.priceByMeterSquare = priceByMeterSquare;
  }

  public short getArrondissement() {
    return arrondissement;
  }

  public void setArrondissement(short arrondissement) {
    this.arrondissement = arrondissement;
  }

  public String getQuartier() {
    return quartier;
  }

  public void setQuartier(String quartier) {
    this.quartier = quartier;
  }

  public int getNbAnnoncesWithNullPrice() {
    return nbAnnoncesWithNullPrice;
  }

  public void setNbAnnoncesWithNullPrice(int nbAnnoncesWithNullPrice) {
    this.nbAnnoncesWithNullPrice = nbAnnoncesWithNullPrice;
  }
}
