package com.immortal.launcher;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Installs client-side Modrinth .mrpack files into Immortal's shared game directory.
 * Only client files are installed; server-only files and server overrides are ignored.
 */
public final class ModrinthModpackInstaller {
    public interface Listener { void onProgress(String message); }

    private final Context context;
    private final Listener listener;

    public ModrinthModpackInstaller(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    public Result install(File archive) throws Exception {
        if (archive == null || !archive.isFile()) throw new IOException("Modpack archive was not found.");
        JSONObject manifest;
        try (ZipFile zip = new ZipFile(archive)) {
            ZipEntry entry = zip.getEntry("modrinth.index.json");
            if (entry == null) throw new IOException("This is not a valid Modrinth .mrpack archive.");
            try (InputStream in = zip.getInputStream(entry)) {
                manifest = new JSONObject(readText(in));
            }
            if (manifest.optInt("formatVersion", 0) < 1) throw new IOException("Unsupported Modrinth pack format.");

            JSONObject dependencies = manifest.optJSONObject("dependencies");
            if (dependencies == null) throw new IOException("The modpack manifest has no dependencies.");
            String game = dependencies.optString("minecraft", "");
            if (game.isEmpty()) throw new IOException("The modpack does not specify a Minecraft version.");

            String loader = "Vanilla";
            String loaderVersion = null;
            if (dependencies.has("fabric")) {
                loader = "Fabric";
                loaderVersion = dependencies.optString("fabric", "");
            } else if (dependencies.has("quilt")) {
                loader = "Quilt";
                loaderVersion = dependencies.optString("quilt", "");
            } else if (dependencies.has("forge") || dependencies.has("neoforge")) {
                throw new IOException("This modpack requires Forge/NeoForge. Its installer adapter is not enabled yet; no partial pack was installed.");
            }

            progress("Installing Minecraft " + game + " for modpack…");
            new MinecraftInstaller(context, this::progress).install(game);
            String installedId = game;
            if (!"Vanilla".equals(loader)) {
                progress("Installing " + loader + " " + loaderVersion + "…");
                installedId = new LoaderInstaller(context, this::progress).install(game, loader,
                        loaderVersion.isEmpty() ? null : loaderVersion);
            }

            File shared = LauncherPaths.sharedDirectory(context);
            if (!shared.exists() && !shared.mkdirs()) throw new IOException("Cannot create shared game directory.");
            installOverrides(zip, manifest.optString("overrides", "overrides"), shared);
            installClientFiles(manifest.optJSONArray("files"), shared);
            progress("Modpack installed: " + manifest.optString("name", archive.getName()));
            return new Result(game, installedId, loader, loaderVersion, manifest.optString("name", archive.getName()));
        }
    }

    private void installOverrides(ZipFile zip, String overrideFolder, File root) throws Exception {
        if (overrideFolder == null || overrideFolder.trim().isEmpty()) return;
        String prefix = overrideFolder.replace('\\\\', '/');
        while (prefix.startsWith("/")) prefix = prefix.substring(1);
        if (!prefix.endsWith("/")) prefix += "/";
        int count = 0;
        java.util.Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName().replace('\\\\', '/');
            if (!name.startsWith(prefix) || entry.isDirectory()) continue;
            String relative = name.substring(prefix.length());
            if (relative.isEmpty()) continue;
            File target = safeTarget(root, relative);
            ensureDir(target.getParentFile());
            try (InputStream in = zip.getInputStream(entry); FileOutputStream out = new FileOutputStream(target)) {
                copy(in, out);
                out.getFD().sync();
            }
            progress("Installing modpack overrides: " + (++count));
        }
        // Some launchers publish client-overrides alongside the standard overrides folder.
        String clientPrefix = "client-overrides/";
        entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName().replace('\\\\', '/');
            if (!name.startsWith(clientPrefix) || entry.isDirectory()) continue;
            File target = safeTarget(root, name.substring(clientPrefix.length()));
            ensureDir(target.getParentFile());
            try (InputStream in = zip.getInputStream(entry); FileOutputStream out = new FileOutputStream(target)) {
                copy(in, out);
                out.getFD().sync();
            }
        }
    }

    private void installClientFiles(JSONArray files, File root) throws Exception {
        if (files == null) return;
        int total = files.length();
        int completed = 0;
        for (int i = 0; i < total; i++) {
            JSONObject item = files.getJSONObject(i);
            JSONObject env = item.optJSONObject("env");
            String client = env == null ? "required" : env.optString("client", "required");
            if ("unsupported".equalsIgnoreCase(client)) continue;
            String path = item.optString("path", "");
            if (path.isEmpty()) continue;
            File target = safeTarget(root, path);
            if (item.optBoolean("fileSizePresent", false) && target.isFile()
                    && target.length() == item.optLong("fileSize", -1)) {
                completed++;
                continue;
            }
            JSONArray urls = item.optJSONArray("downloads");
            if (urls == null || urls.length() == 0) throw new IOException("No download URL for " + path);
            JSONObject hashes = item.optJSONObject("hashes");
            ensureDir(target.getParentFile());
            File temp = new File(target.getParentFile(), target.getName() + ".part");
            IOException failure = null;
            boolean downloaded = false;
            for (int u = 0; u < urls.length(); u++) {
                String address = urls.optString(u, "");
                if (address.isEmpty()) continue;
                try {
                    download(address, temp);
                    verify(temp, hashes);
                    if (target.exists() && !target.delete()) throw new IOException("Cannot replace " + path);
                    if (!temp.renameTo(target)) throw new IOException("Cannot finalize " + path);
                    downloaded = true;
                    break;
                } catch (IOException error) {
                    failure = error;
                    temp.delete();
                }
            }
            if (!downloaded) throw new IOException("Could not install " + path, failure);
            completed++;
            progress("Modpack files: " + completed + "/" + total);
        }
    }

    private void verify(File file, JSONObject hashes) throws Exception {
        if (hashes == null) return;
        String sha1 = hashes.optString("sha1", "");
        String sha512 = hashes.optString("sha512", "");
        if (!sha1.isEmpty() && !sha1.equalsIgnoreCase(digest(file, "SHA-1")))
            throw new IOException("SHA-1 mismatch: " + file.getName());
        if (!sha512.isEmpty() && !sha512.equalsIgnoreCase(digest(file, "SHA-512")))
            throw new IOException("SHA-512 mismatch: " + file.getName());
    }

    private void download(String address, File target) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(120000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "ImmortalLauncher/0.2.0");
        try {
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) throw new IOException("Download HTTP " + status);
            try (InputStream in = connection.getInputStream(); FileOutputStream out = new FileOutputStream(target)) {
                copy(in, out);
                out.getFD().sync();
            }
        } finally {
            connection.disconnect();
        }
    }

    private File safeTarget(File root, String relative) throws IOException {
        String normalized = relative.replace('\\\\', '/');
        if (normalized.startsWith("/") || normalized.matches("^[A-Za-z]:.*"))
            throw new IOException("Unsafe modpack path: " + relative);
        File target = new File(root, normalized);
        String base = root.getCanonicalPath() + File.separator;
        if (!target.getCanonicalPath().startsWith(base)) throw new IOException("Unsafe modpack path: " + relative);
        return target;
    }

    private String digest(File file, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[32768];
            int count;
            while ((count = in.read(buffer)) != -1) digest.update(buffer, 0, count);
        }
        StringBuilder result = new StringBuilder();
        for (byte value : digest.digest()) result.append(String.format(Locale.ROOT, "%02x", value & 255));
        return result.toString();
    }

    private String readText(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toString("UTF-8");
    }

    private void copy(InputStream in, java.io.OutputStream out) throws IOException {
        byte[] buffer = new byte[32768];
        int count;
        while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
    }

    private void ensureDir(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Cannot create " + dir);
    }

    private void progress(String message) {
        if (listener != null) listener.onProgress(message);
    }

    public static final class Result {
        public final String gameVersion;
        public final String installedVersionId;
        public final String loader;
        public final String loaderVersion;
        public final String name;

        Result(String gameVersion, String installedVersionId, String loader, String loaderVersion, String name) {
            this.gameVersion = gameVersion;
            this.installedVersionId = installedVersionId;
            this.loader = loader;
            this.loaderVersion = loaderVersion;
            this.name = name;
        }
    }
}
