# 추상 클래스보다는 인터페이스를 우선하라

## 핵심 개념

자바에서 다중 구현 메커니즘으로 인터페이스와 추상 클래스가 있는데, 인터페이스가 추상 클래스보다 우수한 선택인 경우가 많다.

## 인터페이스의 장점

**1. 기존 클래스에 손쉽게 새로운 인터페이스를 구현할 수 있다**
- 요구하는 메서드를 추가하고 implements 구문만 추가하면 끝
- 추상 클래스는 계층구조상 두 클래스의 공통 조상이어야 하므로 제약이 큼

**2. 믹스인(mixin) 정의에 적합**
- 믹스인: 클래스가 본래의 타입 외에 특정 선택적 행위를 제공한다고 선언하는 효과
- 예: Comparable, Cloneable, Serializable
- 추상 클래스는 단일 상속만 지원하므로 믹스인 정의 불가능

**3. 계층구조가 없는 타입 프레임워크를 만들 수 있다**
```java
// 가수 인터페이스
public interface Singer {
    AudioClip sing(Song s);
}

// 작곡가 인터페이스
public interface Songwriter {
    Song compose(int chartPosition);
}

// 가수이면서 작곡가인 인터페이스 (조합 가능)
public interface SingerSongwriter extends Singer, Songwriter {
    AudioClip strum();
    void actSensitive();
}
```

만약 추상 클래스로 만들면 조합 폭발(combinatorial explosion)이 발생

**4. 래퍼 클래스 패턴과 함께 사용하면 안전하고 강력**
- 기능을 향상시키는 안전한 방법 제공
- 추상 클래스는 상속으로만 기능을 추가할 수 있어 취약함

## 디폴트 메서드

자바 8부터 인터페이스에 디폴트 메서드를 제공할 수 있어, 인터페이스와 추상 클래스의 차이가 더 줄어듦

```java
public interface MyInterface {
    // 추상 메서드
    void abstractMethod();
    
    // 디폴트 메서드
    default void defaultMethod() {
        System.out.println("기본 구현");
    }
}
```

**디폴트 메서드의 제약사항:**
- Object의 메서드(equals, hashCode, toString)는 디폴트 메서드로 제공 불가
- 인터페이스는 인스턴스 필드를 가질 수 없음
- public이 아닌 정적 멤버를 가질 수 없음 (private 정적 메서드는 예외)

## 템플릿 메서드 패턴: 골격 구현 클래스

인터페이스와 추상 골격 구현 클래스를 함께 제공하면 인터페이스와 추상 클래스의 장점을 모두 취할 수 있습니다.

```java
// 인터페이스
public interface MyInterface {
    void method1();
    void method2();
    void method3();
}

// 골격 구현 (추상 클래스)
public abstract class AbstractMyInterface implements MyInterface {
    // 공통 구현
    @Override
    public void method1() {
        // 기본 구현
    }
    
    @Override
    public void method2() {
        // 기본 구현
    }
    
    // method3는 하위 클래스에서 구현
}

// 실제 구현
public class ConcreteClass extends AbstractMyInterface {
    @Override
    public void method3() {
        // 구체적 구현
    }
}
```

**관례:** 골격 구현 클래스는 Abstract + 인터페이스명으로 명명 (예: AbstractCollection, AbstractSet, AbstractMap)

**시뮬레이트한 다중 상속:**
골격 구현 클래스를 private 내부 클래스로 만들어서 상속받고, 외부 클래스는 이를 **위임(delegation)**하는 방식

```java
// 인터페이스
public interface Flyable {
    void fly();
    void land();
}

// 골격 구현 클래스
public abstract class AbstractFlyable implements Flyable {
    // 공통 구현 제공
    @Override
    public void fly() {
        System.out.println("Taking off...");
        System.out.println("Flying high!");
    }

    @Override
    public void land() {
        System.out.println("Landing safely...");
    }
}

// 이미 다른 클래스를 상속받은 상황
public class Vehicle {
    private String brand;

    public void move() {
        System.out.println("Moving...");
    }
}

// 시뮬레이트한 다중 상속
public class FlyingCar extends Vehicle implements Flyable {

    // private 내부 클래스가 골격 구현을 상속
    private class FlyableHelper extends AbstractFlyable {
        // 필요시 메서드 오버라이드 가능
    }

    // 내부 클래스 인스턴스 생성
    private final FlyableHelper flyableHelper = new FlyableHelper();

    // Flyable 인터페이스의 메서드들을 내부 클래스에 위임
    @Override
    public void fly() {
        flyableHelper.fly();
    }

    @Override
    public void land() {
        flyableHelper.land();
    }

    // Vehicle의 기능도 사용 가능
    public void drive() {
        super.move();
    }
}

// 사용
public class Main {
    public static void main(String[] args) {
        FlyingCar car = new FlyingCar();

        car.drive();  // Vehicle의 기능
        car.fly();    // AbstractFlyable의 기능 (위임을 통해)
        car.land();   // AbstractFlyable의 기능 (위임을 통해)

        // 마치 Vehicle과 AbstractFlyable을 
        // 동시에 상속받은 것처럼 동작!
    }
}
```
