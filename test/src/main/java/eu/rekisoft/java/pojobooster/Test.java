package eu.rekisoft.java.pojobooster;

import java.util.UUID;

import eu.rekisoft.java.pojotoolkit.JSON;
import eu.rekisoft.java.pojotoolkit.Parcelable;
import eu.rekisoft.java.pojotoolkit.Pool;
import eu.rekisoft.java.pojotoolkit.Serializable;

/**
 * Created by rene on 26.05.2016.
 */
@Parcelable
@JSON
@Pool
@Serializable
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
