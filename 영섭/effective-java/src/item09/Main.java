package item09;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Bad Case: try-finally ===");
        badCase();

        System.out.println("\n=== Good Case: try-with-resources ===");
        goodCase();
    }

    // Bad Case: 첫 번째 예외가 사라짐
    static void badCase() {
        try {
            BadResource resource = new BadResource();
            try {
                resource.doWork(); // 예외 A 발생
            } finally {
                resource.close();  // 예외 B 발생 - 이게 A를 덮어씀!
            }
        } catch (Exception e) {
            System.out.println("잡힌 예외: " + e.getMessage());
            System.out.println("Suppressed 예외 개수: " + e.getSuppressed().length);
            // 예외 A는 완전히 사라짐!
        }
    }

    // Good Case: 모든 예외가 보존됨
    static void goodCase() {
        try (GoodResource resource = new GoodResource()) {
            resource.doWork(); // 예외 A 발생
        } catch (Exception e) { // close()에서 예외 B 발생
            System.out.println("잡힌 주 예외: " + e.getMessage());
            System.out.println("Suppressed 예외 개수: " + e.getSuppressed().length);

            // Suppressed 예외 확인
            for (Throwable suppressed : e.getSuppressed()) {
                System.out.println("  - Suppressed: " + suppressed.getMessage());
            }
        }
    }
}
