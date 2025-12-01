# equals는 일반 규약을 지켜 재정의하라

## equals 재정의가 필요 없는 경우
- 각 인스턴스가 본질적으로 고유할 때 (예: Thread)
- 논리적 동치성 검사가 필요 없을 때
- 상위 클래스의 equals가 하위 클래스에도 적합할 때
- 클래스가 private이거나 package-private이고 equals를 호출할 일이 없을 때

## equals 재정의가 필요한 경우
값 클래스(Integer, String 등)처럼 **객체의 동일성이 아닌 논리적 동치성**을 확인해야 할 때

## equals 규약 (5가지)

1. **반사성(reflexivity)**: x.equals(x)는 true
2. **대칭성(symmetry)**: x.equals(y)가 true면 y.equals(x)도 true
3. **추이성(transitivity)**: x.equals(y)가 true이고 y.equals(z)가 true면 x.equals(z)도 true
4. **일관성(consistency)**: 여러 번 호출해도 항상 같은 결과
5. **null-아님**: x.equals(null)은 false

## equals 구현 방법

```java
@Override
public boolean equals(Object o) {
    // 1. 연산자로 자기 자신 참조 확인
    if (o == this) return true;
    
    // 2. instanceof로 타입 확인
    if (!(o instanceof MyClass)) return false;
    
    // 3. 올바른 타입으로 형변환
    MyClass that = (MyClass) o;
    
    // 4. 핵심 필드들이 모두 일치하는지 확인
    return this.field1.equals(that.field1) 
        && this.field2 == that.field2;
}
```

## 주의사항
- equals를 재정의하면 **hashCode도 반드시 재정의**해야 함
- 너무 복잡하게 만들지 말 것
- Object 타입 외의 매개변수는 사용하지 말 것 (오버로딩 됨)