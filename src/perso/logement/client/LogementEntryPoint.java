package perso.logement.client;

import static java.math.BigDecimal.ROUND_HALF_UP;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import perso.logement.client.dto.AnnonceAggregateDto;
import perso.logement.client.dto.AnnonceDto;
import perso.logement.core.AnnonceKey;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

public class LogementEntryPoint implements EntryPoint {

  private static final Logger log = Logger.getLogger(LogementEntryPoint.class.getName());

  private ListBox arrondissementListBox = new ListBox();
  private ListBox quartierListBox = new ListBox();
  private ListBox queryTypeListBox = new ListBox();
  private TextBox startDateBox = new TextBox();
  private CellTable<AnnonceAggregateDto> annonceTable = new CellTable<AnnonceAggregateDto>();
  private SimplePager pager;

  private static final String ANNONCES_WITH_PRICE_CHANGE_ONLY = "priceFallOnly";
  private static final String ALL_ANNONCES = "all";
  private static final String ALL_QUARTIERS = "all";
  private AnnonceServiceAsync annonceService = GWT.create(AnnonceService.class);

  @Override
  public void onModuleLoad() {
    initArrondissementListBox();
    initQueryTypeListBox();
    initAnnonceTable();

    HorizontalPanel datePanel = new HorizontalPanel();
    RootPanel.get().add(datePanel);
    datePanel.add(new Label("Date de debut : "));
    datePanel.add(startDateBox);

    HorizontalPanel arrondissementPanel = new HorizontalPanel();
    RootPanel.get().add(arrondissementPanel);
    arrondissementPanel.add(new Label("Arrondissement : "));
    arrondissementPanel.add(arrondissementListBox);

    HorizontalPanel quartierPanel = new HorizontalPanel();
    RootPanel.get().add(quartierPanel);
    quartierPanel.add(new Label("Quartier : "));
    quartierPanel.add(quartierListBox);

    HorizontalPanel queryTypePanel = new HorizontalPanel();
    RootPanel.get().add(queryTypePanel);
    queryTypePanel.add(new Label("Annonce evolution : "));
    queryTypePanel.add(queryTypeListBox);

    Button submitButton = createSubmitButton();
    RootPanel.get().add(submitButton);
    RootPanel.get().add(annonceTable);
    RootPanel.get().add(pager);
  }

  private void initAnnonceTable() {
    TextColumn<AnnonceAggregateDto> referenceColumn = new TextColumn<AnnonceAggregateDto>() {
      @Override
      public String getValue(AnnonceAggregateDto annonce) {
        return annonce.getReference();
      }
    };
    TextColumn<AnnonceAggregateDto> arrondissementColumn = new TextColumn<AnnonceAggregateDto>() {
      @Override
      public String getValue(AnnonceAggregateDto annonce) {
        return Short.toString(annonce.getArrondissement());
      }
    };
    TextColumn<AnnonceAggregateDto> superficieColumn = new TextColumn<AnnonceAggregateDto>() {
      @Override
      public String getValue(AnnonceAggregateDto annonce) {
        return Double.toString(annonce.getSuperficie());
      }
    };
    TextColumn<AnnonceAggregateDto> quartierColumn = new TextColumn<AnnonceAggregateDto>() {
      @Override
      public String getValue(AnnonceAggregateDto annonce) {
        return annonce.getQuartier();
      }
    };
    TextColumn<AnnonceAggregateDto> pricesColumn = new TextColumn<AnnonceAggregateDto>() {
      @Override
      public String getValue(AnnonceAggregateDto annonce) {
        return annonce.getPricesAsString();
      }
    };

    TextColumn<AnnonceAggregateDto> pricesBySquareMeterColumn = new TextColumn<AnnonceAggregateDto>() {
      @Override
      public String getValue(AnnonceAggregateDto annonce) {
        return annonce.getPricesBySquareMeterAsString();
      }
    };

    TextColumn<AnnonceAggregateDto> meanPriceDifferenceColumn = new TextColumn<AnnonceAggregateDto>() {
      @Override
      public String getValue(AnnonceAggregateDto annonce) {
        List<Long> pricesBySquareMeter = annonce.getPricesBySquareMeter();
        double meanTagPercentage = 0;
        if (!pricesBySquareMeter.isEmpty()) { // It may be empty when the superficie is equal to 0
          Long lastPriceBySquareMeter = pricesBySquareMeter.get(pricesBySquareMeter.size() - 1);
          if (annonce.getQueryMeanPriceBySquareMeter() != 0) {
            meanTagPercentage =
                round(
                    100 * new Double(lastPriceBySquareMeter - annonce.getQueryMeanPriceBySquareMeter())
                        / annonce.getQueryMeanPriceBySquareMeter(), 2, ROUND_HALF_UP);
          } else {
            meanTagPercentage = 0;
          }
        }
        return String.valueOf(meanTagPercentage);
      }

      private double round(double x, int scale, int roundingMethod) {
        try {
          return (new BigDecimal(Double.toString(x)).setScale(scale, roundingMethod)).doubleValue();
        } catch (NumberFormatException ex) {
          if (Double.isInfinite(x)) {
            return x;
          } else {
            return Double.NaN;
          }
        }
      }
    };

    annonceTable.addColumn(referenceColumn, "Réference");
    annonceTable.addColumn(arrondissementColumn, "Superficie");
    annonceTable.addColumn(superficieColumn, "Arrondissement");
    annonceTable.addColumn(quartierColumn, "Quartier");
    annonceTable.addColumn(pricesColumn, "Prix");
    annonceTable.addColumn(pricesBySquareMeterColumn, "Prix/m²");
    annonceTable.addColumn(meanPriceDifferenceColumn, "Différence à la moyenne(en %)");

    SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
    pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
    pager.setDisplay(annonceTable);
    pager.setPageSize(100);
  }

