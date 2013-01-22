package perso.logement.client;

import java.util.List;

import perso.logement.client.dto.AnnonceDto;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("getAnnonces")
public interface AnnonceService extends RemoteService {
  List<AnnonceDto> getAnnonces(String startDate, String arrondissement, String quartier, String queryType);
}
