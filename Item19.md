## 4장 클래스와 인터페이스

### 아이템 19, 상속을 고려해 설계하고 문서화하라. 그러지 않았다면 상속을 금지하라

- 상속용 클래스는 재정의할 수 있는 메서드들을 내부적으로 어떻게 이용하는지 문서로 남겨야 한다.
- 클래스를 확장해야 할 명확한 이유가 없다면 상속을 금지하는 편이 낫다.

### 상속을 고려한 문서화

**상속용 클래스는 재정의할 수 있는 메서드들을 내부적으로 어떻게 사용하는지 문서로 남겨야 한다.**

클래스의 API로 공개된 메서드에서 클래스 자신의 또 다른 메서드를 호출할 수도 있다. 또한 그 메서드가 **재정의 가능 메서드**(`public`, `protected` 중 `final`이 아닌 모든 메서드)라면
그 사실을 호출하는 메서드의 API 설명에 적시해야 한다.

**예시 - java.util.AbstractCollection의 remove:**

```java
/**
 * {@inheritDoc}
 *
 * @implSpec
 * 이 구현은 컬렉션을 순회하면서 지정된 요소를 찾는다. 요소를 찾으면
 * iterator의 remove 메서드를 사용하여 컬렉션에서 해당 요소를 제거한다.
 *
 * <p>이 컬렉션의 iterator 메서드가 반환하는 반복자가 {@code remove}
 * 메서드를 구현하지 않은 경우, {@code UnsupportedOperationException}을 던진다.
 *
 * @throws UnsupportedOperationException {@inheritDoc}
 * @throws ClassCastException            {@inheritDoc}
 * @throws NullPointerException          {@inheritDoc}
 */
public boolean remove(Object o) {
    Iterator<E> it = iterator();
   ...
}
```

해당 설명에 따르면 `remove` 메서드가 내부적으로 `iterator()` 메서드를 사용하고, `iterator()`가 반환하는 이터레이터의 `remove` 메서드를 호출한다는 점을 알 수 있다. 따라서
`iterator()` 메서드를 재정의하면 `remove` 메서드의 동작에 영향을 준다.

**중요:**

- 일단 문서화한 것은 그 클래스가 쓰이는 한 반드시 지켜야 한다
- 그렇지 않으면 그 내부 구현 방식을 믿고 활용하던 하위 클래스를 오동작하게 만들 수 있다

### 상속을 고려한 설계

**protected 메서드 공개**

효율적인 하위 클래스를 어려움 없이 만들 수 있게 하려면 클래스 내부 동작 과정 중간에 끼어들 수 있는 **훅(Hook)** 을 잘 선별하여 `protected` 메서드 형태로 공개해야 할 수도 있다.

**예시 - java.util.AbstractList의 removeRange:**

```java
protected void removeRange(int fromIndex, int toIndex) {
    // ...
}
```

`List`의 구현체의 최종 사용자는 해당 메서드에 관심이 없다. 그럼에도 이 메서드를 제공한 이유는 단지 하위 클래스에서 부분리스트의 `clear` 메서드를 고성능으로 만들기 쉽게 하기 위해서다.

해당 메서드가 없다면 하위 클래스에서 `clear`를 호출했을 때 제거할 원소의 제곱에 비례해 성능이 느려지거나 부분리스트의 메커니즘을 새로 구현해야 했을 것이다.

**어떤 메서드를 protected로 노출해야 할까?**

심사숙고해서 잘 예측한 다음, **실제 하위 클래스를 만들어서 시험해보는 것이 최선이자 유일하다.**

- 꼭 필요한 `protected` 멤버를 놓쳤다면 하위 클래스를 작성할 때 그 빈자리가 확연히 드러난다
- `private`한 멤버를 하위 클래스를 생성하다 보면 성능 상의 이슈 등으로 `protected`로 변경하는 경우가 잦다
- 하위 클래스를 여러 개 만들 때까지 전혀 쓰이지 않은 `protected` 멤버는 사실 `private`이었어야 할 가능성이 크다

**주의사항:**

- `protected` 메서드 하나하나가 내부 구현에 해당하므로 그 수는 가능한 적어야 한다
- 한편으로 너무 적게 노출해서 상속으로 얻는 이점마저 없애지 않도록 주의해야 한다

### 상속용 클래스의 생성자는 재정의 가능 메서드를 호출해선 안 된다

**문제점:**

상위 클래스의 생성자는 하위 클래스의 생성자보다 먼저 실행된다. 따라서, 상위 클래스에서 재정의될 메서드를 호출하는 경우 오동작할 수 있다.

**예시:**

