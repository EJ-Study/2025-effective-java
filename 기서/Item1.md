## Item 1. 생성자 대신 정적 팩터리 메서드를 고려하라

정적 팩터리 메서드(static factory method)는 객체 생성 방법 중 하나로, 생성자를 직접 호출하는 대신 정적 메서드를 통해 객체를 만드는 방식

이 방식은 단순한 대체가 아니라, 여러 상황에서 생성자보다 더 유연하고 명확한 장점을 제공한다.

<br>

## 팩터리 메서드의 장점
### 1. 의미 있는 이름을 가질 수 있다

생성자는 이름이 클래스명과 고정되지만, 정적 팩터리는 메서드명으로 목적을 드러낼 수 있다.
```
User.of(id)
User.from(dto)
User.withName(name)
```

### 2. 호출할 때마다 새로운 객체를 만들 필요가 없다

내부적으로 캐싱(pooling) 을 통해 동일한 인스턴스를 재사용할 수 있다.
덕분에 불필요한 객체 생성을 줄이고 성능 개선 가능.
```
Boolean.valueOf(boolean)
```


### 3. 하위 타입 객체를 반환할 수 있다

반환 타입이 인터페이스 또는 부모 타입이라면,
팩터리 메서드 안에서 실제 반환 객체를 숨길 수 있다.

``` java
/**
 * List 인터페이스를 반환하지만,
 * 우리가 직접 사용할 수 없는 전용 불변 리스트 구현체를 반환함.
 **/

List.of()     

static <E> List<E> of() {
    return (List<E>) ImmutableCollections.EMPTY_LIST;
}
```

컬렉션 프레임워크는 이 API 외견을 훨씬 작게 만들어서, 프로그래머가 이 API를 쉽게 사용할 수 있게 되었다.


### 4. 입력값에 따라 다른 객체를 반환할 수 있다

조건에 따라 다른 구현체를 반환할 수 있어 유연하다.
```
enum Style { BOLD, ITALIC, UNDERLINE }
EnumSet<Style> styles = EnumSet.of(Style.BOLD, Style.ITALIC);

public static <E extends Enum<E>> EnumSet<E> of(E first, E... rest) {
    if (first.getDeclaringClass().getEnumConstants().length <= 64)
        return new RegularEnumSet<>(first, rest);
    else
        return new JumboEnumSet<>(first, rest);
}
```

### 5. 정적 팩터리 메서드를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.

이 메서드가 반환할 실제 클래스가 무엇인지, 지금 당장 정의할 필요가 없다는 것
```java
//특정 DB 드라이버를 JDBC 시스템에 등록
DriverManager.registerDriver(new MySqlDriver());

//클라이언트는 DB URL만 제공, 클라이언트 코드는 DB 종류를 신경쓰지 않아도 됨
Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/db");
```

<br>

## 🔧 단점
### 1. 상속을 위한 생성자가 없다

생성자를 private으로 숨기는 경우가 많기 때문에,
상속이 어려워진다.

대신 컴포지션을 유도한다는 점에서는 오히려 장점일 수 있다.

### 2. 정적 팩터리 메서드만 보고 생성자인지 알기 어렵다
IDE에서 바로 구분되지 않고,
API 문서를 읽는 습관이 필요하다.

---
### 📖 흔히 사용하는 정적 팩터리 메서드 네이밍 패턴
| 메서드 이름              | 의미 / 용도                                                        |
| ------------------- | -------------------------------------------------------------- |
| **of**              | 매개변수를 받아 **간단한 객체 생성**                                         |
| **from**            | **다른 형태로부터 변환** (ex. 타입 변환, 형식 변환)                             |
| **valueOf**         | 변환 또는 타입 캐스팅 (ex. `Integer.valueOf("123")`)                    |
| **getInstance**     | **매번 동일 인스턴스**를 반환하거나 **조건부 생성** (ex. 싱글턴, 캐싱)                 |
| **newInstance**     | **호출할 때마다 새로운 객체 생성**                                          |
| **get*** / **new*** | **인스턴스 생성 목적을 명확하게 표현** (ex. `getLogger`, `newBufferedReader`) |
