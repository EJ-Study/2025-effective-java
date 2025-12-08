## 2장 객체 생성과 파괴

### 아이템 13, clone 재정의는 주의해서 진행하라

- `Cloneable` 인터페이스를 구현한 클래스는 `clone()` 메서드를 재정의해야 한다.
- 하지만 `clone()` 메서드는 복잡하고 위험한 메커니즘을 가지고 있어 주의가 필요하다.

### Cloneable 인터페이스란?

`Cloneable`은 일종의 **마커 인터페이스**로, 'clone에 의해 복제할 수 있다'를 표시하는 인터페이스이다.

```java
public interface Cloneable {
    // 빈 인터페이스 - 메서드가 하나도 없음!
}
```

**특징:**

- `clone()` 메서드는 `Cloneable` 내부가 아닌 `java.lang.Object` 클래스에 `protected` 접근 지정자로 구현되어 있다.
- `Cloneable`을 구현하지 않으면 `clone()` 호출 시 `CloneNotSupportedException` 예외가 발생한다.
- 생성자를 호출하지 않고도 객체를 생성할 수 있다.

### clone() 사용법

**기본 사용법:**

```java
public class Digimon implements Cloneable {
    private List<String> skills;
    private int attack;
    private int defense;
    private int hp;

    @Override
    public Digimon clone() {
        try {
            return (Digimon) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
```

**주의사항:**

1. **피상적 복사 (Shallow Copy)**
    - `clone()`은 단순히 참조만 복사한다
    - 배열을 예로 들면, 내부 요소 하나하나 복사되는 것이 아니라 배열의 참조를 복사한다
    - 원본과 복사본이 같은 참조를 가리키게 된다

2. **Cloneable 미구현 시 예외**
    - `Cloneable`을 구현하지 않으면 `CloneNotSupportedException` 예외가 발생한다

### clone() 재정의 시 문제점

#### 1. 가변 객체를 참조하는 경우

가변 객체를 참조하는 객체를 복사하는 경우, 원본과 복사본이 같은 참조를 가리키게 되어 문제가 발생한다.

**문제가 있는 구현:**

```java
public class Digimon implements Cloneable {
    private List<String> skills;

    @Override
    public Digimon clone() {
        try {
            return (Digimon) super.clone(); // skills가 같은 참조를 가리킴
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
```

**올바른 구현:**

```java

@Override
public Digimon clone() {
    try {
        Digimon result = (Digimon) super.clone();
        result.skills = new ArrayList<>(skills); // 새로운 리스트 생성
        return result;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();
    }
}
```

#### 2. 복잡한 가변 객체를 참조하는 경우

내부 엔트리를 통해서 특정 값에 접근할 수 있도록 구현한 해시테이블을 예로 살펴보자.

**HashTable 구조:**

```java
public class HashTable implements Cloneable {
    private Entry[] buckets;

    private static class Entry {
        final Object key;
        Object value;
        Entry next; // 다음 Entry를 가리키는 포인터

        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
}
```

**문제점:**

- 단순하게 `clone()`을 사용하면 복사본이 원본의 엔트리를 통해서 값을 찾기 때문에 잘못된 값을 찾게 된다
- `buckets` 배열만 복사하면, 각 `Entry` 객체는 원본과 같은 참조를 가리킨다
- 각 `Entry`의 `next`로 연결된 전체 체인을 복사해야 한다

**재귀적 deepCopy 구현 (스택 오버플로우 위험):**

```java
Entry deepCopy() {
    return new Entry(key, value, next == null ? null : next.deepCopy());
}

@Override
public HashTable clone() {
    try {
        HashTable result = (HashTable) super.clone();
        result.buckets = new Entry[buckets.length];
        for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] != null) {
                result.buckets[i] = buckets[i].deepCopy();
            }
        }
        return result;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();
    }
}
```

**문제점:**

- 재귀의 특성상 연결 리스트가 매우 길면 스택 오버플로우와 같은 문제를 일으킬 수 있다
- 따라서 반복문을 쓰는 방법을 추천한다

