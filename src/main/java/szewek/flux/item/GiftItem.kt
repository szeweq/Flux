package szewek.flux.item

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.UseAction
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraftforge.items.ItemHandlerHelper
import szewek.flux.util.gift.Gifts.get
import szewek.ktutils.minusAssign

class GiftItem(properties: Properties) : Item(properties) {
    override fun onItemRightClick(worldIn: World, playerIn: PlayerEntity, handIn: Hand): ActionResult<ItemStack> {
        playerIn.activeHand = handIn
        return ActionResult(ActionResultType.CONSUME, playerIn.getHeldItem(handIn))
    }

    override fun getUseDuration(stack: ItemStack): Int = 30

    override fun getUseAction(stack: ItemStack): UseAction = UseAction.EAT

    override fun onItemUseFinish(stack: ItemStack, worldIn: World, entityLiving: LivingEntity): ItemStack {
        if (!worldIn.isRemote && entityLiving is ServerPlayerEntity && stack.item === this) {
            val tag = stack.tag
            if (tag == null || tag.isEmpty) {
                entityLiving.sendMessage(TranslationTextComponent("flux.gift.invalid"))
                return ItemStack.EMPTY
            }
            val xday = tag.getInt("xDay")
            val gd = get(xday)
            if (gd == null) {
                entityLiving.sendMessage(TranslationTextComponent("flux.gift.invalid"))
                return ItemStack.EMPTY
            }
            val stacks = gd.stacks
            val player = entityLiving as PlayerEntity
            for (giftStack in stacks) {
                ItemHandlerHelper.giveItemToPlayer(player, giftStack, -1)
            }
            player.sendMessage(gd.text)
            stack -= 1
        }
        return stack
    }

}