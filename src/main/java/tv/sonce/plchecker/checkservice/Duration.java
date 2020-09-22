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

public class Duration implements IParticularFeatureChecker {

    private final String PATH_TO_PROPERTY = "properties/PLChecker/checkerservice/Duration.properties";

    @Override
    public Map<String, Integer> checkFeature(PLKeeper plKeeper) throws IOException {

        Properties properties = new Properties();

        InputStream fin = ClassLoader.getSystemResourceAsStream(PATH_TO_PROPERTY);
        properties.load(fin);
        fin.close();

        final String FEATURE_NAME = properties.getProperty("FEATURE_NAME");
        final String ERROR_MESSAGE = properties.getProperty("ERROR_MESSAGE");
        final int TIME_END_PL = TimeCode.delimitedStrToFrames(properties.getProperty("TIME_END_PL"));

        int numOfErrors = 0;

        List<Event> allEvents = plKeeper.getAllEventsList();
        for(int i = 0; i < allEvents.size() - 1; i++){

            // Проверка 1: если (TcIn != 0) || (TcOut != 0), то считаем durationInOut;
            int durationInOut = (allEvents.get(i).getTcIn() != 0 || allEvents.get(i).getTcOut() != 0) ? allEvents.get(i).getTcOut() - allEvents.get(i).getTcIn() : allEvents.get(i).getDuration();

            // Проверка 2: считаем differenceBetweenStarts
            int differenceBetweenStarts = TimeCode.TCDifferenceConsideringMidnight(allEvents.get(i + 1).getTime(), allEvents.get(i).getTime());

            if(durationInOut != differenceBetweenStarts || differenceBetweenStarts != allEvents.get(i).getDuration()){
                allEvents.get(i).errors.add(ERROR_MESSAGE);
                numOfErrors++;
            }
        }

        // последнее событие обрабатываем отдельно - точнее не обрабатываем вообще
//        Event lastEvent = allEvents.get(allEvents.size() - 1);
//        int durationInOut = (lastEvent.getTcIn() != 0 || lastEvent.getTcOut() != 0) ? lastEvent.getTcOut() - lastEvent.getTcIn() : lastEvent.getDuration();
//
//        // Проверка 2: считаем differenceBetweenStarts
//        int differenceBetweenStarts = TimeCode.TCDifferenceConsideringMidnight(TIME_END_PL, lastEvent.getTime());
//
//        if(durationInOut != differenceBetweenStarts || differenceBetweenStarts != lastEvent.getDuration()){
//            lastEvent.errors.add(ERROR_MESSAGE);
//            numOfErrors++;
//        }


        Map<String, Integer> nameAndNumberOfErrors = new HashMap<>(1);
        nameAndNumberOfErrors.put(FEATURE_NAME, numOfErrors);
        return nameAndNumberOfErrors;
    }
}