**반복문을 사용한 개선된 deepCopy (권장):**

```java
Entry deepCopy() {
    Entry result = new Entry(key, value, next);
    for (Entry p = result; p.next != null; p = p.next) {
        p.next = new Entry(p.next.key, p.next.value, p.next.next);
    }
    return result;
}
```

**참고:**

- 다른 방법으로 고수준 메서드를 만들어서 복제하는 방법도 있다
- 하지만 좋은 성능을 기대할 수 없고, `Cloneable` 아키텍처와 어울리지 않는 방법이다

#### 3. 상속 가능한 객체

상속 가능한 객체, 즉 상속 클래스는 `Cloneable`을 구현해서는 안 된다.

**왜 상속 클래스는 Cloneable을 구현하면 안 될까?**

**문제점 1: 생성자 연쇄 문제**

`clone()`은 생성자를 호출하지 않고도 객체를 생성한다는 점이 문제가 될 수 있다.

```java
// 상위 클래스
public class Animal implements Cloneable {
    private String name;

    @Override
    public Animal clone() {
        try {
            return (Animal) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

// 하위 클래스
public class Dog extends Animal {
    private String breed;

    @Override
    public Dog clone() {
        try {
            Dog result = (Dog) super.clone(); // Animal 타입 반환 가능성
            // breed 필드 복사 필요
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
```

**문제점:**

- 하위 클래스에서 `super.clone()`을 호출하면 상위 클래스 타입 객체를 반환할 수 있다.
- `clone()`은 생성자를 호출하지 않으므로, 하위 클래스의 추가 필드를 제대로 초기화하기 어렵다.

**문제점 2: clone() 재정의의 복잡성**

상속 구조가 복잡해질수록 각 클래스에서 `clone()`을 올바르게 재정의하기 어려워진다.

**사용해도 무방한 경우:**

1. **복사할 객체가 `final` 클래스이다**
    - `final` 클래스는 상속이 불가능하므로 하위 클래스 문제가 발생하지 않음
    - 하지만 `final` 클래스라면 `Cloneable`을 구현할 이유도 없지않나..?

2. **모든 필드가 기본 타입이고 불변 객체를 참조한다**
    - 복잡한 가변 객체가 없으므로 `super.clone()`만으로 충분함
    - 하지만 이런 경우에도 변환 생성자/팩토리를 사용하는 것이 더 안전함

**결론:**

- 상속 가능한 클래스는 `Cloneable`을 구현하지 않는 것이 좋다.
- 기존 `Object` 방식(`protected`, `throwable`)을 따르거나 동작을 막아야 한다.
- 복제가 필요하다면 변환 생성자나 변환 팩토리를 사용하라

### 변환 생성자와 변환 팩토리 (권장 방법)

`clone()`을 사용할 경우가 많지도 않고 썩 좋은 방법이 아니다. 생성자를 사용해서 새로운 복사본을 넘겨주는 **변환 생성자**, **변환 팩토리**를 사용하는 것을 추천한다.

#### 변환 생성자 (Copy Constructor)

```java
public Digimon(Digimon digimon) {
    this.skills = new ArrayList<>(digimon.skills);
    this.attack = digimon.attack;
    this.defense = digimon.defense;
    this.hp = digimon.hp;
}
```

#### 변환 팩토리 (Copy Factory)

```java
public static Digimon newInstance(Digimon digimon) {
    Digimon result = new Digimon();
    result.skills = new ArrayList<>(digimon.skills);
    result.attack = digimon.attack;
    result.defense = digimon.defense;
    result.hp = digimon.hp;
    return result;
}
```

**장점:**

- `Cloneable` 인터페이스를 구현할 필요가 없다.
- 예외를 던지지 않는다.
- 형변환이 필요 없다.
- 인터페이스 타입의 객체를 복제할 수 있다.

**결론:**
- 복제는 그냥 **복사 생성자**와 **복사 팩토리**를 사용하라
