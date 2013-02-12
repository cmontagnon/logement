package perso.logement.client;

import java.util.Date;
import java.util.List;
import java.util.Map;

import perso.logement.client.dto.AnnonceDto;
import perso.logement.client.dto.MonthStatisticDto;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AnnonceServiceAsync {
  void getAnnonces(Date startDate, String arrondissement, String quartier, Double surfaceMin, Double surfaceMax,
      Double priceMin, Double priceMax, String queryType, AsyncCallback<List<AnnonceDto>> callback);

  void getPriceEvolution(Date startDate, String arrondissement, String quartier,
      AsyncCallback<Map<Date, Double>> callback);

  void getStatistic(Date fromDate, String arrondissement, String quartier,
      AsyncCallback<List<MonthStatisticDto>> callback);
}
