package szewek.flux.util;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class FluxItemTier implements IItemTier {
	private final int harvestLevel;
	private final int maxUses;
	private final float efficiency;
	private final float attackDamage;
	private final int enchantability;
	public final Tag<Item> repairMaterialTag;

	public FluxItemTier(int harvestLevel, int maxUses, float efficiency, float attackDamage, int enchantability, String tagName) {
		this(harvestLevel, maxUses, efficiency, attackDamage, enchantability, new ItemTags.Wrapper(new ResourceLocation("forge", tagName)));
	}

	public FluxItemTier(int harvestLevel, int maxUses, float efficiency, float attackDamage, int enchantability, Tag<Item> repairMaterialTag) {
		this.harvestLevel = harvestLevel;
		this.maxUses = maxUses;
		this.efficiency = efficiency;
		this.attackDamage = attackDamage;
		this.enchantability = enchantability;
		this.repairMaterialTag = repairMaterialTag;
	}


	@Override
	public int getMaxUses() {
		return maxUses;
	}

	@Override
	public float getEfficiency() {
		return efficiency;
	}

	@Override
	public float getAttackDamage() {
		return attackDamage;
	}

	@Override
	public int getHarvestLevel() {
		return harvestLevel;
	}

	@Override
	public int getEnchantability() {
		return enchantability;
	}

	@Override
	public Ingredient getRepairMaterial() {
		return Ingredient.fromTag(repairMaterialTag);
	}
}
