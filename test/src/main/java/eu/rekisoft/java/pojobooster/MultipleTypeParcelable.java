package eu.rekisoft.java.pojobooster;

import android.os.Bundle;

import java.util.List;

import java.util.Date;
import java.util.UUID;

/**
 * Created on 26.07.2016.
 *
 * @author René Kilczan
 */
@Enhance(extensions = Parcabler.class, name = "MultipleTypeParcelableResult")
abstract class MultipleTypeParcelable {
    @Field
    boolean aBoolean;
    @Field
    byte aByte;
    @Field
    short aShort;
    @Field
    int aInt;
    @Field
    long aLong;
    @Field
    char aChar;
    @Field
    float aFloat;
    @Field
    double aDouble;
    @Field
    UUID aSerialziable;
    @Field
    Boolean boxedBoolean;
    @Field
    Byte boxedByte;
    @Field
    Short boxedShort;
    @Field
    Integer boxedInt;
    @Field
    Long boxedLong;
    @Field
    Character boxedChar;
    @Field
    Float boxedFloat;
    @Field
    Double boxedDouble;
    @Field
    boolean[] booleanArray;
    @Field
    byte[] byteArray;
    @Field
    short[] shortArray;
    @Field
    int[] intArray;
    @Field
    long[] longArray;
    @Field
    char[] charArray;
    @Field
    float[] floatArray;
    @Field
    double[] doubleArray;
    @Field
    MyEnum aEnum;
    @Field
    Date date;
    @Field
    Bundle bundle;
    //@Field
    //SparseArray sparseArray;
    @Field
    List<String> list;
    //@Field
    //Map<String, String> map;

    public enum MyEnum {
        foo,
        bar,
        foobar
    }
}