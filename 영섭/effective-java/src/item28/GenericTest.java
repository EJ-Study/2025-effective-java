package item28;

import java.util.ArrayList;
import java.util.List;

public class GenericTest {

    public static void main(String[] args) {
        String[] strings = new String[5];

        System.out.println(strings.getClass().getComponentType());

        Object[] objects = strings;

//        objects[0] = 123; // 런타임 오류

        // 리스트 - 아예 컴파일 에러
//        List<Object> ol = new ArrayList<Long>(); // 컴파일 에러!

        List<String> stringList = new ArrayList<>();
        List<Integer> integerList = new ArrayList<>();

        System.out.println(stringList.getClass() == integerList.getClass());
    }
}
