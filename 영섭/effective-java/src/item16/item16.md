# public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라

## 핵심 원칙

**public 클래스는 절대 가변 필드를 직접 노출해서는 안됨**

## 나쁜 예: 필드를 직접 노출

```java
// 이렇게 하지 마세요!
public class Point {
    public double x;
    public double y;
}
```

**문제점:**
1. **API를 수정하지 않고는 내부 표현을 바꿀 수 없음**
2. **불변식을 보장할 수 없음** (값 검증 불가)
3. **외부에서 필드 접근 시 부수 작업을 수행할 수 없음** (로깅, 알림 등)

## 좋은 예: 접근자 메서드 사용

```java
// 이렇게 하세요!
public class Point {
    private double x;
    private double y;
    
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}
```

**장점:**
1. **내부 표현 변경 가능**: 나중에 좌표를 극좌표로 바꿔도 API는 그대로
2. **불변식 보장**: setter에서 값 검증 가능
3. **부수 작업 수행 가능**: 필드 변경 시 로깅, 이벤트 발생 등

## 예외 상황

**1. package-private 클래스나 private 중첩 클래스**

```java
// 패키지 내부에서만 사용 - 필드 노출 허용
class Point {
    public double x;
    public double y;
}

// private 중첩 클래스 - 필드 노출 허용
public class OuterClass {
    private static class InnerPoint {
        public double x;
        public double y;
    }
}
```

이 경우는 같은 패키지나 외부 클래스 내부에서만 사용되므로 필드를 노출해도 문제가 적음

**2. public 클래스의 불변 필드**

```java
// 불변 필드는 그나마 덜 나쁨 (여전히 권장하지 않음)
public final class Time {
    public final int hour;
    public final int minute;
    
    public Time(int hour, int minute) {
        if (hour < 0 || hour >= 24)
            throw new IllegalArgumentException("시간: " + hour);
        if (minute < 0 || minute >= 60)
            throw new IllegalArgumentException("분: " + minute);
        this.hour = hour;
        this.minute = minute;
    }
}
```

**불변 필드 노출의 단점:**
- 여전히 API를 변경하지 않고는 내부 표현을 바꿀 수 없음
- 필드를 읽을 때 부수 작업을 수행할 수 없음
- 하지만 불변식은 보장됨

## 실제 사례: java.awt.Point와 Dimension

```java
// 나쁜 예 - Java 플랫폼 라이브러리의 실수
public class Point {
    public int x;  // 직접 노출!
    public int y;  // 직접 노출!
}

public class Dimension {
    public int width;   // 직접 노출!
    public int height;  // 직접 노출!
}
```

이는 Java 초기의 설계 실수로, 지금까지도 성능 문제를 일으키고 있다.

## 더 나은 설계: 불변 객체

```java
// 최선의 방법 - 불변 객체로 만들기
public final class Point {
    private final double x;
    private final double y;
    
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    
    // setter 없음 - 불변!
}
```