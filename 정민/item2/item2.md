# 아이템2. 생성자에 매개변수가 많다면 빌더를 고려하라

> **💡 핵심 내용**
>
> 생성자나 정적 팩터리가 처리해야 할 매개변수가 많다면 빌더 패턴을 선택하는게 낫다. (매개변수가 4개 이상일 경우)
## 0. 개요

정적 팩터리 메서드와 생성자는 선택적 매개변수가 많을 때 유연하게 대응하기 어렵다는 공통된 제약이 있다. 이를 해결하기 위한 세 가지 패턴을 살펴보자.

- **점층적 생성자 패턴**: 매개변수가 많아지면 가독성이 떨어진다
- **자바빈즈 패턴**: 객체 일관성이 깨지고 불변 객체를 만들 수 없다
- **빌더 패턴**: 가독성이 좋고 불변 객체를 만들 수 있다 ⭐

<br/>

## 1. 점층적 생성자 패턴 (Telescoping Constructor Pattern)

#### 특징
- 필수 매개변수만 받는 생성자부터 시작해 선택 매개변수를 하나씩 추가하며 생성자를 오버로딩하는 방식이다.

<div style="margin-left: 20px">
<details>
<summary>코드 예시</summary>
<div markdown="1">

```java

public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    public NutritionFacts(int servingSize, int servings) {
        this(servingSize, servings, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories) {
        this(servingSize, servings, calories, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat) {
        this(servingSize, servings, calories, fat, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium) {
        this(servingSize, servings, calories, fat, sodium, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium, int carbohydrate) {
        this.servingSize = servingSize;
        this.servings = servings;
        this.calories = calories;
        this.fat = fat;
        this.sodium = sodium;
        this.carbohydrate = carbohydrate;
    }
}

// 클라이언트 코드
public class Item2Main {
    public static void main(String[] args) {
        // 점층적 생성자 패턴
        NutritionFacts cocaCola = new NutritionFacts(240, 8, 100, 0, 35, 27);
    }
}

```

</div>
</details>
</div>

<br/>

####  단점
- 매개 변수 개수가 많아지면 클라이언트 코드를 작성하거나 읽기 어렵다
    - 값의 의미를 바로 파악하기 어렵다
    - 타입이 같은 매개변수가 연달아 늘어서 있으면 찾이 어려운 버그로 이어짐

<br/>

## 2. 자바빈즈 패턴 (JavaBeans Pattern)
#### 특징
- 매개변수 없는 생성자로 객체를 생성한 뒤, setter 메서드로 값을 설정하는 방식이다.

<div style="margin-left: 20px">
<details>
<summary>코드 예시</summary>
<div markdown="1">

```java

public class NutritionFacts {
    // 매개변수들은 (기본값이 있다면) 기본값으로 초기화
    private int servingSize = -1; // 필수; 기본값 없음
    private int servings = -1; // 필수; 기본값 없음
    private int calories = 0;
    private int fat = 0;
    private int sodium = 0;
    private int carbohydrate = 0;

    public NutritionFacts2() {

    }

    public void setServingSize(int servingSize) {
        this.servingSize = servingSize;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public void setSodium(int sodium) {
        this.sodium = sodium;
    }

    public void setCarbohydrate(int carbohydrate) {
        this.carbohydrate = carbohydrate;
    }
}

// 클라이언트 코드
public class Item2Main {
    public static void main(String[] args) {
        // 자바빈즈 패턴
        NutritionFacts cocaCola = new NutritionFacts();
        cocaCola.setServingSize(240);
        cocaCola.setServings(8);
        cocaCola.setCalories(100);
        cocaCola.setSodium(35);
        cocaCola.setCarbohydrate(27);
    }
}

```

</div>
</details>
</div>

<br/>

####  단점
- 객체가 완전히 생성되기 전까지는 일관성(consistency)이 깨진 상태가 된다.
- 클래스를 불변으로 만들 수 없으며 스레드 안정성을 얻으려면 프로그래머가 추가 작업을 해줘야 한다.

<br/>

## 3. 빌더 패턴 (Builder Pattern)
#### 특징
- 필수 매개변수만으로 빌더 객체를 생성한 뒤, 선택 매개변수를 체이닝 방식으로 설정하고 build()로 최종 객체를 만든다.

<div style="margin-left: 20px">
<details>
<summary>코드 예시</summary>
<div markdown="1">

```java

public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    public static class Builder {
        // 필수 매개변수
        private final int servingSize;
        private final int servings;

        // 선택 매개변수 - 기본값으로 초기화
        private int calories = 0;
        private int fat = 0;
        private int sodium = 0;
        private int carbohydrate = 0;

        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }

        public Builder calories(int val) {
            this.calories = val;
            return this;
        }

        public Builder fat(int val) {
            this.fat =  val;
            return this;
        }

        public Builder sodium(int val) {
            this.sodium = val;
            return this;
        }

        public Builder carbohydrate(int val) {
            this.carbohydrate = val;
            return this;
        }

        public NutritionFacts build() {
            return new NutritionFacts(this);
        }
    }

    public NutritionFacts(Builder builder) {
        this.servingSize = builder.servingSize;
        this.servings = builder.servings;
        this.calories = builder.calories;
        this.fat = builder.fat;
        this.sodium = builder.sodium;
        this.carbohydrate = builder.carbohydrate;
    }
}

// 클라이언트 코드
public class Item2Main {
    public static void main(String[] args) {
        // 빌더 패턴
        NutritionFacts3 cocaCola3 = new NutritionFacts3.Builder(240, 8)
                .calories(100)
                .sodium(35)
                .carbohydrate(27)
                .build();
    }
}

```

