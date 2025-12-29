# 배열보다는 리스트를 사용하라

## 핵심 개념

배열과 제네릭은 타입 안전성 방식이 다르다. 배열은 런타임에, 제네릭은 컴파일 타임에 타입을 확인한다.

## 배열과 제네릭의 차이

### 1. 공변 vs 불공변

```java
// 배열: 공변 - 컴파일은 되지만 런타임 실패
Object[] objectArray = new Long[1];
objectArray[0] = "문자열"; // ArrayStoreException!

// 제네릭: 불공변 - 컴파일 시점에 오류 발견
List<Object> ol = new ArrayList<Long>(); // 컴파일 에러!
```

### 2. 실체화 vs 타입 소거

#### 실체화란?

**런타임에도 타입 정보가 유지**되는 것. 배열은 실행 중에도 자신이 담을 수 있는 타입을 알고 검사한다.

```java
// 배열: 런타임에도 타입 정보 유지
String[] strings = new String[5];
System.out.println(strings.getClass().getComponentType()); 
// 출력: class java.lang.String

Object[] objects = strings;
objects[0] = 123; // ArrayStoreException - 런타임에 타입 검사!
```

#### 타입 소거란?

**컴파일 타임의 타입 정보가 런타임에는 제거**되는 것. 제네릭은 컴파일 후 타입 정보가 사라진다.

```java
// 제네릭: 런타임에 타입 정보 소거
List<String> stringList = new ArrayList<>();
List<Integer> integerList = new ArrayList<>();

// 런타임엔 둘 다 그냥 ArrayList
System.out.println(stringList.getClass() == integerList.getClass()); 
// 출력: true

// 컴파일 전
public class Box<T> {
    private T item;
    public T get() { return item; }
}

// 컴파일 후 (개념적으로)
public class Box {
    private Object item; // T → Object로 변환
    public Object get() { return item; }
}
```

**타입 소거를 사용하는 이유**: 제네릭 도입 전 레거시 코드와의 호환성

## 제네릭 배열을 만들 수 없는 이유

```java
// 모두 컴파일 에러
new List<E>[]
new List<String>[]
new E[]

// 만약 허용된다면...
List<String>[] stringLists = new List<String>[1]; // (1) 가정
List<Integer> intList = List.of(42);              // (2)
Object[] objects = stringLists;                    // (3) 배열은 공변
objects[0] = intList;                              // (4) 타입 소거로 검사 불가
String s = stringLists[0].get(0);                  // (5) ClassCastException!
```

제네릭은 런타임에 타입 정보가 없어 배열의 타입 검사를 우회하므로 타입 안전성이 깨진다.

## 실전 예제

```java
// ❌ 나쁜 예: Object 배열
public class Chooser {
    private final Object[] choiceArray;
    
    public Chooser(Collection choices) {
        choiceArray = choices.toArray();
    }
    
    public Object choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceArray[rnd.nextInt(choiceArray.length)];
    }
}

// ⚠️ 경고 발생: 제네릭 배열 - 타입 안전성을 보장할 수 없어 컴파일러 경고
public class Chooser<T> {
    private final T[] choiceArray;
    
    public Chooser(Collection<T> choices) {
        choiceArray = (T[]) choices.toArray(); // 경고!
    }
}

// ✅ 좋은 예: 리스트 사용 - 타입 안전하고 형변환 불필요
public class Chooser<T> {
    private final List<T> choiceList;
    
    public Chooser(Collection<T> choices) {
        choiceList = new ArrayList<>(choices);
    }
    
    public T choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceList.get(rnd.nextInt(choiceList.size()));
    }
}
```

## 실체화 불가 타입

```java
// 런타임에 타입 정보가 소거되는 타입들 (실체화 불가)
E, List<E>, List<String>, List<? extends Number>, List<?>

// 런타임에 타입 정보가 유지되는 타입들 (실체화 가능)
String[], int[], List (raw 타입)
```

## 핵심 정리

- **배열**: 공변, 실체화, 런타임 타입 안전
- **제네릭**: 불공변, 타입 소거, 컴파일 타임 타입 안전
- **실체화**: 런타임에도 타입 정보 유지 (배열)
- **타입 소거**: 컴파일 후 타입 정보 제거 (제네릭)
- 배열과 제네릭은 잘 어우러지지 못함
- 컴파일 오류나 경고 발생 시 배열을 리스트로 대체하라
- 성능은 약간 떨어질 수 있지만 타입 안전성이 더 중요