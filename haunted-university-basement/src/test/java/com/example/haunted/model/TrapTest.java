package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TrapTest {

    static Stream<Arguments> trapCases() {
        return Stream.of(
            Arguments.of(TrapType.ELECTRIC, 8, true, true),
            Arguments.of(TrapType.STEAM, 0, false, false),
            Arguments.of(TrapType.STEAM, 3, true, false),
            Arguments.of(TrapType.ELECTRIC, 5, false, true));
    }
    @ParameterizedTest
    @MethodSource("trapCases")
    void shouldKeepPropertiesAndDisarm(TrapType type, int damage, boolean armed, boolean oneTime) {
        Trap trap = new Trap("Basement trap", type, damage, armed, oneTime);
        assertEquals("Basement trap", trap.getName());
        assertEquals(type, trap.getType());
        assertEquals(damage, trap.getDamage());
        assertEquals(armed, trap.isArmed());
        assertEquals(oneTime, trap.isOneTimeTrigger());
        trap.disarm();
        assertFalse(trap.isArmed());
        trap.disarm();
        assertFalse(trap.isArmed());
        assertEquals(damage, trap.getDamage());
    }
    @Test
    void shouldRejectNullNameAndType() {
        assertThrows(NullPointerException.class, () -> new Trap(null, TrapType.STEAM, 1, true, true));
        assertThrows(NullPointerException.class, () -> new Trap("Trap", null, 1, true, true));
    }

}
