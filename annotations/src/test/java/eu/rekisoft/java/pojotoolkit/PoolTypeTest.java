package eu.rekisoft.java.pojotoolkit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Well what do you do for a nice coverage?
 *
 * Created on 05.10.2016.
 *
 * @author René Kilczan
 */
public class PoolTypeTest {
    @Test
    public void basicStructure() {
        assertEquals(2, PoolType.values().length);
        assertTrue(PoolType.values()[0] == PoolType.Simple);
        assertTrue(PoolType.values()[1] == PoolType.Synced);
    }
}