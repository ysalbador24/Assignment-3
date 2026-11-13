package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class RoomTest {

    private Room room;
    @BeforeEach
    void setUp() { room = new Room("hall", "Hall", "A dusty hall"); }

    @Test
    void shouldStartWithExpectedPropertiesAndEmptyContents() {
        assertEquals("hall", room.getId());
        assertEquals("Hall", room.getName());
        assertEquals("A dusty hall", room.getDescription());
        assertTrue(room.getExits().isEmpty());
        assertTrue(room.getItems().isEmpty());
        assertTrue(room.getMonsters().isEmpty());
        assertFalse(room.hasLivingMonsters());
        assertFalse(room.isLocked());
        assertNull(room.getRequiredKeyName());
        assertNull(room.getTrap());
    }

    static Stream<Direction> directions() { return Stream.of(Direction.values()); }
    @ParameterizedTest
    @MethodSource("directions")
    void shouldConnectAndReplaceExitInEachDirection(Direction direction) {
        Room first = new Room("one", "One", "First");
        Room second = new Room("two", "Two", "Second");
        assertNull(room.getExit(direction));
        room.connect(direction, first);
        assertSame(first, room.getExit(direction));
        assertSame(first, room.getExits().get(direction));
        assertNull(first.getExit(direction));
        room.connect(direction, second);
        assertSame(second, room.getExit(direction));
        assertEquals(1, room.getExits().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Archive Key", "archive key", "ARCHIVE KEY"})
    void shouldFindAndRemoveItemIgnoringCase(String name) {
        Key other = new Key("Other", "Unrelated");
        Key key = new Key("Archive Key", "Opens archive");
        room.addItem(other);
        room.addItem(key);
        assertSame(key, room.findItem(name).orElseThrow());
        assertSame(key, room.removeItemByName(name));
        assertTrue(room.findItem(name).isEmpty());
        assertEquals(List.of(other), room.getItems());
        assertNull(room.removeItemByName(name));
    }

    @Test
    void shouldRemoveOnlyFirstMatchingItemAndPreserveMissingLookup() {
        Key first = new Key("Key", "First");
        Key second = new Key("KEY", "Second");
        room.addItem(first);
        room.addItem(second);
        assertTrue(room.findItem("missing").isEmpty());
        assertNull(room.removeItemByName("missing"));
        assertEquals(2, room.getItems().size());
        assertSame(first, room.removeItemByName("key"));
        assertSame(second, room.findItem("key").orElseThrow());
    }

    @Test
    void shouldFindMonstersAndDetectLivingMonsters() {
        Monster dead = new Monster("Ghost", 5, 1, 0, List.of());
        Monster alive = new Monster("TA", 5, 1, 0, List.of());
        dead.takeDamage(5);
        room.addMonster(dead);
        assertFalse(room.hasLivingMonsters());
        room.addMonster(alive);
        assertTrue(room.hasLivingMonsters());
        assertSame(alive, room.findMonster("ta").orElseThrow());
        assertSame(dead, room.findMonster("GHOST").orElseThrow());
        assertTrue(room.findMonster("missing").isEmpty());
        assertEquals(List.of(dead, alive), room.getMonsters());
        alive.takeDamage(5);
        assertFalse(room.hasLivingMonsters());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Archive Key", "archive key", "ARCHIVE KEY"})
    void shouldUnlockWithMatchingName(String name) {
        room.setLocked(true, "Archive Key");
        assertTrue(room.isLocked());
        assertEquals("Archive Key", room.getRequiredKeyName());
        assertTrue(room.unlock(name));
        assertFalse(room.isLocked());
        assertEquals("Archive Key", room.getRequiredKeyName());
    }

    @Test
    void shouldHandleWrongMissingAndUnnecessaryKey() {
        assertTrue(room.unlock("anything"));
        room.setLocked(true, "Archive Key");
        assertFalse(room.unlock("Wrong Key"));
        assertFalse(room.unlock(null));
        assertTrue(room.isLocked());
        room.setLocked(true, null);
        assertFalse(room.unlock("Archive Key"));
        assertTrue(room.isLocked());
        room.setLocked(false, null);
        assertFalse(room.isLocked());
        assertNull(room.getRequiredKeyName());
    }

    @Test
    void shouldStoreReplaceAndClearTrap() {
        Trap first = new Trap("Wire", TrapType.ELECTRIC, 8, true, true);
        Trap second = new Trap("Steam", TrapType.STEAM, 3, false, false);
        room.setTrap(first);
        assertSame(first, room.getTrap());
        room.setTrap(second);
        assertSame(second, room.getTrap());
        room.setTrap(null);
        assertNull(room.getTrap());
    }

    @Test
    void shouldExposeUnmodifiableCollections() {
        Room next = new Room("next", "Next", "Next");
        Key key = new Key("Key", "Key");
        Monster ghost = new Monster("Ghost", 1, 1, 0, List.of());
        room.connect(Direction.NORTH, next);
        room.addItem(key);
        room.addMonster(ghost);
        assertThrows(UnsupportedOperationException.class, () -> room.getExits().clear());
        assertThrows(UnsupportedOperationException.class, () -> room.getItems().clear());
        assertThrows(UnsupportedOperationException.class, () -> room.getMonsters().clear());
        assertSame(next, room.getExit(Direction.NORTH));
        assertEquals(List.of(key), room.getItems());
        assertEquals(List.of(ghost), room.getMonsters());
    }

    @Test
    void shouldRejectNullRequiredArguments() {
        assertThrows(NullPointerException.class, () -> new Room(null, "Name", "Desc"));
        assertThrows(NullPointerException.class, () -> new Room("id", null, "Desc"));
        assertThrows(NullPointerException.class, () -> new Room("id", "Name", null));
        assertThrows(NullPointerException.class, () -> room.connect(null, room));
        assertThrows(NullPointerException.class, () -> room.connect(Direction.NORTH, null));
        assertThrows(NullPointerException.class, () -> room.addItem(null));
        assertThrows(NullPointerException.class, () -> room.addMonster(null));
    }

}
