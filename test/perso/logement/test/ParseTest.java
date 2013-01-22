package perso.logement.test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import perso.logement.FeedAnnonceServlet;
import perso.logement.core.Annonce;

public class ParseTest {

  @Test
  public void parsePage() throws IOException {
    Document doc = Jsoup.parse(new File("test/perso/logement/test/page1.html"), "UTF-8");
    Set<Annonce> annonces = FeedAnnonceServlet.parsePage(doc, new Short("10"), "133091");
    assertThat(annonces).hasSize(7);
    assertThat(extractProperty("reference").from(annonces)).containsOnly("SL119718", "75010 Dav", "7774868", "A184",
        "MT0060608", "59042273", "4561");
    assertThat(extractProperty("prix").from(annonces)).containsOnly(65000d, 59000d, 50000d, 76000d, 93000d, 79000d,
        94830d);
    assertThat(extractProperty("superficie").from(annonces)).containsOnly(10d, 0d, 0d, 10d, 0d, 8d, 9d);
    assertThat(extractProperty("arrondissement").from(annonces)).containsOnly(new Short("10"));
    assertThat(extractProperty("quartier").from(annonces)).containsOnly("Saint Vincent de Paul/Lariboisière");
    assertThat(extractProperty("text").from(annonces))
        .contains(
            "MÉTRO GARE DU NORD. Dans un immeuble ancien en pierre de Taille, au 7ème étage avec ascenseur jusqu'au 6ème, studette de 6m² comprenant une pièce avec kitchenette, douche et WC...");
  }
}
