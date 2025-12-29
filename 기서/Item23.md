## 4장 클래스와 인터페이스

### 아이템 23, 태그 달린 클래스보다는 클래스 계층구조를 활용하라

- 태그 달린 클래스는 장황하고, 오류를 내기 쉽고, 비효율적이다.
- 태그 달린 클래스는 클래스 계층구조를 어설프게 흉내 낸 아류이다.
- 태그 달린 클래스를 사용하지 말고 클래스 계층구조를 사용하라.

### 태그 달린 클래스란?

태그 달린 클래스는 두 가지 이상의 의미를 표현할 수 있으며, 그중 현재 표현하는 의미를 태그 값으로 알려주는 클래스를 의미한다.

**예시:**

```java
public class Figure {
    enum Shape {RECTANGLE, CIRCLE}

    // 태그 필드 - 현재 모양을 나타낸다
    final Shape shape;

    // 다음 필드들은 모양이 사각형(RECTANGLE)일 때만 쓰인다
    double length;
    double width;

    // 다음 필드는 모양이 원(CIRCLE)일 때만 쓰인다
    double radius;

    // 원용 생성자
    Figure(double radius) {
        shape = Shape.CIRCLE;
        this.radius = radius;
    }

    // 사각형용 생성자
    Figure(double length, double width) {
        shape = Shape.RECTANGLE;
        this.length = length;
        this.width = width;
    }

    double area() {
        switch (shape) {
            case RECTANGLE:
                return length * width;
            case CIRCLE:
                return Math.PI * (radius * radius);
            default:
                throw new AssertionError(shape);
        }
    }
}
```

### 태그 달린 클래스의 단점

1. **불필요한 코드가 많다**
    - 열거 타입 선언, 태그 필드, `switch` 문 등

2. **가독성이 나쁘다**
    - 여러 구현이 한 클래스에 혼합되어 있음

3. **메모리 낭비**
    - 다른 의미를 위한 코드도 언제나 함께 하기 때문에 메모리도 많이 사용함
    - 필드들을 `final`로 선언하기 위해 해당 의미에 쓰이지 않는 필드들까지 생성자에서 초기화해야 함

4**확장성 문제**

- 새로운 의미를 추가하려면 모든 곳을 수정해야 함
- 생성자 오버로딩 문제로 인해 새로운 타입 추가가 어려울 수 있음

**결론:** 태그 달린 클래스는 장황하고, 비효율적이다.

### 클래스 계층구조

태그 달린 클래스를 클래스 계층구조로 변환하면 모든 단점을 해소할 수 있다.

### 태그 달린 클래스를 클래스 계층구조로 변환하는 방법

1. **계층구조의 루트가 될 추상 클래스를 정의**
   - 태그 값에 따라 동작이 달라지는 메서드들을 루트 클래스의 추상 메서드로 선언

2. **공통 메서드와 필드 처리**
   - 태그 값에 상관없이 동작이 일정한 메서드들을 루트 클래스에 일반 메서드로 추가
   - 공통으로 사용하는 데이터 필드들도 전부 루트 클래스로 올림

3. **구체 클래스 정의**
   - 루트 클래스를 확장한 구체 클래스를 의미별로 하나씩 정의

**변환된 예시:**

```java
abstract class Figure {
    abstract double area();
}

class Circle extends Figure {
    final double radius;

    Circle(double radius) {
        this.radius = radius;
    }

    @Override
    double area() {
        return Math.PI * (radius * radius);
    }
}

class Rectangle extends Figure {
    final double length;
    final double width;

    Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }

    @Override
    double area() {
        return length * width;
    }
}
```

**장점:**

1. **코드가 간결함**
    - 태그 필드, `switch` 문 등 불필요한 코드 제거

2. **가독성 향상**
    - 각 클래스가 하나의 의미만 표현

3. **메모리 효율적**
    - 필요한 필드만 포함

4. **타입 안정성**
    - 인스턴스 타입만으로 의미를 알 수 있음

5. **확장성**
    - 새로운 타입 추가가 쉬움

### 유연성 비교

**정사각형(Square)을 추가하는 경우:**

#### 태그 달린 클래스

```java
public class Figure {
    enum Shape {RECTANGLE, CIRCLE, SQUARE}

    final Shape shape;
    double length;
    double width;
    double radius;
    double side;  // 새로운 필드 추가

    // ❌ 문제: 생성자 오버로딩 불가능
    // 원형 생성자와 매개변수 1개가 겹침
    Figure(double radius) { ...}

    Figure(double side) { ...}  // 컴파일 에러!

    Figure(double length, double width) { ...}

    double area() {
        switch (shape) {
            case RECTANGLE:
                return length * width;
            case CIRCLE:
                return Math.PI * (radius * radius);
            case SQUARE:
                return side * side;  // 새로운 case 추가
            default:
                throw new AssertionError(shape);
        }
    }
}
```

**문제점:**

- 생성자 오버로딩 문제로 새로운 타입 추가가 어려움
- 모든 곳을 수정해야 함

#### 클래스 계층구조

```java
class Square extends Figure {
    final double side;

    Square(double side) {
        this.side = side;
    }

    @Override
    double area() {
        return side * side;
    }
}
```

**장점:**

- 전혀 문제 없이 새로운 클래스를 추가할 수 있음
- 기존 코드 수정 불필요

**결론:**

- 태그 달린 클래스를 사용하지 말고 클래스 계층구조를 사용하라
- 새로운 클래스를 작성하는 데 태그 필드가 등장한다면 태그를 없애고 계층구조로 대체하는 방법을 생각해본다
- 기존 클래스가 태그 필드를 사용하고 있다면 계층구조로 리팩토링하는 것을 고민해본다

