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
import szewek.flux.util.market.MarketUtil;
import szewek.flux.util.market.OnlineMarketMerchantOffers;

import javax.annotation.Nullable;
import java.util.Collections;

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
		return level;
	}

	private void updateBox() {
		final int x = worldPosition.getX();
		final int y = worldPosition.getY();
		final int z = worldPosition.getZ();
		scanAABB = new AxisAlignedBB(x - 32, y - 32, z - 32, x + 32, y + 32, z + 32);
	}

	@Override
	public void setLevelAndPosition(World world, BlockPos pos) {
		super.setLevelAndPosition(world, pos);
		updateBox();
	}

	@Override
	public void setPosition(BlockPos posIn) {
		super.setPosition(posIn);
		updateBox();
	}

	@Override
	public Container createMenu(int id, PlayerInventory pi, PlayerEntity player) {
		setTradingPlayer(pi.player);
		return new OnlineMarketContainer(id, pi, this);
	}

	@Override
	public void setTradingPlayer(@Nullable PlayerEntity customer) {
		this.customer = customer;
	}

	@Nullable
	@Override
	public PlayerEntity getTradingPlayer() {
		return customer;
	}

	@Override
	public MerchantOffers getOffers() {
		return offers;
	}

	private void updateOffers() {
		offers.clear();
		for (VillagerEntity v : level.getEntities(EntityType.VILLAGER, scanAABB, EntityPredicates.NO_SPECTATORS)) {
			MerchantOffers vmo = v.getOffers();
			for (MerchantOffer offer : vmo) {
				if (offers.stream().noneMatch(o -> MarketUtil.areSameOffers(o, offer))) {
					int index = Collections.binarySearch(offers, offer, MarketUtil::compareOffers);
					if (index < 0) {
						offers.add(-index - 1, offer);
					}
				}
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void overrideOffers(@Nullable MerchantOffers offers) {

	}

	@Override
	public void notifyTrade(MerchantOffer offer) {
		offer.increaseUses();
	}

	@Override
	public void notifyTradeUpdated(ItemStack stack) {

	}

	@Override
	public int getVillagerXp() {
		return 0;
	}

	@Override
	public void overrideXp(int xpIn) {

	}

	@Override
	public boolean showProgressBar() {
		return false;
	}

	@Override
	public SoundEvent getNotifyTradeSound() {
		return SoundEvents.VILLAGER_YES;
	}

	@Override
	public boolean canRestock() {
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
