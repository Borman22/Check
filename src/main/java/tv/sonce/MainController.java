package tv.sonce;

import tv.sonce.plchecker.PLChecker;

import java.io.IOException;

public class MainController {
    public static void main(String[] args) {

        try {
            PLChecker plChecker = new PLChecker();
            plChecker.checkPL();
        } catch (IOException e) {
            System.out.println("Не удалось проверить плейлист");
            e.printStackTrace();
        }

    }
}
