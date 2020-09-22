package tv.sonce.plchecker.checkservice;

import tv.sonce.plchecker.PLKeeper;
import tv.sonce.plchecker.entity.Event;
import tv.sonce.utils.TimeCode;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

// Длительность рекламы в течении часа не должна привышать 9 минут
public class ReklamaDurationInHour implements IParticularFeatureChecker{

    private final String PATH_TO_PROPERTY = "properties/PLChecker/checkerservice/ReklamaDurationInHour.properties";

    @Override
    public Map<String, Integer> checkFeature(PLKeeper plKeeper) throws IOException {

        Properties properties = new Properties();
//        properties.load(new FileReader(PATH_TO_PROPERTY));
        InputStream fin = ClassLoader.getSystemResourceAsStream(PATH_TO_PROPERTY);
        properties.load(fin);
        fin.close();

        final String FEATURE_NAME = properties.getProperty("FEATURE_NAME");
        final String ERROR_MESSAGE = properties.getProperty("ERROR_MESSAGE");
        final int FRAMES_IN_HOUR = Integer.parseInt(properties.getProperty("FRAMES_IN_HOUR"));
        final int REKLAMA_DURATION_IN_HOUR = Integer.parseInt(properties.getProperty("REKLAMA_DURATION_IN_HOUR"));

        int numOfErrors = 0;

        int tempHour = -1;
        int tempDuration = 0;
        List<Event> reklamEventsInCurrentHour = new ArrayList<>(50);
        for (List<Event> reklamBlock : plKeeper.getAllReklamBloksList()) {
            for(Event reklamaEvent : reklamBlock) {
                if(reklamaEvent.getTime() / FRAMES_IN_HOUR != tempHour) {
                    if(tempDuration > REKLAMA_DURATION_IN_HOUR) {
                        for(Event event : reklamEventsInCurrentHour) {
                            event.errors.add(ERROR_MESSAGE + TimeCode.framesToDelimitedStr(tempDuration));
                        }
                        numOfErrors++;
                    }
                    tempDuration = reklamaEvent.getDuration();
                    tempHour = reklamaEvent.getTime() / FRAMES_IN_HOUR;
                    reklamEventsInCurrentHour = new ArrayList<>(50);
                    reklamEventsInCurrentHour.add(reklamaEvent);
                } else {
                    tempDuration += reklamaEvent.getDuration();
                    reklamEventsInCurrentHour.add(reklamaEvent);
                }
            }
        }

        // проверка для последнего рекламного блока
        if (tempDuration > REKLAMA_DURATION_IN_HOUR) {
            for (Event event : reklamEventsInCurrentHour) {
                event.errors.add(ERROR_MESSAGE + TimeCode.framesToDelimitedStr(tempDuration));
            }
            numOfErrors++;
        }

        Map<String, Integer> nameAndNumberOfErrors = new HashMap<>(1);
        nameAndNumberOfErrors.put(FEATURE_NAME, numOfErrors);
        return nameAndNumberOfErrors;
    }
}
