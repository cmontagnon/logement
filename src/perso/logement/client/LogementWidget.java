package perso.logement.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import perso.logement.client.dto.AnnonceDto;
import perso.logement.core.AnnonceKey;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.Portlet;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.container.PortalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;

public class LogementWidget implements IsWidget {

  private static final Logger log = Logger.getLogger(LogementWidget.class.getName());

  private ListBox arrondissementListBox = new ListBox();
  private ListBox quartierListBox = new ListBox();
  private ListBox queryTypeListBox = new ListBox();
  private DateField datePicker = new DateField();
  private TextField queryMeanPrice = new TextField();
  private Grid<AnnonceAggregate> annonceGrid;
  private ListStore<AnnonceAggregate> store;

  private static final String ANNONCES_WITH_PRICE_CHANGE_ONLY = "priceFallOnly";
  private static final String ALL_ANNONCES = "all";
  private static final String ALL_QUARTIERS = "all";
  private AnnonceServiceAsync annonceService = GWT.create(AnnonceService.class);

  private static final AnnonceAggregateProperties properties = GWT.create(AnnonceAggregateProperties.class);

  interface AnnonceAggregateProperties extends PropertyAccess<AnnonceAggregate> {
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

  @Override
  public Widget asWidget() {
    PortalLayoutContainer portal = new PortalLayoutContainer(1);
    portal.getElement().getStyle().setBackgroundColor("white");

    Portlet queryPortlet = new Portlet();
    queryPortlet.setHeadingText("Query");
    configPanel(queryPortlet);
    queryPortlet.add(createQueryForm());
    queryPortlet.setHeight(250);
    portal.add(queryPortlet, 0);

    Portlet resultPortlet = new Portlet();
    resultPortlet.setHeadingText("Result");
    configPanel(resultPortlet);
    resultPortlet.add(initAnnonceTable());
    resultPortlet.setHeight(500);
    portal.add(resultPortlet, 0);

    return portal;
  }

  private static void configPanel(final Portlet panel) {
    panel.setCollapsible(true);
    panel.setAnimCollapse(false);
    panel.getHeader().addTool(new ToolButton(ToolButton.GEAR));
    panel.getHeader().addTool(new ToolButton(ToolButton.CLOSE, new SelectHandler() {
      @Override
      public void onSelect(SelectEvent event) {
        panel.removeFromParent();
      }
    }));
  }

  private Widget createQueryForm() {
    VerticalPanel vp = new VerticalPanel();
    vp.setSpacing(10);

    initArrondissementListBox();
    initQueryTypeListBox();

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

    HorizontalPanel queryMeanPricePanel = new HorizontalPanel();
    queryMeanPrice.setReadOnly(true);
    vp.add(queryMeanPricePanel);
    queryMeanPricePanel.add(new Label("Moyenne du prix au m² pour cette requête : "));
    queryMeanPricePanel.add(queryMeanPrice);

    return vp;
  }

  private Widget initAnnonceTable() {
    final ColumnModel<AnnonceAggregate> columnModel = new ColumnModel<AnnonceAggregate>(buildColumnConfig());
    store = new ListStore<AnnonceAggregate>(properties.key());

    annonceGrid = new Grid<AnnonceAggregate>(store, columnModel);
    annonceGrid.setHeight(500); // TODO : comment faire autrement?
    annonceGrid.setView(buildView());
    annonceGrid.setBorders(true);
    return annonceGrid;
  }

  /**
   * build store configuration with respect to ext-gwt format
   * 
   * @return List<ColumnConfig> list of column configuration
   */
  public List<ColumnConfig<AnnonceAggregate, ?>> buildColumnConfig() {
    List<ColumnConfig<AnnonceAggregate, ?>> columnsConfig = new ArrayList<ColumnConfig<AnnonceAggregate, ?>>();
    ColumnConfig<AnnonceAggregate, String> cc1 =
        new ColumnConfig<AnnonceAggregate, String>(properties.reference(), 100, "Reference");
    ColumnConfig<AnnonceAggregate, Double> cc2 =
        new ColumnConfig<AnnonceAggregate, Double>(properties.superficie(), 100, "Superficie");
    ColumnConfig<AnnonceAggregate, Short> cc3 =
        new ColumnConfig<AnnonceAggregate, Short>(properties.arrondissement(), 100, "Arrondissement");
    ColumnConfig<AnnonceAggregate, String> cc4 =
        new ColumnConfig<AnnonceAggregate, String>(properties.quartier(), 100, "Quartier");
    ColumnConfig<AnnonceAggregate, String> cc5 =
        new ColumnConfig<AnnonceAggregate, String>(properties.prices(), 100, "Prix");
    ColumnConfig<AnnonceAggregate, ImageResource> cc6 =
        new ColumnConfig<AnnonceAggregate, ImageResource>(properties.evolutionImage(), 100, "Evolution");
    cc6.setCell(new ImageResourceCell());
    ColumnConfig<AnnonceAggregate, String> cc7 =
        new ColumnConfig<AnnonceAggregate, String>(properties.pricesBySquareMeter(), 100, "Prix/m²");
    ColumnConfig<AnnonceAggregate, Double> cc8 =
        new ColumnConfig<AnnonceAggregate, Double>(properties.meanPriceDifference(), 100, "Différence à la moyenne");
    ColumnConfig<AnnonceAggregate, ImageResource> cc9 =
        new ColumnConfig<AnnonceAggregate, ImageResource>(properties.meanPriceDifferenceImage(), 100, "");
    cc9.setCell(new ImageResourceCell());
    ColumnConfig<AnnonceAggregate, String> cc10 =
        new ColumnConfig<AnnonceAggregate, String>(properties.seLogerLink(), 100, "Lien");
    cc10.setCell(new AbstractCell<String>() {
      @Override
      public void render(Context context, String value, SafeHtmlBuilder sb) {
        if (value != null) {
          sb.append(SafeHtmlUtils.fromTrustedString(value));
        }
      }
    });

    columnsConfig.add(cc1);
    columnsConfig.add(cc2);
    columnsConfig.add(cc3);
    columnsConfig.add(cc4);
    columnsConfig.add(cc5);
    columnsConfig.add(cc6);
    columnsConfig.add(cc7);
    columnsConfig.add(cc8);
    columnsConfig.add(cc9);
    columnsConfig.add(cc10);
    return columnsConfig;
  }

  public GridView<AnnonceAggregate> buildView() {
    GridView<AnnonceAggregate> view = new GridView<AnnonceAggregate>();
    view.setForceFit(true);
    return view;
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
            store.clear();
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

            queryMeanPrice.setValue(String.valueOf(meanPriceBySquareMeter));

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
