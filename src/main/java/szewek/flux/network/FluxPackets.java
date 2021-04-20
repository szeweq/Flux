package szewek.flux.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import szewek.fl.network.FluxAnalytics;

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
		CHANNEL.messageBuilder(GiftReceived.class, 1, NetworkDirection.PLAY_TO_CLIENT)
				.decoder(GiftReceived::decode)
				.encoder(GiftReceived::encode)
				.consumer(GiftReceived::handle)
				.add();
	}

	public static void updateData2Server(ContainerType<?> ctype, int window, int id, int val) {
		CHANNEL.sendToServer(UpdateData.of(ctype, window, id, val));
	}

	public static void sendGiftReceived(final ServerPlayerEntity player) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), GiftReceived.INSTANCE);
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

		public final ContainerType<?> getType() {
			//noinspection deprecation
			return Registry.MENU.byId(ctype);
		}

		public static UpdateData of(ContainerType<?> ctype, int window, int id, int val) {
			final int cid = reg.getID(ctype);
			return new UpdateData(cid, window, id, val);
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

		public static void handle(UpdateData msg, Supplier<NetworkEvent.Context> fn) {
			final NetworkEvent.Context ctx = fn.get();
			final ServerPlayerEntity player = ctx.getSender();
			if (player != null) {
				ctx.enqueueWork(() -> {
					ContainerType<?> ctype = msg.getType();
					Container container = player.containerMenu;
					if (container.getType() == ctype && msg.window == container.containerId) {
						container.setData(msg.id, msg.value);
					}
				});
			}
			ctx.setPacketHandled(true);
		}
	}

	public enum GiftReceived {
		INSTANCE;

		private static final ResourceLocation DING = new ResourceLocation("entity.player.levelup");

		public static void encode(GiftReceived gr, PacketBuffer buf) {}

		public static GiftReceived decode(PacketBuffer buf) {
			return INSTANCE;
		}

		public static void handle(GiftReceived gr, Supplier<NetworkEvent.Context> fn) {
			final NetworkEvent.Context ctx = fn.get();
			if (ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
				ctx.enqueueWork(() -> Minecraft.getInstance().player.playSound(new SoundEvent(DING), 0.5F, 1));
			}
			FluxAnalytics.putView("flux/gift");
			ctx.setPacketHandled(true);
		}
	}
}
