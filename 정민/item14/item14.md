# 아이템14. Comparable을 구현할지 고려하라

<br/>

**💡 핵심 내용**
> 순서를 고려해야 하는 값 클래스를 작성한다면 꼭 Comparable 인터페이스를 구현하여,
> 그 인스턴스들을 쉽게 정렬 / 검색 / 비교 기능을 제공하는 컬렉션과 어울려지도록 해야한다.
>
> compareTo 메서드에서 필드의 값을 비교할 때, 아래 두가지 방식 중 하나를 사용한다.
> - 박싱된 기본 타입 클래스가 제공하는 정적 compare 메서드
> - Comparator 인터페이스가 제공하는 비교자 생성 메서드

<br/>

## 0. 개요

Comparable을 구현하면 객체 간 순서를 비교할 수 있다.
- 단 하나만 제공되는 compareTo 메서드를 통해 동치성과 순서를 모두 비교할 수 있다.

<br/>

## 1. compareTo 메서드의 규약

#### compareTo 메서드의 반환값
- x.compareTo(y)
    - x < y : 음수
    - x == y : 0
    - x > y : 양수

#### (1) 반사성
- x.compareTo(x) == 0
    - 자기 자신과 비교하면 0(동치)을 반환해야 한다.

#### (2) 대칭성
- x.compareTo(y)) == -(y.compareTo(x))
    - x가 y보다 크면, y는 x보다 작아야 한다.

#### (3) 추이성
- x.compareTo(y) > 0 이고 y.compareTo(z) > 0) 이면, x.compareTo(z) > 0
    - x가 y보다 크고, y가 z보다 크면, x는 z보다 커야 한다.

#### (4) compareTo 메서드의 동치성 결과와 equals 메서드의 결과와 같아야함 (권장사항)
- (x.compareTo(y) == 0) == (x.equals(y))
    -  같은 데이터를 넣었는데 컬렉션 종류에 따라 결과가 달라질 수 있다.
    - 정렬 컬렉션은 equals대신 compareTo로 동치성을 판단하기 때문에

<div style="margin-left: 35px">
<details>
<summary>이 규약을 지키지 않는 BigDecimal클래스 예시</summary>
<div markdown="1">

- BigDecimal클래스는 compareTo와 equals의 결과가 일관되지 않다.

```java
public class Main {
    public static void main(String[] args) {
        BigDecimal bd1 = new BigDecimal("1.0");
        BigDecimal bd2 = new BigDecimal("1.00");

        // equals: false
        bd1.equals(bd2);  // false

        // compareTo: 0 (값은 같음)
        bd1.compareTo(bd2);  // 0

        // 결과
        HashSet<BigDecimal> hashSet = new HashSet<>();
        hashSet.add(bd1);
        hashSet.add(bd2); // → 크기: 2개 (equals 사용)
        
        TreeSet<BigDecimal> treeSet = new TreeSet<>();
        treeSet.add(bd1);
        treeSet.add(bd2); // → 크기: 1개 (compareTo 사용)
        
    }
}
```

</div>
</details>
</div>

<br/>

### 주의사항
- Comparable을 구현한 클래스를 상속하여 값 컴포넌트를 더 추가하면, compareTo 규약을 지킬 수 없다.

<div style="margin-left: 10px">
<details>
<summary>문제 상황</summary>
<div markdown="1">

- ColorPoint extends Point면 ColorPoint는 Point와도 비교 가능해야 한다.
    - Point의 비교기준(x,y)과 ColorPoint의 비교기준(x,y,color)이 달라서 규약을 지킬 수 없다.

```java
// Comparable을 구현한 클래스
class Point implements Comparable<Point> {
    // constructor, getter 생략
    private int x, y;

    public int compareTo(Point p) {
        int result = Integer.compare(x, p.x);
        if (result == 0) {
            result = Integer.compare(y, p.y);
        }
        return result;
    }
}

// Comparable을 구현한 클래스를 상속한 클래스
class ColorPoint extends Point {
    // constructor, getter 생략
    private String color;  // 새로운 값 컴포넌트

    @Override
    public int compareTo(Point p) {
        
    }
}

```

