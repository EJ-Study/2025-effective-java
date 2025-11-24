# 빌더 패턴 예제

## 예제 1: 기본 빌더 패턴

**요구사항**
- 회원가입 시 사용하는 User 클래스를 빌더 패턴으로 구현한다.
- 필수 필드: `email`, `password`
- 선택 필드: `username`, `phoneNumber`, `address`, `age`
- 모든 필드는 불변이어야 함

<br/>

<details>
<summary>코드 보기</summary>

```java
class User {
    // 필수 필드
    private final String email;
    private final String password;

    // 선택 필드
    private final String username;
    private final String phoneNumber;
    private final String address;
    private final Integer age;

    public static class Builder {
        // 필수 필드
        private final String email;
        private final String password;

        // 선택 필드 - 기본값
        private String username = "Unknown";
        private String phoneNumber = null;
        private String address = null;
        private Integer age = null;

        public Builder(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public Builder username(String val) {
            this.username = val;
            return this;
        }

        public Builder phoneNumber(String val) {
            this.phoneNumber = val;
            return this;
        }

        public Builder address(String val) {
            this.address = val;
            return this;
        }

        public Builder age(Integer val) {
            this.age = val;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    private User(Builder builder) {
        this.email = builder.email;
        this.password = builder.password;
        this.username = builder.username;
        this.phoneNumber = builder.phoneNumber;
        this.address = builder.address;
        this.age = builder.age;
    }

    @Override
    public String toString() {
        return String.format("User{email='%s', username='%s', phoneNumber='%s', address='%s', age=%d}",
                email, username, phoneNumber, address, age);
    }
}

// 클라이언트 코드
public class Main {
    public static void main(String[] args) {
        User user1 = new User.Builder("john@example.com", "password123")
                .build();
        System.out.println(user1);
        // 출력: User{email='john@example.com', username='Unknown', phoneNumber='null', address='null', age=null}

        User user2 = new User.Builder("jane@example.com", "securepass456")
                .username("Jane Doe")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .age(28)
                .build();
        System.out.println(user2);
        // 출력: User{email='jane@example.com', username='Jane Doe', phoneNumber='010-1234-5678', address='서울시 강남구', age=28}

        User user3 = new User.Builder("bob@example.com", "mypassword789")
                .username("Bob")
                .age(35)
                .build();
        System.out.println(user3);
        // 출력: User{email='bob@example.com', username='Bob', phoneNumber='null', address='null', age=35}
    }
}
```

</details>

<br/><br/>

## 예제 2: 계층 구조 빌더 패턴

**요구사항**
- 상품 등록시 시스템의 계층 구조를 빌더 패턴으로 구현
- 추상 클래스 `Product`: 모든 상품의 기본 클래스
    - 공통 필수 속성: `name`(상품명), `price`(가격)
    - 공통 선택 옵션: `DAWN_DELIVERY`(새벽배송), `NORMAL_DELIVERY`(일반배송),
- `Book` 클래스: 도서 상품
    - 고유 필수 속성: `isbn`, `author`(저자)
- `Electronics` 클래스: 전자제품
    - 고유 선택 속성: `warrantyYears`(보증기간, 기본 1년)

<br/>

<details>
<summary>코드 보기</summary>

```java
import java.util.*;

public abstract class Product {
    public enum DeliveryOption { DAWN_DELIVERY, NORMAL_DELIVERY}

    final String name;
    final int price;
    final Set<DeliveryOption> options;

    abstract static class Builder<T extends Builder<T>> {
        // 필수 필드
        private final String name;
        private final int price;

        // 선택 필드
        EnumSet<DeliveryOption> options = EnumSet.noneOf(DeliveryOption.class);

        public Builder(String name, int price) {
            this.name = name;
            this.price = price;
        }

        public T addOption(DeliveryOption option) {
            options.add(Objects.requireNonNull(option));
            return self();
        }

        abstract Product build();

        protected abstract T self();
    }

    Product(Builder<?> builder) {
        this.name = builder.name;
        this.price = builder.price;
        this.options = builder.options.clone();
    }
}

public class Book extends Product {
    private final String isbn;
    private final String author;

    public static class Builder extends Product.Builder<Builder> {
        private final String isbn;
        private final String author;

        public Builder(String name, int price, String isbn, String author) {
            super(name, price);
            this.isbn = isbn;
            this.author = author;
        }

        @Override
        public Book build() {
            return new Book(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private Book(Builder builder) {
        super(builder);
        this.isbn = builder.isbn;
        this.author = builder.author;
    }

    @Override
    public String toString() {
        return String.format("Book{name='%s', price=%d원, isbn='%s', author='%s', options=%s}",
                name, price, isbn, author, options);
    }
}

public class Electronics extends Product {
    private final int warrantyYears;

    public static class Builder extends Product.Builder<Builder> {
        private int warrantyYears = 1;

        public Builder(String name, int price) {
            super(name, price);
        }

        public Builder warrantyYears(int val) {
            this.warrantyYears = val;
            return this;
        }

        @Override
        public Electronics build() {
            return new Electronics(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private Electronics(Builder builder) {
        super(builder);
        this.warrantyYears = builder.warrantyYears;
    }

    @Override
    public String toString() {
        return String.format("Electronics{name='%s', price=%d원, warranty=%d년, options=%s}",
                name, price, warrantyYears, options);
    }
}

// 사용 예시
public class Main {
    public static void main(String[] args) {
        Book book1 = new Book.Builder(
                "Effective Java", 
                36000,
                "978-0134685991",
                "Joshua Bloch"
        ).build();
        System.out.println(book1);
        // 출력: Book{name='Effective Java', price=36000원, isbn='978-0134685991', author='Joshua Bloch', options=[]}

        Book book2 = new Book.Builder(
                "Clean Code",
                33000,
                "978-0132350884",
                "Robert C. Martin"
        )
        .addOption(Product.DeliveryOption.NORMAL_DELIVERY)
        .build();
        System.out.println(book2);
        // 출력: Book{name='Clean Code', price=33000원, isbn='978-0132350884', author='Robert C. Martin', options=[NORMAL_DELIVERY]}

        Electronics laptop1 = new Electronics.Builder("맥북 프로", 2500000)
                .addOption(Product.DeliveryOption.DAWN_DELIVERY)
                .build();
        System.out.println(laptop1);
        // 출력: Electronics{name='맥북 프로', price=2500000원, warranty=1년, options=[DAWN_DELIVERY]}
    }
}
```

</details>

<br/>