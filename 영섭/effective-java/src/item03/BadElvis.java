package item03;

import java.io.Serializable;

public class BadElvis implements Serializable {

    private static final BadElvis INSTANCE = new BadElvis();
    private BadElvis() {}

    public static BadElvis getInstance() {
        return INSTANCE;
    }
}
