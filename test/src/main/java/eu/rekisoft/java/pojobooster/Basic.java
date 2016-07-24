package eu.rekisoft.java.pojobooster;

/**
 * Created by rekir on 24.07.2016.
 */
@Enhance(extensions = {Serializer.class}, name = "BasicExample")
abstract class Basic {
    @Field
    int foo;
    @Field
    String bar;
}
