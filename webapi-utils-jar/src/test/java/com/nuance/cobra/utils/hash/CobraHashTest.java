package com.nuance.cobra.utils.hash;

import org.junit.Test;

import static org.junit.Assert.*;

public class CobraHashTest {

    @Test
    public void sha256() {
        assertEquals("3bbb624d9d591c2a9692df5df5788d6bff403c6b3272c41204ea7c891bbd4253", CobraHash.sha256("testApp"));
    }

    @Test
    public void BadAlgorithm() {
        assertEquals("", CobraHash.digest("noAlgorithm", "testData"));
    }
}