package tv.sonce.plchecker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tv.sonce.plchecker.entity.Event;
import tv.sonce.plchecker.entity.ProgramDescription;
import tv.sonce.utils.JsonReader;
import tv.sonce.utils.TimeCode;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

// Этот класс парсит плейлисты, которые находятся в исходном виде, т.е. в таком, который получаем от редактора
public class PLKeeper {

    private final String PATH_TO_PROPERTY = "properties/PLChecker/PLKeeper.properties";
    private final String PATH_TO_NOT_CONSIDERED_A_PROGRAM;

    private List<Event> allEventsList;
    private List<List<Event>> allProgramsList;
    private List<List<Event>> reklamBloksList;
    private List<List<Event>> telemagazinBloksList;

    private final String PATH_TO_PL;
    private final int MAX_NUM_OF_PROGRAMS;
    private final int MAX_REKLAMA_DURATION;
    private final int MIN_EVENTS_IN_REKLAM_BLOCK;
    private final String TELEMAGAZIN_BUMPER_TEMPLATE;
    private final String TELEMAGAZIN_TEMPLATE;
    private final String[] REDUNDANT_EVENT_TEMPLATE;
    private final int TC_DEVIATION;
    // Tag Names
    private final String EVENT;
    private final String DATE;
    private final String TIME;
    private final String DURATION;
    private final String TC_IN;
    private final String TC_OUT;
    private final String ASSET_ID;
    private final String NAME;



    public PLKeeper() throws IOException {

        InputStream fin = ClassLoader.getSystemResourceAsStream(PATH_TO_PROPERTY);
        Properties properties = new Properties();
        properties.load(fin);
        fin.close();

        this.PATH_TO_PL = properties.getProperty("PATH_TO_PL");
        this.MAX_NUM_OF_PROGRAMS = Integer.parseInt(properties.getProperty("MAX_NUM_OF_PROGRAMS"));
        this.MAX_REKLAMA_DURATION = Integer.parseInt(properties.getProperty("MAX_REKLAMA_DURATION"));
        this.MIN_EVENTS_IN_REKLAM_BLOCK = Integer.parseInt(properties.getProperty("MIN_EVENTS_IN_REKLAM_BLOCK"));
        this.PATH_TO_NOT_CONSIDERED_A_PROGRAM = properties.getProperty("PATH_TO_NOT_CONSIDERED_A_PROGRAM");
        this.TC_DEVIATION = Integer.parseInt(properties.getProperty("TC_DEVIATION"));

        this.TELEMAGAZIN_BUMPER_TEMPLATE = properties.getProperty("TELEMAGAZIN_BUMPER_TEMPLATE");
        this.TELEMAGAZIN_TEMPLATE = properties.getProperty("TELEMAGAZIN_TEMPLATE");
        this.REDUNDANT_EVENT_TEMPLATE = properties.getProperty("REDUNDANT_EVENT_TEMPLATE").split("&");

        this.EVENT = properties.getProperty("EVENT");
        this.DATE = properties.getProperty("DATE");
        this.TIME = properties.getProperty("TIME");
        this.DURATION = properties.getProperty("DURATION");
        this.TC_IN = properties.getProperty("TC_IN");
        this.TC_OUT = properties.getProperty("TC_OUT");
        this.ASSET_ID = properties.getProperty("ASSET_ID");
        this.NAME = properties.getProperty("NAME");

        fillAllEventsList(Objects.requireNonNull(parsePl()));
        fillAllProgramsList(allEventsList);
        fillReklamBloksListAndTelemagazinBlockList(allEventsList, allProgramsList);
    }

    public List<Event> getAllEventsList() {
        return allEventsList;
    }

    public List<List<Event>> getAllProgramsList() {
        return allProgramsList;
    }

    public List<List<Event>> getAllReklamBloksList() {
        return reklamBloksList;
    }

    public List<List<Event>> getAllTelemagazinBloksList() {
        return telemagazinBloksList;
    }


