package miku.bai_ze_li.genesis.core.api.equipment;

import dev.xkmc.l2library.serial.config.BaseConfig;
import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;

@SerialClass
public class EquipmentStatsConfig extends BaseConfig {
    @SerialClass.SerialField
    public Integer durability;

    @SerialClass.SerialField
    public Weapon weapon;

    @SerialClass.SerialField
    public Armor armor;

    @SerialClass.SerialField
    public Curio curio;

    @SerialClass.SerialField
    public ArrayList<AttributeEntry> attributes = new ArrayList<>();

    public EquipmentStatsConfig() {
    }

    public static EquipmentStatsConfig fromStats(EquipmentStats stats) {
        EquipmentStatsConfig config = new EquipmentStatsConfig();
        config.durability = stats.durability();
        if (stats.weapon() != null) {
            config.weapon = new Weapon(stats.weapon().attackDamage(), stats.weapon().attackSpeed());
        }
        if (stats.armor() != null) {
            config.armor = new Armor(stats.armor().armor(), stats.armor().armorToughness(), stats.armor().knockbackResistance());
        }
        if (stats.curio() != null) {
            config.curio = new Curio();
        }
        stats.attributes().forEach(attribute -> config.attributes.add(new AttributeEntry(
                attribute.attribute(),
                attribute.amount(),
                EquipmentStats.operationName(attribute.operation())
        )));
        return config;
    }

    public EquipmentStats toStats() {
        Integer durabilityStats = durability == null ? null : Math.max(0, durability);
        EquipmentStats.WeaponStats weaponStats = null;
        EquipmentStats.ArmorStats armorStats = null;
        EquipmentStats.CurioStats curioStats = curio == null ? null : new EquipmentStats.CurioStats();

        if (weapon != null) {
            weaponStats = new EquipmentStats.WeaponStats(weapon.attack_damage, weapon.attack_speed);
        }
        if (armor != null) {
            armorStats = new EquipmentStats.ArmorStats(
                    Math.max(0.0D, armor.armor),
                    Math.max(0.0D, armor.armor_toughness),
                    Math.max(0.0D, armor.knockback_resistance)
            );
        }

        ArrayList<EquipmentStats.AttributeStat> attributeStats = new ArrayList<>();
        for (AttributeEntry entry : attributes) {
            if (entry.attribute == null) {
                continue;
            }
            AttributeModifier.Operation operation = EquipmentStats.parseOperation(entry.operation == null ? "addition" : entry.operation);
            if (operation == null) {
                continue;
            }
            attributeStats.add(new EquipmentStats.AttributeStat(entry.attribute, entry.amount, operation));
        }

        return new EquipmentStats(durabilityStats, weaponStats, armorStats, curioStats, attributeStats);
    }

    @SerialClass
    public static class Weapon {
        @SerialClass.SerialField
        public double attack_damage;

        @SerialClass.SerialField
        public double attack_speed;

        public Weapon() {
        }

        public Weapon(double attackDamage, double attackSpeed) {
            this.attack_damage = attackDamage;
            this.attack_speed = attackSpeed;
        }
    }

    @SerialClass
    public static class Armor {
        @SerialClass.SerialField
        public double armor;

        @SerialClass.SerialField
        public double armor_toughness;

        @SerialClass.SerialField
        public double knockback_resistance;

        public Armor() {
        }

        public Armor(double armor, double armorToughness, double knockbackResistance) {
            this.armor = armor;
            this.armor_toughness = armorToughness;
            this.knockback_resistance = knockbackResistance;
        }
    }

    @SerialClass
    public static class Curio {
        public Curio() {
        }
    }

    @SerialClass
    public static class AttributeEntry {
        @SerialClass.SerialField
        public ResourceLocation attribute;

        @SerialClass.SerialField
        public double amount;

        @SerialClass.SerialField
        public String operation = "addition";

        public AttributeEntry() {
        }

        public AttributeEntry(ResourceLocation attribute, double amount, String operation) {
            this.attribute = attribute;
            this.amount = amount;
            this.operation = operation;
        }
    }
}
