package tv.sonce.plchecker.entity;

import tv.sonce.utils.TimeCode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Event {

    public final int NumberOfEvent;

    private final String date;
    private final TimeCode time;
    private final TimeCode duration;
    private final TimeCode tcIn;
    private final TimeCode tcOut;
    private final int assetId;
    private final String name;
    private final String canonicalName;

    public final List<String> errors = new ArrayList<>();


    public Event(int NumberOfEvent, String date, String time, String duration, String tcIn, String tcOut, String assetId, String name){
        this.NumberOfEvent = NumberOfEvent;
        this.date = date;
        this.time = new TimeCode(time);
        this.duration = new TimeCode(duration);
        this.tcIn = new TimeCode(tcIn);
        this.tcOut = new TimeCode(tcOut);
        this.assetId = assetId == "" ? -1 : Integer.parseInt(assetId);
        this.name = name.trim();
        this.canonicalName = nameToCanonical(name);
    }

    public int getNumberOfEvent(){
        return NumberOfEvent;
    }
    public String getDate(){
        return date;
    }
    public int getTime(){
        return time.getFrames();
    }
    public int getDuration(){
        return duration.getFrames();
    }
    public int getTcIn(){
        return tcIn.getFrames();
    }
    public int getTcOut(){
        return tcOut.getFrames();
    }
    public int getAssetId(){
        return assetId;
    }
    public String getName(){
        return name;
    }
    public String getCanonicalName(){
        return canonicalName;
    }

    public boolean isTheSameName(String canonicalName){
        return this.canonicalName.equals(canonicalName);
    }

    // TODO переделать nameToCanonical
    private String nameToCanonical(String name){
        String tempName = name;
        int tempIndexStart = 0;
        while((tempIndexStart != tempName.length()) && (  Character.isDigit(tempName.charAt(tempIndexStart)) || (tempName.charAt(tempIndexStart) == '_'))  )
            tempIndexStart++;

        Pattern pattern;
        Matcher matcher;
        final String es = "(S\\d\\dE\\d\\d)";
        pattern = Pattern.compile(es);
        matcher = pattern.matcher(name);

        if(matcher.find()){
            return name.substring(tempIndexStart, matcher.end());
        }

        final String es2 = "(s\\d\\de\\d\\d)";
        pattern = Pattern.compile(es2);
        matcher = pattern.matcher(name);

        if(matcher.find()){
            return name.substring(tempIndexStart, matcher.end());
        }

        int tempIndexEnd = name.indexOf(" сег.");
        int tempIndexVTR = name.indexOf("VTR");
        if(tempIndexEnd == -1)
            tempIndexEnd = tempIndexVTR;
        else if(!(tempIndexVTR == -1))
            tempIndexEnd = Math.min(tempIndexEnd, tempIndexVTR);


        if((tempIndexStart != 0) | (tempIndexEnd != -1)){
            tempIndexEnd = (tempIndexEnd == -1) ? name.length() : tempIndexEnd;
            tempName = name.substring(tempIndexStart, tempIndexEnd);
        }
        return tempName;
    }

    @Override
    public String toString(){
        return "  " + date +"  " + time + "   IN = " + tcIn + "   OUT = " + tcOut + "   DUR = " + duration + "   " + name;
    }
}
