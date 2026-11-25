package com.immortal.launcher;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Creates official Fabric/Quilt version profiles on top of an installed Mojang version. */
public final class LoaderInstaller {
    public interface Listener { void onProgress(String message); }
    private final Context context;
    private final Listener listener;

    public LoaderInstaller(Context context, Listener listener) {
        this.context=context.getApplicationContext();
        this.listener=listener;
    }

    public String install(String gameVersion, String loader) throws Exception {
        return install(gameVersion,loader,null);
    }

    public String install(String gameVersion, String loader, String requestedLoaderVersion) throws Exception {
        if (!"Fabric".equals(loader) && !"Quilt".equals(loader))
            throw new IllegalArgumentException(loader+" currently needs its official installer/runtime workflow and is not marked installed.");
        progress("Checking "+loader+" builds for Minecraft "+gameVersion+"…");
        String base="Fabric".equals(loader)
            ? "https://meta.fabricmc.net/v2/versions/loader/"
            : "https://meta.quiltmc.org/v3/versions/loader/";
        String response=get(base+java.net.URLEncoder.encode(gameVersion,"UTF-8"));
        JSONArray builds=new JSONArray(response);
        if(builds.length()==0) throw new java.io.IOException(loader+" has no compatible builds for "+gameVersion);
        JSONObject selected=null;
        for(int i=0;i<builds.length();i++) {
            JSONObject candidate=builds.getJSONObject(i);
            JSONObject loaderInfo=candidate.optJSONObject("loader");
            if(loaderInfo==null) continue;
            if(requestedLoaderVersion!=null && requestedLoaderVersion.equals(loaderInfo.optString("version"))) {
                selected=candidate; break;
            }
            if(requestedLoaderVersion==null && loaderInfo.optBoolean("stable",false) && selected==null) selected=candidate;
        }
        if(selected==null && requestedLoaderVersion==null) selected=builds.getJSONObject(0);
        if(selected==null) throw new java.io.IOException("Requested "+loader+" build "+requestedLoaderVersion+" is not available for Minecraft "+gameVersion);
        JSONObject loaderInfo=selected.getJSONObject("loader");
        String loaderVersion=loaderInfo.getString("version");
        String profileUrl=base+java.net.URLEncoder.encode(gameVersion,"UTF-8")+"/"
            +java.net.URLEncoder.encode(loaderVersion,"UTF-8")+"/profile/json";
        progress("Downloading "+loader+" profile "+loaderVersion+"…");
        JSONObject profile=new JSONObject(get(profileUrl));
        profile.put("id",gameVersion+"-"+loader.toLowerCase(java.util.Locale.ROOT)+"-"+loaderVersion);
        profile.put("inheritsFrom",gameVersion);
        profile.put("type","release");
        File root=context.getExternalFilesDir(null);
        if(root==null) root=context.getFilesDir();
        File dir=new File(new File(root,"minecraft/versions"),profile.getString("id"));
        if(!dir.exists() && !dir.mkdirs()) throw new java.io.IOException("Cannot create loader profile directory");
        File json=new File(dir,profile.getString("id")+".json");
        try(FileOutputStream out=new FileOutputStream(json)) {
            out.write(profile.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        progress(loader+" profile created: "+profile.getString("id"));
        return profile.getString("id");
    }

    private String get(String address) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(address).openConnection();
        c.setConnectTimeout(20000); c.setReadTimeout(60000);
        c.setRequestProperty("User-Agent","ImmortalLauncher/0.1.0");
        try(java.io.InputStream in=c.getInputStream();java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()) {
            byte[] b=new byte[16384]; int n;
            while((n=in.read(b))!=-1) out.write(b,0,n);
            return out.toString("UTF-8");
        } finally { c.disconnect(); }
    }
    private void progress(String message) { if(listener!=null) listener.onProgress(message); }
}
