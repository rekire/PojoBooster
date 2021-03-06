package eu.rekisoft.android.pojobooster;

import eu.rekisoft.java.pojobooster.Enhance;
import eu.rekisoft.java.pojobooster.JsonPacker;
import eu.rekisoft.java.pojobooster.ParcelPacker;

/**
 * Created on 15.08.2016.
 *
 * @author René Kilczan
 */
@Enhance(extensions = {ParcelPacker.class, JsonPacker.class}, name = "ResultB")
abstract class BaseB {
    ResultA test;

}