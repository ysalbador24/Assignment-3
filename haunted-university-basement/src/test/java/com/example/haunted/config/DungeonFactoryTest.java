package com.example.haunted.config;

import static org.junit.jupiter.api.Assertions.*;
import com.example.haunted.engine.GameEngine;
import com.example.haunted.events.MoveResult;
import com.example.haunted.model.Armor;
import com.example.haunted.model.BossMonster;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Item;
import com.example.haunted.model.Key;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Player;
import com.example.haunted.model.Potion;
import com.example.haunted.model.QuestItem;
import com.example.haunted.model.QuestStatus;
import com.example.haunted.model.Room;
import com.example.haunted.model.Trap;
import com.example.haunted.model.TrapType;
import com.example.haunted.model.Weapon;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DungeonFactoryTest {

    private GameEngine game;
    private java.util.Map<String, Room> rooms;
    @BeforeEach
    void setUp() {
        game = DungeonFactory.createGame();
        rooms = new java.util.HashMap<>();
        java.util.ArrayDeque<Room> pending = new java.util.ArrayDeque<>();
        pending.add(game.getCurrentRoom());
        while (!pending.isEmpty()) {
            Room room = pending.remove();
            if (rooms.putIfAbsent(room.getId(), room) == null) pending.addAll(room.getExits().values());
        }
    }
    @Test
    void shouldCreateExpectedStartingPlayerAndQuest() {
        Player player = game.getPlayer();
        assertEquals("Student Explorer", player.getName());
        assertEquals(50, player.getHealth());
        assertEquals(50, player.getMaxHealth());
        assertEquals(7, player.getBaseAttack());
        assertEquals(2, player.getBaseDefense());
        assertEquals(8, player.getInventory().getCapacity());
        assertTrue(player.getInventory().getItems().isEmpty());
        assertNull(player.getEquippedArmor());
        assertNull(player.getEquippedWeapon());
        assertEquals("stairwell", game.getCurrentRoom().getId());
        assertEquals("Escape the Basement", game.getQuest().getName());
        assertEquals("Recover the Lost Gradebook and defeat the Final Exam Phantom.", game.getQuest().getDescription());
        assertEquals(QuestStatus.NOT_STARTED, game.getQuest().getStatus());
        assertFalse(game.isGameWon());
        assertFalse(game.isGameOver());
    }
    static Stream<Arguments> roomCases() {
        return Stream.of(
            Arguments.of("stairwell", "Maintenance Stairwell", "The flickering stairwell leading into the basement.", 1, 0, 0, false, null),
            Arguments.of("lectureHall", "Abandoned Lecture Hall", "Dusty desks and a broken projector.", 4, 1, 1, false, null),
            Arguments.of("labStorage", "Lab Storage", "Shelves of outdated hardware and chemicals.", 2, 2, 0, false, null),
            Arguments.of("brokenElevator", "Broken Elevator", "The elevator doors hang over a dark shaft.", 1, 0, 0, false, null),
            Arguments.of("serverCloset", "Server Closet", "A warm humming room full of blinking servers.", 2, 1, 1, false, null),
            Arguments.of("examArchive", "Exam Archive", "Cabinets of old exams and paperwork.", 2, 1, 1, true, "Archive Key"),
            Arguments.of("deanVault", "Dean Vault", "A hidden chamber guarded by university secrets.", 3, 1, 1, false, null),
            Arguments.of("finalChamber", "Final Chamber", "A spectral exam hall where the boss waits.", 1, 0, 1, true, "Vault Key"));
    }
    @ParameterizedTest
    @MethodSource("roomCases")
    void shouldBuildAllEightRoomsWithExpectedContents(String id, String name, String desc,
            int exits, int items, int monsters, boolean locked, String key) {
        assertEquals(8, rooms.size());
        Room room = rooms.get(id);
        assertNotNull(room);
        assertEquals(name, room.getName());
        assertEquals(desc, room.getDescription());
        assertEquals(exits, room.getExits().size());
        assertEquals(items, room.getItems().size());
        assertEquals(monsters, room.getMonsters().size());
        assertEquals(locked, room.isLocked());
        assertEquals(key, room.getRequiredKeyName());
        if (!id.equals("brokenElevator")) assertNull(room.getTrap());
    }
    static Stream<Arguments> exitCases() {
        return Stream.of(
            Arguments.of("stairwell", Direction.EAST, "lectureHall"),
            Arguments.of("lectureHall", Direction.WEST, "stairwell"),
            Arguments.of("lectureHall", Direction.EAST, "labStorage"),
            Arguments.of("lectureHall", Direction.SOUTH, "brokenElevator"),
            Arguments.of("lectureHall", Direction.NORTH, "examArchive"),
            Arguments.of("labStorage", Direction.WEST, "lectureHall"),
            Arguments.of("labStorage", Direction.NORTH, "serverCloset"),
            Arguments.of("brokenElevator", Direction.NORTH, "lectureHall"),
            Arguments.of("serverCloset", Direction.SOUTH, "labStorage"),
            Arguments.of("serverCloset", Direction.EAST, "deanVault"),
            Arguments.of("examArchive", Direction.SOUTH, "lectureHall"),
            Arguments.of("examArchive", Direction.EAST, "deanVault"),
            Arguments.of("deanVault", Direction.WEST, "examArchive"),
            Arguments.of("deanVault", Direction.SOUTH, "serverCloset"),
            Arguments.of("deanVault", Direction.NORTH, "finalChamber"),
            Arguments.of("finalChamber", Direction.SOUTH, "deanVault"));
    }
    @ParameterizedTest
    @MethodSource("exitCases")
    void shouldConnectEveryExpectedExit(String from, Direction direction, String to) {
        assertSame(rooms.get(to), rooms.get(from).getExit(direction));
    }
    static Stream<Arguments> itemCases() {
        return Stream.of(
            Arguments.of("lectureHall", "Coffee Potion", Potion.class, "A suspiciously warm cup of coffee.", 15),
            Arguments.of("labStorage", "Archive Key", Key.class, "Opens the Exam Archive.", 0),
            Arguments.of("labStorage", "Calculator Shield", Armor.class, "A battered calculator used as a shield.", 3),
            Arguments.of("serverCloset", "Stapler of Justice", Weapon.class, "A heavy-duty stapler with righteous force.", 4),
            Arguments.of("examArchive", "Lost Gradebook", QuestItem.class, "The legendary missing gradebook.", 0),
            Arguments.of("deanVault", "Vault Key", Key.class, "Opens the Final Chamber.", 0));
    }
    @ParameterizedTest
    @MethodSource("itemCases")
    void shouldPlaceExpectedItems(String roomId, String name, Class<?> type, String description, int bonus) {
        Item item = rooms.get(roomId).findItem(name).orElseThrow();
        assertEquals(type, item.getClass());
        assertEquals(name, item.getName());
        assertEquals(description, item.getDescription());
        if (item instanceof Potion) assertEquals(bonus, ((Potion)item).getHealingAmount());
        if (item instanceof Weapon) assertEquals(bonus, ((Weapon)item).getAttackBonus());
        if (item instanceof Armor) assertEquals(bonus, ((Armor)item).getDefenseBonus());
    }
    static Stream<Arguments> monsterCases() {
        return Stream.of(
            Arguments.of("lectureHall", "Sleep-Deprived TA",18,6,1,"Coffee Potion","A backup coffee drop.",15),
            Arguments.of("serverCloset", "Spreadsheet Golem",28,7,4,"Dry Erase Sword","An improvised marker tray blade.",6),
            Arguments.of("examArchive", "Plagiarism Ghost",22,8,2,null,null,0),
            Arguments.of("deanVault", "Registrar Wraith",30,9,3,"Graduation Gown Armor","Ceremonial robes with uncanny resilience.",5),
            Arguments.of("finalChamber", "Final Exam Phantom",40,10,4,null,null,0));
    }
    @ParameterizedTest
    @MethodSource("monsterCases")
    void shouldPlaceMonstersWithExpectedStatsAndLoot(String roomId, String name, int hp,
            int attack, int defense, String lootName, String lootDescription, int bonus) {
        Monster monster = rooms.get(roomId).findMonster(name).orElseThrow();
        assertEquals(name, monster.getName());
        assertEquals(hp, monster.getHealth());
        assertEquals(hp, monster.getMaxHealth());
        assertEquals(attack, monster.getAttack());
        assertEquals(defense, monster.getDefense());
        assertTrue(monster.isAlive());
        assertEquals(lootName == null ? 0 : 1, monster.getLoot().size());
        if (lootName != null) {
            Item item = monster.getLoot().get(0);
            assertEquals(lootName, item.getName());
            assertEquals(lootDescription, item.getDescription());
            if (roomId.equals("lectureHall")) {
                assertInstanceOf(Potion.class, item);
                assertEquals(bonus, ((Potion)item).getHealingAmount());
            } else if (roomId.equals("serverCloset")) {
                assertInstanceOf(Weapon.class, item);
                assertEquals(bonus, ((Weapon)item).getAttackBonus());
            } else {
                assertInstanceOf(Armor.class, item);
                assertEquals(bonus, ((Armor)item).getDefenseBonus());
            }
        }
        if (roomId.equals("finalChamber")) {
            assertInstanceOf(BossMonster.class, monster);
            BossMonster boss = (BossMonster)monster;
            assertEquals(10, boss.getCurrentAttack());
            boss.takeDamage(20);
            assertEquals(13, boss.getCurrentAttack());
        } else assertEquals(Monster.class, monster.getClass());
    }
    @Test
    void shouldPlaceArmedOneTimeElectricTrapInElevator() {
        Trap trap = rooms.get("brokenElevator").getTrap();
        assertNotNull(trap);
        assertEquals("Loose Wires Trap", trap.getName());
        assertEquals(TrapType.ELECTRIC, trap.getType());
        assertEquals(8, trap.getDamage());
        assertTrue(trap.isArmed());
        assertTrue(trap.isOneTimeTrigger());
        game.move(Direction.EAST);
        MoveResult result = game.move(Direction.SOUTH);
        assertTrue(result.isTrapTriggered());
        assertEquals(42, game.getPlayer().getHealth());
        assertFalse(trap.isArmed());
    }
    @Test
    void shouldCreateIndependentGames() {
        GameEngine second = DungeonFactory.createGame();
        assertNotSame(game, second);
        assertNotSame(game.getPlayer(), second.getPlayer());
        assertNotSame(game.getQuest(), second.getQuest());
        assertNotSame(game.getCurrentRoom(), second.getCurrentRoom());
        game.getPlayer().takeDamage(10);
        game.getQuest().markGradebookRecovered();
        game.move(Direction.EAST);
        game.pickUpItem("Coffee Potion");
        assertEquals(50, second.getPlayer().getHealth());
        assertEquals(QuestStatus.NOT_STARTED, second.getQuest().getStatus());
        assertEquals("stairwell", second.getCurrentRoom().getId());
        second.move(Direction.EAST);
        assertTrue(second.getCurrentRoom().findItem("Coffee Potion").isPresent());
    }
    @Test
    void shouldAllowCompleteAdventureUsingOnlyPublicGameActions() {
        // Take equipment and keys before fighting; use coffee when needed.
        assertTrue(game.move(Direction.EAST).isSuccess());
        assertTrue(game.pickUpItem("Coffee Potion").isSuccess());
        assertTrue(game.move(Direction.EAST).isSuccess());
        assertTrue(game.pickUpItem("Archive Key").isSuccess());
        assertTrue(game.pickUpItem("Calculator Shield").isSuccess());
        assertTrue(game.equipItem("Calculator Shield").isSuccess());
        assertTrue(game.move(Direction.NORTH).isSuccess());
        assertTrue(game.pickUpItem("Stapler of Justice").isSuccess());
        assertTrue(game.equipItem("Stapler of Justice").isSuccess());
        for (int i = 0; i < 4; i++) assertTrue(game.attack("Spreadsheet Golem").isSuccess());
        assertFalse(game.getCurrentRoom().findMonster("Spreadsheet Golem").orElseThrow().isAlive());
        assertTrue(game.pickUpItem("Dry Erase Sword").isSuccess());
        assertTrue(game.equipItem("Dry Erase Sword").isSuccess());
        game.move(Direction.SOUTH);
        game.move(Direction.WEST);
        assertTrue(game.unlockRoom(Direction.NORTH).isSuccess());
        assertTrue(game.move(Direction.NORTH).isSuccess());
        assertTrue(game.pickUpItem("Lost Gradebook").isSuccess());
        assertEquals(QuestStatus.IN_PROGRESS, game.getQuest().getStatus());
        assertTrue(game.move(Direction.EAST).isSuccess());
        assertTrue(game.pickUpItem("Vault Key").isSuccess());
        for (int i = 0; i < 3; i++) assertTrue(game.attack("Registrar Wraith").isSuccess());
        assertTrue(game.pickUpItem("Graduation Gown Armor").isSuccess());
        assertTrue(game.equipItem("Graduation Gown Armor").isSuccess());
        assertTrue(game.useItem("Coffee Potion").isSuccess());
        assertTrue(game.unlockRoom(Direction.NORTH).isSuccess());
        assertTrue(game.move(Direction.NORTH).isSuccess());
        for (int i = 0; i < 5; i++) assertTrue(game.attack("Final Exam Phantom").isSuccess());
        assertTrue(game.getQuest().isGradebookRecovered());
        assertTrue(game.getQuest().isPhantomDefeated());
        assertTrue(game.isGameWon());
        assertFalse(game.isGameOver());
        assertEquals(QuestStatus.COMPLETED, game.getQuest().getStatus());
    }

}
