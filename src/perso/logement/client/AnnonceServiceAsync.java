package perso.logement.client;

import java.util.Date;
import java.util.List;

import perso.logement.client.dto.AnnonceDto;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AnnonceServiceAsync {
  void getAnnonces(Date startDate, String arrondissement, String quartier, String queryType,
      AsyncCallback<List<AnnonceDto>> callback);
}
