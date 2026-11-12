package com.example.haunted.inventory.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.haunted.model.Inventory;
import com.example.haunted.model.Potion;

class InventoryTest {

    private Inventory inventory;
    private Potion potion1;
    private Potion potion2;
    private Potion potion3;

    @BeforeEach
    void setUp() {
        inventory = new Inventory(2);

        potion1 = new Potion("Health Potion", "Restores health.", 20);
        potion2 = new Potion("Mana Potion", "Restores mana.", 15);
        potion3 = new Potion("Strength Potion", "Increases strength.", 10);
    }

    @Test
    void newInventoryIsEmpty() {
        assertTrue(inventory.getItems().isEmpty());
        assertFalse(inventory.isFull());
    }

    @Test
    void inventoryStoresItsCapacity() {
        assertEquals(2, inventory.getCapacity());
    }

    @Test
    void itemCanBeAddedToInventory() {
        assertTrue(inventory.addItem(potion1));

        assertEquals(1, inventory.getItems().size());
        assertTrue(inventory.contains("Health Potion"));
        assertSame(potion1, inventory.getItems().get(0)); // item 1 in inventory is the same as the newly added item
    }

    @Test
    void inventoryBecomesFullWhenCapacityIsReached() {
        inventory.addItem(potion1);

        assertFalse(inventory.isFull());

        inventory.addItem(potion2);

        assertTrue(inventory.isFull());
    }

    @Test
    void cannotAddItemWhenInventoryIsFull() {
        inventory.addItem(potion1);
        inventory.addItem(potion2);

        assertFalse(inventory.addItem(potion3));

        assertEquals(2, inventory.getItems().size());
        assertFalse(inventory.contains("Strength Potion"));
    }

    @Test
    void canFindItemByName() {
        inventory.addItem(potion1);

        assertTrue(inventory.findItem("Health Potion").isPresent());
        assertEquals(potion1, inventory.findItem("Health Potion").get());
    }

    @Test
    void findingItemIsCaseInsensitive() {
        inventory.addItem(potion1);

        assertTrue(inventory.findItem("health potion").isPresent());
        assertTrue(inventory.contains("HEALTH POTION"));
    }

    @Test
    void findingNonexistentItemReturnsEmptyOptional() {
        assertTrue(inventory.findItem("Missing Potion").isEmpty());
        assertFalse(inventory.contains("Missing Potion"));
    }

    @Test
    void canRemoveItemFromInventory() {
        inventory.addItem(potion1);

        Potion removedPotion = (Potion) inventory.removeItem("Health Potion");

        assertEquals(potion1, removedPotion);
        assertFalse(inventory.contains("Health Potion"));
        assertEquals(0, inventory.getItems().size());
    }

    @Test
    void removingNonexistentItemReturnsNull() {
        assertNull(inventory.removeItem("Missing Potion"));
        assertTrue(inventory.getItems().isEmpty());
    }

    @Test
    void removingItemMakesRoomForAnotherItem() {
        inventory.addItem(potion1);
        inventory.addItem(potion2);

        inventory.removeItem("Health Potion");

        assertFalse(inventory.isFull());
        assertTrue(inventory.addItem(potion3));
        assertEquals(2, inventory.getItems().size());
    }

    @Test
    void returnedItemListCannotBeModified() {
        assertThrows(
            UnsupportedOperationException.class,
            () -> inventory.getItems().add(potion1)
        );
    }
}
