# Effective Java Item 63 — 문자열 연결은 느리니 주의하라

## 한 줄 요약
많은 문자열을 연결할 때는 `+` 대신 `StringBuilder`(멀티 스레드면 `StringBuffer`)를 사용하라.

## 왜 느린가
- `String`은 **불변**이라, `+`로 붙일 때마다 새 문자열을 만든다.
- n개를 `+`로 잇는 비용은 **O(n^2)** 에 가깝다.

## 잘못된 예 (성능 저하)
```java
public String statement() {
    String result = "";
    for (int i = 0; i < numItems(); i++) {
        result += lineForItem(i);
    }
    return result;
}
```

## 권장 예 (StringBuilder)
```java
public String statement2() {
    StringBuilder b = new StringBuilder(numItems() * LINE_WIDTH);
    for (int i = 0; i < numItems(); i++) {
        b.append(lineForItem(i));
    }
    return b.toString();
}
```

## 핵심 정리
- **짧고 고정된 문자열**은 `+`도 괜찮다.
- **반복/대량 연결**은 `StringBuilder`를 쓰자.
- **멀티 스레드**가 필요하면 `StringBuffer`를 고려하자.
