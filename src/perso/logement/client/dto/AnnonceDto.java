package perso.logement.client.dto;

import java.io.Serializable;
import java.util.Date;

public class AnnonceDto implements Serializable {

  private static final long serialVersionUID = 1L;

  private String reference;
  private String text;
  private double prix;
  private double superficie;
  private Date date;
  private short arrondissement;
  private String quartier;

  public AnnonceDto() {
    super();
  }

  public AnnonceDto(String reference, String text, double prix, double superficie, Date date, short arrondissement,
      String quartier) {
    super();
    this.reference = reference;
    this.text = text;
    this.prix = prix;
    this.superficie = superficie;
    this.date = new Date(date.getTime());
    this.arrondissement = arrondissement;
    this.quartier = quartier;
  }

  public String getReference() {
    return reference;
  }

  public String getText() {
    return text;
  }

  public double getPrix() {
    return prix;
  }

  public double getSuperficie() {
    return superficie;
  }

  public Date getDate() {
    return date;
  }

  public short getArrondissement() {
    return arrondissement;
  }

  public String getQuartier() {
    return quartier;
  }
}
