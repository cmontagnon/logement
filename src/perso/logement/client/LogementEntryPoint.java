package perso.logement.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import perso.logement.client.dto.AnnonceDto;
import perso.logement.client.resources.LogementResources;
import perso.logement.core.AnnonceEvolution;
import perso.logement.core.AnnonceKey;

import com.extjs.gxt.themes.client.Slate;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.ThemeManager;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;

public class LogementEntryPoint implements EntryPoint {

  private static final Logger log = Logger.getLogger(LogementEntryPoint.class.getName());

  private ListBox arrondissementListBox = new ListBox();
  private ListBox quartierListBox = new ListBox();
  private ListBox queryTypeListBox = new ListBox();
  private DateField datePicker = new DateField();
  private TextField<Integer> queryMeanPrice = new TextField<Integer>();
  private Grid<AnnonceAggregate> annonceGrid;
  private GroupingStore<AnnonceAggregate> store;

  private static final String ANNONCES_WITH_PRICE_CHANGE_ONLY = "priceFallOnly";
  private static final String ALL_ANNONCES = "all";
  private static final String ALL_QUARTIERS = "all";
  private AnnonceServiceAsync annonceService = GWT.create(AnnonceService.class);
  private LogementResources resources = GWT.create(LogementResources.class);

  private VerticalPanel vp;

  @Override
  public void onModuleLoad() {
    //Init GXT 
    ThemeManager.register(Slate.SLATE);
    GXT.setDefaultTheme(Slate.SLATE, true);

    vp = new VerticalPanel();
    vp.setAutoWidth(true);
    vp.setSpacing(10);

    createQueryForm();
    createResultForm();

    RootPanel.get().add(vp);
  }

  private void createResultForm() {
    HorizontalPanel queryMeanPricePanel = new HorizontalPanel();
    queryMeanPrice.setReadOnly(true);
    vp.add(queryMeanPricePanel);
    queryMeanPricePanel.add(new Label("Moyenne du prix au m² pour cette requête : "));
    queryMeanPricePanel.add(queryMeanPrice);

    vp.add(annonceGrid);
  }

  private void createQueryForm() {

    initArrondissementListBox();
    initQueryTypeListBox();
    initAnnonceTable();

    HorizontalPanel datePanel = new HorizontalPanel();
    vp.add(datePanel);
    datePanel.add(new Label("Date de debut : "));
    datePanel.add(datePicker);

    HorizontalPanel arrondissementPanel = new HorizontalPanel();
    vp.add(arrondissementPanel);
    arrondissementPanel.add(new Label("Arrondissement : "));
    arrondissementPanel.add(arrondissementListBox);

    HorizontalPanel quartierPanel = new HorizontalPanel();
    vp.add(quartierPanel);
    quartierPanel.add(new Label("Quartier : "));
    quartierPanel.add(quartierListBox);

    HorizontalPanel queryTypePanel = new HorizontalPanel();
    vp.add(queryTypePanel);
    queryTypePanel.add(new Label("Annonce evolution : "));
    queryTypePanel.add(queryTypeListBox);

    Button submitButton = createSubmitButton();
    vp.add(submitButton);
  }

  private void initAnnonceTable() {
    store = new GroupingStore<AnnonceAggregate>() {
      private Map<String, AnnonceAggregate> mapModel = new HashMap<String, AnnonceAggregate>();

      @Override
      protected void insert(List<? extends AnnonceAggregate> items, int index, boolean supressEvent) {
        for (AnnonceAggregate item : items) {
          mapModel.put(item.getKey(), item);
        }
        super.insert(items, index, supressEvent);
      }

      @Override
      public void remove(AnnonceAggregate model) {
        super.remove(model);
        mapModel.remove(model.getKey());
      }

      @Override
      public AnnonceAggregate findModel(AnnonceAggregate model) {
        return model;
      }

      @Override
      public AnnonceAggregate findModel(String key) {
        return mapModel.get(key);
      }
    };

    final ColumnModel columnModel = new ColumnModel(buildColumnConfig());
    store.setKeyProvider(new AnnonceAggregateKeyProvider());

    annonceGrid = new Grid<AnnonceAggregate>(store, columnModel);
    annonceGrid.setHeight(500); // TODO : comment faire autrement?
    annonceGrid.setView(buildView());
    annonceGrid.setBorders(true);
    annonceGrid.disableTextSelection(false);
  }

