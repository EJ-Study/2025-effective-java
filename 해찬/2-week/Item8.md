# Item 8: finalizer와 cleaner 사용을 피해라
### 객체 소멸자
* Java도 객체 소멸자를 제공한다 (C++의 소멸자와는 다름)
	* finalizer
		* 기본적로 Deprecated되어있고 상황에 따라 위험할 수 있어서 사용하면 안된다
	* cleaner
		* finalizer보다는 덜 위험하지만, 예측불가능해 사용을 권고하지는 않는다
* Java에서의 객체 소멸자는 즉시 수행된다는 보장이 없다
	* 이는, GC에 따라 다르며 자원 회수가 지연되어, OOM을 발생할 수도 있다
		* Deep
			* finalizer thread는 다른 thread보다 우선순위가 낮아 더욱 지연될 가능성이 높다
		* System.gc 를 쓰면되지 않나...?
			* 이 메서드로 finalizer나 cleaner를 수행한다는 보장은 하지 않는다.
	* 그렇기 때문에 상태를 영구적으로 수정하는 작업에서는 finalizer, cleaner에 의존하면 안된다
		* ex) DB Lock 해제 등
### 단점
* 성능 문제
	* AutoCloseable 객체보다 finalizer, cleaner 는 50배정도 느림
	* 하지만, 안정망 형태로 하면 훨씬 빨라짐
* 보안 문제
	* 생성자, 직렬화를 호출할때, Excpetion 발생시 하위 클래스에서 finalizer가 수행됨
	* ex) 아래와 같은 케이스에서 생성자에서 예외를 던지면 문제가 생김 
	```java
	public class Currency {
		private final int currency;
	
		public Currency(Integer currency) {
			if (currency == null)
				throw Exception("Currency can't be a null");
		 }
	}
	```
	* 이를 막기 위해서는 아무일도 하지 않는 finalize method를 만들고 `final`로 선언하자 
### 소멸자를 사용하고 싶을때...? 
* 즉, file이나 thread등을 종료하고 싶은 경우는 `AutoCloseable`을 구현하자 
	* 인스턴스를 다쓰면 `close` 메서드를 호출하자 
	* `close`는 이 객체는 더 이상 유효하지 않는다는 것을 field에 기록한다 
		* 만약, field를 검사후 객체가 닫힌 후에 불리면 `IllegalStateException` 이 발생한다 

## finalizer, cleaner의 사용처는...? 
1. Resource의 소유자가 `close`를 호출하지 않는것에 대한 안전망 역할로 사용
	* FileInputStream, ThreadPoolExcutor등이 해당 내용을 사용함
	* 안정망 역할인거지 **자원을 회수하고 싶을때는 무조건 close부터 사용하자**
2. Native peer와 연결된 객체에서 사용함 
	* Native method를 통해 기능을 위임한 Native 객체에서 사용한다 
	* GC는 Heap을 보니 Native 객체를 회수하지는 않음 
### Cleaner 예제
```java
public class Room implements AutoCloseable {
	private static final Cleaner cleaner = Cleaner.create();

	// Room을 참조하면 문제 발생 -> 순환 참조 발생 GC가 회수하지않음
	private static class State implements Runnalbe {
		int numJunkPiles; // 쓰레기 갯수
		
		State(int numJunkPiles) {
			this.numJunkPiles = numJunkPiles;
		}
	
		// 단 한번만 cleanable에 의해 호출됨 
		@override public void run() {
			System.out.println("방 청소");
			numJunkPiles = 0;
		}
	}

	// 방의 상태
	private final State state;
	// cleanable 객체, 수거 대상이 된다면 방을 청소함 
	private final Cleaner.Cleanable cleanable;

	public Room(int numJunkPiels) {
		state = new State(numJunkPiles);
		cleanable = cleaner.register(this, state);
	}

	@Override public void close() {
		cleanable.clean();
	}
}
```

* `Room`의 cleaner는 단지 안정망으로 쓰여서 아래와 같이 호출이 가능하다 
```java
public class Adult {
	public static void main(String[] args) {
		try (Room myRoom = new Room(7)) {
			System.out.println("안녕");
		}
	}
}
```
* 안녕 -> 방 청소 순으로 호출한다 
* 만약, try-resrouce등으로 `close`를 반드시 호출하는 형태가 아니라면 
	* ex) `new Room(99)` 끝 
	* 언제 호출을 할지등이 보장되지 않는다 

### 핵심정리
* cleaner는 안정망 역할이나 중요하지 않은 Native resource 회수용으로만 사용하자
* 항상 사용할때는 불확실성과 성능 저하에 주의하자
