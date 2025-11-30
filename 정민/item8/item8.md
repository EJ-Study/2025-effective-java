# 아이템8. finalizer와 cleaner 사용을 피하라

<br/>

**💡 핵심 내용**
>
> cleaner(자바 8까지는 finalizer)는 기본적으로 사용하지 말아야 한다.
> 안전망 역할 / 네이티브 자원 회수용에만 사용하고, 불확실성 / 성능 저하에 주의해야 한다.

<br/>

## 0. 개요

자바는 두 가지 객체 소멸자를 제공한다.
- finalizer (자바 8까지)
- cleaner (finalizer 대안)
  
**=> 예측할 수 없고, 느리고, 성능 문제 등, 일반적으로 불필요하다.**

<br/><br/>

## 1. finalizer와 cleaner를 사용하지 말아야 하는 이유

### (1) 즉시 수행된다는 보장이 없다
- finalizer와 cleaner의 실행 시점은 GC구현마다 다르다.

<br/>

### (2) 수행 여부를 보장하지 않는다
- finalizer와 cleaner는 GC에 의해 처리되는데, GC가 언제 실행될지 보장되지 않기 때문이다.
  - 프로그램이 종료될 때까지 finalizer와 cleaner가 실행되지 않을 수 있다.

<br/>

### (3) 심각한 성능 문제가 있다
- 객체 회수 시, GC가 객체를 바로 회수하지 못하고 여러 단계를 거치게 되며, 불필요한 오버헤드가 생겨 전체적인 성능이 크게 저하된다.
  - AutoCloseable, try-with-resources를 이용한 방식에 비해 성능이 좋지 않다.


<br/>

### (4) 보안 문제를 유발할 수 있다.
- finalizer를 사용한 클래스는 finalizer 공격에 노출될 수 있다.
  - 생성자나 직렬화 과정에서 예외가 발생하면, 이 생성되다만 객체에서 악의적인 하위 클래스의 finalizer가 수행될 수 있게 된다.

<div style="margin-left: 20px">
<details>
<summary>보안 문제 관련 코드 예시</summary>
<div markdown="1">

```java
public class SecureAccount {
    private final String accountNumber;
    private final int balance;

    public SecureAccount(String accountNumber, int balance) {
        if (!isValidUser()) {
            throw new SecurityException("Unauthorized access!");
        }

        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    private boolean isValidUser() {
        return false;  // 인증 실패
    }

    public void withdraw(int amount) {
        System.out.println("Withdrawing: " + amount);
    }
}

// 공격자가 만든 악의적인 하위 클래스
public class AttackAccount extends SecureAccount {
    // 정적 필드에 자신의 참조 할당
    private static AttackAccount stolenAccount;

    public AttackAccount(String accountNumber, int balance) {
        super(accountNumber, balance);  // 생성자에서 예외 발생!
    }

    @Override
    protected void finalize() throws Throwable {
        // 1. GC가 회수하기 전에 정적 필드에 자신의 참조를 할당
        stolenAccount = this;
        System.out.println("GC 회수 막음");
    }
    
    public static AttackAccount getStolenAccount() {
        return stolenAccount;
    }
}

// 공격자가 만든 악의적인 하위 클래스
public class AttackAccount extends SecureAccount {
    // 1. 정적 필드에 자신의 참조를 저장
    private static AttackAccount stolenAccount;

    public AttackAccount(String accountNumber, int balance) {
        super(accountNumber, balance);  // 생성자에서 예외 발생!
    }

    @Override
    protected void finalize() throws Throwable {
        // 1. GC가 회수하기 전에 정적 필드에 자신의 참조를 할당
        // → GC가 이 객체를 수집하지 못하게 막음
        stolenAccount = this;
        System.out.println("객체 부활! GC 회수 막음!");
    }

    // 공격자가 훔친 객체에 접근
    public static AttackAccount getStolenAccount() {
        return stolenAccount;
    }
}

// 공격 예시
public class Main {
    public static void main(String[] args) throws InterruptedException {
        // 공격 객체 생성
        try {
            new AttackAccount("1234-5678", 1000000);
        } catch (SecurityException e) {
            System.out.println("생성 실패: " + e.getMessage());
        }

        // GC 실행 (finalizer 동작) 
        System.gc();
        Thread.sleep(1000);
        
        AttackAccount stolenAccount = AttackAccount.getStolenAccount();

        if (stolenAccount != null) {
            System.out.println("일그러진 객체 획득");

            // 보안 검증을 통과하지 못한 객체의 메서드를 호출
            // 애초에는 허용되지 않았을 작업 수행
            stolenAccount.withdraw(1000000); // 돈 인출
        }
    }
}

```

