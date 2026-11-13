package com.example.haunted.engine;

import static org.junit.jupiter.api.Assertions.*;
import com.example.haunted.events.InteractionResult;
import com.example.haunted.model.Armor;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Item;
import com.example.haunted.model.Key;
import com.example.haunted.model.Player;
import com.example.haunted.model.Potion;
import com.example.haunted.model.QuestItem;
import com.example.haunted.model.Room;
import com.example.haunted.model.Weapon;
import com.example.haunted.rules.QuestTracker;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class InteractionEquipmentAndUnlockTest {

    private Player player;
    private Room current;
    private Room target;
    private InteractionEngine engine;
    @BeforeEach
    void setUp() {
        player = new Player("Student", 50, 7, 2, new Inventory(8));
        current = new Room("current", "Current", "Current room");
        target = new Room("archive", "Archive", "Locked room");
        current.connect(Direction.NORTH, target);
        player.setCurrentRoom(current);
        engine = new InteractionEngine(new QuestTracker());
    }
    @Test
    void shouldEquipAndReplaceWeaponWithoutConsumingIt() {
        Weapon first = new Weapon("Sword", "First", 4);
        Weapon second = new Weapon("Stapler", "Second", 6);
        player.getInventory().addItem(first);
        player.getInventory().addItem(second);
        InteractionResult result = engine.equipItem(player, "sWoRd");
        assertTrue(result.isSuccess());
        assertEquals("Equipped weapon Sword.", result.getMessage());
        assertSame(first, player.getEquippedWeapon());
        assertEquals(11, player.getAttackPower());
        assertEquals(2, player.getDefensePower());
        assertNull(player.getEquippedArmor());
        assertEquals(List.of(first, second), player.getInventory().getItems());
        assertTrue(engine.equipItem(player, "Stapler").isSuccess());
        assertSame(second, player.getEquippedWeapon());
        assertEquals(13, player.getAttackPower());
    }
    @Test
    void shouldEquipAndReplaceArmorWithoutConsumingIt() {
        Armor first = new Armor("Shield", "First", 3);
        Armor second = new Armor("Gown", "Second", 5);
        player.getInventory().addItem(first);
        player.getInventory().addItem(second);
        InteractionResult result = engine.equipItem(player, "sHiElD");
        assertTrue(result.isSuccess());
        assertEquals("Equipped armor Shield.", result.getMessage());
        assertSame(first, player.getEquippedArmor());
        assertEquals(5, player.getDefensePower());
        assertEquals(7, player.getAttackPower());
        assertNull(player.getEquippedWeapon());
        assertEquals(List.of(first, second), player.getInventory().getItems());
        assertTrue(engine.equipItem(player, "Gown").isSuccess());
        assertSame(second, player.getEquippedArmor());
        assertEquals(7, player.getDefensePower());
    }
    static Stream<Item> unequippableItems() {
        return Stream.of(new Key("Key", "Key"), new Potion("Potion", "Potion", 5),
            new QuestItem("Book", "Book"));
    }
    @ParameterizedTest
    @MethodSource("unequippableItems")
    void shouldRejectUnequippableItemsAndPreserveEquipment(Item item) {
        Weapon weapon = new Weapon("Sword", "Sword", 4);
        Armor armor = new Armor("Shield", "Shield", 3);
        player.equipWeapon(weapon);
        player.equipArmor(armor);
        player.getInventory().addItem(item);
        InteractionResult result = engine.equipItem(player, item.getName());
        assertFalse(result.isSuccess());
        assertEquals("That item cannot be equipped.", result.getMessage());
        assertSame(weapon, player.getEquippedWeapon());
        assertSame(armor, player.getEquippedArmor());
        assertEquals(List.of(item), player.getInventory().getItems());
    }
    @Test
    void shouldRejectMissingEquipment() {
        InteractionResult result = engine.equipItem(player, "Missing");
        assertFalse(result.isSuccess());
        assertEquals("Item not found in inventory.", result.getMessage());
        assertNull(player.getEquippedWeapon());
        assertNull(player.getEquippedArmor());
        assertTrue(player.getInventory().getItems().isEmpty());
    }
    @Test
    void shouldRejectMissingExitWithoutMoving() {
        InteractionResult result = engine.unlockRoom(player, Direction.SOUTH);
        assertFalse(result.isSuccess());
        assertEquals("There is no room in that direction.", result.getMessage());
        assertSame(current, player.getCurrentRoom());
    }
    @Test
    void shouldAcceptAlreadyUnlockedRoomWithoutKey() {
        InteractionResult result = engine.unlockRoom(player, Direction.NORTH);
        assertTrue(result.isSuccess());
        assertEquals("The room is already unlocked.", result.getMessage());
        assertFalse(target.isLocked());
        assertSame(current, player.getCurrentRoom());
    }
    @Test
    void shouldRejectLockedRoomWithoutRequiredKeyName() {
        target.setLocked(true, null);
        InteractionResult result = engine.unlockRoom(player, Direction.NORTH);
        assertFalse(result.isSuccess());
        assertEquals("The room cannot be unlocked.", result.getMessage());
        assertTrue(target.isLocked());
    }
    static Stream<Arguments> wrongKeys() {
        return Stream.of(Arguments.of((Item)null), Arguments.of(new Key("Wrong Key", "Wrong")),
            Arguments.of(new Potion("Archive Key", "Not a key", 5)),
            Arguments.of(new QuestItem("Archive Key", "Not a key")));
    }
    @ParameterizedTest
    @MethodSource("wrongKeys")
    void shouldRejectMissingWrongAndNonKeyItems(Item item) {
        target.setLocked(true, "Archive Key");
        if (item != null) player.getInventory().addItem(item);
        InteractionResult result = engine.unlockRoom(player, Direction.NORTH);
        assertFalse(result.isSuccess());
        assertEquals("You do not have the correct key.", result.getMessage());
        assertTrue(target.isLocked());
        assertSame(current, player.getCurrentRoom());
        assertEquals(item == null ? 0 : 1, player.getInventory().getItems().size());
    }
    @ParameterizedTest
    @ValueSource(strings = {"Archive Key", "archive key", "ARCHIVE KEY"})
    void shouldUnlockWithCorrectKeyWithoutConsumingOrMoving(String name) {
        target.setLocked(true, "Archive Key");
        Key key = new Key(name, "Opens archive");
        player.getInventory().addItem(key);
        InteractionResult result = engine.unlockRoom(player, Direction.NORTH);
        assertTrue(result.isSuccess());
        assertEquals("Unlocked Archive.", result.getMessage());
        assertFalse(target.isLocked());
        assertEquals("Archive Key", target.getRequiredKeyName());
        assertEquals(List.of(key), player.getInventory().getItems());
        assertSame(current, player.getCurrentRoom());
        assertTrue(engine.unlockRoom(player, Direction.NORTH).isSuccess());
        assertEquals(List.of(key), player.getInventory().getItems());
    }

}
