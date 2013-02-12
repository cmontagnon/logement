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
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.chart.client.chart.Chart.Position;
import com.sencha.gxt.chart.client.chart.Legend;
import com.sencha.gxt.chart.client.chart.axis.CategoryAxis;
import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
import com.sencha.gxt.chart.client.chart.series.BarSeries;
import com.sencha.gxt.chart.client.chart.series.SeriesHighlighter;
import com.sencha.gxt.chart.client.draw.Color;
import com.sencha.gxt.chart.client.draw.DrawFx;
import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.chart.client.draw.sprite.Sprite;
import com.sencha.gxt.chart.client.draw.sprite.TextSprite;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.fx.client.easing.BounceOut;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.Portlet;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.container.PortalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.grid.RowExpander;

public class LogementWidget implements IsWidget {

  private static final Logger log = Logger.getLogger(LogementWidget.class.getName());

  private ListBox arrondissementListBox = new ListBox();
  private ListBox quartierListBox = new ListBox();
  private ListBox queryTypeListBox = new ListBox();
  private DateField datePicker = new DateField();
  private TextField queryMeanPrice = new TextField();
  private TextField surfaceMinTextBox = new TextField();
  private TextField surfaceMaxTextBox = new TextField();
  private TextField priceMinTextBox = new TextField();
  private TextField priceMaxTextBox = new TextField();

  private Grid<AnnonceAggregate> annonceGrid;
  private ListStore<AnnonceAggregate> annonceStore;
  private AnnonceAggregateStore annonceAggregateStore = new AnnonceAggregateStore();
  private ListStore<AnnonceStatistic> annonceStatisticStore;
  private GridView<AnnonceAggregate> view;
  private Chart<AnnonceStatistic> chart;
  private RowExpander<AnnonceAggregate> expander;

  private static final String ANNONCES_WITH_PRICE_CHANGE_ONLY = "priceFallOnly";
  private static final String ALL_ANNONCES = "all";
  private static final String ALL_QUARTIERS = "all";

  private AnnonceQueryCommand annonceQueryCommand = new AnnonceQueryCommand(this);

  private static final AnnonceAggregateProperties annonceAggregateProperties = GWT
      .create(AnnonceAggregateProperties.class);
  private static final AnnonceStatisticProperties annonceStatisticProperties = GWT
      .create(AnnonceStatisticProperties.class);

  @Override
  public Widget asWidget() {
    PortalLayoutContainer portal = new PortalLayoutContainer(1);
    portal.getElement().getStyle().setBackgroundColor("white");

    Portlet queryPortlet = new Portlet();
    queryPortlet.setHeadingText("Query");
    configPanel(queryPortlet);
    queryPortlet.add(createQueryForm());
    queryPortlet.setHeight(250);
    queryPortlet.setWidth(300);
    portal.add(queryPortlet, 0);

    Portlet annoncesPortlet = new Portlet();
    annoncesPortlet.setHeadingText("Annonces");
    configPanel(annoncesPortlet);
    annoncesPortlet.add(createAnnonceTable());
    annoncesPortlet.setHeight(500);
    queryPortlet.setWidth(300);
    portal.add(annoncesPortlet, 0);

    Portlet statPortlet = new Portlet();
    statPortlet.setHeadingText("Statistics");
    configPanel(statPortlet);
    statPortlet.add(createResultsStatisticsWidget());
    statPortlet.setHeight(100);
    portal.add(statPortlet, 0);

    Portlet pricesBySquareMeterGraphPortlet = new Portlet();
    pricesBySquareMeterGraphPortlet.setHeadingText("Graphs");
    configPanel(pricesBySquareMeterGraphPortlet);
    pricesBySquareMeterGraphPortlet.add(createPricesBySquareMeterGraph());
    pricesBySquareMeterGraphPortlet.setHeight(500);
    portal.add(pricesBySquareMeterGraphPortlet, 0);

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

    HorizontalPanel localisationPanel = new HorizontalPanel();
    vp.add(localisationPanel);
    localisationPanel.add(new Label("Arrondissement : "));
    localisationPanel.add(arrondissementListBox);
    localisationPanel.add(new Label("Quartier : "));
    localisationPanel.add(quartierListBox);

    HorizontalPanel surfacePanel = new HorizontalPanel();
    vp.add(surfacePanel);
    surfacePanel.add(new Label("Surface min : "));
    surfacePanel.add(surfaceMinTextBox);
    surfacePanel.add(new Label("Surface max : "));
    surfacePanel.add(surfaceMaxTextBox);

    HorizontalPanel pricePanel = new HorizontalPanel();
    vp.add(pricePanel);
    pricePanel.add(new Label("Prix min : "));
    pricePanel.add(priceMinTextBox);
    pricePanel.add(new Label("Prix max : "));
    pricePanel.add(priceMaxTextBox);

    HorizontalPanel queryTypePanel = new HorizontalPanel();
    vp.add(queryTypePanel);
    queryTypePanel.add(new Label("Annonce evolution : "));
    queryTypePanel.add(queryTypeListBox);

    Button submitButton = createSubmitButton();
    vp.add(submitButton);

    return vp;
  }

