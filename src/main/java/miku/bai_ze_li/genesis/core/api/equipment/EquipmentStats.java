package miku.bai_ze_li.genesis.core.api.equipment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;
import java.util.Locale;

public record EquipmentStats(
        Integer durability,
        WeaponStats weapon,
        ArmorStats armor,
        CurioStats curio,
        List<AttributeStat> attributes
) {
    public EquipmentStats {
        attributes = List.copyOf(attributes);
    }

    public static EquipmentStats weapon(int durability, double attackDamage, double attackSpeed) {
        return new EquipmentStats(durability, new WeaponStats(attackDamage, attackSpeed), null, null, List.of());
    }

    public static EquipmentStats armor(int durability, double armor, double armorToughness, double knockbackResistance, List<AttributeStat> attributes) {
        return new EquipmentStats(durability, null, new ArmorStats(armor, armorToughness, knockbackResistance), null, attributes);
    }

    public static EquipmentStats durability(int durability) {
        return new EquipmentStats(durability, null, null, null, List.of());
    }

    public static EquipmentStats curio(List<AttributeStat> attributes) {
        return new EquipmentStats(null, null, null, new CurioStats(), attributes);
    }

    public static String operationName(AttributeModifier.Operation operation) {
        return switch (operation) {
            case ADDITION -> "addition";
            case MULTIPLY_BASE -> "multiply_base";
            case MULTIPLY_TOTAL -> "multiply_total";
        };
    }

    public static AttributeModifier.Operation parseOperation(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "0", "add", "addition" -> AttributeModifier.Operation.ADDITION;
            case "1", "multiply", "multiply_base" -> AttributeModifier.Operation.MULTIPLY_BASE;
            case "2", "multiply_total" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> null;
        };
    }

    public record WeaponStats(double attackDamage, double attackSpeed) {
    }

    public record ArmorStats(double armor, double armorToughness, double knockbackResistance) {
    }

    public record CurioStats() {
    }

    public record AttributeStat(ResourceLocation attribute, double amount, AttributeModifier.Operation operation) {
    }
}