    private NodeList parsePl() {
        try {
            File theLatestFile = getLastestFile(PATH_TO_PL);
            if(theLatestFile == null) {
                System.out.println("Не удалось найти папку " + PATH_TO_PL);
                return null;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(theLatestFile);
            doc.getDocumentElement().normalize();
            return doc.getElementsByTagName(EVENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void fillAllEventsList(NodeList nodeList) {
        allEventsList = new ArrayList<>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nNode = nodeList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                addNode(i, (Element) nNode, allEventsList);
            }
        }
    }

    private void fillAllProgramsList(List<Event> allEventsList) throws IOException {

        allProgramsList = new ArrayList<>(MAX_NUM_OF_PROGRAMS);

        // программа - событие дольше MAX_REKLAMA_DURATION и не в списке событий, которые не считаются программой
        // или меньше MAX_REKLAMA_DURATION, но начинается не с 00:00:00 и имя совпадает с предыдущими субклипами
        // субклип начинается с 00:00:00 - первый субклип
        // если начинается не с 00:00:00, но имя или таймкод совпадает с предыдущим - следующий субклип
        // ни имя ни таймкод не совпадает с предыдущими субклипами - новое событие, которое начинается не с 00:00:00

        List<String> allSynonymsNotAPrograms = new ArrayList<>();

//        Gson gson = new Gson();
//        FileReader fr = new FileReader(PATH_TO_NOT_CONSIDERED_A_PROGRAM);
//        ProgramDescription[] notConsideredAProgram = gson.fromJson(fr, ProgramDescription[].class);

        JsonReader<ProgramDescription[]> jsonReader = new JsonReader<>(PATH_TO_NOT_CONSIDERED_A_PROGRAM, new ProgramDescription[]{});
        ProgramDescription[] notConsideredAProgram = jsonReader.getContent();

        for (ProgramDescription notAProgramDescription : notConsideredAProgram) {
            allSynonymsNotAPrograms.addAll(Arrays.asList(notAProgramDescription.getProgramSynonyms()));
        }

        List<Event> program = new ArrayList<>();
        Event old_event = allEventsList.get(0);
        for (Event event : allEventsList) {
            if ((event.getDuration() > MAX_REKLAMA_DURATION || event.getTcIn() != 0) && !isMatchesStrWithRegexList(event.getCanonicalName(), allSynonymsNotAPrograms)) {
                if (event.getTcIn() == 0) { // начинается с 00:00:00
                    program = new ArrayList<>();
                    allProgramsList.add(program);
                    program.add(event);
                    old_event = event;
                } else {    // начинается не с 00:00:00
                    if(!TimeCode.isTheSameTC(old_event.getTcOut(), event.getTcIn(), TC_DEVIATION) && !old_event.isTheSameName(event.getCanonicalName())) {
                        program = new ArrayList<>();
                        allProgramsList.add(program);
                        for(int i = old_event.getNumberOfEvent(); i < event.getNumberOfEvent() - 1; i++){
                            if(TimeCode.isTheSameTC(allEventsList.get(i).getTcOut(), event.getTcIn(), TC_DEVIATION) || allEventsList.get(i).isTheSameName(event.getCanonicalName())){
                                program.add(allEventsList.get(i));
                                break;
                            }
                        }
                    }
                    program.add(event);
                    old_event = event;
                }
            }
        }
    }

    private void fillReklamBloksListAndTelemagazinBlockList(List<Event> allEventsList, List<List<Event>> allProgramsList){

        reklamBloksList = new ArrayList<>(3 * MAX_NUM_OF_PROGRAMS);
        telemagazinBloksList = new ArrayList<>();

        // Рекламный блок состоит из N реклам и отбивок. Располагается между субклипами или программами. Не содержит отбивку на тлемагазин или T- (телемагазин) или A- (анонс)
        // Телемагазинный блок состоит из N телемагазинов и отбивок. Содержит отбивку на телемагазин и T-. Не содержит А- (анонс)
        List<Event> tempBlock;
        Event previousEvent = allEventsList.get(0);

        for(List<Event> currentProgram : allProgramsList){
            for(Event subclip : currentProgram){
                tempBlock = new ArrayList<>();
                for(int i = previousEvent.getNumberOfEvent(); i < subclip.getNumberOfEvent() - 1; i++)
                    tempBlock.add(allEventsList.get(i));
                previousEvent = subclip;

                if(tempBlock.size() <= MIN_EVENTS_IN_REKLAM_BLOCK)
                    continue;

                // исключим все избыточные события - анонсы, отбивки, новости...
                excludeRedundantEvents(tempBlock, REDUNDANT_EVENT_TEMPLATE);

                int numberOfTelemagazin = 0;
                for(Event event : tempBlock) {
                    if(event.getName().matches(TELEMAGAZIN_BUMPER_TEMPLATE) || event.getName().matches(TELEMAGAZIN_TEMPLATE))
                        numberOfTelemagazin++;
                }
                if(tempBlock.size() / 2 < numberOfTelemagazin)
                    telemagazinBloksList.add(tempBlock);
                else
                    reklamBloksList.add(tempBlock);
            }
        }
    }

    private void excludeRedundantEvents(List<Event> tempBlock, String[] redundantEvents){
        Iterator<Event> tempBlockIterator = tempBlock.iterator();
        while(tempBlockIterator.hasNext()){
            Event tempEvent = tempBlockIterator.next();
            for(String redundantEventTemplate : redundantEvents)
                if(tempEvent.getName().matches(redundantEventTemplate))
                    tempBlockIterator.remove();
        }
    }

    private boolean isMatchesStrWithRegexList(String str, List<String> regexList) {
        for (String regex : regexList) {
            if (str.matches(regex))
                return true;
        }
        return false;
    }

    private File getLastestFile(String path){
        // Находим файл, который создан позже всех и возвращаем его из метода
        File[] folderEntries;
        folderEntries = new File(path).listFiles();
        File lastestFile = null;

        if(folderEntries == null)
            return null;

        long tempTimeFileCreate = 0;
        for (File currentFile:folderEntries) {
            if (tempTimeFileCreate < currentFile.lastModified()) {
                tempTimeFileCreate = currentFile.lastModified();
                lastestFile = currentFile;
            }
        }
        return lastestFile;
    }

    private void addNode(int numberOfEvent, Element element, List<Event> listOfEvents) {
        Event tempEvent = new Event(
                numberOfEvent + 1,  // События считаются с 1, но хранятся в массиве с 0
                element.getAttribute(DATE),
                element.getAttribute(TIME),
                element.getAttribute(DURATION),
                element.getChildNodes().item(1).getAttributes().getNamedItem(TC_IN) == null ? null : element.getChildNodes().item(1).getAttributes().getNamedItem(TC_IN).getTextContent().substring(0, 11),
                element.getChildNodes().item(1).getAttributes().getNamedItem(TC_OUT) == null ? null : element.getChildNodes().item(1).getAttributes().getNamedItem(TC_OUT).getTextContent().substring(0, 11),
                element.getChildNodes().item(1).getAttributes().getNamedItem(ASSET_ID).getTextContent(),
                element.getAttribute(NAME)
        );
        listOfEvents.add(tempEvent);
    }

}
