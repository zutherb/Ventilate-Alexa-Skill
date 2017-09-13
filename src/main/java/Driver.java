import ventilate.model.VantilationMeasure;
import ventilate.service.ForecastService;
import ventilate.service.MeasurementService;

import java.util.List;


public class Driver {


    public static void main(String[] args) {
        MeasurementService measurementService = new MeasurementService();
        List<VantilationMeasure> measures = measurementService.getMeasuresOfTheLast30Minutes();

        ForecastService forecastService = new ForecastService();
        System.out.println(forecastService.getVentilationForecastInMinutes(measures));

    }
}
