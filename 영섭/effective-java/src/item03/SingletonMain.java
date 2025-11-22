package item03;

import java.io.*;

public class SingletonMain {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        BadElvis badElvis1 = BadElvis.getInstance();

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("elvis.ser"));
        oos.writeObject(badElvis1);

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("elvis.ser"));
        BadElvis badElvis2 = (BadElvis) ois.readObject();

        System.out.println(badElvis1 == badElvis2);

        GoodElvis goodElvis1 = GoodElvis.getInstance();
        oos = new ObjectOutputStream(new FileOutputStream("elvis.ser"));
        oos.writeObject(goodElvis1);

        ois = new ObjectInputStream(new FileInputStream("elvis.ser"));
        GoodElvis goodElvis2 = (GoodElvis) ois.readObject();

        System.out.println(goodElvis1 == goodElvis2);

        oos.close();
        ois.close();
    }
}
