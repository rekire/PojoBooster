package eu.rekisoft.java.pojobooster;

/**
 * Created on 24.07.2016.
 *
 * @author René Kilczan
 */
@Enhance(extensions = {Serializer.class}, name = "BasicExample")
abstract class Basic {
    @Field
    int foo;
    @Field
    String bar;
}