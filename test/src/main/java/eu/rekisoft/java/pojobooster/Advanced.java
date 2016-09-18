package eu.rekisoft.java.pojobooster;

import java.util.UUID;

/**
 * Created by rekir on 24.07.2016.
 */
@Enhance(extensions = {ParcelPacker.class}, name = "AdvancedExample")
abstract class Advanced {
    @Field
    BasicExample genParcelable;
    @Field
    UUID serId;
}