# 비트 필드 대신 EnumSet을 사용하라

## 핵심 내용

**비트 필드의 문제점**

과거에는 열거 값들을 집합으로 사용할 때 비트 필드 패턴을 사용했습니다:

```java
public class Text {
    public static final int STYLE_BOLD = 1 << 0;  // 1
    public static final int STYLE_ITALIC = 1 << 1;  // 2
    public static final int STYLE_UNDERLINE = 1 << 2;  // 4
    
    public void applyStyles(int styles) { ... }
}

// 사용
text.applyStyles(STYLE_BOLD | STYLE_ITALIC);
```

비트 필드는 다음과 같은 단점이 존재:
- 정수 열거 상수의 단점을 그대로 가짐
- 비트 필드 값이 출력되면 해석하기 어려움
- 비트 필드 하나에 녹아 있는 모든 원소를 순회하기 까다로움
- 최대 몇 비트가 필요한지 미리 예측하여 타입을 선택해야 함

**EnumSet의 장점**

java.util 패키지의 EnumSet 클래스를 사용하면 비트 필드를 대체할 수 있다:

```java
public class Text {
    public enum Style { BOLD, ITALIC, UNDERLINE, STRIKETHROUGH }
    
    public void applyStyles(Set<Style> styles) { ... }
}

// 사용
text.applyStyles(EnumSet.of(Style.BOLD, Style.ITALIC)); 
```

EnumSet의 장점:
- Set 인터페이스를 완벽히 구현하며 타입 안전함
- 내부적으로 비트 벡터를 사용하여 성능이 비트 필드에 필적함
- 난해한 비트 조작을 직접 할 필요가 없음
- 열거 타입의 장점을 모두 누릴 수 있음

**권장사항**

열거할 수 있는 타입을 한데 모아 집합 형태로 사용한다고 해서 비트 필드를 사용할 이유는 없다. EnumSet 클래스가 비트 필드 수준의 명료함과 성능, 그리고 열거 타입의 장점까지 제공하기 때문

EnumSet의 유일한 단점은 불변 EnumSet을 만들 수 없다는 것입니다(자바 9까지). 이 경우 Collections.unmodifiableSet으로 감싸서 사용할 수 있음