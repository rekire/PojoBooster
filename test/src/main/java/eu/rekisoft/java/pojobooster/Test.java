package eu.rekisoft.java.pojobooster;

import java.util.UUID;

import eu.rekisoft.java.pojotoolkit.Enhance;
import eu.rekisoft.java.pojotoolkit.Extension;
import eu.rekisoft.java.pojotoolkit.Field;
import eu.rekisoft.java.pojotoolkit.JSON;
import eu.rekisoft.java.pojotoolkit.JsonSupport;
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
@Enhance(extensions = Serializer.class, name = "Example")
abstract class Test {
    @Field
    int age;
    @Field("user")
    String name;
    @Field
    UUID id;

    public String test() {
        return null;
    }

    public static void main(String[] args) {
        //System.out.println(new TestBean().toJSON());
    }
}