</div>
</details>
</div>


- 계층적으로 설계된 클래스와 함께 쓰기에 좋다
    - 클라리언트가 형변환에 신경 쓰지 않고도 빌더를 사용할 수 있다.
        - 상위 클래스의 Builder 클래스는 **재귀적 타입 한정**을 이용하는 제네릭 타입
        - 상위 클래스의 **추상메서드인 self**를 통해 하위 클래스에서 형변환하지 않고 메서드 연쇄가 가능 (simulated self-type 관용구)
        - 각 하위 클래스의 Builder가 정의한 build 메서드는 해당하는 구체 하위 클래스를 반환 (공변 반환 타이핑)

<div style="margin-left: 20px">
<details>
<summary>코드 예시</summary>
<div markdown="1">

```java

// Pizza.java
public abstract class Pizza {
    public enum Topping { HAM, MUSHROOM, ONION, PEPPER, SAUSAGE };
    final Set<Topping> toppings;

    abstract static class Builder<T extends Builder<T>> {
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);
        public T addTopping(Topping topping) {
            toppings.add(Objects.requireNonNull(topping));
            return self();
        }

        abstract Pizza build();

        // 하위 클래스는 이 메서드를 재정의(overriding)하여
        // "this"를 반환하도록 해야 한다.
        protected abstract T self();
    }

    Pizza(Builder<?> builder) {
        toppings = builder.toppings.clone();
    }
}

// NyPizza.class
public class NyPizza extends Pizza {
    public enum Size { SMALL, MEDIUM, LARGE }
    private final Size size;

    public static class Builder extends Pizza.Builder<Builder> {
        private final Size size;

        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }

        @Override
        public NyPizza build() {
            return new NyPizza(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private NyPizza(Builder builder) {
        super(builder);
        this.size = builder.size;
    }
}

// Calzone.java
public class Calzone extends Pizza {
    private final boolean sauceInside;

    public static class Builder extends Pizza.Builder<Builder> {
        private boolean sauceInside = false; // 기본값

        public Builder sauceInside() {
            sauceInside = true;
            return this;
        }

        @Override
        public Calzone build() {
            return new Calzone(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private Calzone(Builder builder) {
        super(builder);
        this.sauceInside = builder.sauceInside;
    }
}

// 클라이언트 코드
public class Item2Main {
    public static void main(String[] args) {
        // 계층 클래스들에서의 빌더 패턴
        NyPizza pizza = new NyPizza.Builder(NyPizza.Size.SMALL)
                .addTopping(Pizza.Topping.SAUSAGE)
                .addTopping(Pizza.Topping.ONION)
                .build();

        Calzone calzone = new Calzone.Builder()
                .addTopping(Pizza.Topping.HAM)
                .sauceInside()
                .build();
    }
}

```

</div>
</details>
</div>

<br/>

#### 장점
- 가독성이 뛰어나고 실수를 줄일 수 있다
- 객체를 불변으로 만들 수 있다
    - build() 메서드에서 호출하는 생성자에서 불변식을 검증할 수 있다
- 계층적으로 설계된 클래스와 함께 쓰기에 좋다

#### 단점
- 객체 생성 전 빌더 객체를 만들어야 하므로 약간의 비용이 필요하다
- 매개변수가 4개 이상은 되어야 값어치를 한다
    - API는 시간이 지날수록 매개변수가 많아지는 경향이 있어서, 객체 생성을 빌더로 시작하는 편이 나을때가 많다

<br/>

## 4. 실무에서의 Builder Pattern
- 실무에서는 Lombok의 @Builder 어노테이션을 사용해 빌더 패턴을 간편하게 적용할 수 있다.
    - 직접 빌더 클래스를 작성하지 않아도 어노테이션 하나로 빌더 패턴을 적용할 수 있다.
    - **주의 사항**
        - 클래스 레벨에 `@Builder`를 사용하면 모든 필드를 포함한 빌더가 생성되는데, 이는 필드 순서 변경 시 생성자 파라미터 순서가 자동으로 바뀌어 버그를 유발할 수 있다.
        - **생성자에 `@Builder`를 적용하고, 생성자 내부에서 유효성 검증을 수행하는 것이 안전하다.**

<div style="margin-left: 20px">
<details>
<summary>코드 예시</summary>
<div markdown="1">

```java

public class NutritionFacts4 {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    @Builder
    public NutritionFacts4(int servingSize, int servings, int calories, int fat, int sodium, int carbohydrate) {
        // todo: 파라미터 검증 코드 추가
        this.servingSize = servingSize;
        this.servings = servings;
        this.calories = calories;
        this.fat = fat;
        this.sodium = sodium;
        this.carbohydrate = carbohydrate;
    }
}

// 클라이언트 코드
public class Item2Main {
    public static void main(String[] args) {

        // Lombok을 통한 builder 패턴
        NutritionFacts4 cocaCola4 = NutritionFacts4.builder()
                .servingSize(240)
                .servings(8)
                .calories(100)
                .sodium(35)
                .carbohydrate(27)
                .build();
    }
}


```

</div>
</details>
</div>

<br/>