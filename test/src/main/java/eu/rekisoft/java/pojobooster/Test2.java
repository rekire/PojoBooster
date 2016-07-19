package eu.rekisoft.java.pojobooster;

import eu.rekisoft.java.pojotoolkit.Enhance;
import eu.rekisoft.java.pojotoolkit.Field;

/**
 * Created by rene on 17.07.2016.
 */
@Enhance(name = "Example2", extensions = {Serializer.class, Parcabler.class})
abstract class Test2 {
    @Field
    Test complex;
    @Field("user")
    long blub;
}
