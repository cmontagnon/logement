package perso.logement;

import static com.google.appengine.labs.repackaged.com.google.common.collect.Lists.newArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeLogerUtils {

  public static Map<String, String> humanReadableQuartier = new HashMap<String, String>();
  public static Map<Short, List<Quartier>> arrondissements = new HashMap<Short, List<Quartier>>();

  static {
    List<Quartier> quartiers10 =
        newArrayList(new Quartier("133091", "Saint Vincent de Paul/Lariboisière"), new Quartier("133092",
            "Louis Blanc/Aqueduc"), new Quartier("133093", "Grange aux belles/Terrage"), new Quartier("133094",
            "Faubourg du temple/Hôpital Saint Louis"), new Quartier("133095", "Château d'Eau/Lancry"), new Quartier(
            "133096", "Porte Saint-Denis/Paradis"));
    arrondissements.put(new Short("10"), quartiers10);

    // Arrondissement 10
    humanReadableQuartier.put("133091", "Saint Vincent de Paul/Lariboisière");
    humanReadableQuartier.put("133092", "Louis Blanc/Aqueduc");
    humanReadableQuartier.put("133093", "Grange aux belles/Terrage");
    humanReadableQuartier.put("133094", "Faubourg du temple/Hôpital Saint Louis");
    humanReadableQuartier.put("133095", "Château d'Eau/Lancry");
    humanReadableQuartier.put("133096", "Porte Saint-Denis/Paradis");

    // Arrondissement 18
    humanReadableQuartier.put("133146", "Moskowa/Porte Montmartre/Porte de Clignancourt");
    humanReadableQuartier.put("133147", "Grandes Carrières/Clichy");
    humanReadableQuartier.put("133148", "Clignacourt/Jules Joffrin");
    humanReadableQuartier.put("133149", "Montmartre");
    humanReadableQuartier.put("133150", "Amiraux/Simplon-Poissonniers"); // OK
    humanReadableQuartier.put("133151", "Charles/Hermite/Evangile"); // OK
    humanReadableQuartier.put("133152", "La Chapelle/Marx Dormoy");
    humanReadableQuartier.put("133153", "Goutte d'or/Château Rouge");
  }
}
