package com.numplates.nomera3.presentation.view.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileUtilsTest {

    @Before
    public void setUp() throws Exception {
    }


    @Test
    public void testExample() {
        int a = 5;
        int b = 5;

        assertEquals(a, b);
    }

    @Test
    public void splitTest() {
        String textText = "rwerЧrwerwerЙ,,,,,nnnnnЧ podЙm,. Ч dffdfd Й fsdfds";
        String[] tokens = textText.split("Й|Ч");

        for (int i = 0; i < tokens.length; i++) {
            System.out.println(tokens[i]);
        }

    }




}