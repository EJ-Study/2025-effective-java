package item09;

public class BadResource {
    public void doWork() throws Exception {
        throw new Exception("작업 중 예외 발생! (예외 A)");
    }

    public void close() throws Exception {
        throw new Exception("자원 닫기 실패! (예외 B)");
    }
}
