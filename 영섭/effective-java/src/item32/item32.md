# 제네릭과 가변인수를 함께 쓸 때는 신중하라

## 핵심 요약

가변인수(varargs)와 제네릭을 함께 사용하면 타입 안전성이 깨질 수 있습니다. 가변인수 메서드를 호출하면 가변인수를 담기 위한 배열이 자동으로 생성되는데, 제네릭 배열은 본래 생성이 금지되어 있기 때문입니다.

## 왜 문제가 되는가?

**1. 힙 오염(Heap Pollution) 발생**

```java
static void dangerous(List<String>... stringLists) {
    List<Integer> intList = List.of(42);
    Object[] objects = stringLists;  // 배열은 공변(covariant)
    objects[0] = intList;             // 힙 오염 발생!
    String s = stringLists[0].get(0); // ClassCastException!
}
```

**2. 제네릭 배열 생성 금지와의 모순**

```java
// 컴파일 에러 - 실체화 불가 타입
List<String>[] stringLists = new List<String>[1];

// 하지만 가변인수는 허용됨 (경고만 발생)
static void method(List<String>... stringLists) { }
```

## 안전하게 사용하는 방법

### @SafeVarargs 애너테이션

메서드가 타입 안전함을 보장할 수 있다면 `@SafeVarargs`로 경고를 제거할 수 있습니다.

**안전한 사용 조건:**
1. 가변인수 배열에 아무것도 저장하지 않음
2. 배열의 참조가 외부로 노출되지 않음

```java
@SafeVarargs
static <T> List<T> flatten(List<? extends T>... lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists) {
        result.addAll(list);  // 안전: 읽기만 수행
    }
    return result;
}
```

### 위험한 예시

```java
// 위험: 배열 참조 노출
static <T> T[] toArray(T... args) {
    return args;  // 가변인수 배열을 그대로 반환 - 위험!
}

static <T> T[] pickTwo(T a, T b, T c) {
    return toArray(a, b);  // Object[]가 반환됨
}

String[] attributes = pickTwo("좋은", "빠른", "저렴한");
// ClassCastException 발생!
```

## 대안: List 매개변수 사용

가변인수 대신 `List`를 사용하면 컴파일 시점에 타입 안전성을 보장할 수 있습니다.

```java
// 안전한 대안
static <T> List<T> flatten(List<List<? extends T>> lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists) {
        result.addAll(list);
    }
    return result;
}

// 사용
List<String> result = flatten(List.of(
    List.of("a", "b"),
    List.of("c", "d")
));
```

## 실전 가이드라인

**가변인수와 제네릭을 함께 써야 한다면:**

1. 메서드가 타입 안전한지 확실히 검증
2. `@SafeVarargs` 애너테이션 추가 (재정의 불가능한 메서드에만)
3. 가변인수 배열에 저장하지 않고, 외부로 노출하지 않기

**더 안전한 방법:**
- 가능하면 `List` 매개변수 사용
- 타입 안전성을 컴파일 타임에 보장