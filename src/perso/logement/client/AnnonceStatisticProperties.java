package perso.logement.client;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface AnnonceStatisticProperties extends PropertyAccess<AnnonceStatistic> {
  @Path("priceBySquareMeter")
  ModelKeyProvider<AnnonceStatistic> key();

  ValueProvider<AnnonceStatistic, Long> priceBySquareMeter();

  ValueProvider<AnnonceStatistic, Integer> nbAnnonces();
}
