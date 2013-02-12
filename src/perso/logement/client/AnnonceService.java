package perso.logement.client;

import java.util.Date;
import java.util.List;
import java.util.Map;

import perso.logement.client.dto.AnnonceDto;
import perso.logement.client.dto.MonthStatisticDto;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("getAnnonces")
public interface AnnonceService extends RemoteService {
  List<AnnonceDto> getAnnonces(Date startDate, String arrondissement, String quartier, Double surfaceMin,
      Double surfaceMax, Double priceMin, Double priceMax, String queryType);

  Map<Date, Double> getPriceEvolution(Date startDate, String arrondissement, String quartier);

  List<MonthStatisticDto> getStatistic(Date fromDate, String arrondissement, String quartier);
}
