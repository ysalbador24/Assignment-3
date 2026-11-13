package com.example.haunted.events;

import static org.junit.jupiter.api.Assertions.*;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MoveResultTest {

    static Stream<Arguments> resultCases() {
        return Stream.of(Arguments.of(true, "Moved", false, 0),
            Arguments.of(false, "Blocked", false, 0),
            Arguments.of(true, "Trap triggered", true, 8),
            Arguments.of(true, "Zero damage trap", true, 0));
    }
    @ParameterizedTest
    @MethodSource("resultCases")
    void shouldReturnAllSuppliedResultValues(boolean success, String message, boolean triggered, int damage) {
        MoveResult result = new MoveResult(success, message, triggered, damage);
        assertEquals(success, result.isSuccess());
        assertEquals(message, result.getMessage());
        assertEquals(triggered, result.isTrapTriggered());
        assertEquals(damage, result.getTrapDamage());
    }

}
