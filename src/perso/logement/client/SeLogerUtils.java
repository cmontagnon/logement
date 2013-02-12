package perso.logement.client;

import java.util.HashMap;
import java.util.Map;

public class SeLogerUtils {

  public static Map<Short, Map<String, String>> arrondissements = new HashMap<Short, Map<String, String>>();

  static {

    // Arrondissement 6
    Map<String, String> quartier6 = new HashMap<String, String>();
    quartier6.put("133050", "Odéon");
    quartier6.put("133071", "Monnaie");
    quartier6.put("133072", "Saint Germain des Prés");
    quartier6.put("133073", "Rennes");
    quartier6.put("133074", "Saint Placide");
    quartier6.put("133166", "Notre Dame des Champs");
    arrondissements.put(new Short("6"), quartier6);

    // Arrondissement 7
    Map<String, String> quartier7 = new HashMap<String, String>();
    quartier7.put("133075", "Saint Thomas d'Aquin");
    quartier7.put("133076", "Invalides");
    quartier7.put("133077", "Ecole Militaire"); // Too big
    quartier7.put("133078", "Gros Caillou");
    quartier7.put("133765", "Seine et Berges");
    arrondissements.put(new Short("7"), quartier7);

    // Arrondissement 8
    Map<String, String> quartier8 = new HashMap<String, String>();
    quartier8.put("133079", "Triangle d'Or");
    quartier8.put("133080", "Elysées/Madeleine");
    quartier8.put("133081", "Saint Philippe du Roule");
    quartier8.put("133082", "Hoche-Friedland");
    quartier8.put("133083", "Parc Monceau");
    quartier8.put("133084", "Mairie");
    quartier8.put("133085", "Europe");
    arrondissements.put(new Short("8"), quartier8);

    // Arrondissement 9
    Map<String, String> quartier9 = new HashMap<String, String>();
    quartier9.put("133086", "Provence Opéra");
    quartier9.put("133087", "Lafayette-Richer");
    quartier9.put("133088", "Clichy/Trinité");
    quartier9.put("133089", "Lorette/Martyrs");
    quartier9.put("133090", "Trudaine/Maubeuge");
    arrondissements.put(new Short("9"), quartier9);

    // Arrondissement 10
    Map<String, String> quartier10 = new HashMap<String, String>();
    quartier10.put("133091", "Saint Vincent de Paul/Lariboisière");
    quartier10.put("133092", "Louis Blanc/Aqueduc");
    quartier10.put("133093", "Grange aux belles/Terrage");
    quartier10.put("133094", "Faubourg du temple/Hôpital Saint Louis");
    quartier10.put("133095", "Château d'Eau/Lancry");
    quartier10.put("133096", "Porte Saint-Denis/Paradis");
    arrondissements.put(new Short("10"), quartier10);

    // Arrondissement 11
    Map<String, String> quartier11 = new HashMap<String, String>();
    quartier11.put("133097", "Belleville/Saint Maur");
    quartier11.put("133098", "Republique/Saint Ambroise"); // Too big
    quartier11.put("133099", "Bastille/Popincourt"); // Too big
    quartier11.put("133100", "Leon Blum/Folie Regnault"); // Too big
    quartier11.put("133101", "Nation/Alexandre Dumas");
    arrondissements.put(new Short("11"), quartier11);

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

    // Arrondissement 13
    Map<String, String> quartier13 = new HashMap<String, String>();
    quartier13.put("133109", "Salpêtrière/Austerlitz");
    quartier13.put("133110", "Croulebarbe");
    quartier13.put("133111", "Buttes aux Cailles/Glacière");
    quartier13.put("133112", "Nationale/Deux Moulins");
    quartier13.put("133113", "Dunois/Bibliothèque/Jeanne d'Arc");
    quartier13.put("133114", "Patay/Masséna");
    quartier13.put("133115", "Bièvres Sud Tolbiac");
    quartier13.put("133116", "Olympiades/Choisy");
    arrondissements.put(new Short("13"), quartier13);

    // Arrondissement 14
    Map<String, String> quartier14 = new HashMap<String, String>();
    quartier14.put("133117", "Raspail/Montparnasse");
    quartier14.put("133118", "Montsouris Dareau");
    quartier14.put("133119", "Mouton Duvernet"); // Too big
    quartier14.put("133120", "Pernety");
    quartier14.put("133121", "Didot/Porte de Vanves");
    quartier14.put("133122", "Jean Moulin/Porte d'Orléans");
    arrondissements.put(new Short("14"), quartier14);

    // Arrondissement 15
    Map<String, String> quartier15 = new HashMap<String, String>();
    quartier15.put("133123", "Pasteur Montparnasse");
    quartier15.put("133124", "Cambronne/Garibaldi"); // Too big
    quartier15.put("133125", "Georges Brassens");
    quartier15.put("133126", "Alleray Procession");
    quartier15.put("133127", "Saint Lambert");
    quartier15.put("133128", "Vaugirard/Parc des Expositions"); // Too big
    quartier15.put("133129", "Citroën/Boucicaut");
    quartier15.put("133130", "Violet Commerce");
    quartier15.put("133131", "Dupleix/Motte Picquet");
    quartier15.put("133132", "Emeriau/Zola");
    quartier15.put("133766", "Seine et Berges");
    arrondissements.put(new Short("15"), quartier15);

    // Arrondissement 16
    Map<String, String> quartier16 = new HashMap<String, String>();
    quartier16.put("133051", "Chaillot"); // Too big
    quartier16.put("133133", "Porte Dauphine"); // Too big
    quartier16.put("133134", "Seine et Berges");
    quartier16.put("133135", "Muette Sud");
    quartier16.put("133136", "Bois de Boulogne");
    quartier16.put("133137", "Auteuil Sud"); // Too big
    quartier16.put("133769", "Muette Nord"); // Too big
    quartier16.put("133770", "Auteuil Nord"); // Too big
    arrondissements.put(new Short("16"), quartier16);

    // Arrondissement 17
    Map<String, String> quartier17 = new HashMap<String, String>();
    quartier17.put("133138", "Batignolles/Cardinet");
    quartier17.put("133139", "La Fourche/Guy Môquet");
    quartier17.put("133140", "Epinettes/Bessières");
    quartier17.put("133141", "Legendre/Lévis");
    quartier17.put("133142", "Pereire/Malesherbes");
    quartier17.put("133143", "Champerret/Berthier");
    quartier17.put("133144", "Ternes/Maillot");
    quartier17.put("133145", "Courcelles/Wagram"); // Too big
    quartier17.put("133678", "Clichy/Batignolles");
    arrondissements.put(new Short("17"), quartier17);

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

    // Arrondissement 19
    Map<String, String> quartier19 = new HashMap<String, String>();
    quartier19.put("133154", "Pont de Flandre");
    quartier19.put("133155", "Flandre/Aubervilliers"); // Too big
    quartier19.put("133156", "Bassin de la Villette");
    quartier19.put("133157", "Danube/Porte des Lilas");
    quartier19.put("133158", "Manin/Jaurès");
    quartier19.put("133159", "Secrétan");
    quartier19.put("133160", "Bas Belleville");
    quartier19.put("133161", "Buttes Chaumont"); // Too big
    arrondissements.put(new Short("19"), quartier19);

    // Arrondissement 20
    Map<String, String> quartier20 = new HashMap<String, String>();
    quartier20.put("133163", "Belleville");
    quartier20.put("133164", "Amandiers");
    quartier20.put("133165", "Père Lachaise/Réunion");
    quartier20.put("133167", "Plaine");
    quartier20.put("133168", "Saint Blaise");
    quartier20.put("133169", "Gambetta");
    quartier20.put("133170", "Télégraphe/Pelleport Saint Fargeau");
    arrondissements.put(new Short("20"), quartier20);
  }
}
