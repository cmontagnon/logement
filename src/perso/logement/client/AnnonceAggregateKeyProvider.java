package perso.logement.client;

import com.sencha.gxt.data.shared.ModelKeyProvider;

public class AnnonceAggregateKeyProvider implements ModelKeyProvider<AnnonceAggregate> {
  @Override
  public String getKey(AnnonceAggregate model) {
    return model.getKey();
  }
}
