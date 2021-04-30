package szewek.fl.network;

import com.google.common.io.ByteStreams;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class NetCommon {
    static final ScheduledExecutorService DEFAULT_EXEC = Executors.newSingleThreadScheduledExecutor();
    static final String SESSION_ID = UUID.randomUUID().toString();
    static final String VM_VENDOR = System.getProperty("java.vm.vendor");
    static final String VM_VERSION = System.getProperty("java.vm.version");
    static String flVersion = "";
    static String playerId = "";

    private NetCommon() {}

    public static void init() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> NetCommon::getPlayerId);
    }

    public static void updateVersion(String newVersion) {
        flVersion = newVersion;
    }

    public static void getPlayerId() {
        playerId = Minecraft.getInstance().getUser().getUuid();
    }

    static String getIdFrom(PlayerEntity player) {
        return player.getGameProfile().getId().toString();
    }

    static InputStream handleResponse(HttpURLConnection huc) throws IOException {
        int status = huc.getResponseCode();
        if (status / 100 != 2) {
            InputStream err = huc.getErrorStream();
            ByteArrayOutputStream bs = new ByteArrayOutputStream(Math.max(32, err.available()));
            //noinspection UnstableApiUsage
            ByteStreams.copy(err, bs);
            err.close();
            throw new IOException("HTTP " + status + ": " + bs.toString("UTF-8"));
        }
        return huc.getInputStream();
    }

    public static void putEvent(String name1, String name2) {
        FluxAnalytics.putView(playerId, name1);
        FluxAnalytics2.designEvent(playerId, name2);
    }

    public static void putEvent(PlayerEntity player, String name1, String name2) {
        String playerId = getIdFrom(player);
        FluxAnalytics.putView(playerId, name1);
        FluxAnalytics2.designEvent(playerId, name2);
    }

    public static void putAction(PlayerEntity player, String type, String data) {
        putEvent(player, type + '/' + data, type + ':' + data);
    }
}
