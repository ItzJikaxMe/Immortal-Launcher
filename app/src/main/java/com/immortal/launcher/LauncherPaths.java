package com.immortal.launcher;

import android.content.Context;
import java.io.File;
import java.io.IOException;

/** The engine and Immortal installers share the same Minecraft and instance folders. */
public final class LauncherPaths {
    private LauncherPaths() {}

    public static File appDataRoot(Context context) {
        File root = context.getExternalFilesDir(null);
        return root != null ? root : context.getFilesDir();
    }

    public static File minecraftDirectory(Context context) {
        return new File(appDataRoot(context), ".minecraft");
    }

    public static File versionsDirectory(Context context) {
        return new File(minecraftDirectory(context), "versions");
    }

    public static File versionDirectory(Context context, String versionId) {
        return new File(versionsDirectory(context), versionId);
    }

    /** Mojo engine's shared-data instance uses this exact directory for mods and game data. */
    public static File sharedDirectory(Context context) {
        return new File(appDataRoot(context), "shared_dir");
    }

    public static File sharedFolder(Context context, String folderName) {
        File folder = new File(sharedDirectory(context), folderName);
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    public static void migrateLegacyMinecraftDirectory(Context context) throws IOException {
        File legacy = new File(appDataRoot(context), "minecraft");
        File current = minecraftDirectory(context);
        if (legacy.isDirectory() && !current.exists()) {
            if (!legacy.renameTo(current)) copyDirectory(legacy, current);
        }
        // Migrate legacy per-version user content into the shared game directory used by the engine.
        File oldVersions = new File(legacy, "versions");
        File shared = sharedDirectory(context);
        File[] profiles = oldVersions.listFiles();
        if (profiles != null) {
            String[] gameFolders = {"mods", "resourcepacks", "shaderpacks", "screenshots", "saves"};
            for (File profile : profiles) {
                if (!profile.isDirectory()) continue;
                for (String folder : gameFolders) {
                    File from = new File(profile, folder);
                    File to = new File(shared, folder);
                    if (from.isDirectory() && !to.exists()) copyDirectory(from, to);
                }
            }
        }
    }

    private static void copyDirectory(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists() && !target.mkdirs()) throw new IOException("Cannot create " + target);
            File[] children = source.listFiles();
            if (children == null) throw new IOException("Cannot read " + source);
            for (File child : children) copyDirectory(child, new File(target, child.getName()));
        } else {
            java.nio.file.Files.copy(source.toPath(), target.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
