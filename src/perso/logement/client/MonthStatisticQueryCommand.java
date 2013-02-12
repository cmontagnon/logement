package perso.logement.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import perso.logement.client.dto.MonthStatisticDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;

public class MonthStatisticQueryCommand {

  private static final Logger log = Logger.getLogger(MonthStatisticQueryCommand.class.getName());

  private static AnnonceServiceAsync annonceService = GWT.create(AnnonceService.class);
  private List<MonthStatisticDto> toReturn = new ArrayList<MonthStatisticDto>();
  private StatisticWidget widget;

  public MonthStatisticQueryCommand(StatisticWidget widget) {
    super();
    this.widget = widget;
  }

  public void getStatistics(Date fromDate, String arrondissement, String quartier) {
    AsyncCallback<List<MonthStatisticDto>> callback = new AsyncCallback<List<MonthStatisticDto>>() {

      @Override
      public void onSuccess(List<MonthStatisticDto> result) {
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
    log.info("call annonce service to get MonthStatistics");
    annonceService.getStatistic(fromDate, arrondissement, quartier, callback);
    widget.displayPendingStatus();
  }
}
