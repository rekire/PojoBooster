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
@Enhance(extensions = {ParcelPacker.class, JsonPacker.class}, name = "MultipleTypeParcelableResult")
abstract class MultipleTypeParcelable {
    boolean aBoolean;
    byte aByte;
    short aShort;
    int aInt;
    long aLong;
    char aChar;
    float aFloat;
    double aDouble;
    String string;
    UUID aSerializable;
    Boolean boxedBoolean;
    Byte boxedByte;
    Short boxedShort;
    Integer boxedInt;
    Long boxedLong;
    Character boxedChar;
    Float boxedFloat;
    Double boxedDouble;
    boolean[] booleanArray;
    byte[] byteArray;
    short[] shortArray;
    int[] intArray;
    long[] longArray;
    char[] charArray;
    float[] floatArray;
    double[] doubleArray;
    MyEnum aEnum;
    Date date;
    Bundle bundle;
    //SparseArray sparseArray;
    List<String> list;
    //Map<String, String> map;

    public MultipleTypeParcelable() {}

    public MultipleTypeParcelable(int blub) {}

    public MultipleTypeParcelable(int blub, Date date) {}

    public enum MyEnum {
        foo,
        bar,
        foobar
    }
}