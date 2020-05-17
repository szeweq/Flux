package szewek.fl.util;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

/**
 * Custom item tier used in Flux mod(s).
 * This uses tags to determine repair materials.
 */
public class FluxItemTier implements IItemTier {
	private final int harvestLevel;
	private final int maxUses;
	private final float efficiency;
	private final float attackDamage;
	private final int enchantability;
	public final Tag<Item> repairMaterialTag;
	public final Item material;

	public FluxItemTier(Builder b) {
		harvestLevel = b.harvestLevel;
		maxUses = b.maxUses;
		efficiency = b.efficiency;
		attackDamage = b.attackDamage;
		enchantability = b.enchantability;
		repairMaterialTag = b.repairMaterialTag;
		material = b.material;
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

	public static class Builder {
		private int harvestLevel;
		private int maxUses;
		private float efficiency;
		private float attackDamage;
		private int enchantability;
		private String tagName;
		private Item material;
		private Tag<Item> repairMaterialTag = new ItemTags.Wrapper(new ResourceLocation("forge", tagName));

		public Builder setHarvestLevel(int harvestLevel) {
			this.harvestLevel = harvestLevel;
			return this;
		}

		public Builder setMaxUses(int maxUses) {
			this.maxUses = maxUses;
			return this;
		}

		public Builder setEfficiency(float efficiency) {
			this.efficiency = efficiency;
			return this;
		}

		public Builder setAttackDamage(float attackDamage) {
			this.attackDamage = attackDamage;
			return this;
		}

		public Builder setEnchantability(int enchantability) {
			this.enchantability = enchantability;
			return this;
		}

		public Builder setTagName(String tagName) {
			this.tagName = tagName;
			return this;
		}

		public Builder setMaterial(Item material) {
			this.material = material;
			return this;
		}

		public Builder setRepairMaterialTag(Tag<Item> repairMaterialTag) {
			this.repairMaterialTag = repairMaterialTag;
			return this;
		}

		public FluxItemTier build() {
			return new FluxItemTier(this);
		}
	}
}
