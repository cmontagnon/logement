package perso.logement.client;

import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface AnnonceAggregateProperties extends PropertyAccess<AnnonceAggregate> {
  @Path("key")
  ModelKeyProvider<AnnonceAggregate> key();

  ValueProvider<AnnonceAggregate, String> reference();

  ValueProvider<AnnonceAggregate, Double> superficie();

  ValueProvider<AnnonceAggregate, Short> arrondissement();

  ValueProvider<AnnonceAggregate, String> quartier();

  @Path("pricesAsString")
  ValueProvider<AnnonceAggregate, String> prices();

  ValueProvider<AnnonceAggregate, ImageResource> evolutionImage();

  @Path("pricesBySquareMeterAsString")
  ValueProvider<AnnonceAggregate, String> pricesBySquareMeter();

  ValueProvider<AnnonceAggregate, Double> meanPriceDifference();

  ValueProvider<AnnonceAggregate, ImageResource> meanPriceDifferenceImage();

  ValueProvider<AnnonceAggregate, String> seLogerLink();
}
