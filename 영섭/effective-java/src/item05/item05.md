# 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라

## 잘못된 구현 방식들

### 정적 유틸리티 클래스 사용

```java
public class SpellChecker {
    private static final Lexicon dictionary = ...;
    
    private SpellChecker() {} // 객체 생성 방지
    
    public static boolean isValid(String word) { ... }
    public static List<String> suggestions(String typo) { ... }
}
```

### 싱글턴 사용

```java
public class SpellChecker {
    private final Lexicon dictionary = ...;
    
    private SpellChecker(...) {}
    public static INSTANCE = new SpellChecker(...);
    
    public boolean isValid(String word) { ... }
}
```

### 문제점

- 사전은 언어별로 따로 존재하고, 특수 어휘용 사전도 존재 (여러 사전이 존재)
- **사용하는 자원에 따라 동작이 달라지는 클래스에는 정적 유틸리티 클래스나 싱글턴 방식이 적합하지 않다.**

## 해결책

### 의존 객체 주입

인스턴스를 생성할 때 필요한 자원을 넘겨주는 방식

```java
public class SpellChecker {
    private final Lexicon dictionary;
    
    public SpellChecker(Lexicon dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }
    
    public boolean isValid(String word) { ... }
    public List<String> suggestions(String typo) { ... }
}
```

#### 의존 객체 주입의 특징

- 불변을 보장: dictionary를 final로 선언
- 같은 자원(Lexicon dictionary)을 사용하려는 여러 클라이언트가 의존 객체들을 안심하고 공유
- 생성자, 정적 팩터리, 빌더 모두에 똑같이 응용 가능

### 팩터리 메서드 패턴

```java
// Supplier<T> 인터페이스를 팩터리로 활용
Mosaic create(Supplier<? extends Tile> tileFactory) { ... }
```

- 팩터리: 호출할 때마다 특정 타입의 인스턴스를 반복해서 만들어주는 객체
- `Supplier<T>` 인터페이스가 팩터리를 표현한 완벽한 예
- 한정적 와일드카드 타입을 사용해 팩터리의 타입 매개변수를 제한

## 정리

클래스가 내부적으로 하나 이상의 자원에 의존하고, 그 자원이 클래스 동작에 영향을 준다면 싱글턴과 정적 유틸리티 클래스는 사용하지 않는 것이 좋다.