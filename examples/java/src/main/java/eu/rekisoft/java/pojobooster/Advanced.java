package eu.rekisoft.java.pojobooster;

import java.util.UUID;

/**
 * Created on 24.07.2016.
 *
 * @author René Kilczan
 */
@Enhance(extensions = {ParcelPacker.class}, name = "AdvancedExample")
abstract class Advanced {
    @Field
    BasicExample genParcelable;
    @Field
    UUID serId;
}