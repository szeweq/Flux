package szewek.flux.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public final class FluxToolItem extends Item {
	public FluxToolItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext itemUse) {
		World w = itemUse.getWorld();
		PlayerEntity p = itemUse.getPlayer();
		if (!w.isRemote() && p != null) {
			TileEntity tile = w.getTileEntity(itemUse.getPos());
			if (tile != null) {
				LazyOptional<IEnergyStorage> lazy = tile.getCapability(CapabilityEnergy.ENERGY);
				if (lazy.isPresent()) {
					IEnergyStorage ie = lazy.orElse(null);
					p.sendMessage(new TranslationTextComponent("flux.energystat", ie.getEnergyStored(), ie.getMaxEnergyStored()));
				}
			}
		}

		return ActionResultType.PASS;
	}
}
