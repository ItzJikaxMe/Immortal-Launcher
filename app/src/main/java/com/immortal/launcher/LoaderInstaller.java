package com.immortal.launcher;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

/**
 * Installs Fabric/Quilt version profiles and their Java library artifacts.
 * Native game libraries and the Android game runtime are handled by the engine layer.
 */
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
            throw new IllegalArgumentException(loader+" needs its official installer/runtime workflow.");
        progress("Checking "+loader+" builds for Minecraft "+gameVersion+"…");
        String base="Fabric".equals(loader)
            ? "https://meta.fabricmc.net/v2/versions/loader/"
            : "https://meta.quiltmc.org/v3/versions/loader/";
        JSONArray builds=new JSONArray(get(base+java.net.URLEncoder.encode(gameVersion,"UTF-8")));
        if(builds.length()==0) throw new IOException(loader+" has no compatible builds for "+gameVersion);
        JSONObject selected=null;
        for(int i=0;i<builds.length();i++) {
            JSONObject candidate=builds.getJSONObject(i);
            JSONObject info=candidate.optJSONObject("loader");
            if(info==null) continue;
            if(requestedLoaderVersion!=null && requestedLoaderVersion.equals(info.optString("version"))) {
                selected=candidate; break;
            }
            if(requestedLoaderVersion==null && info.optBoolean("stable",false) && selected==null) selected=candidate;
        }
        if(selected==null && requestedLoaderVersion==null) selected=builds.getJSONObject(0);
        if(selected==null) throw new IOException("Requested "+loader+" build is not available for "+gameVersion);
        String loaderVersion=selected.getJSONObject("loader").getString("version");
        String profileUrl=base+java.net.URLEncoder.encode(gameVersion,"UTF-8")+"/"
            +java.net.URLEncoder.encode(loaderVersion,"UTF-8")+"/profile/json";
        progress("Downloading "+loader+" profile "+loaderVersion+"…");
        JSONObject profile=new JSONObject(get(profileUrl));
        String profileId=gameVersion+"-"+loader.toLowerCase(java.util.Locale.ROOT)+"-"+loaderVersion;
        profile.put("id",profileId);
        profile.put("inheritsFrom",gameVersion);
        profile.put("type","release");

        File root=context.getExternalFilesDir(null);
        if(root==null) root=context.getFilesDir();
        File minecraft=new File(root,"minecraft");
        File versions=new File(minecraft,"versions");
        File libraries=new File(minecraft,"libraries");
        ensureDir(versions); ensureDir(libraries);
        installLibraries(profile.optJSONArray("libraries"),libraries);

        File dir=new File(versions,profileId);
        ensureDir(dir);
        File json=new File(dir,profileId+".json");
        try(FileOutputStream out=new FileOutputStream(json)) {
            out.write(profile.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            out.getFD().sync();
        }
        progress(loader+" profile and library dependencies installed: "+profileId);
        return profileId;
    }

    private void installLibraries(JSONArray items,File libraryRoot)throws Exception {
        if(items==null)return;
        for(int i=0;i<items.length();i++) {
            JSONObject lib=items.getJSONObject(i);
            if(!allowedOnAndroid(lib.optJSONArray("rules")))continue;
            JSONObject downloads=lib.optJSONObject("downloads");
            JSONObject artifact=downloads==null?null:downloads.optJSONObject("artifact");
            String path=artifact==null?"":artifact.optString("path","");
            String address=artifact==null?"":artifact.optString("url","");
            String sha=artifact==null?"":artifact.optString("sha1","");
            if(path.isEmpty()) {
                String coordinate=lib.optString("name","");
                String repository=lib.optString("url","https://libraries.minecraft.net/");
                path=mavenPath(coordinate);
                if(path.isEmpty())continue;
                if(!repository.endsWith("/"))repository+="/";
                address=repository+path;
            }
            if(address.isEmpty())continue;
            File target=new File(libraryRoot,path);
            if(!target.getCanonicalPath().startsWith(libraryRoot.getCanonicalPath()+File.separator))
                throw new IOException("Invalid library path");
            downloadVerified(address,target,sha);
        }
    }

    private String mavenPath(String coordinate) {
        String[] p=coordinate.split(":");
        if(p.length<3)return "";
        StringBuilder path=new StringBuilder(p[0].replace('.','/')).append('/')
            .append(p[1]).append('/').append(p[2]).append('/')
            .append(p[1]).append('-').append(p[2]);
        if(p.length>=4)path.append('-').append(p[3]);
        path.append(".jar");
        return path.toString();
    }

    private boolean allowedOnAndroid(JSONArray rules)throws Exception {
        if(rules==null||rules.length()==0)return true;
        boolean allowed=false;
        for(int i=0;i<rules.length();i++) {
            JSONObject rule=rules.getJSONObject(i);
            JSONObject os=rule.optJSONObject("os");
            boolean match=true;
            if(os!=null) {
                String name=os.optString("name","");
                if(!name.isEmpty()&&!name.equals("linux"))match=false;
                String arch=os.optString("arch","");
                String device=System.getProperty("os.arch","");
                if(!arch.isEmpty()&&!arch.equals(device)&&!(arch.equals("aarch64")&&device.equals("arm64")))match=false;
            }
            if(match)allowed="allow".equals(rule.optString("action","disallow"));
        }
        return allowed;
    }

    private void downloadVerified(String address,File target,String expectedSha1)throws Exception {
        if(target.exists()&&(expectedSha1.isEmpty()||expectedSha1.equalsIgnoreCase(sha1(target))))return;
        File parent=target.getParentFile();
        ensureDir(parent);
        File temp=new File(parent,target.getName()+".part");
        progress("Downloading loader library: "+target.getName());
        HttpURLConnection c=(HttpURLConnection)new URL(address).openConnection();
        c.setConnectTimeout(20000);c.setReadTimeout(90000);
        c.setRequestProperty("User-Agent","ImmortalLauncher/0.1.0");
        try(InputStream in=c.getInputStream();FileOutputStream out=new FileOutputStream(temp)) {
            byte[] b=new byte[32768];int n;
            while((n=in.read(b))!=-1)out.write(b,0,n);
            out.getFD().sync();
        } finally {c.disconnect();}
        if(!expectedSha1.isEmpty()&&!expectedSha1.equalsIgnoreCase(sha1(temp))) {
            temp.delete();throw new IOException("SHA-1 verification failed for "+target.getName());
        }
        if(target.exists()&&!target.delete())throw new IOException("Cannot replace "+target.getName());
        if(!temp.renameTo(target)) {
            try(InputStream in=new FileInputStream(temp);OutputStream out=new FileOutputStream(target)) {
                byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1)out.write(b,0,n);
            }
            temp.delete();
        }
    }

    private String sha1(File file)throws Exception {
        MessageDigest d=MessageDigest.getInstance("SHA-1");
        try(InputStream in=new FileInputStream(file)) {
            byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1)d.update(b,0,n);
        }
        StringBuilder s=new StringBuilder();
        for(byte b:d.digest())s.append(String.format(java.util.Locale.ROOT,"%02x",b&255));
        return s.toString();
    }

    private String get(String address)throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(address).openConnection();
        c.setConnectTimeout(20000);c.setReadTimeout(60000);
        c.setRequestProperty("User-Agent","ImmortalLauncher/0.1.0");
        try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1)out.write(b,0,n);
            return out.toString("UTF-8");
        } finally {c.disconnect();}
    }
    private void ensureDir(File dir)throws IOException {
        if(!dir.exists()&&!dir.mkdirs())throw new IOException("Cannot create "+dir.getAbsolutePath());
    }
    private void progress(String message){if(listener!=null)listener.onProgress(message);}
}
