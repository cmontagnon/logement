package perso.logement.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import perso.logement.client.dto.AnnonceDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;

public class AnnonceQueryCommand {

  private static final Logger log = Logger.getLogger(AnnonceQueryCommand.class.getName());

  private static AnnonceServiceAsync annonceService = GWT.create(AnnonceService.class);
  private List<AnnonceDto> toReturn = new ArrayList<AnnonceDto>();
  private LogementWidget widget;

  public AnnonceQueryCommand(LogementWidget widget) {
    super();
    this.widget = widget;
  }

  public void getAnnonces(Date startDate, String arrondissement, String quartier, Double surfaceMin, Double surfaceMax,
      Double priceMin, Double priceMax, String queryType) {
    AsyncCallback<List<AnnonceDto>> callback = new AsyncCallback<List<AnnonceDto>>() {

      @Override
      public void onSuccess(List<AnnonceDto> result) {
        log.info("onSuccess called with a result size : " + result.size());
        toReturn.clear();
        toReturn.addAll(result);
        widget.updateResult(toReturn);
      }

      @Override
      public void onFailure(Throwable caught) {
        DialogBox dialogBox = new DialogBox();
        dialogBox.setText("Failure : " + caught.getMessage());
        dialogBox.show();
      }
    };
    log.info("call annonce service to get Annonces");
    annonceService.getAnnonces(startDate, arrondissement, quartier, surfaceMin, surfaceMax, priceMin, priceMax,
        queryType, callback);
    widget.displayPendingStatus();
  }
}
