# 생성자 대신 정적 팩터리 메서드를 고려하라

**정적 팩터리 메서드**란?

객체 생성을 담당하는 **static 메서드**를 말함. **생성자 대신** 객체를 반환하는 정적 메서드를 제공하는 방식

## 정적 팩터리 메서드의 장•단점

### 장점

1. 이름을 가질 수 있다.
2. 호출될 때마다 인스턴스를 새로 생성하지는 않아도 된다.
   - **생성자**는 **무조건 새 객체**를 만들지만, **정적 팩터리 메서드**는 필요에 따라 **미리 생성된 인스턴스를 반환**할 수 있다.
3. 반환 타입의 하위 타입 객체를 반환할 수 있는 능력이 있다.
4. 입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다.
5. 정적 팩터리 메서드는 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.
    - 반환 타입을 인터페이스로 지정하면, 메서드 작성 시점에 구체적인 구현 클래스가 없어도 됨
    - 예시로 DriverManager.getConnection()은 Connection 인터페이스를 반환

### 단점

1. 상속을 하려면 public 이나 protected 생성자가 필요하니 정적 팩터리 메서드만 제공하면 하위 클래스를 만들 수 없다.
   - 인스턴스 생성을 외부에서 막고 싶지만, 상속 시 자식이 super()로 부모 생성자를 호출해야 하는 언어 제약 때문에 private으로 만들 수 없음
2. 정적 팩터리 메서드는 프로그래머가 찾기 어렵다.

## 흔히 사용되는 명명 방식
- from: 매개변수를 받아 인스턴스 반환 (**형변환**)
    ```java
    Date d = Date.from(instant);
    ```

- of: 여러 매개변수를 받아 인스턴스 반환
    ```java
    Set<Rank> cards = EnumSet.of(JACK, QUEEN, KING);
    ```

- valueOf: from과 of의 더 자세한 버전
    ```java
    BigInteger prime = BigInteger.valueOf(Integer.MAX_VALUE);
    ```

- instance / getInstance: 인스턴스 반환 (싱글턴 등)
    ```java
    StackWalker luke = StackWalker.getInstance(options);
    ```

- create / newInstance: 매번 새로운 인스턴스 생성 보장
    ```java
    Object newArray = Array.newInstance(classObject, arrayLen);
    ```

- getType: 다른 타입의 인스턴스 반환
    ```java
    FileStore fs = Files.getFileStore(path);
    ```

- newType: 다른 타입의 새 인스턴스 생성
    ```java
    BufferedReader br = Files.newBufferedReader(path);
    ```

- type: getType과 newType의 간결한 버전
    ```java
    List<Complaint> litany = Collections.list(legacyLitany);
    ```