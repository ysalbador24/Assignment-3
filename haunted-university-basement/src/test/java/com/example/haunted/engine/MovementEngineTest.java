package com.example.haunted.engine;

import static org.junit.jupiter.api.Assertions.*;
import com.example.haunted.events.MoveResult;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Player;
import com.example.haunted.model.Room;
import com.example.haunted.model.Trap;
import com.example.haunted.model.TrapType;
import com.example.haunted.rules.TrapResolver;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class MovementEngineTest {

    private Player player;
    private Room start;
    private Room next;
    private MovementEngine engine;
    @BeforeEach
    void setUp() {
        start = new Room("start", "Start", "Start room");
        next = new Room("next", "Next", "Next room");
        player = new Player("Student", 20, 7, 2, new Inventory(8));
        player.setCurrentRoom(start);
        engine = new MovementEngine(new TrapResolver());
    }
    static Stream<Direction> directions() { return Stream.of(Direction.values()); }
    @ParameterizedTest
    @MethodSource("directions")
    void shouldMoveThroughEachConnectedExit(Direction direction) {
        start.connect(direction, next);
        MoveResult result = engine.move(player, direction);
        assertTrue(result.isSuccess());
        assertSame(next, player.getCurrentRoom());
        assertEquals("Moved to Next.", result.getMessage());
        assertFalse(result.isTrapTriggered());
        assertEquals(0, result.getTrapDamage());
        assertEquals(20, player.getHealth());
    }
    @ParameterizedTest
    @MethodSource("directions")
    void shouldStayInCurrentRoomWhenExitIsMissing(Direction direction) {
        MoveResult result = engine.move(player, direction);
        assertFalse(result.isSuccess());
        assertEquals("There is no room in that direction.", result.getMessage());
        assertSame(start, player.getCurrentRoom());
        assertFalse(result.isTrapTriggered());
        assertEquals(0, result.getTrapDamage());
        assertEquals(20, player.getHealth());
    }
    @Test
    void shouldNotMoveOrTriggerTrapInLockedRoom() {
        start.connect(Direction.EAST, next);
        next.setLocked(true, "Key");
        Trap trap = new Trap("Wire", TrapType.ELECTRIC, 8, true, true);
        next.setTrap(trap);
        MoveResult result = engine.move(player, Direction.EAST);
        assertFalse(result.isSuccess());
        assertEquals("The room is locked.", result.getMessage());
        assertSame(start, player.getCurrentRoom());
        assertFalse(result.isTrapTriggered());
        assertEquals(0, result.getTrapDamage());
        assertEquals(20, player.getHealth());
        assertTrue(trap.isArmed());
        assertTrue(next.isLocked());
    }
    @Test
    void shouldMoveWithoutTriggeringDisarmedTrap() {
        start.connect(Direction.EAST, next);
        next.setTrap(new Trap("Wire", TrapType.ELECTRIC, 8, false, true));
        MoveResult result = engine.move(player, Direction.EAST);
        assertTrue(result.isSuccess());
        assertSame(next, player.getCurrentRoom());
        assertEquals("Moved to Next.", result.getMessage());
        assertFalse(result.isTrapTriggered());
        assertEquals(0, result.getTrapDamage());
        assertEquals(20, player.getHealth());
    }
    static Stream<Arguments> entryCases() {
        return Stream.of(Arguments.of(0,20), Arguments.of(8,12), Arguments.of(20,0), Arguments.of(25,0));
    }
    @ParameterizedTest
    @MethodSource("entryCases")
    void shouldEnterAndReportTrapDamageEvenWhenLethal(int damage, int expectedHealth) {
        start.connect(Direction.EAST, next);
        Trap trap = new Trap("Wire", TrapType.ELECTRIC, damage, true, true);
        next.setTrap(trap);
        MoveResult result = engine.move(player, Direction.EAST);
        assertTrue(result.isSuccess());
        assertSame(next, player.getCurrentRoom());
        assertTrue(result.isTrapTriggered());
        assertEquals(damage, result.getTrapDamage());
        assertEquals("Moved to Next. Trap 'Wire' triggered for " + damage + " damage.", result.getMessage());
        assertEquals(expectedHealth, player.getHealth());
        assertFalse(trap.isArmed());
    }
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldRespectOneTimeAndRepeatingTrapsOnReentry(boolean oneTime) {
        start.connect(Direction.EAST, next);
        next.connect(Direction.WEST, start);
        Trap trap = new Trap("Wire", TrapType.ELECTRIC, 3, true, oneTime);
        next.setTrap(trap);
        engine.move(player, Direction.EAST);
        assertEquals(17, player.getHealth());
        engine.move(player, Direction.WEST);
        MoveResult second = engine.move(player, Direction.EAST);
        assertTrue(second.isSuccess());
        assertEquals(!oneTime, second.isTrapTriggered());
        assertEquals(oneTime ? 0 : 3, second.getTrapDamage());
        assertEquals(oneTime ? 17 : 14, player.getHealth());
        assertEquals(!oneTime, trap.isArmed());
        assertSame(next, player.getCurrentRoom());
    }

}
