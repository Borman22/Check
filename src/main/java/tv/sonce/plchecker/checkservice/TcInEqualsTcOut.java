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

public class TcInEqualsTcOut implements IParticularFeatureChecker {

    private final String PATH_TO_PROPERTY = "properties/PLChecker/checkerservice/TcInEqualsTcOut.properties";

    @Override
    public Map<String, Integer> checkFeature(PLKeeper plKeeper) throws IOException {

        Properties properties = new Properties();
//        properties.load(new FileReader(PATH_TO_PROPERTY));
        InputStream fin = ClassLoader.getSystemResourceAsStream(PATH_TO_PROPERTY);
        properties.load(fin);
        fin.close();

        final String FEATURE_NAME = properties.getProperty("FEATURE_NAME");
        final String ERROR_MESSAGE = properties.getProperty("ERROR_MESSAGE");
        final int TC_DEVIATION = Integer.parseInt(properties.getProperty("TC_DEVIATION"));

        int numOfErrors = 0;

        for(List<Event> currentProgram : plKeeper.getAllProgramsList()){
            for(int i = 0; i < currentProgram.size() - 1; i++){

                int tempTcOut = currentProgram.get(i).getTcOut();                                       // because subclip that starts from 0 can write tcOut == 0 as well;
                if(currentProgram.get(i).getTcIn() == 0 & currentProgram.get(i).getTcOut() == 0)        //
                    tempTcOut = currentProgram.get(i).getDuration();                                    //

                if(!TimeCode.isTheSameTC(tempTcOut, currentProgram.get(i + 1).getTcIn(), TC_DEVIATION)) {
                    currentProgram.get(i + 1).errors.add(ERROR_MESSAGE + TimeCode.absoluteDifferenceToDelimitedStr(currentProgram.get(i + 1).getTcIn(), currentProgram.get(i).getTcOut()));
                    numOfErrors++;
                }
            }
        }

        Map<String, Integer> nameAndNumberOfErrors = new HashMap<>(1);
        nameAndNumberOfErrors.put(FEATURE_NAME, numOfErrors);
        return nameAndNumberOfErrors;
    }
}
