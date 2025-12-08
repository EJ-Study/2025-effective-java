## 2장 객체 생성과 파괴

### 아이템 7, 다 쓴 객체 참조를 해제하라

- C나 C++와는 달리, 자바는 가비지 컬렉터(GC)가 다 쓴 객체를 알아서 회수해 준다.
- 메모리 관리에 더 이상 신경 쓰지 않아도 된다고 생각할 수 있으나, 전혀 사실이 아니다.

```java
package test;

import java.util.Arrays;
import java.util.EmptyStackException;

public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public Stack(int size) {
        this.elements = new Object[size];
    }

    public void push(final Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0) {
            return null;
        }
        return elements[--size];
    }

    public Object size() {
        return this.elements.length;
    }

    /**
     * 원소를 위한 공간을 적어도 하나 이상 확보
     * 배열 크기를 늘려야 할 때마다 2배씩 늘림
     */
    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, (size * 2) + 1);
        }
    }
```

<br>

### 테스트 코드

```java
package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test
void stackTest() {
    Stack stack = new Stack();

    for (int i = 0; i < 10; i++) {
        stack.push(i);
    }

    for (int i = 0; i < 10; i++) {
        stack.pop();
    }
}
```

<img width="357" height="425" alt="Image" src="https://github.com/user-attachments/assets/abb373a7-8638-45db-b5f5-51550c0c0214" />

- 위 코드는 특별한 문제가 없어 보이고, 테스트를 수행해도 통과할 것이다.
- 하지만 '메모리 누수' 라는 문제가 숨어있는데, 이 스택을 오래 실행하다 보면 점차 가비지 컬렉션 활동과 메모리 사용량이 늘어나 결국 성능이 저하될 것이다.
- 심한 경우 디스크 페이징이나 OutOfMemoryError를 일으켜 프로그램이 예기치 않게 종료되기도 한다.

```html
페이징이란, 프로그램 중 자주 사용되지 않는 부분의 작업 메모리를 주기억장치인 메모리로부터 보조기억장치인 하드디스크로 옮기는 방식을 통해 활용 가능한 메모리 공간을 증가시키기 위한 기법
이때, 한번에 옮겨지는 메모리의 용량 단위를 페이지라고 한다.

메모리 누수란, 가비지 컬렉터에 의해 메모리가 정리되지 않고 프로그램이 계속해서 메모리를 점유하고 있는 현상
```

- 위 코드에서 스택이 커졌다가(push), 줄어들때(pop) 스택에서 꺼내진 객체들을 가비지 컬렉터가 회수하지 않는다.
- 스택이 해당 객체들의 다 쓴 참조를 여전히 가지고 있기 때문
- 위 문제를 해결하려면 해당 참조를 다 썼을때 **null 처리**(참조 해제) 하면 된다.
    - 해당 Stack 클래스에서는 스택에서 꺼내질 때(pop), 각 원소의 참조가 더 이상 필요없음

```java
// 기존 pop 메서드
public Object pop() {
    if (size == 0) {
        throw new EmptyStackException();
    }
    return elements[--size];
}

// 수정 pop 메서드
public Object pop() {
    if (size == 0) {
        throw new EmptyStackException();
    }
    Object result = elements[--size];
    elements[size] = null;
    return result;
}
```

### Java의 Stack 클래스

```java
    public synchronized E pop() {
    E obj;
    int len = size();

    obj = peek();
    removeElementAt(len - 1);

    return obj;
}

public synchronized void removeElementAt(int index) {
    modCount++;
    if (index >= elementCount) {
        throw new ArrayIndexOutOfBoundsException(index + " >= " +
            elementCount);
    } else if (index < 0) {
        throw new ArrayIndexOutOfBoundsException(index);
    }
    int j = elementCount - index - 1;
    if (j > 0) {
        System.arraycopy(elementData, index + 1, elementData, index, j);
    }
    elementCount--;
    elementData[elementCount] = null; /* to let gc do its work */
}

```

### Stack 클래스는 왜 메모리 누수에 취약한 걸까?

- 스택이 자기 자신의 메모리를 직접 관리하기 때문
- Stack 클래스는 배열(elements)로 저장소 풀을 만들어 원소들을 관리함
- 배열의 활성 영역부분에 속한 원소들은 사용되고, 비활성 영역은 쓰이지 않는데 문제점은 이러한 비활성 영역을 가비지 컬렉터가 알 방법이 없다는 것
- 보통 자신의 메모리를 직접 관리하는 클래스는 프로그래머가 항상 메모리 누수에 주의해야 함

### 3. 캐시에서의 메모리 누수

캐시 역시 메모리 누수를 일으키는 주범이다. 객체 참조를 캐시에 넣어두고 이 사실을 잊은 채 그 객체를 다 쓴 뒤로도 한참 그냥 놔둘 수 있다.

#### WeakHashMap을 사용한 해결 방법

