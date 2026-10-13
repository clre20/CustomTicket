package clre20.customTicket.update;

import clre20.customTicket.CustomTicket;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

/** 透過 GitHub Releases 檢查新版本。 */
public class UpdateChecker {

    public static final String REPO = "clre20/CustomTicket";
    public static final String RELEASES_URL = "https://github.com/" + REPO + "/releases";
    private static final String API_URL = "https://api.github.com/repos/" + REPO + "/releases/latest";

    public record Result(boolean success, boolean newer, String latest, String url, String error) {
    }

    private final CustomTicket plugin;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private volatile Result lastResult;

    public UpdateChecker(CustomTicket plugin) {
        this.plugin = plugin;
    }

    public String currentVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    /** 非同步檢查，回呼在主執行緒執行。 */
    public void check(Consumer<Result> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Result result = doCheck();
            lastResult = result;
            if (callback != null && plugin.isEnabled()) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(result));
            }
        });
    }

    private Result doCheck() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "CustomTicket-UpdateChecker")
                    .header("Accept", "application/vnd.github+json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                return new Result(false, false, null, RELEASES_URL, "尚未發布任何版本");
            }
            if (response.statusCode() != 200) {
                return new Result(false, false, null, RELEASES_URL, "HTTP " + response.statusCode());
            }
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            String tag = json.get("tag_name").getAsString();
            String url = json.has("html_url") ? json.get("html_url").getAsString() : RELEASES_URL;
            String latest = normalize(tag);
            return new Result(true, compare(latest, normalize(currentVersion())) > 0, latest, url, null);
        } catch (Exception e) {
            return new Result(false, false, null, RELEASES_URL, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /** 將結果傳送給指定對象。 */
    public void report(CommandSender sender, Result result) {
        var cfg = plugin.getConfigManager();
        if (!result.success()) {
            cfg.send(sender, "update-failed", "%error%", result.error());
        } else if (result.newer()) {
            cfg.send(sender, "update-available", "%latest%", result.latest(), "%version%", currentVersion(), "%url%", result.url());
        } else {
            cfg.send(sender, "update-latest");
        }
    }

    /** 若上次檢查發現新版本，提醒指定對象。 */
    public void notifyIfAvailable(CommandSender sender) {
        Result result = lastResult;
        if (result != null && result.success() && result.newer()) report(sender, result);
    }

    private static String normalize(String version) {
        String v = version.trim();
        if (v.startsWith("v") || v.startsWith("V")) v = v.substring(1);
        int dash = v.indexOf('-');
        return dash >= 0 ? v.substring(0, dash) : v;
    }

    /** 逐段比較數字版本號，a > b 回傳正數。 */
    static int compare(String a, String b) {
        String[] pa = a.split("\\.");
        String[] pb = b.split("\\.");
        for (int i = 0; i < Math.max(pa.length, pb.length); i++) {
            int na = i < pa.length ? parse(pa[i]) : 0;
            int nb = i < pb.length ? parse(pb[i]) : 0;
            if (na != nb) return Integer.compare(na, nb);
        }
        return 0;
    }

    private static int parse(String part) {
        String digits = part.replaceAll("\\D.*$", "");
        if (digits.isEmpty()) return 0;
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
