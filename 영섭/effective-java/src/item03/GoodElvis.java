package item03;

import java.io.Serial;
import java.io.Serializable;

public class GoodElvis implements Serializable {

    private static final GoodElvis INSTANCE = new GoodElvis();
    private GoodElvis() {}

    public static GoodElvis getInstance() {
        return INSTANCE;
    }

    // 역직렬화 시 이 메서드가 호출됨
    private Object readResolve() {
        // 새로 생성된 가짜 인스턴스 대신 진짜 INSTANCE 반환
        return INSTANCE;
    }
}
