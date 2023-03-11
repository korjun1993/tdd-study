import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * 참고: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
 */

@ExtendWith(MockitoExtension.class)
public class MockitoTest {

    @Mock
    private ArticleCalculator calculator;

    @BeforeEach
    void setUp() {
        // @Mock 애노테이션이 붙어있는 변수에 인스턴스를 주입한다.
        // 이 방법 외에 JUnit4에서는 @RunWith(MockitoJUnitRunner.class) 를 사용해도된다.
        // JUnit5에서는 ExtendWith(MockitoExtension.class)
//        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("협력객체들의 메서드가 실제로 실행됐는지 확인한다.")
    void 행위검증() {
        // mock creation
        List mockedList = mock(List.class);

        // using mock Object
        mockedList.add("one");
        mockedList.clear();

        // verification
        verify(mockedList).add("one");
        verify(mockedList).clear();
    }

    @Test
    @DisplayName("Stub은 Dummy객체가 실제로 동작하는 것처럼 보이게 만든 객체다.")
    void Stubbing() {
        // mock 대상: 인터페이스가 아닌 구현체
        LinkedList mockedList = mock(LinkedList.class);

        // stubbing
        when(mockedList.get(0)).thenReturn("first");
        when(mockedList.get(1)).thenThrow(new RuntimeException());

        // using mock Object
        // "first" 출력
        System.out.println(mockedList.get(0));

        // runtime exception
        System.out.println(mockedList.get(1));

        // stub 된 적이 없으므로 null 출력
        System.out.println(mockedList.get(999));

        // verification
        verify(mockedList).get(0);
        verify(mockedList).get(1);
        verify(mockedList).get(999);
    }

    @Test
    @DisplayName("Stubbing은 Override될 수 있다.")
    void StubbingOverride() {
        // Fixture Setup
        LinkedList mockedList = mock(LinkedList.class);
        when(mockedList.get(0)).thenReturn(100);

        // Override
        when(mockedList.get(0)).thenReturn(200);

        System.out.println(mockedList.get(0)); // "200" 출력
    }

    @Test
    @DisplayName("equals() 메소드를 활용하여")
    void argumentMatchers() {
        //anyInt()를 활용하여 stubbing
        //anyInt(): 내장 arguments matcher
        LinkedList mockedList = mock(LinkedList.class);
        when(mockedList.get(anyInt())).thenReturn("element");

        // custom matcher를 이용하여 stubbing
        when(mockedList.contains(argThat(new CustomMatcher()))).thenReturn(true);

        System.out.println(mockedList.contains(-1));
        System.out.println(mockedList.contains(0));
        System.out.println(mockedList.contains(1));
        System.out.println(mockedList.contains(2));
        System.out.println(mockedList.contains(4));
        System.out.println(mockedList.contains(5));
        System.out.println(mockedList.contains(6));

        // "element" 출력
        System.out.println(mockedList.get(999));

        // argument matcher 를 사용하는 상황도 verify
        verify(mockedList).get(anyInt());

        // argument matcher는 람다표현식으로 작성 가능하다.
        LinkedList<String> mockedStringList = mock(LinkedList.class);
        verify(mockedStringList).add(argThat(someString -> someString.length() > 5));
    }

    // custom matcher
    private static class CustomMatcher implements ArgumentMatcher<Integer> {
        @Override
        public boolean matches(Integer argument) {
            int value = argument.intValue();
            if (value <= 5 && value >= 0) {
                return true;
            }
            return false;
        }
    }

    @Test
    @DisplayName("ArgumentCaptor를 이용하면 argument 값을 equals() 메서드를 통해 검증할 수 있다.")
    void argumentCaptorTest() {
        // Stubbing 에서는 사용하지 말고, Verification 에서만 사용하기를 권한다.
        List<Person> mock = mock(List.class);
        ArgumentCaptor<Person> varArgs = ArgumentCaptor.forClass(Person.class);

        mock.add(new Person("John"));
        mock.add(new Person("Jane"));

        // capturing argument
        verify(mock, times(2)).add(varArgs.capture());
        List expected = List.of(new Person("John"), new Person("Jane"));

        // Equals() 를 활용하여 argument 검증
        Assertions.assertEquals(expected, varArgs.getAllValues());
    }

    private static class Person {
        private String name;

        Person(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    @Test
    @DisplayName("argument matchers 를 하나라도 활용한다면, 전부 arguments matchers 이어야 한다.")
    void allArgumentMatcherTest() {
        List mock = mock(List.class);
        mock.add(0, "A");
        verify(mock).add(anyInt(), anyString()); // correct

        List mock2 = mock(List.class);
        mock2.add(0, "A");
        verify(mock).add(anyInt(), "A"); // InvalidUseOfMatchersException 발생
    }

    @Test
    @DisplayName("invocation 횟수를 명시한다")
    void numberOfInvocationTest() {
        List mockedList = mock(List.class);

        // using mock
        mockedList.add("once");

        mockedList.add("twice");
        mockedList.add("twice");

        mockedList.add("third");
        mockedList.add("third");
        mockedList.add("third");

        // 아래 두 개는 동일하게 동작한다.
        verify(mockedList).add("once");
        verify(mockedList, times(1)).add("once");

        // 몇번 호출됐는지 정확히 검증한다.
        verify(mockedList, times(2)).add("twice");
        verify(mockedList, times(3)).add("third");

        // 호출이 안된 경우를 검증
        verify(mockedList, never()).add("never happend");

        // 호출 최소횟수를 검증할 수 있다.
        verify(mockedList, atLeastOnce()).add("three times");
        verify(mockedList, atLeast(2)).add("five times");

        // 호출 최대횟수를 검증할 수 있다.
        verify(mockedList, atMost(5)).add("three times");
    }

    @Test
    @DisplayName("Exception 을 발생시키는 Stub를 만들 수 있다.")
    void stubbingException() {
        List mockedList = mock(List.class);
        doThrow(new RuntimeException()).when(mockedList).clear();

        // RuntimeException 발생
        mockedList.clear();
    }

    @Test
    @DisplayName("순서를 검증할 수 있다.")
    void verificationOrder() {
        //상황A. 메소드가 반드시 특정한 순서로 실행되야하는 객체
        List singleMock = mock(List.class);

        //using a singleMock
        singleMock.add("was added first");
        singleMock.add("was added second");

        //순서 검증 객체
        InOrder inOrder = inOrder(singleMock);

        // add("was added first") -> add("was added second") 순서대로 호출됐음을 검증
        inOrder.verify(singleMock).add("was added first");
        inOrder.verify(singleMock).add("was added second");

        //상황B. 특정 순서로 사용되어야하는 여러 mock 객체
        List firstMock = mock(List.class);
        List secondMock = mock(List.class);

        //using mocks
        firstMock.add("was called first");
        secondMock.add("was called second");

        // 순서를 검증해야할 객체를 inOrder 생성자에 전달한다.
        inOrder = inOrder(firstMock, secondMock);

        // firstMock -> secondMock 순서대로 호출됐음을 검증한다.
        inOrder.verify(firstMock).add("was called first");
        inOrder.verify(secondMock).add("was called second");
    }

    @Test
    @DisplayName("어떤 일이 발생하지 않음을 검증한다.")
    void neverHappenedVerification() {
        List mockOne = mock(List.class);
        List mockTwo = mock(List.class);
        List mockThree = mock(List.class);

        mockOne.add("one");

        verify(mockOne).add("one");

        // 메서드가 호출되지 않음을 검증한다.
        verify(mockOne, never()).add("two");

        // mockTwo, mockThree 가 사용되지 않음을 검증한다.
        verifyZeroInteractions(mockTwo, mockThree);
    }

    @Test
    @DisplayName("verify 이후 다른 인터렉션이 없었다는 것을 검증한다")
    void verifyNoMoreInteractionsTest() {
        List mockedList = mock(List.class);
        mockedList.add("one");
        mockedList.add("two");

        verify(mockedList).add("one"); // add("one") 호출됐는지 검증
        verifyNoMoreInteractions(mockedList); // 이후로 mockedList가 사용됐었는지 검증 → add("two")가 호출했음으로 fail
    }

    @Test
    @DisplayName("반복되는 Mock 생성 코드 제거하기")
    void minimizeMockCreationCode() {
        calculator.plus(10);
        verify(calculator).plus(10);
    }

    @Test
    @DisplayName("같은 메서드 호출이 다른 결과를 반환하게 Stubbing 하기")
    void consecutiveCalls() {
        when(calculator.plus(10))
                .thenThrow(new RuntimeException())
                .thenReturn(30);

        // first call: throws runtime exception
        try {
            calculator.plus(10);
        } catch (Exception e) {
        }

        // second call: "30" 출력
        System.out.println(calculator.plus(10));

        // 이후 연이은 호출은 계속 마지막 Stubbing (="30") 출력
        System.out.println(calculator.plus(10));
        System.out.println(calculator.plus(10));
    }

    @Test
    @DisplayName("[짧은 버전]같은 메서드 호출이 다른 결과를 반환하게 Stubbing 하기")
    void consecutiveShortCalls() {
        when(calculator.plus(10))
                .thenReturn(10, 20, 30, 40);

        // 다음처럼 하면 오버라이딩이 되므로 모든 메소드는 30을 리턴한다.
//        when(calculator.plus(10))
//                .thenReturn(10);
    }

    @Test
    @DisplayName("Stubbing with callbacks")
    void stubbingWithCallBack() {
        // thenReturn(), thenThrow() 만으로 테스트코드를 충분히 작성할 수 있다.
        // 이 기능은 아직 논쟁의 여지가 있음.
        List mock = mock(List.class);
        when(mock.add(anyString())).thenAnswer(
                new Answer<Object>() { // mock 객체와 협력했을 때, 수행될 Action 그리고 반환값을 명시
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        Object mock = invocation.getMock();
                        return "called with arguments: " + Arrays.toString(args);
                    }
                }
        );

        System.out.println(mock.add("foo"));

    }

    @Test
    @DisplayName("Stubbing void methods")
    void stubbingVoidMethods() {
        // void 를 return 하는 메서드는 다른 방법으로 stubbing 한다.
        List mockedList = mock(List.class);
        doThrow(new RuntimeException()).when(mockedList).clear();

        // RuntimeError 발생
        mockedList.clear();

    }

    @Test
    @DisplayName("doReturn, doThrow, doAnswer, doNothing, doCallRealMethod")
    void stubbingDoMethods() {
        List mockedList = mock(List.class);

        // 방법1 (런타임오류발생)-> void mockedList.clear()를 Stubbing 할 수 있지만, void 메소드는 반환값을 return 하도록 Stubbing하면 안됌
        doReturn(true).when(mockedList).clear();

        // 방법2 (컴파일에러발생) -> void mockedList.clear() Stubbing 불가
//        when(mockedList.clear()).thenReturn(true);

        // 방법1은 mockedList.clear()가 수행되지 않고
        // 방법2는 mockedList.clear()가 수행된다.
        // 방법2에서 when() 메소드의 파라미터는 T Method인데, mockedList.clear()의 반환값이 없으므로 에러가 발생한다.

        mockedList.clear();
    }
}
