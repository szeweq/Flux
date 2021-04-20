package szewek.flux.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import szewek.flux.F;
import szewek.flux.network.FluxPackets;

public class SignalControllerContainer extends Container {
	private final boolean remote;
	private final IIntArray data;

	public SignalControllerContainer(int id, PlayerInventory pinv, PacketBuffer data) {
		this(id, pinv, new IntArray(2));
	}

	public SignalControllerContainer(int id, PlayerInventory pinv, IIntArray extra) {
		super(F.C.SIGNAL_CONTROLLER, id);

		remote = pinv.player.level.isClientSide;
		data = extra;

		int xBase;
		int yBase = 84;
		for (int y = 0; y < 3; y++) {
			xBase = 8;
			for (int x = 0; x < 9; x++) {
				addSlot(new Slot(pinv, x + 9 * y + 9, xBase, yBase));
				xBase += 18;
			}
			yBase += 18;
		}
		xBase = 8;
		yBase += 4;
		for (int w = 0; w < 9; w++) {
			addSlot(new Slot(pinv, w, xBase, yBase));
			xBase += 18;
		}
		addDataSlots(extra);
	}

	@Override
	public boolean stillValid(PlayerEntity playerIn) {
		return true;
	}

	@Override
	public void setData(int id, int data) {
		super.setData(id, data);
		if (remote) {
			broadcastChanges();
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void sendDataToServer(int id, int v) {
		int ov = data.get(id);
		if (ov != v) {
			data.set(id, v);
			FluxPackets.updateData2Server(getType(), containerId, id, v);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public int getMode() {
		return data.get(0);
	}

	@OnlyIn(Dist.CLIENT)
	public int getChannel() {
		return (short) data.get(1);
	}

	@OnlyIn(Dist.CLIENT)
	public int cycleMode() {
		int i = (getMode() + 1) % 4;
		sendDataToServer(0, i);
		return i;
	}

	@OnlyIn(Dist.CLIENT)
	public void setChannel(int ch) {
		sendDataToServer(1, ch);
	}
}
