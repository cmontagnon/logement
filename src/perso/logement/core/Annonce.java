package perso.logement.core;

import static javax.persistence.TemporalType.DATE;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.Transient;

@Entity
public class Annonce implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String reference;
  private String text;
  private double prix;
  private double superficie;
  @Temporal(DATE)
  private Date date;
  private short arrondissement;
  private String quartier;

  public Annonce() {
    super();
  }

  public Annonce(String reference, String text, double prix, double superficie, Date date, short arrondissement,
      String quartier) {
    super();
    this.reference = reference;
    this.text = text;
    this.prix = prix;
    this.superficie = superficie;
    this.date = date;
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

  public void setReference(String reference) {
    this.reference = reference;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setPrix(double prix) {
    this.prix = prix;
  }

  public void setSuperficie(double superficie) {
    this.superficie = superficie;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public void setArrondissement(short arrondissement) {
    this.arrondissement = arrondissement;
  }

  public void setQuartier(String quartier) {
    this.quartier = quartier;
  }

  @Transient
  public AnnonceKey getKey() {
    return new AnnonceKey(reference, superficie, arrondissement, quartier);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "reference=" + reference + "; superficie=" + superficie + "; prix=" + prix + "; date=" + date + "; text="
        + text;
  }
}
