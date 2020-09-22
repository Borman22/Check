package tv.sonce.plchecker.entity;

import java.util.Arrays;

public class ProgramDescription {
    private final String programType;
    private final String znakKrug;
    private final String skleroticName;
    private final String[] programSynonyms;

    public ProgramDescription(String programType, String znakKrug, String skleroticName, String[] programSynonyms){
        this.programType = programType;
        this.znakKrug = znakKrug;
        this.skleroticName = skleroticName;
        this.programSynonyms = programSynonyms;
    }

    public String getProgramType() {
        return programType;
    }

    public String getZnakKrug() {
        return znakKrug;
    }

    public String getSkleroticName() {
        return skleroticName;
    }

    public String[] getProgramSynonyms() {
        return programSynonyms;
    }

    @Override
    public String toString() {
        return "Sklerotic{" +
                "programType='" + programType + '\'' +
                ", znakKrug='" + znakKrug + '\'' +
                ", skleroticName='" + skleroticName + '\'' +
                ", programSynonyms=" + Arrays.toString(programSynonyms) +
                '}';
    }
}