  /**
   * build store configuration with respect to ext-gwt format
   * 
   * @return List<ColumnConfig> list of column configuration
   */
  public List<ColumnConfig> buildColumnConfig() {
    List<ColumnConfig> columnsConfig = new ArrayList<ColumnConfig>();
    for (String columnName : getColumns()) {
      ColumnConfig columnConfig = new ColumnConfig(columnName, columnName, 60);

      GridCellRenderer<AnnonceAggregate> columnRenderer = new GridCellRenderer<AnnonceAggregate>() {
        @Override
        public Object render(AnnonceAggregate model, String property, ColumnData config, int rowIndex, int colIndex,
            ListStore<AnnonceAggregate> store, Grid<AnnonceAggregate> grid) {
          if (colIndex == 0) {
            return model.getReference();
          } else if (colIndex == 1) {
            return model.getSuperficie();
          } else if (colIndex == 2) {
            return model.getArrondissement();
          } else if (colIndex == 3) {
            return model.getQuartier();
          } else if (colIndex == 4) {
            return model.getPricesAsString();
          } else if (colIndex == 5) {
            AnnonceEvolution annonceEvolution = model.getAnnonceEvolution();
            switch (annonceEvolution) {
              case DOWN:
                return new Image(resources.down());
              case UP:
                return new Image(resources.up());
              default:
                break;
            }
            return annonceEvolution;
          } else if (colIndex == 6) {
            return model.getPricesBySquareMeterAsString();
          } else if (colIndex == 7) {
            return model.getMeanPriceDifference();
          } else if (colIndex == 8) {
            return model.getMeanPriceDifference() >= 0 ? new Image(resources.green()) : new Image(resources.red());
          } else if (colIndex == 9) {
            return new HTML("<a href=\"http://www.seloger.com/recherche.htm?ci=7501" + model.getArrondissement()
                + "&idtt=2&org=advanced_search&refannonce=" + model.getReference() + "\">seloger</a>");
          }
          return "";
        }
      };
      columnConfig.setRenderer(columnRenderer);
      columnsConfig.add(columnConfig);
    }
    return columnsConfig;
  }

  public GridView buildView() {
    GridView view = new GridView();
    view.setForceFit(true);
    return view;
  }

  private static List<String> getColumns() {
    List<String> res = new ArrayList<String>();
    res.add("Référence");
    res.add("Superficie");
    res.add("Arrondissement");
    res.add("Quartier");
    res.add("Prix");
    res.add("Evolution");
    res.add("Prix/m²");
    res.add("Différence à la moyenne (%)");
    res.add("Lien");
    res.add("");
    return res;
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
            store.removeAll();
            log.info("onSuccess called with a result size : " + result.size());
            Map<AnnonceKey, AnnonceAggregate> annonceAggregateByAnnonceKey =
                new HashMap<AnnonceKey, AnnonceAggregate>();
            for (AnnonceDto annonce : result) {
              AnnonceKey annonceKey =
                  new AnnonceKey(annonce.getReference(), annonce.getSuperficie(), annonce.getArrondissement(), annonce
                      .getQuartier());
              AnnonceAggregate annonceAggregate = null;
              if (annonceAggregateByAnnonceKey.containsKey(annonceKey)) {
                annonceAggregate = annonceAggregateByAnnonceKey.get(annonceKey);
              } else {
                annonceAggregate = new AnnonceAggregate();
                annonceAggregateByAnnonceKey.put(annonceKey, annonceAggregate);
              }
              annonceAggregate.addAnnonce(annonce);
            }

            // 1. We filter annonces with no price change if need be
            String queryTypeParameter = queryTypeListBox.getValue(queryTypeListBox.getSelectedIndex());
            List<AnnonceAggregate> annoncesToDisplay = new ArrayList<AnnonceAggregate>();
            if (queryTypeParameter == null || "".equals(queryTypeParameter)
                || ANNONCES_WITH_PRICE_CHANGE_ONLY.equals(queryTypeParameter)) {
              for (AnnonceAggregate annonceAggregateDto : annonceAggregateByAnnonceKey.values()) {
                if (annonceAggregateDto.hasMultiplePrices()) {
                  annoncesToDisplay.add(annonceAggregateDto);
                }
              }
            } else {
              annoncesToDisplay = new ArrayList<AnnonceAggregate>(annonceAggregateByAnnonceKey.values());
            }

            // 2. We compute the mean price by square meter value
            double currentPriceBySquareMeterSum = 0;
            int nbAnnoncesWithNullSuperficie = 0;
            for (AnnonceAggregate annonceAggregate : annoncesToDisplay) {
              List<Double> prices = annonceAggregate.getPrices();
              if (annonceAggregate.getSuperficie() != 0) {
                currentPriceBySquareMeterSum += (prices.get(prices.size() - 1)) / annonceAggregate.getSuperficie();
              } else {
                nbAnnoncesWithNullSuperficie++;
              }
            }
            int meanPriceBySquareMeter =
                (int) (currentPriceBySquareMeterSum / (annoncesToDisplay.size() - nbAnnoncesWithNullSuperficie));

            for (AnnonceAggregate annonceToDisplay : annoncesToDisplay) {
              annonceToDisplay.setQueryMeanPriceBySquareMeter(meanPriceBySquareMeter);
              store.add(annonceToDisplay);
            }

            queryMeanPrice.setValue(meanPriceBySquareMeter);

            log.info("Nb annonces that should be displayed in the table : " + annoncesToDisplay.size());
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
        annonceService.getAnnonces(datePicker.getValue(), arrondissementListBox.getValue(arrondissementListBox
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
