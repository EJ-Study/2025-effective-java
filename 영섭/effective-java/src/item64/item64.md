## 아이템 64. 객체는 인터페이스를 사용해 참조하라

### 핵심 원칙

**적합한 인터페이스가 있다면, 매개변수·반환값·변수·필드를 모두 인터페이스 타입으로 선언하라.**

---

### 나쁜 예 vs 좋은 예

```java
// ❌ 나쁜 예 - 클래스 타입으로 선언
LinkedHashSet<Son> sonSet = new LinkedHashSet<>();

// ✅ 좋은 예 - 인터페이스 타입으로 선언
Set<Son> sonSet = new LinkedHashSet<>();
```

---

### 왜 인터페이스 타입으로 선언해야 하는가?

**구현체를 유연하게 교체할 수 있다.**

```java
// 인터페이스 타입으로 선언했기 때문에
Set<Son> sonSet = new LinkedHashSet<>();

// 언제든지 다른 구현체로 쉽게 바꿀 수 있다
Set<Son> sonSet = new HashSet<>();
```

선언 타입이 클래스였다면, 새로운 구현체로 바꾸려 할 때 코드 전체를 수정해야 한다.

---

### 주의사항 — 구현체를 바꿀 때

구현체를 교체할 경우, **원래 구현체가 제공하던 특성이 사라질 수 있다.**

예를 들어, `LinkedHashSet`은 **삽입 순서를 보장**하는데, 이를 `HashSet`으로 교체하면 순서 보장이 사라진다. 이런 특성에 의존하는 코드가 있다면 문제가 생긴다.

---

### 인터페이스 타입을 쓰면 안 되는 경우

적합한 인터페이스가 없을 때는 **클래스를 참조 타입으로 사용**해도 된다.

**1. 값 클래스 (Value Class)**
```java
// String, BigInteger 등은 인터페이스가 없음
String str = "hello";
BigInteger big = new BigInteger("12345");
```

**2. 클래스 기반으로 작성된 프레임워크**
```java
// OutputStream 같은 추상 클래스
OutputStream os = new FileOutputStream("file.txt");
```

**3. 인터페이스에 없는 특별한 메서드를 사용해야 할 때**
```java
// PriorityQueue는 Queue 인터페이스에 없는 comparator() 메서드를 가짐
PriorityQueue<String> pq = new PriorityQueue<>();
pq.comparator(); // Queue 인터페이스에는 없는 메서드
```
이 경우에는 클래스 타입을 써야 하지만, 최소한으로 사용해야 한다.

---

### 정리

| 상황 | 권장 타입 |
|------|-----------|
| 적합한 인터페이스가 있을 때 | **인터페이스 타입** |
| 값 클래스 (String, BigInteger) | 클래스 타입 |
| 추상 클래스 기반 프레임워크 | 추상 클래스 타입 |
| 인터페이스에 없는 메서드 필요 | 클래스 타입 (최소한으로) |

> **핵심 한 줄 요약:** 인터페이스를 타입으로 사용하는 습관을 들이면, 프로그램이 훨씬 유연해진다.