package com.example.haunted.engine;

import static org.junit.jupiter.api.Assertions.*;
import com.example.haunted.events.CombatResult;
import com.example.haunted.events.MoveResult;
import com.example.haunted.model.Armor;
import com.example.haunted.model.BossMonster;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Key;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Player;
import com.example.haunted.model.Potion;
import com.example.haunted.model.Quest;
import com.example.haunted.model.QuestItem;
import com.example.haunted.model.QuestStatus;
import com.example.haunted.model.Room;
import com.example.haunted.model.Trap;
import com.example.haunted.model.TrapType;
import com.example.haunted.model.Weapon;
import com.example.haunted.rules.DamageCalculator;
import com.example.haunted.rules.QuestTracker;
import com.example.haunted.rules.TrapResolver;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class GameEngineTest {

    private Player player;
    private Quest quest;
    private Room start;
    private Room next;
    private GameEngine game;
    @BeforeEach
    void setUp() {
        player = new Player("Student", 30, 7, 2, new Inventory(8));
        quest = new Quest("Escape", "Recover book and defeat phantom");
        start = new Room("start", "Start", "Start");
        next = new Room("next", "Next", "Next");
        start.connect(Direction.EAST, next);
        next.connect(Direction.WEST, start);
        player.setCurrentRoom(start);
        QuestTracker tracker = new QuestTracker();
        game = new GameEngine(player, quest, new MovementEngine(new TrapResolver()),
            new CombatEngine(new DamageCalculator(), tracker), new InteractionEngine(tracker));
    }
    @Test
    void shouldExposeActualPlayerQuestAndCurrentRoom() {
        assertSame(player, game.getPlayer());
        assertSame(quest, game.getQuest());
        assertSame(start, game.getCurrentRoom());
        assertTrue(game.move(Direction.EAST).isSuccess());
        assertSame(next, game.getCurrentRoom());
        assertFalse(game.isGameOver());
        assertFalse(game.isGameWon());
    }
    static Stream<Arguments> outcomeCases() {
        return Stream.of(Arguments.of(false,false,true), Arguments.of(true,false,true),
            Arguments.of(false,true,true), Arguments.of(true,true,true),
            Arguments.of(false,false,false), Arguments.of(true,false,false),
            Arguments.of(false,true,false), Arguments.of(true,true,false));
    }
    @ParameterizedTest
    @MethodSource("outcomeCases")
    void shouldOnlyWinWhenBothObjectivesCompleteAndPlayerAlive(boolean book, boolean boss, boolean alive) {
        if (book) quest.markGradebookRecovered();
        if (boss) quest.markPhantomDefeated();
        if (!alive) player.takeDamage(30);
        assertEquals(!alive, game.isGameOver());
        assertEquals(book && boss && alive, game.isGameWon());
    }
    @Test
    void shouldPickUpEquipUseUnlockAndMoveThroughPublicGameMethods() {
        Weapon weapon = new Weapon("Sword", "Sword", 4);
        Armor armor = new Armor("Shield", "Shield", 3);
        Potion potion = new Potion("Coffee", "Coffee", 5);
        Key key = new Key("Archive Key", "Key");
        start.addItem(weapon);
        start.addItem(armor);
        start.addItem(potion);
        start.addItem(key);
        next.setLocked(true, "Archive Key");
        assertFalse(game.move(Direction.EAST).isSuccess());
        assertFalse(game.unlockRoom(Direction.EAST).isSuccess());
        for (String name : List.of("Sword", "Shield", "Coffee", "Archive Key")) {
            assertTrue(game.pickUpItem(name).isSuccess());
        }
        assertTrue(start.getItems().isEmpty());
        assertTrue(game.equipItem("Sword").isSuccess());
        assertTrue(game.equipItem("Shield").isSuccess());
        assertSame(weapon, player.getEquippedWeapon());
        assertSame(armor, player.getEquippedArmor());
        player.takeDamage(9);
        assertTrue(game.useItem("Coffee").isSuccess());
        assertEquals(26, player.getHealth());
        assertFalse(player.getInventory().contains("Coffee"));
        assertFalse(game.useItem("Coffee").isSuccess());
        assertTrue(game.unlockRoom(Direction.EAST).isSuccess());
        assertFalse(next.isLocked());
        assertTrue(game.move(Direction.EAST).isSuccess());
        assertSame(next, game.getCurrentRoom());
    }
    @Test
    void shouldOnlyAttackMonsterInCurrentRoomAndApplyReturnedDamage() {
        Monster ghost = new Monster("Ghost", 10, 5, 1, List.of());
        next.addMonster(ghost);
        CombatResult missing = game.attack("Ghost");
        assertFalse(missing.isSuccess());
        assertEquals(10, ghost.getHealth());
        game.move(Direction.EAST);
        CombatResult result = game.attack("gHoSt");
        assertTrue(result.isSuccess());
        assertEquals(6, result.getDamageToMonster());
        assertEquals(3, result.getDamageToPlayer());
        assertEquals(4, ghost.getHealth());
        assertEquals(27, player.getHealth());
        assertFalse(result.isMonsterDefeated());
    }
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldWinThroughPickupAndCombatInEitherOrder(boolean bookFirst) {
        next.addItem(new QuestItem("Lost Gradebook", "Book"));
        Monster boss = new BossMonster("Final Exam Phantom", 6, 10, 0, List.of(), 3);
        next.addMonster(boss);
        game.move(Direction.EAST);
        if (bookFirst) assertTrue(game.pickUpItem("Lost Gradebook").isSuccess());
        else assertTrue(game.attack("Final Exam Phantom").isMonsterDefeated());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
        assertFalse(game.isGameWon());
        if (bookFirst) assertTrue(game.attack("Final Exam Phantom").isMonsterDefeated());
        else assertTrue(game.pickUpItem("Lost Gradebook").isSuccess());
        assertTrue(quest.isComplete());
        assertTrue(game.isGameWon());
        assertFalse(game.isGameOver());
        assertEquals(30, player.getHealth());
        assertFalse(boss.isAlive());
        assertTrue(player.getInventory().contains("Lost Gradebook"));
    }
    @Test
    void shouldReportGameOverAfterLethalTrap() {
        next.setTrap(new Trap("Deadly steam", TrapType.STEAM, 30, true, true));
        MoveResult result = game.move(Direction.EAST);
        assertTrue(result.isSuccess());
        assertTrue(result.isTrapTriggered());
        assertSame(next, game.getCurrentRoom());
        assertEquals(0, player.getHealth());
        assertTrue(game.isGameOver());
        assertFalse(game.isGameWon());
    }

}
