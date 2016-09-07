package eu.rekisoft.java.pojobooster;

import java.util.UUID;

/**
 * Created by rene on 26.05.2016.
 */
@Enhance(extensions = {Serializer.class, ParcelPacker.class}, name = "Example")
abstract class Test {
    @Field
    int age;
    @Field("user")
    String name;
    @Field
    UUID id;
    @Field
    Example2 boom;

    public String test() {
        return null;
    }

    public static void main(String[] args) {
        //System.out.println(new TestBean().toJSON());
    }
}
