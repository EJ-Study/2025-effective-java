## 6장 열거 타입과 애너테이션

### 아이템 39, 명명 패턴보다 애너테이션을 사용하라

- JUnit3은 명명 패턴(예: `test`로 시작하는 메서드)을 사용했지만 단점이 많음
- 애너테이션은 이 모든 문제를 해결해주는 개념
- **애너테이션으로 할 수 있는 일을 명명 패턴으로 처리하지 말자**

### 명명 패턴의 단점

**예시: JUnit 3의 `test`로 시작하는 메서드**

**문제점:**

1. **오타의 위험**
    - `tsetSafetyOverride`로 오타를 내면 JUnit이 무시하고 지나침
    - 개발자는 테스트가 통과했다고 오해할 수 있음

2. **올바른 프로그램 요소에서만 사용되리라 보증할 방법이 없음**
    - 클래스 이름을 `TestSafetyMechanisms`로 지어도 JUnit은 무시
    - 경고 메시지조차 출력하지 않음

### 애너테이션의 장점

- 컴파일 타임에 오류 검출 가능
- 타입 안전성 보장
- 매개변수 전달 가능
- 명확하고 유지보수 용이

### 마커 애너테이션: @Test

**애너테이션 정의:**

```java

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
}
```

**메타 애너테이션:**

- `@Retention(RetentionPolicy.RUNTIME)`: 런타임에도 유지되어야 함
- `@Target(ElementType.METHOD)`: 메서드 선언에서만 사용 가능

**사용 예시:**

```java
public class Sample {
    @Test
    public static void m1() {
    }  // 성공

    public static void m2() {
    }

    @Test
    public static void m3() {  // 실패
        throw new RuntimeException("실패");
    }

    @Test
    public void m5() {
    }  // 잘못 사용: 정적 메서드가 아님
}
```

**테스트 러너:**

```java
public class RunTests {
    public static void main(String[] args) throws Exception {
        Class<?> testClass = Class.forName(args[0]);
        for (Method m : testClass.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Test.class)) {  // 특정 애너테이션이 붙어있는지 확인
                try {
                    m.invoke(null);  // static 메서드 호출
                    passed++;
                } catch (InvocationTargetException wrappedExc) {
                    Throwable exc = wrappedExc.getCause();
                    System.out.println(m + " 실패: " + exc);
                }
            }
        }
    }
}

// JVM 내부 동작 (의사 코드)
public Object invoke(Object obj, Object... args) {
    if (isStatic(this)) {
        // 정적 메서드: obj 무시하고 클래스 레벨에서 실행
        return executeStaticMethod(args);
    } else {
        // 인스턴스 메서드: obj를 this로 사용
        if (obj == null) {
            throw new NullPointerException();
        }
        return executeInstanceMethod(obj, args);
    }
}
```

### 매개변수를 받는 애너테이션: @ExceptionTest

**애너테이션 정의:**

```java

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
    Class<? extends Throwable> value(); // 매개변수로 예외 타입을 받음
}
```

**사용 예시:**

```java
public class Sample2 {
    @ExceptionTest(ArithmeticException.class)
    public static void m1() {  // 성공
        int i = 0;
        i = i / i;
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m2() {  // 실패: 다른 예외 발생
        int[] a = new int[0];
        int i = a[1];
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m3() {
    }  // 실패: 예외가 발생하지 않음
}
```

**테스트 러너:**

```java
if(m.isAnnotationPresent(ExceptionTest .class)){
tests++;
    try{
    m.

invoke(null);
        System.out.

printf("테스트 %s 실패: 예외를 던지지 않음%n",m);
    }catch(
InvocationTargetException wrappedEx){
Throwable exc = wrappedEx.getCause();
Class<? extends Throwable> excType =
    m.getAnnotation(ExceptionTest.class).value();
        if(excType.

isInstance(exc)){
passed++;
    }else{
    System.out.

printf("테스트 %s 실패: 기대한 예외 %s, 발생한 예외 %s%n",
    m, excType.getName(),exc);
    }
    }catch(
Exception exc){
    System.out.

println("잘못 사용한 @ExceptionTest: "+m);
    }
        }
```

### 반복 가능 애너테이션: @Repeatable

**애너테이션 정의:**

```java
// 반복 가능한 애너테이션
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ExceptionTestContainer.class)
public @interface ExceptionTest {
    Class<? extends Throwable> value();
}

// 컨테이너 애너테이션
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTestContainer {
    ExceptionTest[] value();
}
```

**사용 예시:**

```java

@ExceptionTest(IndexOutOfBoundsException.class)
@ExceptionTest(NullPointerException.class)
public static void doublyBad() {
    List<String> list = new ArrayList<>();
    list.addAll(5, null);
}
```

**주의사항:**

반복 가능 애너테이션을 여러 개 달면 컨테이너 애너테이션 타입이 적용됨:

- `getAnnotationsByType()`: 반복 가능 애너테이션과 컨테이너를 모두 가져옴
- `isAnnotationPresent()`: 둘을 명확히 구분함

**올바른 처리 방법:**

```java
public class RunTests {
    public static void main(String[] args) throws Exception {
        int tests = 0;
        int passed = 0;
        Class<?> testClass = Class.forName(args[0]);

        for (Method m : testClass.getDeclaredMethods()) {
            // 반복 가능 애너테이션을 여러 번 단 경우, 컨테이너가 적용되므로 둘 다 확인
            if (m.isAnnotationPresent(ExceptionTest.class) ||
                m.isAnnotationPresent(ExceptionTestContainer.class)) {
                tests++;
                try {
                    m.invoke(null);
                    System.out.println(m + ", 테스트 실패 (예외를 던지지 않음)");
                } catch (InvocationTargetException wrappedExc) {
                    Throwable exc = wrappedExc.getCause();
                    int oldPassed = passed;

                    // getAnnotationsByType()은 반복 가능 애너테이션과 컨테이너를 모두 가져옴
                    ExceptionTest[] excTests = m.getAnnotationsByType(ExceptionTest.class);

                    for (ExceptionTest excTest : excTests) {
                        if (excTest.value().isInstance(exc)) {
                            System.out.printf("테스트 %s 성공: 기대한 예외 %s, 발생한 예외 %s%n",
                                m, excTest.value().getName(), exc);
                            passed++;
                            break;
                        }
                    }

                    if (oldPassed == passed) {
                        System.out.printf("테스트 %s 실패: 발생한 예외 %s%n", m, exc);
                    }
                } catch (Exception exc) {
                    System.out.println("잘못 사용한 @ExceptionTest: " + m);
                }
            }
        }

        System.out.printf("성공: %d, 실패: %d%n", passed, tests - passed);
    }
}
```

### 요약

**명명 패턴의 단점:**

- 오타의 위험
- 올바른 프로그램 요소에서만 사용되리라 보증할 방법이 없음
- 매개변수 전달 방법이 없음

**애너테이션의 장점:**

- 컴파일 타임 오류 검출
- 타입 안전성 보장
- 매개변수 전달 가능
- 명확하고 유지보수 용이

**결론:**

- 애너테이션으로 할 수 있는 일을 명명 패턴으로 처리하지 말자
- 자바 프로그래머라면 자바가 제공하는 애너테이션 타입들을 사용해야 한다 (ex: `@Override`, `@Deprecated`, `@SuppressWarnings`)
- 도구 제작자가 아니라면 애너테이션 타입을 직접 정의할 일은 거의 없지만, 사용법은 알아두자
