# try-finally보다는 try-with-resources를 사용하라

## 개요

자바 라이브러리에는 `close` 메서드를 호출해 직접 닫아줘야 하는 자원이 많음:
- `InputStream`, `OutputStream`, `Reader`, `Writer`
- `java.sql.Connection`
- `java.net.Socket`

전통적으로 `try-finally`를 사용했지만, Java 7부터 도입된 `try-with-resources`가 훨씬 나음

## try-finally의 문제점

### 1. 자원이 하나일 때
```java
// try-finally 방식
static String firstLineOfFile(String path) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(path));
    try {
        return br.readLine();
    } finally {
        br.close();
    }
}
```

나쁘지 않아 보이지만...

### 2. 자원이 둘 이상일 때 - 코드가 지저분해짐
```java
// try-finally로 자원 2개 사용
static void copy(String src, String dst) throws IOException {
    InputStream in = new FileInputStream(src);
    try {
        OutputStream out = new FileOutputStream(dst);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(buf)) >= 0)
                out.write(buf, 0, n);
        } finally {
            out.close();
        }
    } finally {
        in.close();
    }
}
```

**문제점**:
- 코드가 복잡하고 읽기 어려움
- 중첩된 try-finally로 가독성 저하
- 실수하기 쉬움

### 3. 예외가 먹히는 치명적 문제
```java
static String firstLineOfFile(String path) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(path));
    try {
        return br.readLine(); // 여기서 예외 발생 (예외 A)
    } finally {
        br.close(); // 여기서도 예외 발생 (예외 B)
    }
}
```

**문제 상황**:
1. `readLine()`에서 예외 발생 (예: 디스크 오류)
2. `finally` 블록의 `close()`에서도 예외 발생
3. **두 번째 예외(B)가 첫 번째 예외(A)를 완전히 덮어씀**
4. 스택 트레이스에 첫 번째 예외 정보가 사라짐
5. 디버깅이 매우 어려워짐
```java
// 실제 상황
try {
    return br.readLine(); // IOException: 디스크 읽기 실패
} finally {
    br.close(); // IOException: 파일 닫기 실패
}
// 결과: "파일 닫기 실패" 예외만 보임
// "디스크 읽기 실패"는 사라짐!
```

## try-with-resources - 해결책

### AutoCloseable 인터페이스

`try-with-resources`를 사용하려면 해당 자원이 `AutoCloseable` 인터페이스를 구현해야 함
```java
public interface AutoCloseable {
    void close() throws Exception;
}
```

자바 라이브러리의 수많은 클래스와 인터페이스가 이미 `AutoCloseable`을 구현함

### 1. 자원이 하나일 때
```java
// try-with-resources 방식
static String firstLineOfFile(String path) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine();
    }
}
```

**장점**:
- 코드가 짧고 읽기 편함
- `close()`를 명시적으로 호출할 필요 없음
- 자동으로 자원 해제

### 2. 자원이 둘 이상일 때
```java
// try-with-resources로 자원 2개 사용
static void copy(String src, String dst) throws IOException {
    try (InputStream in = new FileInputStream(src);
         OutputStream out = new FileOutputStream(dst)) {
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = in.read(buf)) >= 0)
            out.write(buf, 0, n);
    }
}
```

**장점**:
- 중첩 없이 깔끔함
- 세미콜론으로 여러 자원 선언 가능
- 자동으로 역순으로 close (out → in)

### 3. 예외가 보존됨 (Suppressed Exception)
```java
static String firstLineOfFile(String path) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine(); // 예외 A 발생
    } // close()에서 예외 B 발생
}
```

**동작**:
1. `readLine()`에서 예외 A 발생
2. `close()`에서 예외 B 발생
3. **예외 A가 메인 예외로 throw됨**
4. **예외 B는 suppressed(숨겨진) 예외로 기록됨**
5. `Throwable.getSuppressed()` 메서드로 숨겨진 예외 확인 가능
```java
try {
    firstLineOfFile("test.txt");
} catch (IOException e) {
    System.out.println("주 예외: " + e);
    // 주 예외: 디스크 읽기 실패
    
    for (Throwable suppressed : e.getSuppressed()) {
        System.out.println("숨겨진 예외: " + suppressed);
        // 숨겨진 예외: 파일 닫기 실패
    }
}
```

**장점**:
- 모든 예외 정보가 보존됨
- 디버깅이 훨씬 쉬움
- 스택 트레이스에 모든 정보 포함

## catch 절과 함께 사용
```java
// try-with-resources도 catch 사용 가능
static String firstLineOfFile(String path, String defaultVal) {
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine();
    } catch (IOException e) {
        return defaultVal; // 예외 발생 시 기본값 반환
    }
}
```

## 직접 AutoCloseable 구현하기
```java
public class MyResource implements AutoCloseable {
    
    public void doSomething() {
        System.out.println("작업 수행");
    }
    
    @Override
    public void close() {
        System.out.println("자원 정리");
    }
}

// 사용
try (MyResource resource = new MyResource()) {
    resource.doSomething();
} // 자동으로 close() 호출됨
```

## try-finally vs try-with-resources 비교

### 예외 처리 차이
```java
// try-finally
try {
    // 작업 수행 - 예외 A
} finally {
    resource.close(); // 예외 B
}
// 결과: 예외 B만 보임, A는 사라짐 ❌

// try-with-resources
try (Resource resource = ...) {
    // 작업 수행 - 예외 A
} // close()에서 예외 B
// 결과: 예외 A가 메인, B는 suppressed ✅
```

### 코드 복잡도 차이
```java
// try-finally: 3개 자원
InputStream in = null;
OutputStream out = null;
Connection conn = null;
try {
    in = new FileInputStream(src);
    out = new FileOutputStream(dst);
    conn = DriverManager.getConnection(url);
    // 작업 수행
} finally {
    if (in != null) try { in.close(); } catch (IOException e) {}
    if (out != null) try { out.close(); } catch (IOException e) {}
    if (conn != null) try { conn.close(); } catch (SQLException e) {}
}

// try-with-resources: 3개 자원
try (InputStream in = new FileInputStream(src);
     OutputStream out = new FileOutputStream(dst);
     Connection conn = DriverManager.getConnection(url)) {
    // 작업 수행
}
```

## 핵심 정리

### try-finally의 문제점
1. 코드가 지저분하고 복잡함 (특히 자원 2개 이상)
2. 예외가 발생하면 첫 번째 예외가 사라질 수 있음
3. close()를 실수로 빠뜨리기 쉬움

### try-with-resources의 장점
1. **코드가 짧고 명확함**
2. **모든 예외 정보가 보존됨** (suppressed exception)
3. **자동으로 자원 해제** (실수 방지)
4. **정확한 역순으로 close** (선언의 역순)

### 결론
**꼭 회수해야 하는 자원을 다룰 때는 try-finally 말고, try-with-resources를 사용하자**

- 코드가 더 짧고 명확
- 만들어지는 예외 정보도 훨씬 유용
- AutoCloseable 인터페이스를 구현한 모든 자원에 적용 가능