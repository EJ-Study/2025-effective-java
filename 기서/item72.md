# Item 72. 표준 예외를 사용하라

## 핵심
- 직접 예외 클래스를 만들기 전에 자바가 제공하는 표준 예외를 먼저 사용하라.
- 표준 예외는 의미가 명확하고, 문서/도구/개발자 경험 측면에서 이점이 크다.
- 같은 상황에 매핑되는 예외 타입을 일관되게 선택해 API를 예측 가능하게 만든다.

## 이유
- **의미 전달**: `IllegalArgumentException`, `IllegalStateException` 등은 팀/커뮤니티가 공유하는 의미가 있다.
- **학습 비용 감소**: 새로운 예외 타입을 배울 필요가 없어서 사용자가 빠르게 이해한다.
- **도구 친화성**: IDE, 로깅, 문서화 도구가 표준 예외를 잘 처리한다.
- **유지보수성**: 불필요한 예외 클래스의 폭증을 막는다.

## 자주 쓰는 표준 예외
- `IllegalArgumentException`: 메서드 인수가 잘못되었을 때
- `IllegalStateException`: 객체 상태 때문에 호출을 처리할 수 없을 때
- `NullPointerException`: `null`이 허용되지 않는 인수에 `null`이 전달되었을 때
- `IndexOutOfBoundsException`: 인덱스가 범위를 벗어났을 때
- `ConcurrentModificationException`: 동시 수정이 감지되었을 때
- `UnsupportedOperationException`: 호출 자체가 지원되지 않을 때

## 선택 기준 (간단 규칙)
- **인수가 문제**면 `IllegalArgumentException`
- **객체 상태가 문제**면 `IllegalStateException`
- **null 허용 안함**이면 `NullPointerException`
- **인덱스**면 `IndexOutOfBoundsException`

## 예시
```java
public void setAge(int age) {
    if (age < 0) {
        throw new IllegalArgumentException("age must be >= 0");
    }
    this.age = age;
}

public void start() {
    if (state != State.READY) {
        throw new IllegalStateException("state must be READY");
    }
    // ...
}
```

## 정리
- 새로운 예외를 만들기 전에 표준 예외로 충분한지 검토한다.
- 표준 예외를 쓰면 API가 읽기 쉬워지고, 사용자가 실수할 확률이 줄어든다.
- 필요한 경우에만 커스텀 예외를 만들고, 그때도 표준 예외의 의미와 일관되게 설계한다.
