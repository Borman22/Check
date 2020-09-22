package tv.sonce.utils;

import com.google.gson.Gson;

import java.io.*;

public class JsonReader <T> {
    private final String pathToFile;
    private final T structure;

    public JsonReader(String pathToFile, T structure) {
        this.pathToFile = pathToFile;
        this.structure = structure;
    }

    public T getContent() throws IOException {
        Gson gson = new Gson();

        try(InputStream is = ClassLoader.getSystemResourceAsStream(pathToFile)) {
            Reader reader = new InputStreamReader(is, "UTF-8");
            return (T) gson.fromJson(reader, structure.getClass());
        } catch (IOException e) {
            throw e;
        }
    }
}
