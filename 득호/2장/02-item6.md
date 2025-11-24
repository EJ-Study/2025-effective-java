# 2025-effective-java
---
## Item6 - 불필요한 객체 생성을 피하라

쓸모 없는 객체를 생성하는 것은 메모리 낭비일 뿐만 아니라, 가비지 컬렉션의 부담을 증가시켜 성능 저하를 초래할 수 있다.

### 문자열 객체 생성
문자열 객체 생성
같은 값임에도 다른 레퍼런스인 경우 - 기존의 인스턴스를 재사용 하자.
String s = new String("java");
String을 new로 생성하면 항상 새로운 객체를 만들게 된다. 아래와 같이 String 객체를 생성하는 것이 올바르다.

`String s = "java";`

이 코드는 새로운 인스턴스를 매번 만드는 대신 하나의 String 인스턴스를 재사용 한다.
같은 가상 머신 안에서 이와 똑같은 문자열 리터럴을 사용하는 모든 코드가 같은 객체를 재사용함이 보장된다.
```text
플라이웨이트 패턴(Flyweight Pattern)은 재사용 가능한 객체 인스턴스를 공유시켜 메모리 사용량을 최소화하는 구조 패턴이다.
간단히 말하면 캐시(Cache) 개념을 코드로 패턴화 한것으로 보면 되는데,
자주 변화는 속성(extrinsit)과 변하지 않는 속성(intrinsit)을 분리하고 변하지 않는 속성을 캐시하여 재사용해 메모리 사용을 줄이는 방식이다.
그래서 동일하거나 유사한 객체들 사이에 가능한 많은 데이터를 서로 공유하여 사용하도록 하여 최적화를 노리는 경량 패턴이라고도 불린다.
```
[Flyweight Pattern - 참고 블로그](https://inpa.tistory.com/entry/GOF-%F0%9F%92%A0-Flyweight-%ED%8C%A8%ED%84%B4-%EC%A0%9C%EB%8C%80%EB%A1%9C-%EB%B0%B0%EC%9B%8C%EB%B3%B4%EC%9E%90)

### 정적 팩토리 활용한 재사용
```java
Boolean true1 = Boolean.valueOf("true");
Boolean true2 = Boolean.valueOf("true");

System.out.println(true1 == true2);
```
new 키워드를 사용하여 새로운 `Boolean` 객체를 생성하는 대신 `Boolean.valueOf` 정적 팩토리 메서드를 사용하면 동일한 값을 나타내는 객체를 재사용할 수 있다.
이처럼 불변 객체, 자주 사용되는 객체에 대해서는 정적 팩토리 메서드를 통해 객체 생성을 제어하는 것이 좋다.

### 무거운 불변 객체 재사용
만드는 메모리나 시간이 오래 걸리는 객체 즉 "비싼 객체"를 반복적으로 만들지 말고 재사용하자.

아래와 같은 코드 보다는 재사용 되는 부분을 `static final`로 선언하여 재사용 하자.
```java
static boolean isRomanNumeralSlow(String s) {
    return s.matches("^(?=.)M*(C[MD]|D?C{0,3})"
            + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
}
```

```java
// 비싼 객체를 재사용하는 예
public class RomanNumerals {
	private static final Pattern ROMAN = Pattern.compile(
		"^(?=.)M*(C[MD]|D?C{0,3})"
			+ "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");

	static boolean isRomanNumeralFast(String s) {
		return ROMAN.matcher(s).matches();
	}
}
```
비싼 객체를 재사용함으로 써 메모리 낭비와 성능 저하를 방지할 수 있다.
[주의사항]
비싼 객체를 재사용할 때는 해당 객체가 불변(immutable)인지, Thread-safe 확인해야 한다.

### 어댑터
**같은 인스턴스를 대변하는 여러 개의 인스턴스를 생성하지 말자**
```java
		Map<String, Object> map = new HashMap<>();
		map.put("HI", "BYE");
		map.put("BYE", "HI");

		Set<String> set1 = map.keySet();
		Set<String> set2 = map.keySet();

		map.remove("HI");

		System.out.println(set1.size()); // 1
		System.out.println(set2.size()); // 1
```
Map 인터페이스의 `keySet` 메서드는 `Map` 객체 안의 키 전부를 담은 `Set` 인터페이스의 뷰를 반환한다.
하지만, 동일한 `Map`에서 호출하는 keySet 메서드는 같은 `Map`을 대변하기 때문에 반환한 객체 중 하나를 수정하면 다른 모든 객체가 따라서 바뀐다.
따라서 `keySet`이 뷰 객체 여러 개를 만들 필요도 없고 이득도 없다.

### 오토박싱 예시

```java
private static long sum() {
	Long sum = 0L;
	for(long i = 0; i <= Integer.MAX_VALUE; i++) {
		sum += i;
		}
		return sum;
}
```

위 예시에서 `sum` 변수는 `Long` 객체로 선언되어 있다.
for 루프 내에서 원시 타입 long으로 표현된 `i` 값이 `sum`에 더해질 때마다 오토박싱과 언박싱이 발생한다.
즉, `i` 값이 `sum`에 더해질 때마다 `Long` 객체가 새로 생성된다.
이로 인해 엄청난 수의 `Long` 객체가 생성되어 **메모리 낭비**와 **성능 저하**가 발생한다.

### 정리 및 오해 금지
이번 아이템에서는 "비싼 객체니까 아껴 써라" 라는 말을 전달하고 싶은 것은 아니다.
비싼 객체는 아껴 써야한 다는 것이 주요 메시지였다면 굳이 가벼운 `keySet` , `오토박싱` 객체를 예시롤 설명하지 않았을 것이다.

저자가 `Map.keySet()` 예시를 통해 전하고 싶었던 메시지는
**기능적으로 완전히 똑같은 객체를 습관적, 무의식 적으로 중복생성하지 마라**
는 것을 전달하고 싶은 것 같다.

### 재사용과 방어적 복사
이번 아이템은 재사용에 관한 내용이지만, 방어적 복사와도 관련이 있다.
재사용을 위해 객체를 반환할 때는 해당 객체가 불변(immutable)인지, Thread-safe한지 반드시 확인해야 한다.
만약 그렇지 않다면 방적 복사를 통해 새로운 객체를 반환해야 한다.
상황에 맞춰 재사용과 방어적 복사 상황을 구분해서 사용하자.

재사용을 했을 때 문제가 발생하면 이를 찾는 것도 어렵고 고치기도 어렵다.
따라서 재사용이 가능한 객체인지 확신이 서지 않는다면 방어적 복사를 선택하는 것이 더 안전하다.
