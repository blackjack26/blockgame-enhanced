package dev.jb0s.blockgameenhanced.config.structure;

@SuppressWarnings("unused")
public enum MMOItemModifiers {
    NONE( "NoTag"),

    // Vanilla stats
    ATTACK_DMG("MMOITEMS_ATTACK_DAMAGE"),
    ATTACK_SPEED("MMOITEMS_ATTACK_SPEED"),
    MAX_HP("MMOITEMS_MAX_HEALTH"),
    HP_REG("MMOITEMS_HEALTH_REGENERATION"),
    MAX_HEALTH_REG("MMOITEMS_MAX_HEALTH_REGENERATION"),
    FOOD_LEVEL("MMOITEMS_FOOD_LEVEL"),
    SAT_LEVEL("MMOITEMS_SATURATION_LEVEL"),

    // Misc
    MOVEMENT_SPEED("MMOITEMS_MOVEMENT_SPEED"),
    SPEED_MALUS_REDTN("MMOITEMS_SPEED_MALUS_REDUCTION"),
    KNOCKBACK_RES("MMOITEMS_KNOCKBACK_RESISTANCE"),

    // Mana
    MAX_MANA("MMOITEMS_MAX_MANA"),
    MANA_REG("MMOITEMS_MANA_REGENERATION"),
    MAX_MANA_REG("MMOITEMS_MAX_MANA_REGENERATION"),

    // Stamina
    MAX_STAMINA("MMOITEMS_MAX_STAMINA"),
    STAMINA_REG("MMOITEMS_STAMINA_REGENERATION"),
    MAX_STAMINA_REG("MMOITEMS_MAX_STAMINA_REGENERATION"),

    // Stellium
    MAX_STELLIUM("MMOITEMS_MAX_STELLIUM"),
    STELLIUM_REG("MMOITEMS_STELLIUM_REGENERATION"),
    MAX_STELLIUM_REG("MMOITEMS_MAX_STELLIUM_REGENERATION"),

    // Vanilla armor stats
    ARMOR("MMOITEMS_ARMOR"),
    ARMOR_TOUGHNESS("MMOITEMS_ARMOR_TOUGHNESS"),

    // Critical strikes
    CRIT_STRIKE_CHANCE("MMOITEMS_CRITICAL_STRIKE_CHANCE"),
    CRIT_STRIKE_POWER("MMOITEMS_CRIT_STRIKE_POWER"),
    SKILL_CRIT_STRIKE_CHANCE("MMOITEMS_SKILL_CRITICAL_STRIKE_CHANCE"),
    SKILL_CRIT_STRIKE_POWER("MMOITEMS_SKILL_CRIT_STRIKE_POWER"),

    // Blunt Weapons
    BLUNT_RATING("MMOITEMS_BLUNT_RATING"),
    BLUNT_POWER("MMOITEMS_BLUNT_POWER"),

    // Mitigation
    DEFENSE("MMOITEMS_DEFENSE"),
    BLOCK_POWER("MMOITEMS_BLOCK_POWER"),
    BLOCK_RATING("MMOITEMS_BLOCK_RATING"),
    BLOCK_CDR("MMOITEMS_BLOCK_COOLDOWN_REDUCTION"),
    DODGE_RATING("MMOITEMS_DODGE_RATING"),
    DODGE_CDR("MMOITEMS_DODGE_COOLDOWN_REDUCTION"),
    PARRY_RATING("MMOITEMS_PARRY_RATING"),
    PARRY_CDR("MMOITEMS_PARRY_COOLDOWN_REDUCTION"),

    // Utility
    ADD_EXP("MMOITEMS_ADDITIONAL_EXPERIENCE"),
    ADD_EXP_MINING("MMOITEMS_ADDITIONAL_EXPERIENCE_MINING"),
    ADD_EXP_LOGGING("MMOITEMS_ADDITIONAL_EXPERIENCE_LOGGING"),
    ADD_EXP_ARCHAEOLOGY("MMOITEMS_ADDITIONAL_EXPERIENCE_ARCHAEOLOGY"),
    ADD_EXP_FISHING("MMOITEMS_ADDITIONAL_EXPERIENCE_FISHING"),
    ADD_EXP_HERBALISM("MMOITEMS_ADDITIONAL_EXPERIENCE_HERBALISM"),
    ADD_EXP_RUNECARVING("MMOITEMS_ADDITIONAL_EXPERIENCE_RUNECARVING"),
    CDR("MMOITEMS_COOLDOWN_REDUCTION"),

    // Damage-type based stats
    MAGIC_DMG("MMOITEMS_MAGIC_DAMAGE"),
    THAUMATURGY_DMG("MMOITEMS_THAUMATURGY_DAMAGE"),
    PHYSICAL_DMG("MMOITEMS_PHYSICAL_DAMAGE"),
    PROJECTILE_DMG("MMOITEMS_PROJECTILE_DAMAGE"),
    WEAPON_DMG("MMOITEMS_WEAPON_DAMAGE"),
    ALL_DMG("MMOITEMS_ALL_DAMAGE"),
    SKILL_DMG("MMOITEMS_SKILL_DAMAGE"),
    UNARMED_DMG("MMOITEMS_UNARMED_DAMAGE"),
    UNDEAD_DMG("MMOITEMS_UNDEAD_DAMAGE"),

    // Misc damage stats
    PVP_DMG("MMOITEMS_PVP_DAMAGE"),
    PVE_DMG("MMOITEMS_PVE_DAMAGE"),
    AOE_SIZE("MMOITEMS_AOE_SIZE_AMPLIFIER"),

    // Damage reduction stats
    DMG_REDTN("MMOITEMS_DAMAGE_REDUCTION"),
    PVE_DMG_REDTN("MMOITEMS_PVE_DAMAGE_REDUCTION"),
    PVP_DMG_REDTN("MMOITEMS_PVP_DAMAGE_REDUCTION"),
    FALL_DMG_REDTN("MMOITEMS_FALL_DAMAGE_REDUCTION"),
    MAGIC_DMG_REDTN("MMOITEMS_MAGIC_DAMAGE_REDUCTION"),
    PHYSICAL_DMG_REDTN("MMOITEMS_PHYSICAL_DAMAGE_REDUCTION"),
    PROJECTILE_DMG_REDTN("MMOITEMS_PROJECTILE_DAMAGE_REDUCTION"),
    WEAPON_DMG_REDTN("MMOITEMS_WEAPON_DAMAGE_REDUCTION"),
    SKILL_DMG_REDTN("MMOITEMS_SKILL_DAMAGE_REDUCTION"),

    // Profession drop bonus modifiers
    SKILL_MINING("MMOITEMS_SKILL_MINING"),
    SKILL_LOGGING("MMOITEMS_SKILL_LOGGING"),
    SKILL_ARCHAEOLOGY("MMOITEMS_SKILL_SKILL_ARCHAEOLOGY"),
    SKILL_FISHING("MMOITEMS_SKILL_SKILL_FISHING"),
    SKILL_HERBALISM("MMOITEMS_SKILL_SKILL_HERBALISM"),

    // Chance of double fishing loot
    CRIT_FISHING_CHANCE("MMOITEMS_CRIT_FISHING_CHANCE");

    private final String tag;

    MMOItemModifiers(String tag) {
        this.tag = tag;
    }

    public String tag() {
        return this.tag;
    }

}
