package com.example.haunted.loot.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;

import com.example.haunted.engine.CombatEngine;
import com.example.haunted.engine.InteractionEngine;
import com.example.haunted.events.CombatResult;
import com.example.haunted.events.InteractionResult;
import com.example.haunted.model.Armor;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Key;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Item;

import com.example.haunted.model.Player;
import com.example.haunted.model.Potion;
import com.example.haunted.model.Quest;
import com.example.haunted.model.QuestItem;
import com.example.haunted.model.QuestStatus;
import com.example.haunted.model.Room;
import com.example.haunted.model.Weapon;
import com.example.haunted.rules.DamageCalculator;

import com.example.haunted.rules.QuestTracker;

class LootTest {

	private Player player;
	private Monster monster;
	private CombatEngine combatEngine;
	private InteractionEngine interactionEngine;
	private Quest quest;
	private Room room;
	private Potion potion;

	@BeforeEach
	void setUp() {
		
		potion = new  Potion("Health Potion", "Restores health.", 20);
		
		List<Item> loot = new ArrayList<>();
		loot.add(potion);
		
		player = new Player("Bob", 100, 10, 5, new Inventory(2));
		
		monster = new Monster("Zombie", 5, 10, 5, loot);
		
		combatEngine = new CombatEngine(new DamageCalculator(), new QuestTracker());
		
		interactionEngine = new InteractionEngine(new QuestTracker());
		
		quest = new Quest("Main Quest", "Defeat the Final Exam Phantom and recover the Lost Gradebook.");
		
		room = new Room("R1", "Basement", "A dark basement.");
		player.setCurrentRoom(room);
		
	}

	// Monster loot is transferred into the room when defeated
    @Test
    void defeatedMonsterDropsLootIntoRoom() {
        CombatResult result = combatEngine.attack(player, quest, monster); // monster health starts at 5

        assertTrue(result.isMonsterDefeated());
        assertTrue(room.findItem("Health Potion").isPresent());
        assertEquals(1, result.getDroppedItems().size());
    }

    // Player can collect an item from the room
    @ParameterizedTest
    @ValueSource(strings = {"Potion", "Weapon", "Armor", "Key", "QuestItem"})
    void playerCanPickUpDroppedLoot(String itemType) {
        Item item = createItem(itemType);
        room.addItem(item);

        InteractionResult result =
                interactionEngine.pickUpItem(player, quest, item.getName());

        assertTrue(result.isSuccess());
        assertTrue(player.getInventory().contains(item.getName()));
        assertFalse(room.findItem(item.getName()).isPresent());
    }

    // Full inventory rejects an additional item
    @Test
    void playerCannotCollectItemWithFullInventory() {
        player.getInventory().addItem(
                new Potion("Potion 1", "First potion.", 10));
        player.getInventory().addItem(
                new Potion("Potion 2", "Second potion.", 10));

        Item loot = new Key("Basement Key", "Opens a locked door.");
        room.addItem(loot);

        InteractionResult result =
                interactionEngine.pickUpItem(player, quest, loot.getName());

        assertFalse(result.isSuccess());
        assertTrue(player.getInventory().isFull());
    }

    // Rejected item remains in the room
    @Test
    void rejectedItemRemainsInRoom() {
        player.getInventory().addItem(
                new Potion("Potion 1", "First potion.", 10));
        player.getInventory().addItem(
                new Potion("Potion 2", "Second potion.", 10));

        Item loot = new Key("Basement Key", "Opens a locked door.");
        room.addItem(loot);

        interactionEngine.pickUpItem(player, quest, loot.getName());

        assertTrue(room.findItem("Basement Key").isPresent());
        assertFalse(player.getInventory().contains("Basement Key"));
    }

    // Player can collect an item after freeing inventory space
    @Test
    void playerCanCollectItemAfterFreeingInventorySpace() {
        player.getInventory().addItem(
                new Potion("Potion 1", "First potion.", 10));
        player.getInventory().addItem(
                new Potion("Potion 2", "Second potion.", 10));

        Item loot = new Key("Basement Key", "Opens a locked door.");
        room.addItem(loot);

        InteractionResult firstAttempt =
                interactionEngine.pickUpItem(player, quest, loot.getName());

        assertFalse(firstAttempt.isSuccess());

        player.getInventory().removeItem("Potion 1");

        InteractionResult secondAttempt =
                interactionEngine.pickUpItem(player, quest, loot.getName());

        assertTrue(secondAttempt.isSuccess());
        assertTrue(player.getInventory().contains("Basement Key"));
        assertFalse(room.findItem("Basement Key").isPresent());
    }
    
    @Test
    void pickingUpLostGradeBookUpdatesQuest() {
    	Item gradebook = new QuestItem("Lost Gradebook", "An important quest item.");
    	
    	room.addItem(gradebook);
    	
    	InteractionResult result = interactionEngine.pickUpItem(player, quest, gradebook.getName());
    	
    	assertTrue(result.isSuccess());
    	assertTrue(quest.isGradebookRecovered());
    	assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus()); // only gradebook has been recovered 1/2 completion
    }
    
    @Test
    void playerCannotPickUpItemNotPresent() {
    	InteractionResult result = interactionEngine.pickUpItem(player, quest, "Missing Item");
    	
    	assertFalse(result.isSuccess());
    	assertFalse(player.getInventory().contains("Missing Item"));
    	assertTrue(room.getItems().isEmpty());
    }

    // Helper method for creating different item types
    private Item createItem(String itemType) {
        switch (itemType) {
            case "Potion":
                return new Potion("Health Potion", "Restores health.", 20);
            case "Weapon":
                return new Weapon("Sword", "A basic sword.", 5);
            case "Armor":
                return new Armor("Shield", "A protective shield.", 5);
            case "Key":
                return new Key("Basement Key", "Opens a locked door.");
            case "QuestItem":
                return new QuestItem("Lost Gradebook", "An important quest item.");
            default:
                throw new IllegalArgumentException("Unknown item type");
        }
    }
}