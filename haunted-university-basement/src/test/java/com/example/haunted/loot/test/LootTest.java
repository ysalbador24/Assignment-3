package com.example.haunted.loot.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

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

	@BeforeEach
	void setUp() {
		player = new Player("Bob", 100, 10, 5, new Inventory(2));
		monster = new Monster("Zombie", 50, 10, 5, new ArrayList<>());
		combatEngine = new CombatEngine(new DamageCalculator(), new QuestTracker());
		interactionEngine = new InteractionEngine(new QuestTracker());
		quest = new Quest("Main Quest", "Defeat the Final Exam Phantom and recover the Lost Gradebook.");
		room = new Room("R1", "Basement", "A dark basement.");
		player.setCurrentRoom(room);
	}

	// Monster loot is transferred into the room when defeated
    @Test
    void defeatedMonsterDropsLootIntoRoom() {
        Item loot = new Potion("Health Potion", "Restores health.", 20);

        ArrayList<Item> monsterLoot = new ArrayList<>();
        monsterLoot.add(loot);

        // Player deals 5 damage, so monster starts with 5 health.
        monster = new Monster("Zombie", 5, 10, 5, monsterLoot);

        CombatResult result = combatEngine.attack(player, quest, monster);

        assertTrue(result.isMonsterDefeated());
        assertTrue(room.findItem("Health Potion").isPresent());
        assertTrue(result.getDroppedItems().contains(loot));
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

    // Inventory cannot exceed its capacity
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5})
    void inventoryCannotExceedCapacity(int capacity) {
        Inventory inventory = new Inventory(capacity);

        for (int i = 0; i < capacity; i++) {
            assertTrue(inventory.addItem(
                    new Potion("Potion " + i, "Test potion.", 10)));
        }

        assertTrue(inventory.isFull());

        boolean added = inventory.addItem(
                new Potion("Extra Potion", "Additional potion.", 10));

        assertFalse(added);
        assertEquals(capacity, inventory.getItems().size());
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