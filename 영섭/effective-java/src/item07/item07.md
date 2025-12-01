# 다 쓴 객체 참조를 해제하라

## 메모리 누수 (Memory Leak)

메모리 누수란 프로그램이 동적으로 할당한 메모리 영역 중 일부를 더 이상 사용하지 않음에도 불구하고 해제하지 않아, 사용할 수 있는 메모리가 점점 줄어드는 현상

이는 애플리케이션의 성능 저하나 시스템의 안전성 문제를 초래할 수 있음.

자바는 가비지 컬렉터가 더 이상 사용되지 않는 객체를 자동으로 회수하지만, 특정 상황에서 메모리 누수가 발생할 수 있음.

## 메모리 누수가 발생하는 주요 상황

### 1. 자기 메모리를 직접 관리하는 클래스

스택처럼 elements 배열을 직접 관리하는 클래스에서 주로 발생

#### 예시: 스택
```java
// 메모리 누수 발생
public Object pop() {
    if (size == 0)
        throw new EmptyStackException();
    return elements[--size];  // 문제!
}

// 참조 해제로 해결
public Object pop() {
    if (size == 0)
        throw new EmptyStackException();
    Object result = elements[--size];
    elements[size] = null;  // 다 쓴 참조 해제
    return result;
}
```

**원인**: 논리적으로 size만 줄였을 뿐, 물리적인 배열에는 객체가 여전히 저장되어 있음.

스택이 계속 커졌다 줄어들었다를 반복하면, 실제로 안 쓰는 객체들이 배열 뒤쪽에 쌓여서 메모리를 계속 차지

**해결**: 다 쓴 참조를 `null`로 처리

### 2. 캐시 (Cache)

객체 참조를 캐시에 넣고 나서 잊어버리는 경우
```java
// 예시
Map<Key, Value> cache = new HashMap<>();
cache.put(key, value);
// 이후 key를 다시 사용하지 않아도 캐시에 계속 남아있음
```

**해결책**:
- `WeakHashMap` 사용: 키가 더 이상 사용되지 않으면 자동 제거
- 캐시 엔트리의 유효 기간 설정
- 백그라운드 스레드로 주기적으로 정리
- 새 엔트리 추가 시 부수 작업으로 오래된 엔트리 제거

### 3. 리스너(Listener)와 콜백(Callback)

클라이언트가 콜백을 등록만 하고 해지하지 않는 경우
```java
// 예시
public class EventSource {
    private List<EventListener> listeners = new ArrayList<>();
    
    public void addListener(EventListener listener) {
        listeners.add(listener);
        // 리스너를 제거하지 않으면 계속 메모리 차지
    }
}
```

**해결책**:
- 콜백을 약한 참조(weak reference)로 저장
- `WeakHashMap`의 키로 저장

## 객체 참조 해제의 원칙

### null 처리는 예외적인 경우에만

객체 참조를 `null` 처리하는 것은 **예외적인 경우**여야 함.

**가장 좋은 방법**: 참조를 담은 **변수를 유효 범위(scope) 밖으로 밀어내는 것**
```java
// 좋은 예 - 변수 scope를 좁게 유지
public void processData() {
    String result;
    {
        List<String> hugeData = loadHugeDataFromDB();
        result = hugeData.get(0);
        // 블록을 벗어나면 hugeData는 자동으로 GC 대상
    }
    
    doSomethingElse(result);
}
```

### null 처리가 필요한 경우

- 자기 메모리를 **직접 관리**하는 클래스
- **활성 영역**(size 이하)과 **비활성 영역**(size 이상)을 구분하는 경우
- 프로그래머만 비활성 영역의 객체가 쓸모없다는 것을 알 수 있는 경우

## 핵심 정리

- 메모리 누수는 겉으로 잘 드러나지 않아 시스템에 수년간 잠복할 수 있음
- 자기 메모리를 직접 관리하는 클래스라면 메모리 누수에 주의해야 함
- 캐시, 리스너, 콜백도 메모리 누수의 주범
- 일반적으로는 변수 scope를 최소화하는 것이 가장 좋은 해법