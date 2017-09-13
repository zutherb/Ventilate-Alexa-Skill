package ventilate.speechlet;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import ventilate.model.VantilationMeasure;
import ventilate.service.ForecastService;
import ventilate.service.MeasurementService;

public class VentilateSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(VentilateSpeechlet.class);
    private static final String VENTILATE_INTENT = "ventilateIntent";
    private static final String SLOT_ROOM = "room";

    private MeasurementService measurementService = new MeasurementService();
    private ForecastService forecastService = new ForecastService();

    public VentilateSpeechlet() {
        measurementService = new MeasurementService();
        forecastService = new ForecastService();
    }

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session) throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("Willkommen bei Lüftung.");
        return SpeechletResponse.newAskResponse(speech, createRepromptSpeech());
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        log.info("Session:" + session + " Intent:" + request.getIntent().getName());

        String intentName = request.getIntent().getName();
        if (VENTILATE_INTENT.equals(intentName)) {
            return handleVentilateRequest(request.getIntent(), session);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return handleHelpIntent();
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            return handleStopIntent();
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session) throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    private SpeechletResponse handleStopIntent() {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("auf wiedersehen.");
        return SpeechletResponse.newTellResponse(speech);
    }

    private SpeechletResponse handleHelpIntent() {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("In welchem Raum soll ich messen?");
        return SpeechletResponse.newTellResponse(speech);
    }

    private SpeechletResponse handleVentilateRequest(Intent intent, Session session) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

        List<VantilationMeasure> measures = measurementService.getMeasuresOfTheLast30Minutes();

        Long forecastInMinutes = forecastService.getVentilationForecastInMinutes(measures);

        if (forecastInMinutes < -10) {
            speech.setText("Nein, du brauchst nicht zu lüften.");
        }
        if (forecastInMinutes >= -10 && forecastInMinutes < 15) {
            speech.setText("Ja, du solltest lüften!");
        }
        if (forecastInMinutes >= 15) {
            double co2Value = (measures.isEmpty()) ? 0 : measures.get(measures.size() - 1).getCO2();
            if (forecastInMinutes < 60){
                speech.setText(String.format("Du brauchst nicht zu lüften, denn der aktuelle CO2 Anteil beträgt %.1f ppm. Aber in %d Minuten solltest du mich wieder fragen!", co2Value, forecastInMinutes));
            } else {
                long forecastInHours = forecastInMinutes / 60;
                if (forecastInHours <= 1) {
                    speech.setText(String.format("Du brauchst nicht zu lüften, denn der aktuelle CO2 Anteil beträgt %.1f ppm. Aber in einer Stunde solltest du mich wieder fragen!", co2Value));
                } else {
                    speech.setText(String.format("Du brauchst nicht zu lüften, denn der aktuelle CO2 Anteil beträgt %.1f ppm. Aber in %d Stunden solltest du mich wieder fragen!", co2Value, forecastInHours));
                }
            }
        }
        return SpeechletResponse.newAskResponse(speech, createRepromptSpeech());
    }

    private Reprompt createRepromptSpeech() {
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText("ich habe dich nicht verstanden");
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);
        return reprompt;
    }
}