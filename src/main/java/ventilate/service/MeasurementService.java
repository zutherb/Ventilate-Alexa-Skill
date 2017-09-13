package ventilate.service;

import losty.netatmo.NetatmoHttpClient;
import losty.netatmo.model.Params;
import losty.netatmo.model.Station;
import org.apache.commons.lang3.Validate;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.joda.time.DateTime;
import ventilate.model.VantilationMeasure;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MeasurementService {

    private static final String NETATMO_CLIENT_ID = "NETATMO_CLIENT_ID";
    private static final String NETATMO_CLIENT_SECRET = "NETATMO_CLIENT_SECRET";
    private static final String NETATMO_ACCOUNT_EMAIL = "NETATMO_ACCOUNT_EMAIL";
    private static final String NETATMO_ACCOUNT_PASSWORT = "NETATMO_ACCOUNT_PASSWORT";

    private static List<String> DEFAULT_TYPES = Arrays.asList(Params.TYPE_TEMPERATURE, Params.TYPE_PRESSURE, Params.TYPE_HUMIDITY, Params.TYPE_CO2);

    private final String clientId;
    private final String clientSecret;
    private final String accountEmail;
    private final String accountPassword;


    public MeasurementService() {
        clientId = System.getenv(NETATMO_CLIENT_ID);
        Validate.notNull(clientId, "Please set enviorment variable '%s'", NETATMO_CLIENT_ID);
        clientSecret = System.getenv(NETATMO_CLIENT_SECRET);
        Validate.notNull(clientSecret, "Please set enviorment variable '%s'", NETATMO_CLIENT_SECRET);
        accountEmail = System.getenv(NETATMO_ACCOUNT_EMAIL);
        Validate.notNull(accountEmail, "Please set enviorment variable '%s'", NETATMO_ACCOUNT_EMAIL);
        accountPassword = System.getenv(NETATMO_ACCOUNT_PASSWORT);
        Validate.notNull(accountPassword, "Please set enviorment variable '%s'", NETATMO_ACCOUNT_PASSWORT);
    }

    public List<VantilationMeasure> getMeasuresOfTheLast30Minutes() {
        try {
            NetatmoHttpClient client = new NetatmoHttpClient(clientId, clientSecret);
            OAuthJSONAccessTokenResponse token = client.login(accountEmail, accountPassword);
            List<Station> devicesList = client.getDevicesList(token);
            Station station = devicesList.get(0);

            Date dateBegin = DateTime.now()
                    .minusMinutes(30)
                    .toDate();
            Date dateEnd = DateTime.now()
                    .toDate();
            return client.getMeasures(token,
                    station,
                    station.getModules().get(0),
                    DEFAULT_TYPES,
                    Params.SCALE_MAX,
                    dateBegin,
                    dateEnd,
                    null,
                    null)
                    .stream()
                    .map(measure -> new VantilationMeasure(measure.getCO2(), new Date(measure.getBeginTime())))
                    .collect(Collectors.toList());
        } catch (OAuthSystemException | OAuthProblemException e) {
            throw new RuntimeException(e);
        }
    }

}
