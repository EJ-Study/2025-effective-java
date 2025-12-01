# toString을 항상 재정의하라

## 이유
- Object의 기본 toString은 `클래스이름@16진수해시코드` 형태 (예: `PhoneNumber@adbbd`)
- 이는 **유용한 정보를 제공하지 못함**
- 좋은 toString은 **디버깅을 쉽게** 만들고 **로그 분석**에 도움이 됨

## toString의 장점

```java
// 기본 toString
System.out.println(phoneNumber); 
// PhoneNumber@adbbd

// 재정의한 toString
System.out.println(phoneNumber);
// 707-867-5309 (훨씬 유용!)
```

- println, printf, 문자열 연결, assert, 디버거 출력 등에서 **자동으로 호출**됨
- 객체를 출력만 해도 **스스로를 완벽히 설명**할 수 있음

## toString 구현 방법

### 간결하고 읽기 쉬운 형태로
```java
@Override
public String toString() {
    return String.format("%03d-%03d-%04d", 
                         areaCode, prefix, lineNum);
}
```

### 모든 중요 정보 포함
```java
@Override
public String toString() {
    return "PhoneNumber{" +
           "areaCode=" + areaCode +
           ", prefix=" + prefix +
           ", lineNum=" + lineNum +
           '}';
}
```

## 주의사항

1. **포맷 문서화 여부 결정**
    - 포맷을 명시하면: 사용자가 파싱하기 쉽지만, 한번 명시하면 평생 그 포맷에 얽매임
    - 포맷을 명시하지 않으면: 유연성은 있지만 사용자가 파싱하기 어려움

2. **포맷 명시 시 정적 팩토리나 생성자 제공**
```java
/**
 * 전화번호의 문자열 표현을 반환한다.
 * 이 문자열은 "XXX-YYY-ZZZZ" 형태의 12글자로 구성된다.
 */
@Override
public String toString() {
    return String.format("%03d-%03d-%04d", 
                         areaCode, prefix, lineNum);
}
```

3. **toString이 반환하는 정보는 접근자로도 제공**
```java
// toString에 포함된 정보를 getter로도 제공
public int getAreaCode() { return areaCode; }
public int getPrefix() { return prefix; }
public int getLineNum() { return lineNum; }
```

## toString 재정의가 불필요한 경우
- 정적 유틸리티 클래스 (toString을 쓸 일이 없음)
- 열거 타입 (이미 완벽한 toString 제공)
