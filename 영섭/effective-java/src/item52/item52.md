# 다중정의(Overloading)는 신중히 사용하라

## 1. 다중정의(Overloading)의 위험성

다중정의된 메서드 사이에서 어떤 것이 호출될지는 **컴파일 타임(Compile Time)**에 결정됩니다. 즉, 객체의 실제 런타임 타입이 무엇인지는 중요하지 않고, 오로지 **컴파일러가 인지하는 매개변수의 타입**에 따라 결정됩니다.

### ❌ 잘못된 예시: 컬렉션 분류기

```java
public class CollectionClassifier {
    public static String classify(Set<?> s) { return "집합"; }
    public static String classify(List<?> l) { return "리스트"; }
    public static String classify(Collection<?> c) { return "그 외"; }

    public static void main(String[] args) {
        Collection<?>[] collections = {
            new HashSet<String>(),
            new ArrayList<BigInteger>(),
            new HashMap<String, String>().values()
        };

        for (Collection<?> c : collections) {
            System.out.println(classify(c)); // 결과는 모두 "그 외"만 세 번 출력됨
        }
    }
}

```

> **이유:** 반복문 안에서 `c`의 컴파일 타임 타입은 항상 `Collection<?>`이기 때문입니다.

## 2. 재정의(Overriding)와의 차이

반면, 재정의된 메서드는 **런타임(Runtime)**에 결정됩니다. 객체의 실제 타입이 무엇인지에 따라 가장 하위의 구현체가 실행되죠.

## 3. 아이템 52의 핵심 가이드라인

* **매개변수 수가 같은 다중정의는 피하라:** 특히 타입이 비슷해서 헷갈릴 수 있다면 아예 만들지 않는 것이 상책입니다.
* **이름을 다르게 지어라:** `ObjectOutputStream`처럼 `writeBoolean(b)`, `writeInt(i)`, `writeLong(l)` 같이 이름을 명시적으로 나누는 것이 훨씬 안전합니다.
* **생성자 주의:** 생성자는 이름을 다르게 지을 수 없으므로 다중정의가 불가피할 때가 많습니다. 이럴 때는 **정적 팩터리 메서드**를 사용해 이름을 부여하세요.
* **함수형 인터페이스 주의:** 자바 8 이후 람다나 메서드 참조를 인자로 받을 때, 다중정의된 메서드들이 서로 다른 함수형 인터페이스를 인자로 받으면 컴파일러가 타입을 추론하지 못해 오류가 발생할 수 있습니다.