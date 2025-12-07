# Comparable을 구현할지 고려하라

## Comparable 인터페이스란?

- 객체의 순서를 비교할 수 있게 해주는 인터페이스로, `compareTo()` 메서드 하나만 정의
- 이를 구현하면 정렬, 검색, 비교 기반 컬렉션에 쉽게 사용할 수 있음

### compareTo() 메서드의 일반 규약

1. **반사성**: x.compareTo(x) == 0
2. **대칭성**: x.compareTo(y)와 y.compareTo(x)의 부호가 반대
3. **추이성**: x.compareTo(y) > 0 && y.compareTo(z) > 0 이면 x.compareTo(z) > 0
4. **일관성 권장**: (x.compareTo(y) == 0) == x.equals(y) (권장사항)

### 반환 값
- 음수: 현재 객체 < 비교 객체
- 0: 현재 객체 == 비교 객체
- 양수: 현재 객체 > 비교 객체

### 구현 방법

```java
// 기본 예시
public class PhoneNumber implements Comparable<PhoneNumber> {
    @Override
    public int compareTo(PhoneNumber pn) {
        int result = Short.compare(areaCode, pn.areaCode);
        if (result == 0) {
            result = Short.compare(prefix, pn.prefix);
            if (result == 0) {
                result = Short.compare(lineNum, pn.lineNum);
            }
        }
        return result;
    }
}

// Comparator 활용 (Java 8+)
private static final Comparator<PhoneNumber> COMPARATOR =
    Comparator.comparingInt((PhoneNumber pn) -> pn.areaCode)
              .thenComparingInt(pn -> pn.prefix)
              .thenComparingInt(pn -> pn.lineNum);

public int compareTo(PhoneNumber pn) {
    return COMPARATOR.compare(this, pn);
}
```

## 주의사항

1. **< , > 연산자 사용 금지**: 박싱된 기본 타입에는 compare() 메서드를 사용

2. **값의 차를 이용한 비교 금지**:
```java
// 나쁜 예 - 오버플로우 위험
return o1.value - o2.value;

// 좋은 예
return Integer.compare(o1.value, o2.value);
```

3. **equals와 일관성**: compareTo가 0을 반환하면 equals도 true를 반환하도록 하는 것이 좋음 (예: BigDecimal은 이를 지키지 않아 컬렉션에서 다르게 동작)

## 언제 구현해야 하나?

- 순서가 명확한 값 클래스 (알파벳, 숫자, 날짜 등)
- 정렬이 필요한 경우
- TreeSet, TreeMap 등 정렬된 컬렉션에 사용할 때

## 장점

- Arrays.sort(), Collections.sort() 사용 가능
- TreeSet, TreeMap에서 자동 정렬
- 검색 알고리즘(이진 검색 등) 활용 가능
- Stream의 sorted() 등에서 활용
