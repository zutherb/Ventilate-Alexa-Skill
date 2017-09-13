package ventilate.model;

import org.apache.commons.lang3.Validate;

import java.util.Date;

public class VantilationMeasure {

    private double CO2;
    private Date timestamp;

    public VantilationMeasure(double CO2, Date timestamp) {
        Validate.notNull(CO2);
        Validate.notNull(timestamp);

        this.CO2 = CO2;
        this.timestamp = timestamp;
    }

    public double getCO2() {
        return CO2;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public double getTimestampAsDouble() {
        return Long.valueOf(timestamp.getTime()).doubleValue();
    }
}