  private Button createSubmitButton() {
    // TODO : refactor
    Button submitButton = new Button("Submit");
    submitButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        AsyncCallback<List<AnnonceDto>> callback = new AsyncCallback<List<AnnonceDto>>() {

          @Override
          public void onSuccess(List<AnnonceDto> result) {

            log.info("onSuccess called with a result size : " + result.size());
            Map<AnnonceKey, AnnonceAggregateDto> annonceAggregateByAnnonceKey =
                new HashMap<AnnonceKey, AnnonceAggregateDto>();
            for (AnnonceDto annonce : result) {
              AnnonceKey annonceKey =
                  new AnnonceKey(annonce.getReference(), annonce.getSuperficie(), annonce.getArrondissement(), annonce
                      .getQuartier());
              AnnonceAggregateDto annonceAggregate = null;
              if (annonceAggregateByAnnonceKey.containsKey(annonceKey)) {
                annonceAggregate = annonceAggregateByAnnonceKey.get(annonceKey);
              } else {
                annonceAggregate = new AnnonceAggregateDto();
                annonceAggregateByAnnonceKey.put(annonceKey, annonceAggregate);
              }
              annonceAggregate.addAnnonce(annonce);
            }

            // 1. We filter annonces with no price change if need be
            String queryTypeParameter = queryTypeListBox.getValue(queryTypeListBox.getSelectedIndex());
            List<AnnonceAggregateDto> annoncesToDisplay = new ArrayList<AnnonceAggregateDto>();
            if (queryTypeParameter == null || "".equals(queryTypeParameter)
                || ANNONCES_WITH_PRICE_CHANGE_ONLY.equals(queryTypeParameter)) {
              for (AnnonceAggregateDto annonceAggregateDto : annonceAggregateByAnnonceKey.values()) {
                if (annonceAggregateDto.hasMultiplePrices()) {
                  annoncesToDisplay.add(annonceAggregateDto);
                }
              }
            } else {
              annoncesToDisplay = new ArrayList<AnnonceAggregateDto>(annonceAggregateByAnnonceKey.values());
            }

            // 2. We compute the mean price by square meter value
            double currentPriceBySquareMeterSum = 0;
            int nbAnnoncesWithNullSuperficie = 0;
            for (AnnonceAggregateDto annonceAggregate : annoncesToDisplay) {
              List<Double> prices = annonceAggregate.getPrices();
              if (annonceAggregate.getSuperficie() != 0) {
                currentPriceBySquareMeterSum += (prices.get(prices.size() - 1)) / annonceAggregate.getSuperficie();
              } else {
                nbAnnoncesWithNullSuperficie++;
              }
            }
            int meanPriceBySquareMeter =
                (int) (currentPriceBySquareMeterSum / (annoncesToDisplay.size() - nbAnnoncesWithNullSuperficie));

            for (AnnonceAggregateDto annonceToDisplay : annoncesToDisplay) {
              annonceToDisplay.setQueryMeanPriceBySquareMeter(meanPriceBySquareMeter);
            }

            log.info("Nb annonces that should be displayed in the table : " + annoncesToDisplay.size());
            annonceTable.setRowCount(annoncesToDisplay.size(), true);
            annonceTable.setRowData(0, annoncesToDisplay);
          }

          @Override
          public void onFailure(Throwable caught) {
            DialogBox dialogBox = new DialogBox();
            dialogBox.setText("Failure : " + caught.getMessage());
            dialogBox.show();
          }
        };
        log.info("call annonce service to get Annonces");
        String quartier = quartierListBox.getValue(quartierListBox.getSelectedIndex());
        annonceService.getAnnonces(startDateBox.getText(), arrondissementListBox.getValue(arrondissementListBox
            .getSelectedIndex()), ALL_QUARTIERS.equals(quartier) ? "" : quartier, queryTypeListBox
            .getValue(queryTypeListBox.getSelectedIndex()), callback);
      }
    });
    return submitButton;
  }

  private void initQueryTypeListBox() {
    queryTypeListBox.addItem("Seulement celles dont le prix change", ANNONCES_WITH_PRICE_CHANGE_ONLY);
    queryTypeListBox.addItem("All", ALL_ANNONCES);
  }

  private ListBox initArrondissementListBox() {
    arrondissementListBox.addItem("");
    List<Short> arrondissementsList = new ArrayList<Short>(SeLogerUtils.arrondissements.keySet());
    Collections.sort(arrondissementsList);
    for (Short arrondissement : arrondissementsList) {
      arrondissementListBox.addItem(arrondissement.toString());
    }

    arrondissementListBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        quartierListBox.clear();
        quartierListBox.addItem("All", ALL_QUARTIERS);
        String selectedArrondissement = arrondissementListBox.getValue(arrondissementListBox.getSelectedIndex());
        if (selectedArrondissement != null && !"".equals(selectedArrondissement)) {
          Map<String, String> quartierById = SeLogerUtils.arrondissements.get(Short.valueOf(selectedArrondissement));
          for (Map.Entry<String, String> entry : quartierById.entrySet()) {
            quartierListBox.addItem(entry.getValue(), entry.getKey());
          }
        }
      }
    });
    return arrondissementListBox;
  }
}
