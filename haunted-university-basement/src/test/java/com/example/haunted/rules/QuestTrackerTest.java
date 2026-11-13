package com.example.haunted.rules;

import static org.junit.jupiter.api.Assertions.*;
import com.example.haunted.model.BossMonster;
import com.example.haunted.model.Item;
import com.example.haunted.model.Key;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Quest;
import com.example.haunted.model.QuestItem;
import com.example.haunted.model.QuestStatus;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class QuestTrackerTest {

    private Quest quest;
    private QuestTracker tracker;
    @BeforeEach
    void setUp() { quest = new Quest("Escape", "Description"); tracker = new QuestTracker(); }
    static Stream<Arguments> itemCases() {
        return Stream.of(
            Arguments.of((Item)null, false),
            Arguments.of(new QuestItem("Lost Gradebook", "Book"), true),
            Arguments.of(new QuestItem("lost gradebook", "Book"), true),
            Arguments.of(new Key("LOST GRADEBOOK", "Name determines progress"), true),
            Arguments.of(new QuestItem("Other Book", "Unrelated"), false));
    }
    @ParameterizedTest
    @MethodSource("itemCases")
    void shouldOnlyRecordItemsWithGradebookName(Item item, boolean expected) {
        tracker.updateQuestForItem(quest, item);
        assertEquals(expected, quest.isGradebookRecovered());
        assertFalse(quest.isPhantomDefeated());
        assertFalse(quest.isComplete());
        assertEquals(expected ? QuestStatus.IN_PROGRESS : QuestStatus.NOT_STARTED, quest.getStatus());
    }
    static Stream<Arguments> monsterCases() {
        return Stream.of(Arguments.of(null,0,false),
            Arguments.of("Final Exam Phantom", 1, false),
            Arguments.of("Final Exam Phantom", 0, true),
            Arguments.of("final exam phantom", 0, true),
            Arguments.of("FINAL EXAM PHANTOM", 0, true),
            Arguments.of("Other Ghost", 0, false),
            Arguments.of("Other Ghost", 1, false));
    }
    @ParameterizedTest
    @MethodSource("monsterCases")
    void shouldOnlyRecordDefeatedPhantom(String name, int health, boolean expected) {
        Monster monster = name == null ? null : new Monster(name, health, 1, 0, List.of());
        tracker.updateQuestForMonster(quest, monster);
        assertEquals(expected, quest.isPhantomDefeated());
        assertFalse(quest.isGradebookRecovered());
        assertFalse(quest.isComplete());
        assertEquals(expected ? QuestStatus.IN_PROGRESS : QuestStatus.NOT_STARTED, quest.getStatus());
    }
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldCompleteQuestThroughTrackerInEitherOrder(boolean bookFirst) {
        Item book = new QuestItem("Lost Gradebook", "Book");
        Monster boss = new BossMonster("Final Exam Phantom", 1, 1, 0, List.of(), 3);
        boss.takeDamage(1);
        if (bookFirst) tracker.updateQuestForItem(quest, book);
        else tracker.updateQuestForMonster(quest, boss);
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
        if (bookFirst) tracker.updateQuestForMonster(quest, boss);
        else tracker.updateQuestForItem(quest, book);
        assertTrue(quest.isComplete());
        tracker.updateQuestForItem(quest, null);
        tracker.updateQuestForMonster(quest, null);
        assertTrue(quest.isComplete());
    }

}
