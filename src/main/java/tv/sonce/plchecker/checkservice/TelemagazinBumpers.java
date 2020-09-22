package tv.sonce.plchecker.checkservice;

import tv.sonce.plchecker.PLKeeper;
import tv.sonce.plchecker.entity.Event;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// Проверяет, что в начале и конце телемагазина есть отбивки
public class TelemagazinBumpers implements IParticularFeatureChecker {

    private final String PATH_TO_PROPERTY = "properties/PLChecker/checkerservice/TelemagazinBumpers.properties";

    @Override
    public Map<String, Integer> checkFeature(PLKeeper plKeeper) throws IOException {

        Properties properties = new Properties();
//        properties.load(new FileReader(PATH_TO_PROPERTY));
        InputStream fin = ClassLoader.getSystemResourceAsStream(PATH_TO_PROPERTY);
        properties.load(fin);
        fin.close();

        final String FEATURE_NAME = properties.getProperty("FEATURE_NAME");
        final String ERROR_MESSAGE_FIRST = properties.getProperty("ERROR_MESSAGE_FIRST");
        final String ERROR_MESSAGE_LAST = properties.getProperty("ERROR_MESSAGE_LAST");

        final String[] BUMPERS = properties.getProperty("BUMPERS").split("&");

        int numOfErrors = 0;

        for (List<Event> telemagazinBlock : plKeeper.getAllTelemagazinBloksList()) {
            if(!isThisABumper(telemagazinBlock.get(0).getCanonicalName(), BUMPERS)){
                telemagazinBlock.get(0).errors.add(ERROR_MESSAGE_FIRST);
                numOfErrors++;
            }

            if(!isThisABumper(telemagazinBlock.get(telemagazinBlock.size() - 1).getCanonicalName(), BUMPERS)){
                telemagazinBlock.get(telemagazinBlock.size() - 1).errors.add(ERROR_MESSAGE_LAST);
                numOfErrors++;
            }
        }

        Map<String, Integer> nameAndNumberOfErrors = new HashMap<>(1);
        nameAndNumberOfErrors.put(FEATURE_NAME, numOfErrors);
        return nameAndNumberOfErrors;
    }

    private boolean isThisABumper(String bumperName, String[] allBumpers) {
        bumperName = bumperName.toLowerCase();
        for(String currentBumper : allBumpers) {
            if (bumperName.matches(currentBumper))
                return true;
        }

        return false;
    }
}
