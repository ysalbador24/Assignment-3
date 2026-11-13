package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class QuestTest {

    @Test
    void shouldBeginWithNeitherObjectiveAndNotStartedStatus() {
        Quest quest = new Quest("Escape", "Find book and defeat boss");
        assertEquals("Escape", quest.getName());
        assertEquals("Find book and defeat boss", quest.getDescription());
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
        assertFalse(quest.isGradebookRecovered());
        assertFalse(quest.isPhantomDefeated());
        assertFalse(quest.isComplete());
        quest.updateStatus();
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
    }
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldCompleteObjectivesInEitherOrder(boolean bookFirst) {
        Quest quest = new Quest("Escape", "Description");
        if (bookFirst) quest.markGradebookRecovered();
        else quest.markPhantomDefeated();
        assertEquals(bookFirst, quest.isGradebookRecovered());
        assertEquals(!bookFirst, quest.isPhantomDefeated());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
        assertFalse(quest.isComplete());
        // Repeating one objective must not complete the other objective.
        if (bookFirst) quest.markGradebookRecovered();
        else quest.markPhantomDefeated();
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
        assertFalse(quest.isComplete());
        if (bookFirst) quest.markPhantomDefeated();
        else quest.markGradebookRecovered();
        assertTrue(quest.isGradebookRecovered());
        assertTrue(quest.isPhantomDefeated());
        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
        assertTrue(quest.isComplete());
        quest.markGradebookRecovered();
        quest.markPhantomDefeated();
        quest.updateStatus();
        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
        assertTrue(quest.isComplete());
    }
    @Test
    void shouldRejectNullNameAndDescription() {
        assertThrows(NullPointerException.class, () -> new Quest(null, "Desc"));
        assertThrows(NullPointerException.class, () -> new Quest("Name", null));
    }

}
