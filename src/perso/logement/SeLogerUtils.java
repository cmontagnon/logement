package perso.logement;

import static com.google.appengine.labs.repackaged.com.google.common.collect.Maps.newHashMap;

import java.util.Map;

public class SeLogerUtils {

  public static Map<Short, Map<String, String>> arrondissements = newHashMap();

  static {

    // Arrondissement 10
    Map<String, String> quartier10 = newHashMap();
    quartier10.put("133091", "Saint Vincent de Paul/Lariboisière");
    quartier10.put("133092", "Louis Blanc/Aqueduc");
    quartier10.put("133093", "Grange aux belles/Terrage");
    quartier10.put("133094", "Faubourg du temple/Hôpital Saint Louis");
    quartier10.put("133095", "Château d'Eau/Lancry");
    quartier10.put("133096", "Porte Saint-Denis/Paradis");
    arrondissements.put(new Short("10"), quartier10);

    // Arrondissement 18
    Map<String, String> quartier18 = newHashMap();
    quartier18.put("133146", "Moskowa/Porte Montmartre/Porte de Clignancourt");
    quartier18.put("133147", "Grandes Carrières/Clichy");
    quartier18.put("133148", "Clignacourt/Jules Joffrin");
    quartier18.put("133149", "Montmartre");
    quartier18.put("133150", "Amiraux/Simplon-Poissonniers"); // OK
    quartier18.put("133151", "Charles/Hermite/Evangile"); // OK
    quartier18.put("133152", "La Chapelle/Marx Dormoy");
    quartier18.put("133153", "Goutte d'or/Château Rouge");
    arrondissements.put(new Short("18"), quartier18);
  }
}
