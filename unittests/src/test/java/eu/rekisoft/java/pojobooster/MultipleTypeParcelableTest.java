package eu.rekisoft.java.pojobooster;

import org.junit.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import eu.rekisoft.java.example.MultipleTypeParcelable;

/**
 * Created on 06.08.2016.
 *
 * @author René Kilczan
 */
public class MultipleTypeParcelableTest {
    @Before
    public void setup() {

    }

    @org.junit.Test
    public void checkJson() {
        MultipleTypeParcelableResult sut = new MultipleTypeParcelableResult();
        sut.aBoolean = true;
        sut.aByte = 42;
        sut.aShort = 12345;
        sut.aInt = 123456789;
        sut.aLong = 1234567890123L;
        sut.aChar = 'a';
        sut.aFloat = 0.128f;
        sut.aDouble = 123456789.64;
        sut.string = "Hello World!";
        sut.aSerializable = UUID.randomUUID();
        sut.boxedBoolean = Boolean.TRUE;
        sut.boxedByte = 21;
        sut.boxedShort = (short) 23456;
        sut.boxedInt = 987654321;
        sut.boxedLong = 31415592653L;
        sut.boxedChar = 'b';
        sut.boxedFloat = 2.5f;
        sut.boxedDouble = 987654321.25;
        sut.booleanArray = new boolean[] {true, false, true};
        sut.byteArray = "Hallo".getBytes();
        sut.shortArray = new short[] {12, 34, 56};
        sut.intArray = new int[] {815, 4711, 4242};
        sut.longArray = new long[] {111222333444L, 123L, 987654321L};
        sut.charArray = "World".toCharArray();
        sut.floatArray = new float[] {1.5f, 2.25f};
        sut.doubleArray = new double[] {132, 456, 789};
        sut.aEnum = MultipleTypeParcelable.MyEnum.foobar;
        sut.date = new Date(0);
        sut.bundle = null;
        sut.list = new ArrayList<>(2);
        sut.list.add("abc");
        sut.list.add("def");
        System.out.println(sut.toJson());
    }

}