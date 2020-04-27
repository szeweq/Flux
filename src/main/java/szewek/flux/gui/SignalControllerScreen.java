package szewek.flux.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import szewek.flux.container.SignalControllerContainer;

import static szewek.flux.FluxMod.MODID;

@OnlyIn(Dist.CLIENT)
public class SignalControllerScreen extends ContainerScreen<SignalControllerContainer> implements Button.IPressable {
	private static final ResourceLocation BG_TEX = new ResourceLocation(MODID, "textures/gui/signal_controller.png");
	private TextFieldWidget numberInput;
	private Button modeBtn;
	private final IContainerListener listener = new IContainerListener() {
		@Override
		public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {}

		@Override
		public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {}

		@Override
		public void sendWindowProperty(Container containerIn, int id, int v) {
			if (id == 0 && modeBtn != null) {
				modeBtn.setMessage(I18n.format("gui.flux.signal_controller.mode" + v));
			} else if (id == 1 && numberInput != null) {
				numberInput.setText(Integer.toString(v));
			}
		}
	};

	public SignalControllerScreen(SignalControllerContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);

	}

	@Override
	protected void init() {
		super.init();
		container.addListener(listener);
		int i = (xSize - 100) / 2;
		modeBtn = new Button(guiLeft + i, guiTop + 44, 100, 20, I18n.format("gui.flux.signal_controller.mode" + container.getMode()), this);
		numberInput = new TextFieldWidget(font, i, 28, 100, 14, I18n.format("gui.flux.type_channel"));
		numberInput.setMaxStringLength(3);
		addButton(modeBtn);
		children.add(numberInput);
		setFocused(numberInput);
		numberInput.setText(Integer.toString(container.getChannel()));
		numberInput.setCanLoseFocus(false);
		numberInput.setFocused2(true);
		numberInput.setValidator(this::validText);
	}

	@Override
	public void tick() {
		if (numberInput != null) {
			numberInput.tick();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		renderBackground(0);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		assert minecraft != null;
		minecraft.getTextureManager().bindTexture(BG_TEX);
		blit(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = title.getFormattedText();
		font.drawString(s, (float)((xSize - font.getStringWidth(s)) / 2), 5.0F, 0x404040);
		s = I18n.format("gui.flux.type_channel");
		font.drawString(s, (float)((xSize - font.getStringWidth(s)) / 2), 16.0F, 0x404040);
		float z = getBlitOffset();
		numberInput.render(mouseX, mouseY, z);

		font.drawString(playerInventory.getDisplayName().getFormattedText(), 8.0F, ySize - 96 + 2, 0x404040);
	}

	@Override
	public boolean charTyped(char c, int k) {
		boolean b = super.charTyped(c, k);
		if (getFocused() == numberInput) {
			int st;
			String txt = numberInput.getText();
			try {
				st = Short.parseShort(txt);
				if (st < 0 || st > 255) {
					st = container.getChannel();
				}
				container.setChannel(st);
			} catch (NumberFormatException ignored) {
				st = container.getChannel();
			}
			numberInput.setText(Integer.toString(st));
		}
		return b;
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public void onPress(Button btn) {
		int cm = container.cycleMode();
		btn.setMessage(I18n.format("gui.flux.signal_controller.mode" + cm));
	}

	private boolean validText(String txt) {
		if ("".equals(txt)) {
			numberInput.setText("0");
			return false;
		}
		try {
			int st = Integer.parseInt(txt);
			if (st < 0 || st > 255) {
				return false;
			}
			container.setChannel(st);
		} catch (NumberFormatException ignored) {
			return false;
		}
		return true;
	}
}
