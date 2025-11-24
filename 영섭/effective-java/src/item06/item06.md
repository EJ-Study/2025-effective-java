# 불필요한 객체 생성을 피하라

**똑같은 기능의 객체를 매번 생성하기보다는 객체 하나를 재사용하는 편이 나을 때가 많다. 특히 불변 객체는 언제든 재사용할 수 있다.**

## 문자열 객체 생성

### 나쁜 예
```java
String s = new String("bikini"); // 따라 하지 말 것!
```
- 이 문장은 실행될 때마다 String 인스턴스를 새로 만든다
- 완전히 쓸데없는 행위

### 좋은 예
```java
String s = "bikini";
```
- 새로운 인스턴스를 매번 만드는 대신 하나의 String 인스턴스를 사용
- 같은 가상 머신 안에서 똑같은 문자열 리터럴을 사용하는 모든 코드가 같은 객체를 재사용함이 보장됨

## 정적 팩터리 메서드 사용

```java
// 나쁜 예 - 호출할 때마다 새로운 객체 생성
Boolean b1 = new Boolean("true");

// 좋은 예 - 객체를 재사용
Boolean b2 = Boolean.valueOf("true");
```

- 생성자는 호출할 때마다 새로운 객체를 만들지만
- 정적 팩터리 메서드는 그렇지 않음
- **생성자 대신 정적 팩터리 메서드를 제공하는 불변 클래스에서는 정적 팩터리 메서드를 사용해 불필요한 객체 생성을 피할 수 있다**

## 생성 비용이 비싼 객체

### 나쁜 예 - 매번 생성
```java
static boolean isRomanNumeral(String s) {
    return s.matches("^(?=.)M*(C[MD]|D?C{0,3})"
            + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
}
```

**문제점**
- `String.matches`는 내부에서 `Pattern` 인스턴스를 만들어 사용
- `Pattern` 인스턴스는 한 번 쓰고 버려져서 곧바로 가비지 컬렉션 대상이 됨
- `Pattern`은 생성 비용이 높음

### 좋은 예 - 재사용
```java
public class RomanNumerals {
    // 값비싼 객체를 재사용하여 성능 개선
    private static final Pattern ROMAN = Pattern.compile(
            "^(?=.)M*(C[MD]|D?C{0,3})"
            + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
    
    static boolean isRomanNumeral(String s) {
        return ROMAN.matcher(s).matches();
    }
}
```

**개선 효과**
- 성능이 상당히 개선됨
- 코드가 더 명확해짐 (ROMAN이라는 이름으로 의미 명확화)

**추가 개선: 지연 초기화(lazy initialization)**
```java
public class RomanNumerals {
    private static Pattern ROMAN;
    
    static boolean isRomanNumeral(String s) {
        if (ROMAN == null) {
            ROMAN = Pattern.compile(
                "^(?=.)M*(C[MD]|D?C{0,3})"
                + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
        }
        return ROMAN.matcher(s).matches();
    }
}
```
- `isRomanNumeral` 메서드가 처음 호출될 때 필드를 초기화
- **권장하지 않음**: 코드를 복잡하게 만들고 성능 개선이 크지 않음

## 어댑터 (뷰)

```java
public class AdapterExample {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        
        Set<String> keys1 = map.keySet();
        Set<String> keys2 = map.keySet();
        
        // keys1과 keys2는 같은 객체
        System.out.println(keys1 == keys2); // true
    }
}
```

**설명**
- `Map` 인터페이스의 `keySet` 메서드는 `Map` 객체 안의 키를 담은 `Set` 뷰를 반환
- `keySet`을 호출할 때마다 새로운 `Set` 인스턴스를 만들 것 같지만, 사실은 같은 `Set` 인스턴스를 반환
- 반환된 `Set` 인스턴스가 기능적으로 모두 똑같으므로 여러 개를 만들 필요가 없음

## 오토박싱 (Auto Boxing)

### 나쁜 예
```java
private static long sum() {
    Long sum = 0L; // Long으로 선언 (주의!)
    for (long i = 0; i <= Integer.MAX_VALUE; i++) {
        sum += i; // 매번 Long 인스턴스 생성!
    }
    return sum;
}
```

**문제점:**
- `sum` 변수를 `Long`으로 선언하여 불필요한 `Long` 인스턴스가 약 2^31개 만들어짐
- 성능이 매우 느려짐

### 좋은 예
```java
private static long sum() {
    long sum = 0L; // long으로 선언 (기본 타입)
    for (long i = 0; i <= Integer.MAX_VALUE; i++) {
        sum += i;
    }
    return sum;
}
```

**개선 효과:**
- 저자 컴퓨터 기준 6.3초 → 0.59초로 개선

**박싱된 기본 타입보다는 기본 타입을 사용하고, 의도치 않은 오토박싱이 숨어들지 않도록 주의하자**

## 주의사항

### 객체 생성은 비싸니 피해야 한다? ❌

- 프로그램의 명확성, 간결성, 기능을 위해서 객체를 추가로 생성하는 것은 일반적으로 좋은 일
- 아주 무거운 객체가 아닌 다음에야 단순히 객체 생성을 피하고자 자신만의 객체 풀(pool)을 만들지 말자
- 데이터베이스 연결 같은 경우 생성 비용이 비싸니 재사용하는 편이 낫지만, 일반적으로는 자체 객체 풀은 코드를 헷갈리게 만들고 메모리 사용량을 늘리고 성능을 떨어뜨림

### 방어적 복사와의 대조

- **아이템 50**: 방어적 복사가 필요한 상황에서 객체를 재사용했을 때의 피해가, 필요 없는 객체를 반복 생성했을 때의 피해보다 훨씬 크다
- 방어적 복사에 실패하면 버그와 보안 구멍으로 이어지지만, 불필요한 객체 생성은 그저 코드 형태와 성능에만 영향을 준다

## 정리

1. **문자열 리터럴을 재사용하라** (`new String()` 금지)
2. **정적 팩터리 메서드를 활용하라** (`Boolean.valueOf()` 같은)
3. **생성 비용이 비싼 객체는 캐싱하여 재사용하라** (Pattern 같은 객체)
4. **어댑터는 동일한 인스턴스를 반환한다는 것을 이해하라**
5. **오토박싱을 조심하라** (박싱된 기본 타입보다 기본 타입 사용)
6. **하지만 "객체 생성은 비싸다"는 오해는 하지 말자**

**기존 객체를 재사용해야 한다면 새로운 객체를 만들지 마라. 하지만 방어적 복사가 필요한 상황에서는 재사용하면 안 된다!**