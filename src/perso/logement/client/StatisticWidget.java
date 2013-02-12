package perso.logement.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import perso.logement.client.dto.MonthStatisticDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.sencha.gxt.chart.client.chart.series.LineSeries;
import com.sencha.gxt.chart.client.chart.series.Primitives;
import com.sencha.gxt.chart.client.chart.series.SeriesHighlighter;
import com.sencha.gxt.chart.client.draw.Color;
import com.sencha.gxt.chart.client.draw.DrawFx;
import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.chart.client.draw.sprite.Sprite;
import com.sencha.gxt.chart.client.draw.sprite.TextSprite;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.fx.client.easing.BounceOut;
import com.sencha.gxt.widget.core.client.Portlet;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.container.PortalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.DateField;

public class StatisticWidget implements IsWidget {

  private static final Logger log = Logger.getLogger(StatisticWidget.class.getName());

  private ListBox arrondissementListBox = new ListBox();
  private ListBox quartierListBox = new ListBox();
  private DateField datePicker = new DateField();
  private ListStore<MonthStatisticDto> monthStatisticStore = new ListStore<MonthStatisticDto>(
      monthStatisticDtoProperties.key());
  private Chart<MonthStatisticDto> priceChart;
  private Chart<MonthStatisticDto> volumeChart;

  private static final String ALL_QUARTIERS = "all";

  private MonthStatisticQueryCommand monthStatisticQueryCommand = new MonthStatisticQueryCommand(this);

  private static final MonthStatisticDtoProperties monthStatisticDtoProperties = GWT
      .create(MonthStatisticDtoProperties.class);

  @Override
  public Widget asWidget() {
    PortalLayoutContainer portal = new PortalLayoutContainer(1);
    portal.getElement().getStyle().setBackgroundColor("white");

    Portlet queryPortlet = new Portlet();
    queryPortlet.setHeadingText("Query");
    configPanel(queryPortlet);
    queryPortlet.add(createQueryForm());
    queryPortlet.setHeight(250);
    queryPortlet.setWidth(900);
    portal.add(queryPortlet, 0);

    Portlet pricesBySquareMeterGraphPortlet = new Portlet();
    pricesBySquareMeterGraphPortlet.setHeadingText("Prix/m²");
    configPanel(pricesBySquareMeterGraphPortlet);
    pricesBySquareMeterGraphPortlet.add(createPricesBySquareMeterGraph());
    pricesBySquareMeterGraphPortlet.setHeight(250);
    portal.add(pricesBySquareMeterGraphPortlet, 0);

    Portlet volumesGraphPortlet = new Portlet();
    volumesGraphPortlet.setHeadingText("Volumes");
    configPanel(volumesGraphPortlet);
    volumesGraphPortlet.add(createVolumesGraph());
    volumesGraphPortlet.setHeight(250);
    portal.add(volumesGraphPortlet, 0);

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

    Button submitButton = createSubmitButton();
    vp.add(submitButton);

    return vp;
  }

  private Widget createVolumesGraph() {
    volumeChart = new Chart<MonthStatisticDto>();
    volumeChart.setStore(monthStatisticStore);
    volumeChart.setShadowChart(true);
    volumeChart.setAnimationDuration(750);
    volumeChart.setAnimationEasing(new BounceOut());

    NumericAxis<MonthStatisticDto> nbAnnonceAxis = new NumericAxis<MonthStatisticDto>();
    nbAnnonceAxis.setPosition(Position.LEFT);
    nbAnnonceAxis.addField(monthStatisticDtoProperties.nbAnnonces());
    TextSprite nbAnnoncesTitle = new TextSprite("Nb Annonces");
    //TextSprite title = new TextSprite("Prix/m²");
    nbAnnoncesTitle.setFontSize(18);
    nbAnnonceAxis.setTitleConfig(nbAnnoncesTitle);
    nbAnnonceAxis.setDisplayGrid(true);
    nbAnnonceAxis.setMinimum(0);
    volumeChart.addAxis(nbAnnonceAxis);

    CategoryAxis<MonthStatisticDto, String> catAxis = new CategoryAxis<MonthStatisticDto, String>();
    catAxis.setPosition(Position.BOTTOM);
    catAxis.setField(monthStatisticDtoProperties.period());
    TextSprite periodeTitle = new TextSprite("Periode");
    periodeTitle.setFontSize(18);
    catAxis.setTitleConfig(periodeTitle);
    TextSprite categoryLabelConfig = new TextSprite();
    categoryLabelConfig.setRotation(45);
    categoryLabelConfig.setTranslation(0, -30);
    catAxis.setLabelConfig(categoryLabelConfig);
    volumeChart.addAxis(catAxis);

    BarSeries<MonthStatisticDto> columnSeries = new BarSeries<MonthStatisticDto>();
    columnSeries.setYAxisPosition(Position.LEFT);
    columnSeries.addYField(monthStatisticDtoProperties.nbAnnonces());
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
    volumeChart.addSeries(columnSeries);

    Legend<MonthStatisticDto> legend = new Legend<MonthStatisticDto>();
    legend.setPosition(Position.RIGHT);
    legend.setItemHighlighting(true);
    legend.setItemHiding(true);
    volumeChart.setLegend(legend);
    volumeChart.setLayoutData(new VerticalLayoutData(1, 1));

    return volumeChart;
  }

