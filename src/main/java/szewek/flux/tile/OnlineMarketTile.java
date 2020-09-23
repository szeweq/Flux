package szewek.flux.tile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import szewek.flux.F;
import szewek.flux.FluxCfg;
import szewek.flux.container.OnlineMarketContainer;
import szewek.flux.util.market.OnlineMarketMerchantOffers;

import javax.annotation.Nullable;

public final class OnlineMarketTile extends PoweredTile implements IMerchant, INamedContainerProvider {
	private AxisAlignedBB scanAABB;
	private PlayerEntity customer;
	private final MerchantOffers offers = new OnlineMarketMerchantOffers();
	private int countdown;

	public OnlineMarketTile() {
		super(F.T.ONLINE_MARKET, FluxCfg.ENERGY.onlineMarket);
	}

	//MANUAL OVERRIDE
	public World func_190670_t_() {
		return world;
	}

	private void updateBox() {
		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();
		scanAABB = new AxisAlignedBB(x - 32, y - 32, z - 32, x + 32, y + 32, z + 32);
	}

	@Override
	public void setWorldAndPos(World world, BlockPos pos) {
		super.setWorldAndPos(world, pos);
		updateBox();
	}

	@Override
	public void setPos(BlockPos posIn) {
		super.setPos(posIn);
		updateBox();
	}

	@Override
	public Container createMenu(int id, PlayerInventory pi, PlayerEntity player) {
		setCustomer(pi.player);
		return new OnlineMarketContainer(id, pi, this);
	}

	@Override
	public void setCustomer(@Nullable PlayerEntity customer) {
		this.customer = customer;
	}

	@Nullable
	@Override
	public PlayerEntity getCustomer() {
		return customer;
	}

	@Override
	public MerchantOffers getOffers() {
		return offers;
	}

	private void updateOffers() {
		offers.clear();
		for (VillagerEntity v : world.getEntitiesWithinAABB(EntityType.VILLAGER, scanAABB, EntityPredicates.NOT_SPECTATING)) {
			MerchantOffers vmo = v.getOffers();
			for (MerchantOffer offer : vmo) {
				ItemStack first = offer.getBuyingStackFirst();
				ItemStack second = offer.getBuyingStackSecond();
				ItemStack result = offer.getSellingStack();
				if (offers.stream().noneMatch(xmo -> xmo.getBuyingStackFirst().equals(first, false)
						&& xmo.getBuyingStackSecond().equals(second, false)
						&& xmo.getSellingStack().equals(result, false)
				)) {
					offers.add(offer);
				}
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void setClientSideOffers(@Nullable MerchantOffers offers) {

	}

	@Override
	public void onTrade(MerchantOffer offer) {
		offer.increaseUses();
	}

	@Override
	public void verifySellingItem(ItemStack stack) {

	}

	@Override
	public int getXp() {
		return 0;
	}

	@Override
	public void setXP(int xpIn) {

	}

	@Override
	public boolean func_213705_dZ() {
		return false;
	}

	@Override
	public SoundEvent getYesSound() {
		return SoundEvents.ENTITY_VILLAGER_YES;
	}

	@Override
	public boolean func_223340_ej() {
		return true;
	}

	@Override
	public void tick() {
		if (countdown == 0) {
			countdown = 5;
			final int usage = energyUse.get();
			if (energy.use(usage)) {
				updateOffers();
			} else {
				offers.clear();
			}
		} else {
			--countdown;
		}
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("container.flux.online_market");
	}
}
