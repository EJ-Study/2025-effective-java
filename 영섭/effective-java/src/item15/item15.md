# 클래스와 멤버의 접근 권한을 최소화하라

## 핵심 원칙: 정보 은닉(캡슐화)

**모든 클래스와 멤버의 접근성을 가능한 한 좁혀야 함**

## 정보 은닉의 장점

1. **시스템 개발 속도 향상**: 여러 컴포넌트를 병렬로 개발 가능
2. **시스템 관리 비용 감소**: 각 컴포넌트를 독립적으로 파악하고 수정 가능
3. **성능 최적화**: 다른 컴포넌트에 영향 없이 특정 컴포넌트만 최적화
4. **재사용성 증가**: 독립적인 컴포넌트는 다른 환경에서도 유용
5. **큰 시스템 개발 난이도 감소**: 전체 완성 전에도 개별 컴포넌트 검증 가능

## 접근 제한자 종류 (좁은 순서)

1. **private**: 해당 클래스 내부에서만 접근
2. **package-private (default)**: 같은 패키지 내에서만 접근
3. **protected**: 같은 패키지 + 하위 클래스에서 접근
4. **public**: 모든 곳에서 접근

## 설계 원칙

**톱레벨 클래스/인터페이스**
- public으로 선언하면 공개 API가 되므로 하위 호환 필요
- package-private으로 만들면 내부 구현이 되어 자유롭게 수정 가능
- 한 클래스에서만 사용하는 package-private 클래스는 그 클래스의 private static 중첩 클래스로 만들기

**멤버(필드, 메서드, 중첩 클래스)**
- 공개 API를 설계한 후, 나머지는 모두 private으로
- 같은 패키지의 다른 클래스가 접근해야 한다면 package-private으로 (자주 그래야 한다면 설계 재검토)
- protected는 공개 API가 되므로 신중히 사용

## 주의사항

**1. public 클래스의 인스턴스 필드는 되도록 public이 아니어야 함**
```java
// 나쁜 예
public class Point {
    public double x;
    public double y;
}

// 좋은 예
public class Point {
    private double x;
    private double y;
    
    public double getX() { return x; }
    public double getY() { return y; }
}
```

**2. public static final 필드의 위험**
```java
// 나쁜 예 - 배열은 변경 가능!
public static final Thing[] VALUES = { ... };

// 좋은 예 1 - 불변 리스트
private static final Thing[] PRIVATE_VALUES = { ... };
public static final List<Thing> VALUES = 
    Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));

// 좋은 예 2 - 복사본 반환
private static final Thing[] PRIVATE_VALUES = { ... };
public static final Thing[] values() {
    return PRIVATE_VALUES.clone();
}
```

**3. 상속을 고려한 설계**
- 메서드를 재정의할 때 상위 클래스보다 접근 수준을 좁게 설정할 수 없음 (리스코프 치환 원칙)
- 인터페이스의 모든 메서드는 암묵적으로 public

**4. 테스트를 위한 접근성 확대**
- private을 package-private까지는 허용 가능
- 그 이상은 안 됨 (테스트를 같은 패키지에 두면 해결)

**5. 모듈 시스템 (Java 9+)**
- 모듈 단위로 접근 제어 가능
- 패키지를 외부에 공개할지 선택 가능
