package perso.logement.client;

import java.util.HashMap;
import java.util.Map;

public class SeLogerUtils {

  public static Map<Short, Map<String, String>> arrondissements = new HashMap<Short, Map<String, String>>();

  static {

    // Arrondissement 10
    Map<String, String> quartier10 = new HashMap<String, String>();
    quartier10.put("133091", "Saint Vincent de Paul/Lariboisière");
    quartier10.put("133092", "Louis Blanc/Aqueduc");
    quartier10.put("133093", "Grange aux belles/Terrage");
    quartier10.put("133094", "Faubourg du temple/Hôpital Saint Louis");
    quartier10.put("133095", "Château d'Eau/Lancry");
    quartier10.put("133096", "Porte Saint-Denis/Paradis");
    arrondissements.put(new Short("10"), quartier10);

    // Arrondissement 12
    Map<String, String> quartier12 = new HashMap<String, String>();
    quartier12.put("133102", "Aligre/Gare de Lyon");
    quartier12.put("133103", "Bercy");
    quartier12.put("133104", "Jardin de Reuilly");
    quartier12.put("133105", "Nation/Picpus");
    quartier12.put("133106", "Vallée de Fécamp");
    quartier12.put("133107", "Bel-Air Sud");
    quartier12.put("133108", "Bel-Air Nord");
    quartier12.put("133759", "Bois de Vincennes");
    quartier12.put("133764", "Seine et Berges");
    arrondissements.put(new Short("12"), quartier12);

    // Arrondissement 18
    Map<String, String> quartier18 = new HashMap<String, String>();
    quartier18.put("133146", "Moskowa/Porte Montmartre/Porte de Clignancourt");
    quartier18.put("133147", "Grandes Carrières/Clichy");
    quartier18.put("133148", "Clignacourt/Jules Joffrin");
    quartier18.put("133149", "Montmartre"); // Too big
    quartier18.put("133150", "Amiraux/Simplon-Poissonniers");
    quartier18.put("133151", "Charles/Hermite/Evangile");
    quartier18.put("133152", "La Chapelle/Marx Dormoy");
    quartier18.put("133153", "Goutte d'or/Château Rouge");
    arrondissements.put(new Short("18"), quartier18);

    // Arrondissement 11
    Map<String, String> quartier11 = new HashMap<String, String>();
    quartier11.put("133097", "Belleville/Saint Maur");
    quartier11.put("133098", "Republique/Saint Ambroise"); // Too big
    quartier11.put("133099", "Bastille/Popincourt"); // Too big
    quartier11.put("133100", "Leon Blum/Folie Regnault"); // Too big
    quartier11.put("133101", "Nation/Alexandre Dumas");
    arrondissements.put(new Short("11"), quartier11);
  }
}