  private Widget createPricesBySquareMeterGraph() {
    priceChart = new Chart<MonthStatisticDto>();
    priceChart.setStore(monthStatisticStore);
    priceChart.setShadowChart(true);
    priceChart.setAnimationDuration(750);
    priceChart.setAnimationEasing(new BounceOut());

    NumericAxis<MonthStatisticDto> priceAxis = new NumericAxis<MonthStatisticDto>();
    priceAxis.setPosition(Position.LEFT);
    priceAxis.addField(monthStatisticDtoProperties.priceByMeterSquare());
    TextSprite priceTitle = new TextSprite("Prix/m²");
    priceTitle.setFontSize(18);
    priceAxis.setTitleConfig(priceTitle);
    priceAxis.setDisplayGrid(true);
    priceChart.addAxis(priceAxis);

    CategoryAxis<MonthStatisticDto, String> catAxis = new CategoryAxis<MonthStatisticDto, String>();
    catAxis.setPosition(Position.BOTTOM);
    catAxis.setField(monthStatisticDtoProperties.period());
    TextSprite periodeTitle = new TextSprite("Periode");
    periodeTitle.setFontSize(18);
    catAxis.setTitleConfig(periodeTitle);
    TextSprite categoryLabelConfig = new TextSprite();
    categoryLabelConfig.setRotation(45);
    categoryLabelConfig.setTranslation(0, -30);
    catAxis.setLabelConfig(categoryLabelConfig);
    priceChart.addAxis(catAxis);

    LineSeries<MonthStatisticDto> lineSeries = new LineSeries<MonthStatisticDto>();
    lineSeries.setYAxisPosition(Position.LEFT);
    lineSeries.setStroke(RGB.RED);
    lineSeries.setShowMarkers(true);
    Sprite marker = Primitives.cross(0, 0, 6);
    marker.setFill(RGB.RED);
    lineSeries.setMarkerConfig(marker);
    lineSeries.setHighlighting(true);
    lineSeries.setSmooth(true);
    lineSeries.setYField(monthStatisticDtoProperties.priceByMeterSquare());
    priceChart.addSeries(lineSeries);

    Legend<MonthStatisticDto> legend = new Legend<MonthStatisticDto>();
    legend.setPosition(Position.RIGHT);
    legend.setItemHighlighting(true);
    legend.setItemHiding(true);
    priceChart.setLegend(legend);
    priceChart.setLayoutData(new VerticalLayoutData(1, 1));

    return priceChart;
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

        monthStatisticQueryCommand.getStatistics(datePicker.getValue(), arrondissementListBox
            .getValue(arrondissementListBox.getSelectedIndex()), ALL_QUARTIERS.equals(quartier) ? "" : quartier);
      }
    });
    return submitButton;
  }

  public void updateResult(List<MonthStatisticDto> monthStatistics) {
    removePendingStatus();
    monthStatisticStore.clear();

    // We refresh the graph
    for (MonthStatisticDto annonceStatistic : monthStatistics) {
      monthStatisticStore.add(annonceStatistic);
    }

    priceChart.redrawChart();
    volumeChart.redrawChart();
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
    priceChart.mask("Getting statistics");
    volumeChart.mask("Getting statistics");
  }

  private void removePendingStatus() {
    priceChart.unmask();
    volumeChart.unmask();
  }
}
