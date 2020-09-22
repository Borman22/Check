package tv.sonce.plchecker.checkservice;

import tv.sonce.plchecker.PLKeeper;
import tv.sonce.plchecker.entity.Event;
import tv.sonce.utils.TimeCode;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// Проверяет, что длительность телемагазина не меньше N минут
public class TelemagazinDuration implements IParticularFeatureChecker {

    private final String PATH_TO_PROPERTY = "properties/PLChecker/checkerservice/TelemagazinDuration.properties";

    @Override
    public Map<String, Integer> checkFeature(PLKeeper plKeeper) throws IOException {
        Properties properties = new Properties();
//        properties.load(new FileReader(PATH_TO_PROPERTY));
        InputStream fin = ClassLoader.getSystemResourceAsStream(PATH_TO_PROPERTY);
        properties.load(fin);
        fin.close();

        final String FEATURE_NAME = properties.getProperty("FEATURE_NAME");
        final String ERROR_MESSAGE = properties.getProperty("ERROR_MESSAGE");
        final int TELEMAGAZIN_DURATION = Integer.parseInt(properties.getProperty("TELEMAGAZIN_DURATION"));

        int numOfErrors = 0;

        for (List<Event> telemagazinBlock : plKeeper.getAllTelemagazinBloksList()) {
            if(TimeCode.TCDifferenceConsideringMidnight(telemagazinBlock.get(telemagazinBlock.size() - 1).getTime()
                    + telemagazinBlock.get(telemagazinBlock.size() - 1).getDuration(), telemagazinBlock.get(0).getTime()) < TELEMAGAZIN_DURATION){
                for(Event event : telemagazinBlock) {
                    event.errors.add(ERROR_MESSAGE);
                }
                numOfErrors++;
            }
        }

        Map<String, Integer> nameAndNumberOfErrors = new HashMap<>(1);
        nameAndNumberOfErrors.put(FEATURE_NAME, numOfErrors);
        return nameAndNumberOfErrors;
    }
}
