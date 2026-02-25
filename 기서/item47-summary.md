# Effective Java Item 47 — 반환 타입으로는 스트림보다 컬렉션이 낫다

## Java8 이후 원소 시퀀스를 반환하는 방법

- 기존에는 Collection, Iterable 이라는 선택지가 존재했다.
- Java8 이후로는 Stream 이라는 선택지가 하나 더 늘었다.
- Stream 은 반환 타입으로 사용하기보다는 단순히 컬렉션 처리를 위해 사용하는 것이 좋다.
- 반환은 다시 컬렉션으로 변경해주는 것이 활용성이 좋다.
    - someStream.collect(Collectors.toList()) 와 같은 함수를 이용하면 쉽다.

## Stream 이 Iterable 을 확장하지 않는데서 생기는 문제

- 기존에 Stream 의 forEach() 는 Consumer 인터페이스를 사용하는 만큼 값을 생산하기보다 소비하는데에 이용하는 것이 모범적이다.
- 이 상황에서 일반 자바 API의 for-each 문법을 사용하려 하면 다음과 같은 일이 벌어진다.

```java
Stream<ProcessHandle> processes = ProcessHandle.allProcesses();

public void test() {
    for (ProcessHandle ph : ProcessHandle.allProcesses()::iterator) {
        /**
         * 이 코드는 컴파일 에러가 발생한다.
         * Stream.iterator()의 반환 값은 Iterator<ProcessHandle> 이지만, for-each 문법은 Iterable<ProcessHandle>을 요구하며,
         * 컴파일러가 이를 추론해주지 않음
         */
    }
}
```

```java

@Test
public void processHandleTest2() {
// 불편한 수동 캐스팅 필요
    Iterable<ProcessHandle> processHandles = ProcessHandle.allProcesses()::iterator;

    for (ProcessHandle processHandle : processHandles) {
        System.out.println("processHandle = " + processHandle.info());
    }

}

```

- 자바의 기본 for each 문법을 사용하려 하지 말고 스트림을 사용하면 깔끔하긴 하다.
- 그런데 어떠한 이유로든 Iterable 을 이용해야 한다면, 수동 캐스팅을 해야 한다.
- ::iterator 보다는 애초에 반환 타입 자체를 collect() 메서드를 통해 컬렉션으로 변경해주자.

## 어댑터 메서드 만들기

```java
public static <E> Iterable<E> iterableOf(Stream<E> stream) {
    return stream::iterator;
}
```

- iterableOf()와 같은 어댑터 메서드를 구현하면 어떤 스트림도 for-each 문으로 반복할 수 있다.
- 하지만, Collection 인터페이스는 stream() 과 Iterable 구현 모두 하기 때문에 기왕이면 Collection 이나 그 하위 타입을 반환 혹은 파라미터 타입에 사용하는 게 최선이다.

## 정리

- **대부분의 공개 API**: `Collection`(또는 `List`, `Set`) 반환이 최선.
- **이미 컬렉션을 보유**하거나 **원소 수가 작다**면 표준 컬렉션(`ArrayList`, `HashSet`)에 담아 반환.
- **원소 수가 크지만 표현이 간결**하면 전용 컬렉션 구현을 고려.
- **오직 스트림 파이프라인에서만** 쓰일 것이 확실하면 `Stream`을 반환할 수 있다.
