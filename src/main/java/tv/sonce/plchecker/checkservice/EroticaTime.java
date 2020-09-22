package tv.sonce.plchecker.checkservice;

import tv.sonce.plchecker.PLKeeper;
import tv.sonce.plchecker.entity.Event;
import tv.sonce.plchecker.entity.ProgramDescription;
import tv.sonce.utils.JsonReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class EroticaTime implements IParticularFeatureChecker {

    private final String PATH_TO_PROPERTY = "properties/PLChecker/checkerservice/EroticaTime.properties";

    @Override
    public Map<String, Integer> checkFeature(PLKeeper plKeeper) throws IOException {

        Properties properties = new Properties();
//        properties.load(new FileReader(PATH_TO_PROPERTY));
        InputStream fin = ClassLoader.getSystemResourceAsStream(PATH_TO_PROPERTY);
        properties.load(fin);
        fin.close();

        final String PATH_TO_ALL_PROGRAMS_DESCRIPTION = properties.getProperty("PATH_TO_ALL_PROGRAMS_DESCRIPTION");
        final String FEATURE_NAME = properties.getProperty("FEATURE_NAME");
        final String ERROR_MESSAGE = properties.getProperty("ERROR_MESSAGE");
        final String PROGRAM_TYPE = properties.getProperty("PROGRAM_TYPE");
        final int EROTICA_TIME_BEGIN = Integer.parseInt(properties.getProperty("EROTICA_TIME_BEGIN"));
        final int EROTICA_TIME_END = Integer.parseInt(properties.getProperty("EROTICA_TIME_END"));

        JsonReader<ProgramDescription[]> jsonReader = new JsonReader<>(PATH_TO_ALL_PROGRAMS_DESCRIPTION, new ProgramDescription[]{});
        ProgramDescription[] allProgramsDescriptions = jsonReader.getContent();

        int numOfErrors = 0;

        for (List<Event> program : plKeeper.getAllProgramsList()) {
            if (findProgramType(program.get(0), allProgramsDescriptions).matches(PROGRAM_TYPE)) {
                for(Event event : program) {
                    if(event.getTime() > EROTICA_TIME_END || event.getTime() + event.getDuration() > EROTICA_TIME_END){ // такое условие из-за того, что EROTICA_TIME_BEGIN == 0
                        event.errors.add(ERROR_MESSAGE);
                        numOfErrors++;
                    }
                }
            }
        }

        Map<String, Integer> nameAndNumberOfErrors = new HashMap<>(1);
        nameAndNumberOfErrors.put(FEATURE_NAME, numOfErrors);
        return nameAndNumberOfErrors;
    }

    private String findProgramType(Event event, ProgramDescription[] allProgramsDescriptions) {
        for (ProgramDescription currentProgramDescription : allProgramsDescriptions) {
            for (String currentProgramSynonym : currentProgramDescription.getProgramSynonyms()) {
                if (event.getCanonicalName().matches(currentProgramSynonym))
                    return currentProgramDescription.getProgramType();
            }
        }
        return "";
    }
}

