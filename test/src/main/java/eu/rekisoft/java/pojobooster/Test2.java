package eu.rekisoft.java.pojobooster;

import java.util.Date;

/**
 * Created by rene on 17.07.2016.
 */
@Enhance(name = "Example2", extensions = {Serializer.class, ParcelPacker.class, JsonPacker.class})
abstract class Test2 {
    Example2 complex;
    @Field("hallo")
    String test = null;
    @Field("user")
    long blub;
    Example serial;
    @Formatter(value = "MM-YYYY", locale = "en_US")
    Date birth;
    @Formatter(value = "%.2f€", locale = "de")
    float price;

    @JsonDecorator
    String decorateIt(StringBuilder sb) {
        return sb.toString();
    }

    @FactoryOf(Test2.class)
    public static Test2 fromJson(String json) {
        return null;
    }
}