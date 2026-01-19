# @Override 애너테이션을 일관되게 사용하라

## 핵심 내용

**@Override란?**

메서드 선언에 붙이는 애너테이션으로, 상위 타입의 메서드를 재정의했음을 명시

```java
@Override
public boolean equals(Object obj) {
    // ...
}
```

## 왜 필수인가?

**문제 상황: @Override를 안 쓰면**

```java
public class Bigram {
    private final char first;
    private final char second;
    
    public Bigram(char first, char second) {
        this.first = first;
        this.second = second;
    }
    
    // equals를 재정의하려 했지만 실수로 오버로딩함
    public boolean equals(Bigram b) {  // ❌ 매개변수가 Object가 아님!
        return b.first == first && b.second == second;
    }
    
    public int hashCode() {
        return 31 * first + second;
    }
}

// 사용
Set<Bigram> s = new HashSet<>();
for (int i = 0; i < 10; i++) {
    for (char ch = 'a'; ch <= 'z'; ch++) {
        s.add(new Bigram(ch, ch));
    }
}
System.out.println(s.size());  // 26을 기대했지만 260 출력!
```

**원인:** `equals(Bigram)`은 오버라이딩이 아니라 **오버로딩**입니다. Object의 equals는 `equals(Object)`이기 때문입니다.

**해결: @Override 사용**

```java
@Override
public boolean equals(Bigram b) {  // 컴파일 에러!
    return b.first == first && b.second == second;
}

// 에러 메시지: "메서드가 상위 타입 메서드를 재정의하지 않습니다"
```

컴파일러가 실수를 잡아줍니다! 올바르게 고치면:

```java
@Override
public boolean equals(Object o) {  // ✅ 올바른 재정의
    if (!(o instanceof Bigram))
        return false;
    Bigram b = (Bigram) o;
    return b.first == first && b.second == second;
}
```

## 언제 사용하나?

**규칙: 상위 클래스/인터페이스 메서드를 재정의할 때는 항상 @Override를 붙인다**

```java
public class Child extends Parent {
    
    @Override
    public void method1() {  // ✅ 상위 클래스 메서드 재정의
        // ...
    }
    
    @Override
    public void method2() {  // ✅ 인터페이스 메서드 구현
        // ...
    }
    
    public void newMethod() {  // 새로운 메서드는 @Override 불필요
        // ...
    }
}
```

**예외: 구체 클래스에서 추상 메서드를 구현할 때**

```java
public abstract class Parent {
    public abstract void abstractMethod();
}

public class Child extends Parent {
    // @Override 생략 가능 (하지만 붙이는 게 좋음)
    public void abstractMethod() {
        // ...
    }
}
```

추상 메서드를 구현할 때는 @Override를 생략해도 되지만, **일관성을 위해 붙이는 것을 권장**합니다.

## 실전 예시

**자주 하는 실수들:**

```java
// 실수 1: equals 오버로딩
public boolean equals(MyClass obj) { }  // ❌

@Override
public boolean equals(Object obj) { }  // ✅

// 실수 2: hashCode 오타
public int hashcode() { }  // ❌ (소문자 c)

@Override
public int hashCode() { }  // ✅

// 실수 3: toString 오타
public String tostring() { }  // ❌

@Override
public String toString() { }  // ✅

// 실수 4: Comparable 구현
public int compareto(MyClass o) { }  // ❌

@Override
public int compareTo(MyClass o) { }  // ✅
```

## @Override의 장점

1. **컴파일 타임에 오류 발견**: 오타나 실수를 즉시 잡아냄
2. **코드 가독성 향상**: 이 메서드가 재정의된 것임을 명확히 표시
3. **유지보수 용이**: 상위 클래스가 변경되면 컴파일 에러로 알 수 있음
4. **IDE 지원**: 자동 완성, 리팩토링 등에서 더 정확한 도움

```java
// 상위 클래스에서 메서드 시그니처 변경
public class Parent {
    // public void oldMethod() { }  // 삭제됨
    public void newMethod() { }     // 새로 추가
}

public class Child extends Parent {
    @Override
    public void oldMethod() { }  // 컴파일 에러! 상위에 없음
    // → 개발자가 즉시 알아차리고 수정 가능
}
```

## 정리

**핵심 규칙:**
> 상위 타입의 메서드를 재정의하는 모든 메서드에 @Override 애너테이션을 달아라

**효과:**
- 실수를 컴파일 타임에 발견
- 코드 의도를 명확히 표현
- 안전한 코드 작성

**예외:**
- 추상 메서드 구현 시 생략 가능 (하지만 붙이는 게 좋음)

간단히 말해, **@Override는 안전장치**입니다. 귀찮더라도 항상 붙이는 습관을 들이면 많은 버그를 예방할 수 있습니다!