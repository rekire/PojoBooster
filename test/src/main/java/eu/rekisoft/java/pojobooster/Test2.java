package eu.rekisoft.java.pojobooster;

/**
 * Created by rene on 17.07.2016.
 */
@Enhance(name = "Example2", extensions = {Serializer.class, Parcabler.class, Jsoner.class})
abstract class Test2 {
    @Field
    Example2 complex;
    @Field("hallo")
    String test = null;
    @Field("user")
    long blub;
    @Field
    Example serial;
}
