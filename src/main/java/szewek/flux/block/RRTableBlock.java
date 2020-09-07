package szewek.flux.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeBook;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import szewek.flux.F;

import javax.annotation.Nullable;
import java.util.Collections;

public class RRTableBlock extends Block {
	public RRTableBlock() {
		super(Properties.create(Material.IRON).hardnessAndResistance(1.5F).sound(SoundType.METAL));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return F.T.RR_TABLE.create();
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!worldIn.isRemote && player instanceof ServerPlayerEntity) {
			final ServerPlayerEntity sp = (ServerPlayerEntity) player;
			final RecipeBook rb = sp.getRecipeBook();
			for (IRecipe<?> recipe : worldIn.getRecipeManager().getRecipes()) {
				if (!rb.isUnlocked(recipe)) {
					player.unlockRecipes(Collections.singleton(recipe));
					break;
				}
			}
		}
		return ActionResultType.SUCCESS;
	}
}
