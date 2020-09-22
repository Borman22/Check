package tv.sonce.utils;

import java.util.Arrays;

public class TimeCode {
    protected int frames; // в кадрах 23*90_000 + 59*1500 + 59*25 + 24
    private String delimitedStr; // с разделителями ':'  "23:59:59:24"
    private int intStr; // без разделителей 23595924
    private final String[] ARRAY_STR = {"00", "00", "00", "00"}; // {23, 59, 59, 24}
    private final int[] ARRAY_INT = {0, 0, 0, 0}; // {23, 59, 59, 24}
    private static final int TWENTY_FOUR_HOURS = 24*60*60*25; //2_160_000 кадров - сутки

    // 23:59:59:24
    public TimeCode(String delimitedStr) {
        setIntStr(delimitedStr);
    }

    // 23595924
    public TimeCode(int intStr) {
        setIntStr(intStr);
    }

    // 23595924
    public void setIntStr(int intStr) {
        if (intStr < 0 || intStr >= 23595924) {
            System.out.println("Таймкод должен быть от 0 до 23:59:59:24");
            return;
        }
        this.intStr = intStr;
        //Заполняем массив интов
        ARRAY_INT[0] = intStr / 1_000_000;    // часы
        intStr = intStr - 1_000_000 * ARRAY_INT[0];

        ARRAY_INT[1] = intStr / 10_000;  // минуты
        intStr = intStr - 10_000 * ARRAY_INT[1];

        ARRAY_INT[2] = intStr / 100;    // секунды
        ARRAY_INT[3] = intStr - 100 * ARRAY_INT[2]; // кадры

        if (ARRAY_INT[1] > 59 || ARRAY_INT[2] > 59 || ARRAY_INT[3] > 24) {
            System.out.println("Минут и секунд не может быть больше 59, а кадров больше 24. TC = 00:00:00:00.");
            Arrays.fill(ARRAY_INT, 0);
        }

        //Заполняем массив стрингов
        for (int j = 0; j < 4; j++) {
            if (ARRAY_INT[j] < 10) ARRAY_STR[j] = ('0' + Integer.toString(ARRAY_INT[j]));
            else ARRAY_STR[j] = Integer.toString(ARRAY_INT[j]);
        }

        frames = 90_000 * ARRAY_INT[0] + 1500 * ARRAY_INT[1] + 25 * ARRAY_INT[2] + ARRAY_INT[3];

        // Преобразовываем в сторку формата "23:59:59:24"
        delimitedStr = ARRAY_STR[0] + ':' + ARRAY_STR[1] + ':' + ARRAY_STR[2] + ':' + ARRAY_STR[3];
    }

    public void setIntStr(String delimitedStr) {
        setIntStr(delimitedStrToIntStr(delimitedStr));
    }

    public String getDelimitedStr() {
        return delimitedStr;
    }

    public int getFrames() {
        return frames;
    }

    @Override
    public String toString() {
        return delimitedStr;
    }

//    public TimeCode addTC(TimeCode TC2) {
//        int buf = this.TCInFrame + TC2.TCInFrame;
//        buf %= TWENTY_FOUR_HOURS;
//        return new TimeCode(TCInFrameToIntStr(buf));
//    }

//    public void appendTC(int TCIntStr) {
//        int buf = this.TCInFrame + TCIntStrToFrame(TCIntStr);
//        buf %= TWENTY_FOUR_HOURS;
//        setTC(TCInFrameToIntStr(buf));
//    }

    public static int framesToIntStr(int frames) {
        int hours, minutes, seconds, finalFrames;
        hours = frames / 90_000;
        frames = frames - 90_000 * hours;
        minutes = frames / 1500;
        frames = frames - 1500 * minutes;
        seconds = frames / 25;
        finalFrames = frames - 25 * seconds;
        return 1_000_000 * hours + 10_000 * minutes + 100 * seconds + finalFrames;
    }

    public static String framesToDelimitedStr(int frames) {
        return new TimeCode(TimeCode.framesToIntStr(frames)).getDelimitedStr();
    }

    public static int delimitedStrToFrames(String delimitedStr) {
        int intStr = delimitedStrToIntStr(delimitedStr);
        return intStrToFrames(intStr);
    }

    public static int intStrToFrames(int intStr) {
        int hours, minutes, seconds, frames;
        hours = intStr / 1_000_000;
        intStr = intStr - 1_000_000 * hours;
        minutes = intStr / 10_000;
        intStr = intStr - 10_000 * minutes;
        seconds = intStr / 100;
        frames = intStr - 100 * seconds;
        return 90_000 * hours + 1500 * minutes + 25 * seconds + frames;
    }

    public static int delimitedStrToIntStr(String delimitedStr) {
        if (delimitedStr == null)
            return 0;
        if (delimitedStr.length() != 11)
            delimitedStr = delimitedStr.substring(0, 11);
        if (!delimitedStr.matches("\\d\\d:\\d\\d:\\d\\d:\\d\\d")) // todo Exception, Loging
            return 0;
        delimitedStr = delimitedStr.replace(":", "");
        return Integer.parseInt(delimitedStr);
    }

    // считаем таймкод одним и тем же, если разница не больше, чем deviation кадров
    public static boolean isTheSameTC(int tc1, int tc2, int deviation) {
        return Math.abs(tc1 - tc2) <= deviation;
    }

    public static int TCDifferenceConsideringMidnight(int nextTC, int previousTC) {
        if (nextTC < previousTC)
            nextTC += TWENTY_FOUR_HOURS;
        return nextTC - previousTC;
    }

    public static String absoluteDifferenceToDelimitedStr(int nextTC, int previousTC){
        int frames = nextTC - previousTC;
        frames %= TWENTY_FOUR_HOURS;
        return frames < 0 ? "-" + framesToDelimitedStr(Math.abs(frames)) : framesToDelimitedStr(Math.abs(frames));
    }
}
   