</div>
</details>
</div>

<br/>

<div style="margin-left: 10px">
<details>
<summary>우회법</summary>
<div markdown="1">

```java
class ColorPoint implements Comparable<ColorPoint> {
    private final Point point;  // 원래 클래스를 필드로
    private final String color;
    
    public ColorPoint(int x, int y, String color) {
        this.point = new Point(x, y);
        this.color = color;
    }
    
    // 뷰 메서드
    public Point getPoint() {
        return point;
    }
    
    @Override
    public int compareTo(ColorPoint other) {
        // ColorPoint끼리만 비교 → 규약 준수 가능
        int result = point.compareTo(other.point);
        if (result == 0) {
            result = color.compareTo(other.color);
        }
        return result;
    }
}

```

</div>
</details>
</div>

<br/>

## 2. compareTo 메서드 작성 요령
### (1) compareTo 메서드에서 필드의 값을 비교할 때 < 와 > 연산자는 쓰지 말아야 한다.
- 비교 연산자 사용 시 실수로 인해 오류가 발생할 수 있고, 원시타입이 아닌 경우에는 사용할 수 없다. 대신 아래 두 방식 중 하나를 사용한다.
    - 박싱된 기본 타입 클래스가 제공하는 정적 compare 메서드
    - Comparator 인터페이스가 제공하는 비교자 생성 메서드


<details>
<summary>예시 코드</summary>
<div markdown="1">

- 박싱된 기본 타입 클래스가 제공하는 정적 compare 메서드 사용
```java
public class PhoneNumber implements Comparable<PhoneNumber> {
    public Short areaCode;
    public Short prefix;
    public Short lineNum;

    @Override
    public int compareTo(PhoneNumber pn) {
        int result = Short.compare(areaCode, pn.areaCode); // 가장 중요한 필드
        if (result == 0) {
            result = Short.compare(prefix, pn.prefix); // 두 번째로 중요한 필드
            if (result == 0) {
                result = Short.compare(lineNum, pn.lineNum); // 세 번째로 중요한 필드
            }
        }
        return result;
    }
}

```
<br/>

- Comparator 인터페이스가 제공하는 비교자 생성 메서드
```java
public class PhoneNumber implements Comparable<PhoneNumber> {
    public Short areaCode;
    public Short prefix;
    public Short lineNum;

    private static final Comparator<PhoneNumber> COMPARATOR =
            Comparator.comparingInt((PhoneNumber pn) -> pn.areaCode)
                    .thenComparingInt((PhoneNumber pn) -> pn.prefix)
                    .thenComparingInt((PhoneNumber pn) -> pn.lineNum);

    @Override
    public int compareTo(PhoneNumber pn) {
        return COMPARATOR.compare(this, pn);
    }
}

```
</div>
</details>

<br/>

### (2) 값의 차를 기준으로 비교하는 방식은 사용하면 안된다.
- 정수 오버플로가 발생하거나 부동소수점 계산 방식에 따른 오류가 발생할 수 있다. 대신 아래 두 방식 중 하나를 사용한다.
    - 박싱된 기본 타입 클래스가 제공하는 정적 compare 메서드
    - Comparator 인터페이스가 제공하는 비교자 생성 메서드

<details>
<summary>예시 코드</summary>
<div markdown="1">

- 해시코드 값의 차를 기준으로 하는 비교자
```java
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
        return o1.hashCode() - o2.hashCode(); // Integer.MAX_VALUE - (-1) 일 경우, 오버플로 발생 
    }
}
```

- 정적 compare 메서드를 활용한 비교자
```java
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
        return Integer.compare(o1.hashCode(), o2.hashCode());
    }
}
```

- 비교자 생성 메서드를 활용한 비교자
```java
static Comparator<Object> hashCodeOrder = hashCodeOrder =
        Comparator.comparingInt(o -> o.hashCode());
```

</div>
</details>

<br/>