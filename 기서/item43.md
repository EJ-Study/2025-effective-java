# 아이템 43. 람다보다는 메서드 참조를 사용하라

## 요지

- 람다의 장점은 간결함이지만, 메서드 참조(method reference)는 그보다 더 짧고 명확할 수 있다.
- **메서드 참조 쪽이 짧고 명확하면 메서드 참조를 쓰고, 그렇지 않을 때만 람다를 사용하라.**

---

## 1. 메서드 참조가 나은 경우

### Map.merge 예

- `merge(key, value, remappingFunction)`: 키가 없으면 `{key, value}` 저장, 있으면 함수를 현재 값과 주어진 값에 적용해 덮어씀.

```java
// 람다: 매개변수 count, incr이 하는 일은 단순 합인데 공간을 많이 차지
map.merge(key, 1, (count, incr) -> count +incr);

// 메서드 참조: 동일 동작을 더 짧고 명확하게 표현 (Integer::sum)
    map.merge(key, 1,Integer::sum);
```

- 매개변수가 많을수록 메서드 참조로 줄일 수 있는 코드가 늘어난다.

---

## 2. 람다가 나은 경우

1. **매개변수 이름이 가이드가 되는 경우**
    - 람다가 더 길어도, 이름 덕분에 읽기·유지보수가 쉬울 수 있다.
2. **메서드와 람다가 같은 클래스에 있는 경우**
    - 메서드 참조는 클래스 이름까지 드러나서 오히려 길고, 람다가 더 짧다.

```java
// 메서드 참조 (클래스 이름이 길면 장황함)
service.execute(GoshThisClassNameIsHumongous::action);

// 람다가 더 짧고 명확
service.execute(() -> action());
```

- 같은 맥락: `Function.identity()`보다 `(x -> x)`를 쓰는 편이 짧고 명확한 경우가 많다.

---

## 3. 메서드 참조의 장점

- **람다로 못 하는 일은 메서드 참조로도 못 한다.** (동일한 표현력)
- 그래도 메서드 참조를 쓰면:
    - **더 짧고 간결**해지고,
    - **기능을 드러내는 이름**을 줄 수 있으며,
    - 문서화(주석/JavaDoc)를 붙이기 쉽다.
- 람다 본문이 길거나 복잡하면, 그 로직을 **별도 메서드로 추출한 뒤 메서드 참조**로 넘기는 식이 좋다.

---

## 4. 메서드 참조의 다섯 가지 유형

| 유형             | 예                        | 같은 기능의 람다                                            |
|----------------|--------------------------|------------------------------------------------------|
| **정적**         | `Integer::parseInt`      | `str -> Integer.parseInt(str)`                       |
| **한정적(인스턴스)**  | `Instant.now()::isAfter` | `Instant then = Instant.now(); t -> then.isAfter(t)` |
| **비한정적(인스턴스)** | `String::toLowerCase`    | `str -> str.toLowerCase()`                           |
| **클래스 생성자**    | `TreeMap<K,V>::new`      | `() -> new TreeMap<K,V>()`                           |
| **배열 생성자**     | `int[]::new`             | `len -> new int[len]`                                |

- **한정적**: 수신 객체가 이미 정해져 있음. 함수 객체 인수 = 참조 메서드 인수.
- **비한정적**: 수신 객체는 나중에 전달됨. 인수 목록의 **첫 번째 인수**가 수신 객체, 그 다음이 메서드 인수. 스트림의 매핑/필터 등에서 자주 사용.
- **생성자 참조**: 팩터리처럼 사용 (`클래스::new`, `타입[]::new`).

---

## 5. 정리

- **메서드 참조는 람다의 간단한 대안**이 될 수 있다.
- **메서드 참조가 짧고 명확하면 메서드 참조**, 그렇지 않으면 **람다**를 사용하라.

---

## 6. 보충: 제네릭 함수 타입 (람다는 불가, 메서드 참조만 가능)

- **예외**: “람다로 못 하는 일은 메서드 참조로도 못 한다”는 **거의** 맞고, **제네릭 함수 타입**만 유일한 반례다.
- 함수형 인터페이스의 추상 메서드가 제네릭일 수 있듯, **함수 타입도 제네릭**일 수 있다.


```java
/** 값을 읽을 때 '어떤 예외(Ex)든' 던질 수 있다고 선언 */
interface ValueReader {
    <Ex extends Exception> Object getValue() throws Ex;
}

/** 문자열 값을 읽을 때 예외를 던질 수 있다고 선언 */
interface StringValueReader {
    <Ex extends Exception> String getValue() throws Exception;
}

/** 위 두 개를 합친 함수형 인터페이스 → getValue() 하나로 합쳐짐 */
interface DataSource extends ValueReader, StringValueReader {}
```

**잘못된 예** — 람다에 타입 매개변수를 붙이는 문법은 자바에 없음:

```java
// 컴파일 에러: '<Ex ...>' 부분이 허용되지 않음 (illegal start of expression 등)
DataSource wrong = <Ex extends Exception> () -> "hello";

// 이것도 컴파일 에러: 람다식 앞에 제네릭을 쓸 수 없음
DataSource wrong2 = <IOException> () -> { throw new IOException(); };
```


- 람다는 `(인자) -> 본문` 형태만 가능하고, **타입 매개변수 `<Ex ...>`를 람다 앞에 붙이는 문법은 존재하지 않는다.** 그래서 위와 같은 제네릭 함수 타입은 메서드 참조로만 구현할 수 있다.

**올바른 예 (메서드 참조)**:

```java
/** getValue()가 제네릭 메서드라서 DataSource에 맞춰질 수 있음 */
class DefaultDataSource {
    static <Ex extends Exception> String getValue() throws Ex {
        return "hello";
    }
}

// 메서드 참조: 제네릭 메서드 getValue를 가리키면 DataSource 구현 가능
DataSource ok = DefaultDataSource::getValue;
```
