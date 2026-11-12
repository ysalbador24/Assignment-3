
package com.example.haunted.potion.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.haunted.model.Inventory;
import com.example.haunted.model.Player;
import com.example.haunted.model.Potion;

class PotionTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Bob", 100, 10, 5, new Inventory(10));
    }

    @Test
    void potionStoresCorrectHealingAmount() {
        Potion potion = new Potion("Healing Potion", "Restores health.", 20);

        assertEquals(20, potion.getHealingAmount());
    }

    @Test
    void potionRestoresPlayerHealth() {
        Potion potion = new Potion("Healing Potion", "Restores health.", 10);

        player.takeDamage(30);
        potion.use(player);

        assertEquals(80, player.getHealth());
    }

    @Test
    void potionCannotHealBeyondMaximumHealth() {
        Potion potion = new Potion("Healing Potion", "Restores health.", 20);

        player.takeDamage(10);
        potion.use(player);

        assertEquals(100, player.getHealth());
    }

    @Test
    void potionUsedAtFullHealthDoesNotIncreaseHealth() {
        Potion potion = new Potion("Healing Potion", "Restores health.", 20);

        potion.use(player);

        assertEquals(100, player.getHealth());
    }

    @Test
    void zeroHealingPotionDoesNotChangePlayerHealth() {
        Potion potion = new Potion("Weak Potion", "Provides no healing.", 0);

        player.takeDamage(30);
        potion.use(player);

        assertEquals(70, player.getHealth());
    }

    @Test
    void potionOnlyHealsThePlayerItIsUsedOn() {
        Potion potion = new Potion("Healing Potion", "Restores health.", 20);

        Player secondPlayer = new Player(
            "Jerry", 100, 10, 5, new Inventory(10)
        );

        player.takeDamage(30);
        secondPlayer.takeDamage(50);

        potion.use(player);

        assertEquals(90, player.getHealth());
        assertEquals(50, secondPlayer.getHealth());
    }
}
