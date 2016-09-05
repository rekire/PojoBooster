package eu.rekisoft.android.pojobooster;

import java.util.UUID;

import eu.rekisoft.java.pojobooster.Enhance;
import eu.rekisoft.java.pojobooster.JsonPacker;
import eu.rekisoft.java.pojobooster.ParcelPacker;

/**
 * Created by René Kilczan on 05.09.16.
 */
@Enhance(extensions = {ParcelPacker.class, JsonPacker.class}, name = "User")
abstract class Example {
    UUID id;
    String displayName;
}