</div>
</details>
</div>


<br/><br/>


## 2. finalizer와 cleaner를 사용하는 경우

### (1) 안전망 역할
자원의 소유자가 close 메서드를 호출하지 않는 것에 대비하기 위함이다.

<div style="margin-left: 20px">
<details>
<summary>cleaner를 안전망으로 활용하는 코드 예시</summary>
<div markdown="1">

```java
public class Room implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();

    // 방을 청소할 때 수거할 자원들을 담고 있다.
    private static class State implements Runnable {
        int numJunkPiles; // 수거할 자원
        
        State(int numJunkPiles) {
            this.numJunkPiles = numJunkPiles;
        }

        @Override
        public void run() {
            System.out.println("방 청소");
            numJunkPiles = 0;
        }
    }
    
    private final State state; // 방의 상태
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

**코드 설명**
- State의 run 메서드가 호출되는 경우
    - Room의 close메서드를 호출할 때
    - 클라이언트가 close를 호출하지 않을 때, cleaner가 State의 run 메서드를 호출할 것이다. **(안전망 역할)**

<br/>

- State 인스턴스는 절대로 Room 인스턴스를 참조해서는 안된다.
    - Room 인스턴스를 참조할 경우, State에서 계속 Room을 참조하고 있기 때문에(순환참조) GC의 회수 대상이 되지 못한다.
    - State가 정적 클래스인 이유가 위를 방지하기 위함이다. (Item 24 참고)

</div>
</details>
</div>

<br/>

### (2) 네이티브 피어와 연결된 객체를 회수할 때
네이티브 피어는 자바 객체가 아니므로, GC가 네이티브 객체를 회수하지 못하는 경우를 처리하기 위함이다.
- 단, 성능 저하를 감당할 수 있고, 네이티브 피어가 심각한 자원을 가지고 있지 않은 경우에만 해당

<br/>

<details>
<summary>네이티브 피어란?</summary>

> 일반 자바 객체가 네이티브 메서드를 통해 기능을 위임한 네이티브 객체를 의미한다.

</details>


<br/><br/>

## 3. finalizer와 cleaner의 대안

- AutoCloseable을 구현하고 try-with-resources로 안전하게 자원을 관리한다.
  - 추가 팁
    - 각 인스턴스가 자신이 닫혔는지를 추적하도록 구현하는 것이 좋다.
      - `close()` 메서드에서 객체가 더 이상 유효하지 않음을 필드에 기록한다.
      - 다른 메서드들은 이 필드를 검사해서 객체가 닫힌 후에 호출되었다면 `IllegalStateException`을 던진다.

<div style="margin-left: 20px">
<details>
<summary>코드 예시</summary>
<div markdown="1">

```java
// 자원 예시
public class Resource implements AutoCloseable {
    private boolean closed = false;  // 닫힘 상태 추적

    public void doSomething() {
        if (closed) {
            throw new IllegalStateException("Resource is already closed");
        }
        System.out.println("작업 수행");
    }

    @Override
    public void close() {
        if (closed) return;  // 이미 닫혔으면 무시

        closed = true;  // 더 이상 유효하지 않음을 기록
        System.out.println("자원 해제");
        // 실제 자원 해제 로직
    }
}

// 클라이언트 코드
public class Main {
    public static void main(String[] args) {
        
        try (Resource resource = new Resource()) {
            resource.doSomething();
        }  // 자동으로 close() 호출

        // 닫힌 후 사용 시도
        resource.doSomething();  // IllegalStateException 발생
    }
}
```

</div>
</details>
</div>

<br/>
