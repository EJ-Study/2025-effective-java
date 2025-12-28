# 멤버 클래스는 되도록 static으로 만들라

## 핵심 개념

멤버 클래스에서 바깥 인스턴스에 접근할 일이 없다면 무조건 static을 붙여라.

## 중첩 클래스의 4가지 종류

### 1. 정적 멤버 클래스 (static nested class)

```java
public class Outer {
    private static int staticField = 10;
    
    public static class StaticNested {
        void method() {
            System.out.println(staticField); // static 멤버만 접근 가능
        }
    }
}

// 사용
Outer.StaticNested nested = new Outer.StaticNested();
```

**특징:**

- 바깥 클래스의 인스턴스 없이 독립적으로 생성 가능
- 바깥 클래스의 private static 멤버 접근 가능
- 바깥 클래스의 논리적으로 관련 있는 독립적인 클래스
- 비정적 멤버 클래스에 비해 메모리 효율적

**사용 예시:**

- `Map.Entry<K,V>` - Map과 관련 있지만 독립적
- Enum의 멤버
- 빌더 패턴의 Builder 클래스

### 2. 비정적 멤버 클래스 (Non-static Member Class)

```java
public class Outer {
    private int instanceField = 20;
    
    public class Inner {
        void method() {
            System.out.println(instanceField); // 인스턴스 멤버 접근 가능
            System.out.println(Outer.this); // 바깥 인스턴스 참조
        }
    }
}

// 사용
Outer outer = new Outer();
Outer.Inner inner = outer.new Inner(); // 바깥 인스턴스 필요
```

**특징:**

- 바깥 인스턴스와 암묵적으로 연결됨
- 바깥 인스턴스의 **숨은 참조** 보관 (메모리/시간 소비)
- 바깥 인스턴스 없이 생성 불가

**사용 예시:**

- 어댑터 패턴: `HashMap.KeySet`, `HashMap.Values`
- Iterator 구현: `ArrayList.Itr`

### 3. 익명 클래스 (Anonymous Class)

```java
List<String> list = Arrays.asList("a", "b", "c");

// 익명 클래스
Collections.sort(list, new Comparator<String>() {
    @Override
    public int compare(String s1, String s2) {
        return s1.compareTo(s2);
    }
});

// 요즘은 람다로 대체
Collections.sort(list, (s1, s2) -> s1.compareTo(s2));
```

**특징:**

- 사용 지점에서 선언과 동시에 인스턴스 생성
- 비정적 문맥에서 사용될 때만 바깥 인스턴스 참조
- 람다 등장 이후로는 거의 사용 안 함

**제약사항:**

- 선언 지점에서만 인스턴스 생성 가능
- instanceof 검사, 클래스 이름이 필요한 작업 불가
- 여러 인터페이스 구현 불가, 인터페이스 구현과 동시에 클래스 상속 불가
- 짧지 않으면 가독성 떨어짐

### 4. 지역 클래스 (Local Class)

```java
public void method() {
    class LocalClass {
        void print() {
            System.out.println("Local");
        }
    }
    
    LocalClass local = new LocalClass();
    local.print();
}
```

**특징:**

- 메서드 안에서 정의
- 가장 드물게 사용됨
- 지역 변수처럼 스코프 제한

## static을 붙여야 하는 이유

### 메모리 누수

```java
public class OuterClass {
    private byte[] data = new byte[1_000_000]; // 1MB
    
    // ❌ 나쁜 예: static 없음
    public class InnerTask implements Runnable {
        public void run() {
            System.out.println("Task"); // data를 안 쓰는데도
        }
    }
    
    public Runnable createTask() {
        return new InnerTask(); 
        // InnerTask가 살아있는 한 OuterClass(1MB)도 GC 안 됨!
    }
}

// ✅ 좋은 예: static 추가
public static class InnerTask implements Runnable {
    public void run() {
        System.out.println("Task");
    }
}
// OuterClass와 독립적, 메모리 누수 없음
```

### 시간과 공간 낭비

- 바깥 인스턴스로의 숨은 참조를 저장 (공간 낭비)
- 인스턴스 생성 시 바깥 인스턴스와의 관계 설정 (시간 낭비)

### GC 방해

1. 비정적 멤버 클래스는 자동으로 바깥 인스턴스를 참조
2. 중첩 클래스 인스턴스가 살아있으면 → 바깥 인스턴스도 "사용 중"으로 판단
3. 바깥 인스턴스를 더 이상 안 써도 → GC가 회수하지 못함
4. 메모리 누수 발생!