캐시 외부에서 키(key)를 참조하는 동안만 엔트리가 살아 있는 캐시가 필요한 상황이라면 `WeakHashMap`을 사용하자. 다 쓴 엔트리는 그 즉시 자동으로 제거될 것이다.

**WeakHashMap의 특징:**

- 기본적으로 `HashMap`과 유사하지만 키에 대한 참조가 **약한 참조(weak reference)** 로 저장됨
- 약한 참조란 `WeakHashMap`의 키로 사용되는 객체에 대한 유일한 참조가 `WeakHashMap` 내부에만 있을 때, 일반적인 참조와 달리 가비지 컬렉터에 의해 언제든지 회수될 수 있음

```html
[강한 참조]
new 할당 후 새로운 객체를 만들어 해당객체를 참조하는 방식이다.
참조가 해제되지 않는 이상 GC의 대상이 되지 않는다.

[약한 참조]
Integer prime = 1;
WeakReference<Integer> weak = new WeakReference<Integer>(prime);
    위와 같이 WeakReference Class를 이용하여 생성이 가능하다. prime == null이 되면(해당 객체를 가리키는 참조가 WeakReference 뿐일 경우) GC 대상이 된다.
```

### 4. 캐시에서의 메모리 누수 해결방법

캐시를 만들 때 보통은 캐시 엔트리의 유효 기간을 정확히 정의하기 어렵다. 따라서 시간이 지날수록 엔트리의 가치를 떨어뜨리는 방식을 흔히 사용한다. 이런 방식에서는 쓰지 않는 엔트리를 이따금 청소해줘야 한다.

**해결 방법:**

- `ScheduledThreadPoolExecutor` 같은 백그라운드 스레드를 활용
- 캐시에 새 엔트리를 추가할 때 부수 작업으로 수행 (권장)
- `LinkedHashMap`의 `removeEldestEntry` 메서드를 사용
    - 이 메서드는 맵에 새 항목이 추가될 때마다 호출된다.
    - 이 메서드를 오버라이드하여 맵의 크기가 특정 임계값을 초과하면 가장 오래된 항목을 자동으로 제거하는 로직을 구현할 수 있다.

#### LinkedHashMap을 사용한 LRU 캐시

**LinkedHashMap의 특징:**

1. **순서 보장**: 키-값 쌍의 삽입 순서를 기억하고, 반복 시 삽입된 순서대로 반환
2. **액세스 순서 모드**: `get`과 `put` 연산 시 해당 항목이 연결 목록의 끝으로 이동 (LRU 캐시 구현에 유용)
3. **removeEldestEntry 메서드**: 맵에 새 항목이 추가될 때마다 호출되며, 오버라이드하여 가장 오래된 항목을 자동 제거 가능

```java
import java.util.LinkedHashMap;
import java.util.Map;

// 캐시에 새 엔트리를 추가할 때 부수작업으로 수행하는 방법 예제
public class Example {
    public static void main(String[] args) {
        // LRU 캐시로 동작하는 LinkedHashMap
        int cacheSize = 5;
        LinkedHashMap<String, String> lruCache = new LinkedHashMap<String, String>(16, 0.75f, true) {
            // true: 액세스 순서 모드(accessOrder) 활성화
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > cacheSize;
                // true: 가장 오래된 항목 제거, false: 제거 안됨
            }
        };

        for (int i = 1; i <= 10; i++) {
            lruCache.put("key" + i, "value" + i);
        }

        System.out.println(lruCache); // 가장 최근에 추가된 5개의 항목만 출력
    }
}
```

### 5. 리스너(listener) 또는 콜백(callback)에서의 메모리 누수

클라이언트가 콜백을 등록만 하고 명확히 해지하지 않는다면, 뭔가 조치해주지 않는 한 콜백은 계속 쌓여갈 것이다. 이럴 때 콜백을 **약한 참조(weak reference)** 로 저장하면 가비지 컬렉터가 즉시
수거해간다.

```java
import java.util.WeakHashMap;

interface EventListener {
    void onEvent(String event);
}

class EventManager {
    // 리스너를 WeakHashMap의 키로 저장하여 약한 참조를 유지
    private WeakHashMap<EventListener, Boolean> listeners = new WeakHashMap<>();

    public void registerListener(EventListener listener) {
        listeners.put(listener, Boolean.TRUE);
    }

    public void fireEvent(String event) {
        for (EventListener listener : listeners.keySet()) {
            listener.onEvent(event);
        }
    }
}

public class Example {
    public static void main(String[] args) {
        EventManager manager = new EventManager();

        // 리스너를 등록
        EventListener listener = event -> System.out.println("Received event: " + event);
        manager.registerListener(listener);

        // 이벤트 발생
        manager.fireEvent("Test Event 1");

        // 리스너 참조를 제거
        listener = null;

        // 가비지 컬렉터를 힌트로 실행
        System.gc();

        // 이벤트 발생
        manager.fireEvent("Test Event 2"); // 이전 리스너는 더 이상 출력되지 않는다.
    }
}
```
