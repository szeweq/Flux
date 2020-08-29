package szewek.flux.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.function.Supplier;

import static szewek.flux.Flux.MODID;

public class FluxPackets {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(MODID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	public static void init() {
		CHANNEL.messageBuilder(UpdateData.class, 0, NetworkDirection.PLAY_TO_SERVER)
				.decoder(UpdateData::decode)
				.encoder(UpdateData::encode)
				.consumer(UpdateData::handle)
				.add();
	}

	public static void updateData2Server(ContainerType<?> ctype, int window, int id, int val) {
		CHANNEL.sendToServer(UpdateData.of(ctype, window, id, val));
	}

	private FluxPackets() {}

	public static class UpdateData {
		static final ForgeRegistry<ContainerType<?>> reg = (ForgeRegistry<ContainerType<?>>) ForgeRegistries.CONTAINERS;

		private final int ctype, window, id, value;


		UpdateData(int ctype, int window, int id, int value) {
			this.ctype = ctype;
			this.window = window;
			this.id = id;
			this.value = value;
		}

		public static UpdateData of(ContainerType<?> ctype, int window, int id, int val) {
			final int cid = reg.getID(ctype);
			return new UpdateData(cid, window, id, val);
		}

		public final ContainerType<?> getType() {
			//noinspection deprecation
			return Registry.MENU.getByValue(ctype);
		}

		public static void encode(UpdateData msg, PacketBuffer buf) {
			buf.writeVarInt(msg.ctype);
			buf.writeVarInt(msg.window);
			buf.writeVarInt(msg.id);
			buf.writeVarInt(msg.value);
		}

		public static UpdateData decode(PacketBuffer buf) {
			return new UpdateData(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
		}

		public static void handle(UpdateData msg, Supplier<NetworkEvent.Context> ctx) {
			final ServerPlayerEntity player = ctx.get().getSender();
			if (player != null) {
				ctx.get().enqueueWork(() -> {
					ContainerType<?> ctype = msg.getType();
					Container container = player.openContainer;
					if (container.getType() == ctype && msg.window == container.windowId) {
						container.updateProgressBar(msg.id, msg.value);
					}
				});
			}
			ctx.get().setPacketHandled(true);
		}
	}
}
