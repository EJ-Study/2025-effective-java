# Item4 : 인스턴스화를 막으려거든 private 생성자를 사용하라 
## 언제 인스턴스화를 막을까요? 
* `static` method 및 field를 가지느 소위 유틸리티 클래스등을 만들 때 사용함 
	* `java.lang.Math`, `java.util.Collections`등이 해당됨
	* `final` 클래스도 이에 속함 (상속이 불가능)
### 추상 클래스로 막아보기 
* 하위 클래스를 만들어서 인스턴스화 해서 만들 수 있음

```java
/* Example 1 */ 
public final class MathUtils {

	// Assertion등 예외 추가도 가능 
	private MathUtils() {}

	public static finla BinaryOperator<Integer> ADD = (a, b) -> a + b;

	public static add(int a, int b) {
		return a + b;
	{

	public static BinaryOperator<Integer> add() {
		return (a, b) -> a + b;
	}	
}
```

