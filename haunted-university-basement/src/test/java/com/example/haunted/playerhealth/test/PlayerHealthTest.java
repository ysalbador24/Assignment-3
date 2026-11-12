package com.example.haunted.playerhealth.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.haunted.model.Inventory;
import com.example.haunted.model.Player;
import com.example.haunted.model.Weapon;
import com.example.haunted.model.Armor;

class PlayerHealthTest {

	private Player player;
	
	@BeforeEach
	void setUp() {
		player = new Player("Bob", 100, 10, 5, new Inventory(10));
	}
	
	@ParameterizedTest
	@CsvSource({
		"1, 99",
		"25, 75",
		"100, 0",
		"150, 0"
	})
    void playerTakesDamage(int damage, int expectedHealth) {
        player.takeDamage(damage);

        assertEquals(expectedHealth, player.getHealth()); // player takes damage should equal expectedHealth
    }

    @Test
    void playerCannotHaveNegativeHealth() {
        player.takeDamage(150);

        assertEquals(0, player.getHealth()); // health should be 0
        assertFalse(player.isAlive()); // player should be dead
    }

    @ParameterizedTest
    @CsvSource({
    	"10, 70",
    	"40, 100",
    	"80, 100"
    })
    void playerCanHealWithoutExceedingMaximum(int healing, int expectedHealth) {
        player.takeDamage(40);

        player.heal(healing); // heals for 10, 40, 80

        assertEquals(expectedHealth, player.getHealth());
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
    
    @Test
    void equippingWeaponIncreasesAttackPower() {
    	Weapon weapon = new Weapon("Sword", "A sharp sword.", 15);
    	
    	assertEquals(10, player.getAttackPower());
    	
    	player.equipWeapon(weapon);
    	
    	assertEquals(25, player.getAttackPower());
    }
    
    @Test
    void equippingArmorIncreasesDefensePower() {
    	Armor armor = new Armor("Shield", "A sturdy shield.", 10);
    	
    	assertEquals(5, player.getDefensePower());
    	
    	player.equipArmor(armor);
    	
    	assertEquals(15, player.getDefensePower());
    }

}