```java
import java.time.Instant;

public class Parent {
    public Parent() {
        // ❌ 오동작의 원인 - 상위 클래스에서 재정의 메서드 호출
        overrideMe();
    }

    public void overrideMe() {
        System.out.println("부모 override 메서드 호출");
    }
}

final class Child extends Parent {
    // 생성자에서 초기화
    private final Instant instant;

    Child() {
        instant = Instant.now();
    }

    // 재정의 가능 메서드
    @Override
    public void overrideMe() {
        System.out.println(instant); // null 출력됨!
    }

    public static void main(String[] args) {
        Child child = new Child();
        child.overrideMe();
    }
}
```

**실행 결과:**

- 첫 번째 출력: `null` (상위 클래스 생성자가 하위 클래스 필드 초기화 전에 호출)
- 두 번째 출력: 현재 시간

**이유:**

- 모든 클래스는 상위 클래스의 생성자를 먼저 호출하기 때문
- 상위 클래스 생성자가 하위 클래스의 생성자가 인스턴스 필드를 초기화하기 전에 `overrideMe`를 호출
- `private`, `final`, `static` 등의 메서드는 재정의가 불가능하기 때문에 부모 생성자에서 안심하고 호출해도 된다

### Cloneable과 Serializable 인터페이스는 상속 시 주의하자

**문제점:**

`clone`과 `readObject` 메서드는 새로운 객체를 만드는 생성자와 비슷하다. 따라서 상속용 클래스에서 해당 인터페이스를 구현한다면, `clone`과 `readObject`에서 재정의 가능
메서드를 호출해서는 안 된다.

**readObject의 경우:**

- 하위 클래스가 역직렬화되기 전에 재정의한 메서드부터 호출하게 된다

**clone의 경우:**

- 하위 클래스의 `clone` 메서드가 복제본의 상태를 수정하기 전에 재정의한 메서드를 호출하게 된다
- `clone`이 잘못되면 복제본뿐 아니라 원본 객체에도 피해를 줄 수 있다

**예시:**

```java
public class Super implements Cloneable {
    String type;

    public Super() {
        this.type = "super";
    }

    public void overrideMe() {
        System.out.println("super method");
    }

    @Override
    public Super clone() throws CloneNotSupportedException {
        overrideMe(); // ❌ 재정의 가능 메서드 호출
        return (Super) super.clone();
    }
}

public class Sub extends Super {
    String value;

    @Override
    public void overrideMe() {
        System.out.println("sub method");
        System.out.println(value); // null 출력됨!
        type = "sub";
    }

    @Override
    public Sub clone() throws CloneNotSupportedException {
        Sub clone = (Sub) super.clone();
        clone.value = "temp";
        return clone;
    }
}

public static void main(String[] args) {
    Sub sub = new Sub();
    Sub clone = sub.clone();
}
```

**왜 문제인가?**

`clone` 메서드의 실행 순서를 보면 문제가 명확해진다:

1. `Sub.clone()` 호출
2. `super.clone()` 호출 → `Super.clone()` 실행
3. `Super.clone()`에서 `overrideMe()` 호출
    - 이때 다형성에 의해 `Sub.overrideMe()`가 호출됨
    - **아직 `Sub.clone()`의 `clone.value = "temp"`가 실행되지 않음**
    - 따라서 `value`는 `null` (기본값)
4. `Sub.overrideMe()` 실행
    - `System.out.println(value)` → `null` 출력
    - `type = "sub"` 실행 → **원본 객체의 type도 변경됨!**
5. `super.clone()` 완료
6. `Sub.clone()`의 나머지 실행: `clone.value = "temp"`

**문제점 요약:**

1. **복제본의 상태가 수정되기 전에 재정의한 메서드가 호출됨**
    - `value`가 `null`인 상태에서 메서드가 실행됨
    - 복제본의 필드가 올바르게 설정되기 전에 메서드가 실행됨

2. **원본 객체에도 피해를 줄 수 있음**
    - `type = "sub"`가 실행될 때, 아직 `super.clone()`이 완료되지 않아 원본과 복제본이 같은 참조를 가리킬 수 있음
    - 또는 공유 필드인 경우 원본 객체의 상태가 변경될 수 있음

### 상속을 금지하는 방법

**가장 좋은 방법:**

상속용으로 설계하지 않은 클래스는 상속을 금지하는 것이다.

**방법 1: 클래스를 final로 선언**

```java
public final class UtilityClass {
    // ...
}
```

**방법 2: 모든 생성자를 private 또는 package-private으로 선언하고 public 정적 팩터리 제공**

```java
public class UtilityClass {
    // 생성자를 private으로 선언하여 상속 불가능
    private UtilityClass() {
        throw new AssertionError();
    }

    public static UtilityClass getInstance() {
        return new UtilityClass();
    }
}
```

**결론:**

- 클래스를 확장해야 할 명확한 이유가 없다면 상속을 금지하는 편이 낫다
- 굳이 상속을 허용하겠다면 문서를 만들어라
