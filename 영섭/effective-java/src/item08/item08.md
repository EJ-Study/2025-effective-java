# finalizer와 cleaner 사용을 피하라

## 개요

자바의 객체 소멸자는 두 가지가 있음:
- **finalizer**: Java 9부터 deprecated
- **cleaner**: Java 9에서 finalizer의 대안으로 도입

**결론**: 둘 다 사용하지 말아야 함 (예측 불가능하고, 위험하며, 일반적으로 불필요)

## finalizer와 cleaner란?

### finalizer
```java
public class Resource {
    @Override
    protected void finalize() throws Throwable {
        try {
            // 자원 정리 코드
            closeResource();
        } finally {
            super.finalize();
        }
    }
}
```

### cleaner
```java
public class Room implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();
    
    private static class State implements Runnable {
        int numJunkPiles; // 방 안의 쓰레기 수
        
        @Override
        public void run() {
            // 방 청소
            numJunkPiles = 0;
        }
    }
    
    private final State state;
    private final Cleaner.Cleanable cleanable;
    
    public Room(int numJunkPiles) {
        state = new State(numJunkPiles);
        cleanable = cleaner.register(this, state);
    }
    
    @Override
    public void close() {
        cleanable.clean();
    }
}
```

## 사용하면 안 되는 이유

### 1. 즉시 수행된다는 보장이 없음

- finalizer/cleaner가 **언제 실행될지 알 수 없음**
- 객체가 unreachable 상태가 된 후 실행되기까지 시간이 얼마나 걸릴지 모름
- GC 알고리즘에 따라 천차만별

```java
// 문제 상황
public class FileHandler {
    private File file;
    
    @Override
    protected void finalize() {
        file.close(); // 언제 실행될지 모름!
    }
}

// 파일을 계속 열다가 파일 디스크립터가 부족해질 수 있음
for (int i = 0; i < 10000; i++) {
    new FileHandler(new File("test.txt"));
}
```

### 2. 수행 여부조차 보장하지 않음

- 프로그램이 종료될 때까지 finalizer/cleaner가 **한 번도 실행 안 될 수도** 있음
- 상태를 영구적으로 수정하는 작업에서는 **절대 사용하면 안 됨**

```java
// 절대 하면 안 되는 예
@Override
protected void finalize() {
    // 데이터베이스 같은 공유 자원의 영구 락 해제
    database.releaseLock(); // 실행 안 될 수도 있음!
}
```

### 3. 심각한 성능 문제

- AutoCloseable 객체를 만들고 try-with-resources로 닫기: **12ns**
- finalizer 사용: **550ns** (약 50배 느림)
- cleaner 사용: **500ns** 정도

### 4. 보안 문제 (finalizer 공격)

```java
public class Vulnerable {
    public Vulnerable() {
        if (!securityCheck()) {
            throw new SecurityException("보안 검사 실패");
        }
    }
}

// 공격 코드
public class Attack extends Vulnerable {
    @Override
    protected void finalize() {
        // 생성자에서 예외가 발생해도 finalize는 실행됨!
        // 이 객체의 참조를 어딘가에 저장해서 악용 가능
    }
}
```

**방어**: final 클래스로 만들거나, finalize 메서드를 final로 선언

## 올바른 대안: AutoCloseable

```java
// 올바른 방법
public class Resource implements AutoCloseable {
    
    @Override
    public void close() {
        // 자원 정리 코드
        closeResource();
    }
}

// 사용
try (Resource resource = new Resource()) {
    // 자원 사용
} // 자동으로 close() 호출
```

**장점**:
- 명확하고 예측 가능
- 성능 좋음
- 예외 처리도 깔끔

## finalizer/cleaner를 사용해도 되는 경우

### 1. 안전망 (safety net) 역할

클라이언트가 close를 호출하지 않았을 때를 대비한 최후의 수단

```java
public class FileInputStream implements AutoCloseable {
    
    @Override
    public void close() throws IOException {
        // 명시적 자원 정리
    }
    
    @Override
    protected void finalize() throws IOException {
        // 안전망: close가 호출되지 않았을 때를 대비
        close();
    }
}
```

**주의**: 이것도 실행 보장이 안 되므로 완벽한 안전망은 아님

### 2. 네이티브 피어(native peer) 정리

네이티브 객체는 GC가 알지 못하므로 cleaner/finalizer로 정리 가능

```java
// 네이티브 자원을 가진 객체
public class NativeResource implements AutoCloseable {
    private long nativePtr; // 네이티브 메모리 포인터
    
    private static final Cleaner cleaner = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    
    public NativeResource() {
        this.nativePtr = allocateNative();
        this.cleanable = cleaner.register(this, 
            () -> freeNative(nativePtr));
    }
    
    @Override
    public void close() {
        cleanable.clean();
    }
    
    private native long allocateNative();
    private native void freeNative(long ptr);
}
```

**단, 네이티브 자원이 중요하다면 즉시 회수해야 하므로 close 메서드를 사용해야 함**

## 핵심 정리

- **finalizer는 사용하지 말 것** (예측 불가능, 느림, 불필요)
- **cleaner는 finalizer보다 덜 위험하지만 여전히 예측 불가능하고 느림**
- **AutoCloseable을 구현하고 try-with-resources를 사용**하는 것이 최선
- cleaner/finalizer는 안전망이나 중요하지 않은 네이티브 자원 회수용으로만 사용
- 그마저도 불확실성과 성능 저하를 감수해야 함
