package szewek.fl.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public final class FluxAnalytics2 {
    private static final String FORM_TYPE = "application/json";
    private static final String GAME_KEY = "43e1a3fdb580b16cb8e863ceae744f31";
    private static final String SECRET_KEY = "632c81689f84ec970c58e745d88a3f0180de5c00";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final TypeToken<List<Config>> CONFIGS_TYPE_TOKEN = new TypeToken<List<Config>>() {};
    private static final Logger LOGGER = LogManager.getLogger("FluxAnalytics");
    private static final ScheduledExecutorService DEFAULT_EXEC = Executors.newSingleThreadScheduledExecutor();

    private static URL GA_URL;

    static {
        try {
            GA_URL = new URL("https://api.gameanalytics.com/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, List<Config>> configs = Collections.emptyMap();
    private static final LinkedList<JsonObject> eventQueue = new LinkedList<>();

    private static void updateConfigs(List<Config> newConfigs) {
        Map<String, List<Config>> map = new HashMap<>();
        for (Config c : newConfigs) {
            map.computeIfAbsent(c.key, k -> new ArrayList<>()).add(c);
        }
        configs = map;
        LOGGER.info("Updated configs: {}", configs);
    }

    private static HttpURLConnection connect(String path) throws IOException {
        final HttpURLConnection huc = (HttpURLConnection) new URL(GA_URL, path).openConnection();
        huc.setRequestMethod("POST");
        huc.setRequestProperty("Accept", FORM_TYPE);
        huc.setRequestProperty("Content-Type", FORM_TYPE);
        huc.setDoOutput(true);
        return huc;
    }

    private static void handleRequestData(HttpURLConnection huc, byte[] b) throws IOException {
        String hashed = hash(b);
        huc.setRequestProperty("Authorization", hashed);
        OutputStream out = huc.getOutputStream();
        out.write(b);
    }

    private static String hash(byte[] data) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            byte[] encoded = SECRET_KEY.getBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(encoded, "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(data));
        }
        catch(Exception ex)
        {
            System.out.println("Error generating Hmac: " + ex);
            return "";
        }
    }

    private static JsonObject initData(String playerId) {
        JsonObject o = new JsonObject();
        String platform = Util.getPlatform().toString().toLowerCase();
        if (platform.equals("osx")) {
            platform = "mac_osx";
        }
        o.addProperty("platform", platform);
        o.addProperty("os_version", platform + ' ' + System.getProperty("os.version", "unknown"));
        o.addProperty("sdk_version", "rest api v2");
        o.addProperty("user_id", playerId.isEmpty() ? "SERVER " + SESSION_ID : playerId);
        return o;
    }

    private static JsonObject defaultData(String category, String playerId) {
        JsonObject o = initData(playerId);
        String vmName = System.getProperty("java.vm.name");
        o.addProperty("device", (vmName == null ? "Java VM" : vmName) + " " + (NetCommon.VM_VERSION == null ? System.getProperty("java.version") : NetCommon.VM_VERSION));
        o.addProperty("manufacturer", NetCommon.VM_VENDOR);
        if (!NetCommon.flVersion.isEmpty()) {
            o.addProperty("build", NetCommon.flVersion);
        }
        o.addProperty("session_id", SESSION_ID);
        o.addProperty("session_num", 1);
        o.addProperty("v", 2);
        o.addProperty("client_ts",System.currentTimeMillis() / 1000L);
        o.addProperty("custom_01", System.getProperty("user.language", "en"));
        o.addProperty("category", category);
        return o;
    }

    private static void requestInitConfig() throws IOException {
        JsonObject data = initData(NetCommon.playerId);
        final HttpURLConnection huc = connect("/remote_configs/v1/init?game_key=" + GAME_KEY);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bout);
        GSON.toJson(data, osw);
        osw.close();

        handleRequestData(huc, bout.toByteArray());
        final InputStream response = NetCommon.handleResponse(huc);

        data = GSON.fromJson(new InputStreamReader(response), JsonObject.class);
        List<Config> configs = GSON.fromJson(data.getAsJsonArray("configs"), CONFIGS_TYPE_TOKEN.getType());
        updateConfigs(configs);
    }

    private static void sendEvents() {
        if (eventQueue.isEmpty()) {
            return;
        }
        final HttpURLConnection huc;
        try {
            huc = connect("/v2/" + GAME_KEY + "/events");
            JsonArray ja = new JsonArray();
            JsonObject data;
            while ((data = eventQueue.poll()) != null) {
                ja.add(data);
            }

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bout);
            OutputStreamWriter osw = new OutputStreamWriter(gzip);
            GSON.toJson(ja, osw);
            osw.close();
            huc.setRequestProperty("Content-Encoding", "gzip");

            handleRequestData(huc, bout.toByteArray());
            NetCommon.handleResponse(huc);
        } catch (IOException e) {
            LOGGER.warn("Exception thrown during event send!", e);
        }
    }

    public static void init() {
        try {
            requestInitConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DEFAULT_EXEC.scheduleWithFixedDelay(FluxAnalytics2::sendEvents, 5, 20, TimeUnit.SECONDS);
    }

    public static void userEvent() {
        eventQueue.add(defaultData("user", NetCommon.playerId));
    }

    public static void designEvent(String playerId, String name) {
        JsonObject data = defaultData("design", playerId);
        data.addProperty("event_id", name);
        eventQueue.add(data);
    }

    //public static void initServer() { }

    static class Config {
        private String key;
        private String value;
        private long start_ts;
        private long end_ts;

        @Override
        public String toString() {
            return "Config{key='" + key + "', value='" + value + "', start=" + start_ts + ", end=" + end_ts + '}';
        }
    }
}
