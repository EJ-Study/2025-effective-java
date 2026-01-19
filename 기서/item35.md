## 6장 열거 타입과 애너테이션

### 아이템 35, ordinal 메서드 대신 인스턴스 필드를 사용하라

- `ordinal()`은 EnumSet, EnumMap과 같은 열거 타입 기반 범용 자료구조에 쓰일 목적으로 설계됨
- 열거 타입 상수에 연결된 값은 `ordinal()`로 얻지 말고 **인스턴스 필드에 저장**해야 함

### ordinal()의 잘못된 사용

```java
enum Ensemble {
    SOLO, DUET, TRIO, QUARTET, QUINTET,
    SEXTET, SEPTET, OCTET, NONET, DECTET;

    public int numberOfMusicians() {
        return ordinal() + 1;  // ❌ 잘못된 사용
    }
}
```

**문제점:**

- 상수 선언 순서를 바꾸면 오작동
- 중간에 상수를 추가하면 값이 어긋남
- 같은 값을 가진 상수를 추가할 수 없음
- 유지보수 어려움

### 올바른 방법: 인스턴스 필드 사용

```java
enum Ensemble {
    SOLO(1),
    DUET(2),
    TRIO(3),
    QUARTET(4),
    QUINTET(5),
    SEXTET(6),
    SEPTET(7),
    OCTET(8),
    DOUBLE_QUARTET(8),  // 같은 값을 가진 상수 추가 가능
    NONET(9),
    DECTET(10);

    private final int numberOfMusicians;

    Ensemble(int numberOfMusicians) {
        this.numberOfMusicians = numberOfMusicians;
    }

    public int numberOfMusicians() {
        return numberOfMusicians;
    }
}
```

**장점:**

- 상수 선언 순서와 무관
- 중간에 상수 추가 가능
- 같은 값을 가진 상수 추가 가능
- 명확하고 유지보수 용이

### 요약

- 열거 타입 상수에 연결된 값은 **인스턴스 필드에 저장**
- 인스턴스 필드를 사용하면 상수 순서 변경, 중간 상수 추가, 같은 값 상수 추가 등이 모두 가능하며 유지보수가 용이함
