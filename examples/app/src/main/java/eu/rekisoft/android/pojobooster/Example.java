package eu.rekisoft.android.pojobooster;

import java.util.UUID;

import eu.rekisoft.java.pojobooster.Enhance;
import eu.rekisoft.java.pojobooster.JsonPacker;
import eu.rekisoft.java.pojobooster.ParcelPacker;

/**
 * Created on 05.09.16.
 *
 * @author René Kilczan
 */
@Enhance(extensions = {ParcelPacker.class, JsonPacker.class}, name = "User")
abstract class Example {
    UUID id;
    String displayName;
}