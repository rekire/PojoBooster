package eu.rekisoft.android.pojobooster;

import eu.rekisoft.java.pojobooster.Enhance;
import eu.rekisoft.java.pojobooster.JsonPacker;
import eu.rekisoft.java.pojobooster.ParcelPacker;

/**
 * Created by rekir on 15.08.2016.
 */
@Enhance(extensions = {ParcelPacker.class, JsonPacker.class}, name = "ResultA")
abstract class BaseA {
    boolean flag;
    int value;
    String name;
}
