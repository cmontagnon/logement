package perso.logement.client;


import com.extjs.gxt.ui.client.data.ModelKeyProvider;

public class AnnonceAggregateKeyProvider implements ModelKeyProvider<AnnonceAggregate> {
  @Override
  public String getKey(AnnonceAggregate model) {
    return model.getKey();
  }
}
