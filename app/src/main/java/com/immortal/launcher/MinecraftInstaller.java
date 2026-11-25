package com.immortal.launcher;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

/**
 * Downloads official Mojang version metadata, client, Java libraries and assets.
 * This installs game files only; an Android Java runtime and graphics/input bridge
 * are separate requirements for actually launching the game.
 */
public final class MinecraftInstaller {
    public interface Listener { void onProgress(String message); }
    private final Context context;
    private final Listener listener;

    public MinecraftInstaller(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    public void install(String versionId) throws Exception {
        File root = context.getExternalFilesDir(null);
        if (root == null) root = context.getFilesDir();
        File minecraft = new File(root, ".minecraft");
        File versions = new File(minecraft, "versions");
        File libraries = new File(minecraft, "libraries");
        File assets = new File(minecraft, "assets");
        File versionDir = new File(versions, versionId);
        ensureDir(versionDir); ensureDir(libraries); ensureDir(assets);

        progress("Loading Mojang version manifest…");
        JSONObject manifest = new JSONObject(get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"));
        JSONArray versionsList = manifest.getJSONArray("versions");
        String metadataUrl = null;
        for (int i = 0; i < versionsList.length(); i++) {
            JSONObject item = versionsList.getJSONObject(i);
            if (versionId.equals(item.optString("id"))) {
                metadataUrl = item.optString("url", null);
                break;
            }
        }
        if (metadataUrl == null) throw new IOException("Version " + versionId + " was not found in Mojang's manifest.");

        progress("Downloading version metadata…");
        String metadataText = get(metadataUrl);
        JSONObject version = new JSONObject(metadataText);
        write(new File(versionDir, versionId + ".json"), metadataText);

        JSONObject downloads = version.optJSONObject("downloads");
        if (downloads == null || downloads.optJSONObject("client") == null)
            throw new IOException("This legacy version has no standard client download entry.");
        JSONObject client = downloads.getJSONObject("client");
        downloadVerified(client.getString("url"), new File(versionDir, versionId + ".jar"),
                client.optString("sha1", ""), "Downloading Minecraft client…");

        JSONArray libs = version.optJSONArray("libraries");
        int libraryCount = libs == null ? 0 : libs.length();
        int downloadedLibraries = 0;
        for (int i = 0; i < libraryCount; i++) {
            JSONObject lib = libs.getJSONObject(i);
            if (!allowedOnLinux(lib.optJSONArray("rules"))) continue;
            JSONObject libDownloads = lib.optJSONObject("downloads");
            JSONObject artifact = libDownloads == null ? null : libDownloads.optJSONObject("artifact");
            if (artifact == null) continue; // Native classifiers are deliberately not installed here.
            String relative = artifact.optString("path", "");
            String url = artifact.optString("url", "");
            if (relative.isEmpty() || url.isEmpty()) continue;
            File target = new File(libraries, relative);
            if (downloadVerified(url, target, artifact.optString("sha1", ""),
                    "Libraries: " + (++downloadedLibraries) + "/" + libraryCount)) { }
        }

        JSONObject assetIndex = version.optJSONObject("assetIndex");
        if (assetIndex != null) {
            String indexId = assetIndex.optString("id", version.optString("assets", "legacy"));
            File indexes = new File(assets, "indexes");
            ensureDir(indexes);
            File indexFile = new File(indexes, indexId + ".json");
            downloadVerified(assetIndex.getString("url"), indexFile,
                    assetIndex.optString("sha1", ""), "Downloading asset index…");
            JSONObject index = new JSONObject(read(indexFile));
            JSONObject objects = index.optJSONObject("objects");
            if (objects != null) {
                File objectRoot = new File(assets, "objects");
                ensureDir(objectRoot);
                JSONArray names = objects.names();
                int total = names == null ? 0 : names.length();
                for (int i = 0; i < total; i++) {
                    String name = names.getString(i);
                    JSONObject object = objects.getJSONObject(name);
                    String hash = object.getString("hash");
                    if (hash.length() < 2) continue;
                    File target = new File(new File(objectRoot, hash.substring(0, 2)), hash);
                    String url = "https://resources.download.minecraft.net/" + hash.substring(0, 2) + "/" + hash;
                    downloadVerified(url, target, hash,
                            "Assets: " + (i + 1) + "/" + total);
                }
            }
        }
        progress("Minecraft " + versionId + " files downloaded. Java runtime and Android graphics integration are still needed to launch it.");
    }

    private boolean allowedOnLinux(JSONArray rules) throws Exception {
        if (rules == null || rules.length() == 0) return true;
        boolean allowed = false;
        for (int i = 0; i < rules.length(); i++) {
            JSONObject rule = rules.getJSONObject(i);
            JSONObject os = rule.optJSONObject("os");
            boolean matches = true;
            if (os != null) {
                String name = os.optString("name", "");
                if (!name.isEmpty() && !name.equals("linux")) matches = false;
                String arch = os.optString("arch", "");
                if (!arch.isEmpty() && !(arch.equals("aarch64") || arch.equals("arm64")
                        || arch.equals("arm") || arch.equals("x86") || arch.equals("x86_64"))) matches = false;
            }
            if (matches) allowed = "allow".equals(rule.optString("action", "disallow"));
        }
        return allowed;
    }

    private boolean downloadVerified(String address, File target, String expectedSha1, String status) throws Exception {
        if (target.exists() && (expectedSha1.isEmpty() || expectedSha1.equalsIgnoreCase(sha1(target)))) return false;
        File parent = target.getParentFile();
        ensureDir(parent);
        progress(status);
        File temp = new File(parent, target.getName() + ".part");
        HttpURLConnection c = (HttpURLConnection) new URL(address).openConnection();
        c.setConnectTimeout(20000); c.setReadTimeout(90000);
        c.setRequestProperty("User-Agent", "ImmortalLauncher/0.1.0");
        try (InputStream in = c.getInputStream(); FileOutputStream out = new FileOutputStream(temp)) {
            byte[] buffer = new byte[32768]; int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
        } finally { c.disconnect(); }
        if (!expectedSha1.isEmpty() && !expectedSha1.equalsIgnoreCase(sha1(temp))) {
            temp.delete();
            throw new IOException("SHA-1 verification failed for " + target.getName());
        }
        if (target.exists() && !target.delete()) throw new IOException("Cannot replace " + target.getName());
        if (!temp.renameTo(target)) {
            try (InputStream in = new FileInputStream(temp); OutputStream out = new FileOutputStream(target)) {
                byte[] b = new byte[32768]; int n; while ((n = in.read(b)) != -1) out.write(b, 0, n);
            }
            temp.delete();
        }
        return true;
    }

    private String get(String address) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(address).openConnection();
        c.setConnectTimeout(20000); c.setReadTimeout(60000);
        c.setRequestProperty("User-Agent", "ImmortalLauncher/0.1.0");
        try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] b = new byte[16384]; int n; while ((n = in.read(b)) != -1) out.write(b, 0, n);
            return out.toString("UTF-8");
        } finally { c.disconnect(); }
    }

    private String read(File file) throws Exception {
        try (InputStream in = new FileInputStream(file); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] b = new byte[16384]; int n; while ((n = in.read(b)) != -1) out.write(b, 0, n);
            return out.toString("UTF-8");
        }
    }
    private void write(File file, String value) throws Exception {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }
    private String sha1(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream in = new FileInputStream(file)) {
            byte[] b = new byte[32768]; int n; while ((n = in.read(b)) != -1) digest.update(b, 0, n);
        }
        StringBuilder s = new StringBuilder();
        for (byte value : digest.digest()) s.append(String.format(java.util.Locale.ROOT, "%02x", value & 0xff));
        return s.toString();
    }
    private void ensureDir(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Cannot create " + dir.getAbsolutePath());
    }
    private void progress(String value) { if (listener != null) listener.onProgress(value); }
}
