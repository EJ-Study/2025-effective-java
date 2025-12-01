# equals를 재정의하려거든 hashCode도 재정의하라

## 핵심 규칙
**equals를 재정의한 클래스는 반드시 hashCode도 재정의해야 한다**

## 이유
HashMap, HashSet 같은 해시 기반 컬렉션이 제대로 동작하지 않음

```java
Map<PhoneNumber, String> map = new HashMap<>();
map.put(new PhoneNumber(707, 867, 5309), "제니");

// hashCode를 재정의하지 않으면 null 반환!
map.get(new PhoneNumber(707, 867, 5309)); // null
```

## hashCode 규약 (3가지)

1. **일관성**: equals가 사용하는 정보가 변경되지 않았다면, hashCode도 항상 같은 값 반환
2. **equals가 같으면 hashCode도 같아야 함**: x.equals(y)가 true면 x.hashCode() == y.hashCode()
3. **equals가 다르다고 hashCode가 달라야 하는 건 아님** (하지만 다르면 성능 향상)

## hashCode 구현 방법

### 전형적인 구현
```java
@Override
public int hashCode() {
    int result = Integer.hashCode(areaCode);
    result = 31 * result + Integer.hashCode(prefix);
    result = 31 * result + Integer.hashCode(lineNum);
    return result;
}
```

### Objects.hash 사용 (간단하지만 느림)
```java
@Override
public int hashCode() {
    return Objects.hash(areaCode, prefix, lineNum);
}
```

### 성능이 중요하면 캐싱
```java
private int hashCode; // 0으로 초기화

@Override
public int hashCode() {
    int result = hashCode;
    if (result == 0) {
        result = Integer.hashCode(areaCode);
        result = 31 * result + Integer.hashCode(prefix);
        result = 31 * result + Integer.hashCode(lineNum);
        hashCode = result;
    }
    return result;
}
```

## 주의사항
- 성능 때문에 **핵심 필드를 hashCode 계산에서 제외하면 안 됨**
- hashCode가 반환하는 값의 생성 규칙을 API 사용자에게 공표하지 말 것 (나중에 개선 여지를 남김)