  private Widget createAnnonceTable() {
    final ColumnModel<AnnonceAggregate> columnModel = new ColumnModel<AnnonceAggregate>(buildColumnConfig());
    annonceStore = new ListStore<AnnonceAggregate>(annonceAggregateProperties.key());

    annonceGrid = new Grid<AnnonceAggregate>(annonceStore, columnModel);
    annonceGrid.setHeight(500); // TODO : comment faire autrement?
    annonceGrid.setView(buildView());
    annonceGrid.setBorders(true);
    expander.initPlugin(annonceGrid);

    return annonceGrid;
  }

  private Widget createResultsStatisticsWidget() {
    HorizontalPanel queryMeanPricePanel = new HorizontalPanel();
    queryMeanPrice.setReadOnly(true);
    queryMeanPricePanel.add(new Label("Moyenne du prix au m² pour cette requête : "));
    queryMeanPricePanel.add(queryMeanPrice);
    return queryMeanPricePanel;
  }

  private static ContentPanel createContentPanel(String title, Widget... widgetsToAdd) {
    ContentPanel panel = new FramedPanel();
    panel.getElement().getStyle().setMargin(10, Unit.PX);

    panel.setCollapsible(true);
    panel.setHeadingText(title);
    //panel.setPixelSize(620, 500);
    panel.setBodyBorder(true);
    panel.setBodyStyleName("white-bg");

    VerticalLayoutContainer layout = new VerticalLayoutContainer();
    layout.setBorders(true);
    panel.add(layout);
    for (Widget widgetToAdd : widgetsToAdd) {
      layout.add(widgetToAdd);
    }
    return panel;
  }

  private Widget createPricesBySquareMeterGraph() {
    annonceStatisticStore = new ListStore<AnnonceStatistic>(annonceStatisticProperties.key());

    chart = new Chart<AnnonceStatistic>();
    chart.setStore(annonceStatisticStore);
    chart.setShadowChart(true);
    chart.setAnimationDuration(750);
    chart.setAnimationEasing(new BounceOut());

    NumericAxis<AnnonceStatistic> axis = new NumericAxis<AnnonceStatistic>();
    axis.setPosition(Position.LEFT);
    axis.addField(annonceStatisticProperties.nbAnnonces());
    TextSprite title = new TextSprite("Nombre d'annonces");
    title.setFontSize(18);
    axis.setTitleConfig(title);
    axis.setDisplayGrid(true);
    axis.setMinimum(0);
    chart.addAxis(axis);

    CategoryAxis<AnnonceStatistic, Long> catAxis = new CategoryAxis<AnnonceStatistic, Long>();
    catAxis.setPosition(Position.BOTTOM);
    catAxis.setField(annonceStatisticProperties.priceBySquareMeter());
    title = new TextSprite("Prix/m²");
    title.setFontSize(18);
    catAxis.setTitleConfig(title);
    TextSprite categoryLabelConfig = new TextSprite();
    categoryLabelConfig.setRotation(45);
    categoryLabelConfig.setTranslation(0, -30);
    catAxis.setLabelConfig(categoryLabelConfig);
    chart.addAxis(catAxis);

    BarSeries<AnnonceStatistic> columnSeries = new BarSeries<AnnonceStatistic>();
    columnSeries.setYAxisPosition(Position.LEFT);
    columnSeries.addYField(annonceStatisticProperties.nbAnnonces());
    columnSeries.addColor(new RGB(76, 153, 0));
    columnSeries.setColumn(true);
    columnSeries.setHighlighting(true);
    columnSeries.setHighlighter(new SeriesHighlighter() {
      @Override
      public void highlight(Sprite sprite) {
        sprite.setStroke(new RGB(85, 85, 204));
        DrawFx.createStrokeWidthAnimator(sprite, 10).run(250);
      }

      @Override
      public void unHighlight(Sprite sprite) {
        sprite.setStroke(Color.NONE);
        DrawFx.createStrokeWidthAnimator(sprite, 0).run(250);
      }
    });
    chart.addSeries(columnSeries);

    Legend<AnnonceStatistic> legend = new Legend<AnnonceStatistic>();
    legend.setPosition(Position.RIGHT);
    legend.setItemHighlighting(true);
    legend.setItemHiding(true);
    chart.setLegend(legend);
    chart.setLayoutData(new VerticalLayoutData(1, 1));

    ContentPanel panel =
        createContentPanel("Répartition (toutes les annonces y compris celles dont le prix ne change pas)", chart);

    return panel;
  }

