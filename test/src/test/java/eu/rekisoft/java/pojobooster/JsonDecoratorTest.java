package eu.rekisoft.java.pojobooster;

/**
 * @test @summary verify that the annotations will block compiling wrongly annotated methods.
 *
 * Created on 04.08.2016.
 *
 * @author René Kilczan
 *
 * @library /annotations/build/libs/annotations.jar /preprocessor/build/libs/preprocessor.jar /test/build/libs/javapoet-1.7.0.jar
 * @compile/fail/ref=JsonDecoratorTest.ref JsonDecoratorTest.java
 */
public class JsonDecoratorTest {
    @JsonDecorator
    String decorateIt(StringBuilder sb) {
        return sb.toString();
    }

    @FactoryOf(JsonDecoratorTest.class)
    public static JsonDecoratorTest fromJson(String json) {
        return null;
    }
}