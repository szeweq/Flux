package szewek.flux.gui

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.ScreenManager.IScreenFactory
import net.minecraft.client.gui.recipebook.AbstractRecipeBookGui
import net.minecraft.client.gui.recipebook.IRecipeShownListener
import net.minecraft.client.gui.recipebook.RecipeBookGui
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.client.gui.widget.button.Button
import net.minecraft.client.gui.widget.button.Button.IPressable
import net.minecraft.client.gui.widget.button.ImageButton
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.container.ClickType
import net.minecraft.inventory.container.Slot
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import szewek.flux.container.AbstractMachineContainer

@OnlyIn(Dist.CLIENT)
class MachineScreen<T : AbstractMachineContainer>(
        screenContainer: T,
        filterName: String,
        inv: PlayerInventory,
        titleIn: ITextComponent,
        private val guiTexture: ResourceLocation
) : ContainerScreen<T>(screenContainer, inv, titleIn), IRecipeShownListener {
    private val recipeGui: AbstractRecipeBookGui = MachineRecipeGui(screenContainer.recipeType, filterName)
    private var recipeBookShown = false
    public override fun init() {
        super.init()
        recipeBookShown = width < 379
        recipeGui.init(width, height, minecraft!!, recipeBookShown, container)
        guiLeft = recipeGui.updateScreenPosition(recipeBookShown, width, xSize)
        addButton(ImageButton(guiLeft + 20, height / 2 - 49, 20, 18, 0, 0, 19, recipeTex, IPressable { button: Button ->
            recipeGui.initSearchBar(recipeBookShown)
            recipeGui.toggleVisibility()
            guiLeft = recipeGui.updateScreenPosition(recipeBookShown, width, xSize)
            (button as ImageButton).setPosition(guiLeft + 20, height / 2 - 49)
        }))
    }

    override fun tick() {
        super.tick()
        recipeGui.tick()
    }

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        renderBackground()
        if (recipeGui.isVisible && recipeBookShown) {
            drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY)
            recipeGui.render(mouseX, mouseY, partialTicks)
        } else {
            recipeGui.render(mouseX, mouseY, partialTicks)
            super.render(mouseX, mouseY, partialTicks)
            recipeGui.renderGhostRecipe(guiLeft, guiTop, true, partialTicks)
        }
        renderHoveredToolTip(mouseX, mouseY)
        recipeGui.renderTooltip(guiLeft, guiTop, mouseX, mouseY)
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        val s = title.formattedText
        font.drawString(s, (xSize / 2 - font.getStringWidth(s) / 2).toFloat(), 6.0f, 0x404040)
        font.drawString(playerInventory.displayName.formattedText, 8.0f, (ySize - 96 + 2).toFloat(), 0x404040)
        val mx = mouseX - guiLeft
        val my = mouseY - guiTop
        if (mx in 151..168 && my in 16..69) renderTooltip(listOf(container!!.energyText()), mx, my)
    }

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        minecraft!!.getTextureManager().bindTexture(guiTexture)
        val i = guiLeft
        val j = guiTop
        this.blit(i, j, 0, 0, xSize, ySize)
        var n = container!!.energyScaled()
        if (n > 0) {
            this.blit(i + 152, j + 71 - n, 176, 71 - n, 16, n - 1)
        }
        n = container.processScaled()
        if (n > 0) {
            this.blit(i + 79, j + 34, 176, 0, n + 1, 16)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        return if (recipeGui.mouseClicked(mouseX, mouseY, mouseButton)) {
            true
        } else recipeBookShown && recipeGui.isVisible || super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun handleMouseClick(slotIn: Slot, slotId: Int, mouseButton: Int, type: ClickType) {
        super.handleMouseClick(slotIn, slotId, mouseButton, type)
        recipeGui.slotClicked(slotIn)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return !recipeGui.keyPressed(keyCode, scanCode, modifiers) && super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun hasClickedOutside(mouseX: Double, mouseY: Double, guiLeftIn: Int, guiTopIn: Int, mouseButton: Int): Boolean {
        val flag = mouseX < guiLeftIn.toDouble() || mouseY < guiTopIn.toDouble() || mouseX >= (guiLeftIn + xSize).toDouble() || mouseY >= (guiTopIn + ySize).toDouble()
        return recipeGui.func_195604_a(mouseX, mouseY, guiLeft, guiTop, xSize, ySize, mouseButton) && flag
    }

    override fun charTyped(c: Char, modifiers: Int): Boolean {
        return recipeGui.charTyped(c, modifiers) || super.charTyped(c, modifiers)
    }

    override fun recipesUpdated() {
        recipeGui.recipesUpdated()
    }

    override fun getRecipeGui(): RecipeBookGui = recipeGui

    override fun removed() {
        recipeGui.removed()
        super.removed()
    }

    companion object {
        private val recipeTex = ResourceLocation("textures/gui/recipe_button.png")
        fun <M : AbstractMachineContainer> make(filterName: String, guiName: String): IScreenFactory<M, MachineScreen<M>> {
            val texGui = ResourceLocation("flux", "textures/gui/$guiName.png")
            return IScreenFactory { container: M, inv: PlayerInventory, title: ITextComponent -> MachineScreen(container, filterName, inv, title, texGui) }
        }
    }
}