  /**
   * build annonceStore configuration with respect to ext-gwt format
   * 
   * @return List<ColumnConfig> list of column configuration
   */
  private List<ColumnConfig<AnnonceAggregate, ?>> buildColumnConfig() {
    List<ColumnConfig<AnnonceAggregate, ?>> columnsConfig = new ArrayList<ColumnConfig<AnnonceAggregate, ?>>();
    ColumnConfig<AnnonceAggregate, String> cc1 =
        new ColumnConfig<AnnonceAggregate, String>(annonceAggregateProperties.reference(), 100, "Reference");
    ColumnConfig<AnnonceAggregate, Double> cc2 =
        new ColumnConfig<AnnonceAggregate, Double>(annonceAggregateProperties.superficie(), 100, "Superficie");
    ColumnConfig<AnnonceAggregate, Short> cc3 =
        new ColumnConfig<AnnonceAggregate, Short>(annonceAggregateProperties.arrondissement(), 100, "Arrondissement");
    ColumnConfig<AnnonceAggregate, String> cc4 =
        new ColumnConfig<AnnonceAggregate, String>(annonceAggregateProperties.quartier(), 100, "Quartier");
    ColumnConfig<AnnonceAggregate, Double> cc5 =
        new ColumnConfig<AnnonceAggregate, Double>(annonceAggregateProperties.lastPrice(), 100, "Prix");
    ColumnConfig<AnnonceAggregate, ImageResource> cc6 =
        new ColumnConfig<AnnonceAggregate, ImageResource>(annonceAggregateProperties.evolutionImage(), 100, "Evolution");
    cc6.setCell(new ImageResourceCell());
    ColumnConfig<AnnonceAggregate, Long> cc7 =
        new ColumnConfig<AnnonceAggregate, Long>(annonceAggregateProperties.lastPriceBySquareMeter(), 100, "Prix/m²");
    ColumnConfig<AnnonceAggregate, Double> cc8 =
        new ColumnConfig<AnnonceAggregate, Double>(annonceAggregateProperties.meanPriceDifference(), 100,
            "Différence à la moyenne");
    ColumnConfig<AnnonceAggregate, ImageResource> cc9 =
        new ColumnConfig<AnnonceAggregate, ImageResource>(annonceAggregateProperties.meanPriceDifferenceImage(), 100,
            "");
    cc9.setCell(new ImageResourceCell());
    ColumnConfig<AnnonceAggregate, String> cc10 =
        new ColumnConfig<AnnonceAggregate, String>(annonceAggregateProperties.seLogerLink(), 100, "Lien");
    cc10.setCell(new AbstractCell<String>() {
      @Override
      public void render(Context context, String value, SafeHtmlBuilder sb) {
        if (value != null) {
          sb.append(SafeHtmlUtils.fromTrustedString(value));
        }
      }
    });

    IdentityValueProvider<AnnonceAggregate> identity = new IdentityValueProvider<AnnonceAggregate>();

    expander = new RowExpander<AnnonceAggregate>(identity, new AbstractCell<AnnonceAggregate>() {
      @Override
      public void render(Context context, AnnonceAggregate value, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<table style='border:1px solid black;border-collapse:collapse;'>");
        sb.appendHtmlConstant("<tr>");
        sb.appendHtmlConstant("<td style='border:1px solid black;'><b>Date</b></td>");
        sb.appendHtmlConstant("<td style='border:1px solid black;'><b>Prix</b></td>");
        sb.appendHtmlConstant("</tr>");
        for (AnnonceDto annonce : value.getAnnonces()) {
          sb.appendHtmlConstant("<tr>");
          sb.appendHtmlConstant("<td style='border:1px solid black;'>" + annonce.getFormattedDate() + "</td>");
          sb.appendHtmlConstant("<td style='border:1px solid black;'>" + annonce.getPrix() + "</td>");
          sb.appendHtmlConstant("</tr>");
        }
        sb.appendHtmlConstant("</table>");
      }
    });

    columnsConfig.add(expander);
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
    view = new GridView<AnnonceAggregate>();
    view.setForceFit(true);
    return view;
  }

