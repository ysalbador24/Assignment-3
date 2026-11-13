package com.example.haunted.rules;

import static org.junit.jupiter.api.Assertions.*;
import com.example.haunted.events.InteractionResult;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Player;
import com.example.haunted.model.Trap;
import com.example.haunted.model.TrapType;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TrapResolverTest {

    private Player player;
    private TrapResolver resolver;
    @BeforeEach
    void setUp() {
        player = new Player("Student", 20, 7, 2, new Inventory(8));
        resolver = new TrapResolver();
    }
    @Test
    void shouldIgnoreMissingAndDisarmedTraps() {
        InteractionResult missing = resolver.resolveTrap(player, null);
        assertFalse(missing.isSuccess());
        assertEquals("No trap was triggered.", missing.getMessage());
        Trap trap = new Trap("Wire", TrapType.ELECTRIC, 8, false, true);
        InteractionResult disarmed = resolver.resolveTrap(player, trap);
        assertFalse(disarmed.isSuccess());
        assertEquals("No trap was triggered.", disarmed.getMessage());
        assertEquals(20, player.getHealth());
        assertFalse(trap.isArmed());
    }
    static Stream<Arguments> damageCases() {
        return Stream.of(Arguments.of(-1,20), Arguments.of(0,20), Arguments.of(1,19),
            Arguments.of(19,1), Arguments.of(20,0), Arguments.of(21,0));
    }
    @ParameterizedTest
    @MethodSource("damageCases")
    void shouldApplyDamageAtHealthBoundaries(int damage, int expectedHealth) {
        Trap trap = new Trap("Wire", TrapType.ELECTRIC, damage, true, true);
        InteractionResult result = resolver.resolveTrap(player, trap);
        assertTrue(result.isSuccess());
        assertEquals("Trap 'Wire' triggered for " + damage + " damage.", result.getMessage());
        assertEquals(expectedHealth, player.getHealth());
        assertEquals(expectedHealth > 0, player.isAlive());
        assertFalse(trap.isArmed());
        assertFalse(resolver.resolveTrap(player, trap).isSuccess());
        assertEquals(expectedHealth, player.getHealth());
    }
    @Test
    void shouldKeepRepeatingTrapArmedAndApplyDamageEachTime() {
        Trap trap = new Trap("Steam", TrapType.STEAM, 3, true, false);
        assertTrue(resolver.resolveTrap(player, trap).isSuccess());
        assertEquals(17, player.getHealth());
        assertTrue(trap.isArmed());
        assertTrue(resolver.resolveTrap(player, trap).isSuccess());
        assertEquals(14, player.getHealth());
        assertTrue(trap.isArmed());
    }

}
