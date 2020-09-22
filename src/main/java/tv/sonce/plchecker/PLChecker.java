package tv.sonce.plchecker;

import tv.sonce.plchecker.checkservice.IParticularFeatureChecker;
import tv.sonce.plchecker.entity.Event;

import java.io.*;
import java.util.*;

public class PLChecker {

    private final String PATH_PROPERTY = "./properties/PLChecker/PLChecker.properties";

    public void checkPL() throws IOException {

        PLKeeper plKeeper = new PLKeeper();
        Map<String, Integer> nameAndNumberOfErrors = new HashMap<>();

        for (IParticularFeatureChecker particularFeatureChecker : ServiceLoader.load(IParticularFeatureChecker.class))
            nameAndNumberOfErrors.putAll(particularFeatureChecker.checkFeature(plKeeper));

        for (Event event : plKeeper.getAllEventsList()) {
            System.out.printf("%-3d%-130s", event.getNumberOfEvent(), event);
            if(event.errors.size() != 0)
                System.out.println(".  " + event.errors);
            else System.out.println(".");
        }


        System.out.println();
        for (Map.Entry<String, Integer> entry : nameAndNumberOfErrors.entrySet()) {
            if(entry.getValue() != 0)
            System.out.println("Ошибок в " + entry.getKey() + " = " + entry.getValue());
        }
    }
}
