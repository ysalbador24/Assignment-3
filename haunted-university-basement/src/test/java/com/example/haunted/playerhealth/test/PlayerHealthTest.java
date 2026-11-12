package com.example.haunted.playerhealth.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.haunted.model.Inventory;
import com.example.haunted.model.Player;
import com.example.haunted.model.Potion;

class PlayerHealthTest {

	private Player player;
	
	@BeforeEach
	void setUp() {
		player = new Player("Bob", 100, 10, 5, new Inventory(10));
	}
	
	@Test
    void playerTakesDamage() {
        player.takeDamage(10);

        assertEquals(90, player.getHealth()); // player takes 10 dmg, 90 health
    }

    @Test
    void playerCannotHaveNegativeHealth() {
        player.takeDamage(150);

        assertEquals(0, player.getHealth()); // health should be 0
        assertFalse(player.isAlive()); // player should be dead
    }

    @Test
    void playerCanHealWithoutExceedingMaximum() {
        player.takeDamage(40);

        player.heal(100);

        assertEquals(100, player.getHealth());
    }

    @Test
    void negativeDamageDoesNotReduceHealth() {
        player.takeDamage(-10);

        assertEquals(100, player.getHealth());
    }

    @Test
    void negativeHealingDoesNotIncreaseHealth() {
        player.takeDamage(20);
        player.heal(-10);

        assertEquals(80, player.getHealth());
    }
    
    @Test
    void zeroDamageDoesNotChangeHealth() {
    	player.takeDamage(0);
    	
    	assertEquals(100, player.getHealth());
    	
    	player.takeDamage(-0);
    	
    	assertEquals(100, player.getHealth());
    }
    
    // Potion healing
    @Test
    void potionRestoresPlayerHealth() {
    	Potion potion = new Potion("Healing Potion", "A healing potion.", 10);
    	
    	player.takeDamage(30);
    	
    	potion.use(player); // heals 10 health
    	
    	assertEquals(80, player.getHealth());
    }


}
