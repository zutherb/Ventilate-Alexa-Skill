package ventilate.service;


import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import ventilate.model.VantilationMeasure;

import java.util.Date;
import java.util.List;

public class ForecastService {
    private static double[] INITIAL_GUESS = {new Long(DateTime.now().toDate().getTime()).doubleValue(), 30000};

    public long getVentilationForecastInMinutes(List<VantilationMeasure> measures) {
        CurveFitter<ParametricUnivariateFunction> fitter = createCurveFitter(measures);
        ParametricUnivariateFunction function = new PolynomialFunction.Parametric();

        double[] fit = fitter.fit(function, INITIAL_GUESS);

        Date ventilationDate = calculateVentilationDate(function, fit);
        return Minutes.minutesBetween(DateTime.now(), new DateTime(ventilationDate)).getMinutes();
    }

    private Date calculateVentilationDate(ParametricUnivariateFunction sif, double[] fit) {
        return new Date(Double.valueOf(calculateVentilationTimeAsDouble(sif, fit)).longValue());
    }

    private double calculateVentilationTimeAsDouble(ParametricUnivariateFunction sif, double[] fit) {
        return sif.value(1000, fit);
    }

    private CurveFitter<ParametricUnivariateFunction> createCurveFitter(List<VantilationMeasure> measures) {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter(optimizer);

        addObservedPointsToFitter(measures, fitter);
        return fitter;
    }

    private void addObservedPointsToFitter(List<VantilationMeasure> measures, CurveFitter<ParametricUnivariateFunction> fitter) {
        measures.stream()
                .forEach(measure -> fitter.addObservedPoint(measure.getCO2(), measure.getTimestampAsDouble()));
    }
}