  private Button createSubmitButton() {
    Button submitButton = new Button("Submit");
    submitButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String quartier = ALL_QUARTIERS;
        if (quartierListBox.getSelectedIndex() != -1) {
          quartier = quartierListBox.getValue(quartierListBox.getSelectedIndex());
        }

        Double surfaceMin = null;
        if (surfaceMinTextBox.getText() != null && surfaceMinTextBox.getText().length() != 0) {
          surfaceMin = Double.parseDouble(surfaceMinTextBox.getText());
        }

        Double surfaceMax = null;
        if (surfaceMaxTextBox.getText() != null && surfaceMaxTextBox.getText().length() != 0) {
          surfaceMax = Double.parseDouble(surfaceMaxTextBox.getText());
        }

        Double priceMin = null;
        if (priceMinTextBox.getText() != null && priceMinTextBox.getText().length() != 0) {
          priceMin = Double.parseDouble(priceMinTextBox.getText());
        }

        Double priceMax = null;
        if (priceMaxTextBox.getText() != null && priceMaxTextBox.getText().length() != 0) {
          priceMax = Double.parseDouble(priceMaxTextBox.getText());
        }

        annonceQueryCommand.getAnnonces(datePicker.getValue(), arrondissementListBox.getValue(arrondissementListBox
            .getSelectedIndex()), ALL_QUARTIERS.equals(quartier) ? "" : quartier, surfaceMin, surfaceMax, priceMin,
            priceMax, queryTypeListBox.getValue(queryTypeListBox.getSelectedIndex()));
      }
    });
    return submitButton;
  }

  public void updateResult(List<AnnonceDto> annonces) {
    removePendingStatus();
    annonceStore.clear();
    annonceStatisticStore.clear();
    annonceAggregateStore.clear();
    Map<AnnonceKey, AnnonceAggregate> annonceAggregateByAnnonceKey = new HashMap<AnnonceKey, AnnonceAggregate>();
    for (AnnonceDto annonce : annonces) {
      AnnonceKey annonceKey =
          new AnnonceKey(annonce.getReference(), annonce.getSuperficie(), annonce.getArrondissement(),
              annonce.getQuartier());
      AnnonceAggregate annonceAggregate = null;
      if (annonceAggregateByAnnonceKey.containsKey(annonceKey)) {
        annonceAggregate = annonceAggregateByAnnonceKey.get(annonceKey);
      } else {
        annonceAggregate = new AnnonceAggregate();
        annonceAggregateByAnnonceKey.put(annonceKey, annonceAggregate);
      }
      annonceAggregate.addAnnonce(annonce);
    }

    annonceAggregateStore.addAnnonceAggregates(annonceAggregateByAnnonceKey.values());

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
    for (AnnonceAggregate annonceAggregate : annonceAggregateByAnnonceKey.values()) {
      List<Double> prices = annonceAggregate.getPrices();
      if (annonceAggregate.getSuperficie() != 0) {
        currentPriceBySquareMeterSum += (prices.get(prices.size() - 1)) / annonceAggregate.getSuperficie();
      } else {
        nbAnnoncesWithNullSuperficie++;
      }
    }
    int meanPriceBySquareMeter =
        (int) (currentPriceBySquareMeterSum / (annonceAggregateByAnnonceKey.size() - nbAnnoncesWithNullSuperficie));

    for (AnnonceAggregate annonceToDisplay : annoncesToDisplay) {
      annonceToDisplay.setQueryMeanPriceBySquareMeter(meanPriceBySquareMeter);
      if (annonceToDisplay.getLastPrice() > 0) {
        annonceStore.add(annonceToDisplay);
      }
    }

    // We refresh the graph
    List<AnnonceStatistic> annonceStatistics = annonceAggregateStore.getAnnonceStatistics();
    for (AnnonceStatistic annonceStatistic : annonceStatistics) {
      annonceStatisticStore.add(annonceStatistic);
    }

    queryMeanPrice.setValue(String.valueOf(meanPriceBySquareMeter));
    view.refresh(true);
    chart.redrawChart();
    log.info("Nb annonces that should be displayed in the table : " + annoncesToDisplay.size());
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

  public void displayPendingStatus() {
    annonceGrid.mask("Getting annonces");
    chart.mask("Getting annonces");
  }

  private void removePendingStatus() {
    annonceGrid.unmask();
    chart.unmask();
  }
}
