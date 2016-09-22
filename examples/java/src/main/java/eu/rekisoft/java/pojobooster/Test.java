package eu.rekisoft.java.pojobooster;

import java.util.UUID;

/**
 * Created on 26.05.2016.
 *
 * @author René Kilczan
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