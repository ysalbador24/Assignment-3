package com.example.haunted.monstercombat.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
	private CombatEngine combatEngine;
	private Quest quest;

	@BeforeEach
	void setUp() {
		player = new Player("Bob", 100, 10, 5, new Inventory(10));
		monster = new Monster("Zombie", 50, 10, 5, new ArrayList<>());
		combatEngine = new CombatEngine(new DamageCalculator(), new QuestTracker());
		quest = new Quest("Main Quest", "Recover the Lost Gradebook and defeat the Final Exam Phantom.");
	}

	// Damage calculation
	@Test
	void playerAttackDamagesMonsterCorrectly() {
		combatEngine.attack(player, null, monster); // player attacks monster with 10 attack power

		assertEquals(45, monster.getHealth()); // 10 attack - 5 defense = 45 health
		assertEquals(95, player.getHealth()); // counterattack with 5 damage, 10 attack - 5 defense
	}

	// Excessive damage
	@Test
	void monsterCannotHaveNegativeHealth() {
		monster.takeDamage(100);

		assertEquals(0, monster.getHealth()); // monster health should be 0
	}

	// Normal boss behavior
	@Test
	void bossUsesNormalAttackAboveHalfHealth() {
		BossMonster bossMonster = new BossMonster("Final Exam Phantom", 100, 20, 10, new ArrayList<>(), 3);

		combatEngine.attack(player, quest, bossMonster);

		assertEquals(99, bossMonster.getHealth()); // bossMonster has taken the minimum of 1 damage
		assertEquals(85, player.getHealth()); // player gets counterattacked with 15 damage
	}

	// Special attack threshold
	@Test
	void bossBecomesEnragedAtHalfHealth() {
		BossMonster bossMonster = new BossMonster("Final Exam Phantom", 100, 20, 10, new ArrayList<>(), 3);
		
		bossMonster.takeDamage(50);
		
		assertEquals(23, bossMonster.getCurrentAttack()); // attack 20 + 3 bonus = 23 
	}

	// Integration with damage calculator
	@Test
	void bossDealsIncreasedDamageWhenEnraged() {
		BossMonster bossMonster = new BossMonster("Final Exam Phantom", 100, 20, 10, new ArrayList<>(), 3);
		
		bossMonster.takeDamage(50);
		
		CombatResult result = combatEngine.attack(player, quest, bossMonster);
		
		assertEquals(18 ,result.getDamageToPlayer()); // player should take 18 damage
		assertEquals(82, player.getHealth());
		assertEquals(49, bossMonster.getHealth()); // player deals minimum of 1 damage with 10 attack power and monsters 10 defense
	}
	// Boss defeat and quest progression
	@Test
	void defeatingFinalBossUpdatesQuest() {

        BossMonster bossMonster = new BossMonster("Final Exam Phantom", 100, 10, 5, new ArrayList<>(), 3);

        bossMonster.takeDamage(99);

        combatEngine.attack(player, quest, bossMonster);

        assertFalse(bossMonster.isAlive());
        assertTrue(quest.isPhantomDefeated());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
    }
}
