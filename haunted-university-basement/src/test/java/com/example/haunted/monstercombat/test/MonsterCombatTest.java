package com.example.haunted.monstercombat.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.haunted.engine.CombatEngine;
import com.example.haunted.events.CombatResult;
import com.example.haunted.model.BossMonster;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Player;
import com.example.haunted.model.Quest;
import com.example.haunted.model.QuestStatus;
import com.example.haunted.rules.DamageCalculator;
import com.example.haunted.rules.QuestTracker;

class MonsterCombatTest {
	
	private Player player;
	private Monster monster;
	private BossMonster boss;
	private CombatEngine combatEngine;
	private DamageCalculator damageCalculator;
	private Quest quest;

	@BeforeEach
	void setUp() {
		player = new Player("Bob", 100, 10, 5, new Inventory(10));
		
		monster = new Monster("Zombie", 50, 10, 5, new ArrayList<>());
		
		boss = new BossMonster("Final Exam Phantom", 100, 10, 5, new ArrayList<>(), 3);
		
		damageCalculator = new DamageCalculator();
		
		combatEngine = new CombatEngine(damageCalculator, new QuestTracker());
	}

	// Excessive damage
	@Test
	void monsterCannotHaveNegativeHealth() {
		monster.takeDamage(100);

		assertEquals(0, monster.getHealth()); // monster health should be 0
		assertFalse(monster.isAlive()); // monster dead
	}
	
	@Test
	void monsterDamageCannotBeNegative() {
		monster.takeDamage(-10);
		
		assertEquals(50, monster.getHealth());
	}

	// Normal boss behavior
	@Test
	
	void bossUsesNormalAttackAboveHalfHealth() {
		combatEngine.attack(player, null, boss);

		assertEquals(95, boss.getHealth()); 
		assertEquals(95, player.getHealth()); // player gets counterattacked with 5 damage, 10 normal attack - 5 defense
	}

	// Special attack threshold
	@ParameterizedTest
	@CsvSource({
		"51, 13",
		"50, 13",
		"0, 10" // 0 damage, no bonus
	})
	void bossBecomesEnragedAtHalfHealth(int healthDamage, int expectedAttack) {
		boss.takeDamage(healthDamage);
		
		assertEquals(expectedAttack, boss.getCurrentAttack()); // attack + 3 bonus
	}

	// Integration with damage calculator
	@Test
	void bossDealsIncreasedDamageWhenEnraged() {
		boss.takeDamage(50); // boss starts at 100 health
		
		int damage = damageCalculator.calculateMonsterDamage(boss, player);
		
		assertEquals(8, damage);
	}
	
	// Boss defeat and quest progression
	@Test
	void defeatingFinalBossUpdatesQuest() {
        quest = new Quest("Main Quest", "Recover the Lost Gradebook and defeat the Final Exam Phantom.");
        
        boss.takeDamage(99);

        combatEngine.attack(player, quest, boss);

        assertFalse(boss.isAlive());
        assertTrue(quest.isPhantomDefeated());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
    }
}
