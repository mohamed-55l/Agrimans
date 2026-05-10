package modules.parcelle.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import modules.parcelle.services.ParcelleService;

import java.util.List;

public class StatistiqueController {

    @FXML
    private BarChart<String, Number> chartLocalisation;

    @FXML
    private NumberAxis yAxis;

    private ParcelleService service = new ParcelleService();

    @FXML
    public void initialize() {

        // 🔴 désactiver l'échelle automatique
        yAxis.setAutoRanging(false);

        // 🔴 afficher seulement des entiers
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(10);   // valeur max possible
        yAxis.setTickUnit(1);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Parcelles");

        List<Object[]> data = service.getParcellesParLocalisation();

        for (Object[] row : data) {

            String localisation = (String) row[0];
            int total = ((Number) row[1]).intValue();

            series.getData().add(new XYChart.Data<>(localisation, total));
        }

        chartLocalisation.getData().add(series);
    }
}