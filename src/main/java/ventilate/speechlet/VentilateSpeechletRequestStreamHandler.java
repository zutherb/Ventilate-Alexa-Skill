package ventilate.speechlet;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;


public class VentilateSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {

    private static final Set<String> supportedApplicationIds;

    static {
 
        supportedApplicationIds = new HashSet<String>();
        
    }

    public VentilateSpeechletRequestStreamHandler() {
        super(new VentilateSpeechlet(), supportedApplicationIds);
    }

    public VentilateSpeechletRequestStreamHandler(Speechlet speechlet,
                                                  Set<String> supportedApplicationIds) {
        super(speechlet, supportedApplicationIds);
    }

}
