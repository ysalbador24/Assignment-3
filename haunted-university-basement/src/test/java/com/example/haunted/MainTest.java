package com.example.haunted;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void shouldPrintStartingRoomMovementAndPickup() throws Exception {
        java.io.PrintStream original = System.out;
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        try (java.io.PrintStream capture = new java.io.PrintStream(bytes, true, java.nio.charset.StandardCharsets.UTF_8)) {
            System.setOut(capture);
            Main.main(new String[0]);
        } finally {
            System.setOut(original);
        }
        String expected = String.join(System.lineSeparator(),
            "Maintenance Stairwell", "Moved to Abandoned Lecture Hall.", "Picked up Coffee Potion.")
            + System.lineSeparator();
        assertEquals(expected, bytes.toString(java.nio.charset.StandardCharsets.UTF_8));
    }

}
