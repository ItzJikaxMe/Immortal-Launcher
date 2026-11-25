package com.immortal.launcher;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;

/** Read-only Modrinth discovery plus verified project file downloads. */
public final class ModrinthClient {
    private final Context context;
    public ModrinthClient(Context context){this.context=context.getApplicationContext();}

    public JSONArray search(String projectType,String query,String gameVersion,String loader) throws Exception {
        JSONArray facets=new JSONArray();
        JSONArray type=new JSONArray(); type.put("project_type:"+projectType); facets.put(type);
        if(gameVersion!=null && !gameVersion.isEmpty()) {
            JSONArray version=new JSONArray(); version.put("versions:"+gameVersion); facets.put(version);
        }
        if("mod".equals(projectType) && loader!=null && !loader.isEmpty() && !"Vanilla".equals(loader)) {
            JSONArray loaderFacet=new JSONArray(); loaderFacet.put("categories:"+loader.toLowerCase(java.util.Locale.ROOT)); facets.put(loaderFacet);
        }
        String url="https://api.modrinth.com/v2/search?query="+enc(query)+"&facets="+enc(facets.toString())+"&limit=20";
        return new JSONObject(get(url)).getJSONArray("hits");
    }

    public JSONObject versions(String projectId,String gameVersion,String loader) throws Exception {
        String url="https://api.modrinth.com/v2/project/"+enc(projectId)+"/version?game_versions="+enc(new JSONArray().put(gameVersion).toString());
        if(loader!=null && !loader.isEmpty() && !"Vanilla".equals(loader)) {
            url+="&loaders="+enc(new JSONArray().put(loader.toLowerCase(java.util.Locale.ROOT)).toString());
        }
        JSONArray versions=new JSONArray(get(url));
        if(versions.length()==0) throw new IOException("No compatible file for Minecraft "+gameVersion+" / "+loader);
        return versions.getJSONObject(0);
    }

    public File downloadPrimary(JSONObject version,String folderName) throws Exception {
        JSONArray files=version.getJSONArray("files");
        JSONObject chosen=null;
        for(int i=0;i<files.length();i++) if(files.getJSONObject(i).optBoolean("primary",false)){chosen=files.getJSONObject(i);break;}
        if(chosen==null && files.length()>0) chosen=files.getJSONObject(0);
        if(chosen==null) throw new IOException("This project version has no downloadable file.");
        String filename=chosen.getString("filename");
        String address=chosen.getString("url");
        File root=context.getExternalFilesDir(null);
        if(root==null)root=context.getFilesDir();
        File dir=new File(root,"minecraft/versions/"+folderName);
        if(!dir.exists()&&!dir.mkdirs())throw new IOException("Cannot create download folder");
        File target=new File(dir,filename);
        File temp=new File(dir,filename+".part");
        HttpURLConnection c=(HttpURLConnection)new URL(address).openConnection();
        c.setConnectTimeout(20000);c.setReadTimeout(120000);c.setRequestProperty("User-Agent","ImmortalLauncher/0.1.0");
        try(InputStream in=c.getInputStream();FileOutputStream out=new FileOutputStream(temp)){
            byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1)out.write(b,0,n);
        }finally{c.disconnect();}
        JSONObject hashes=chosen.optJSONObject("hashes");
        String expected=hashes==null?"":hashes.optString("sha1","");
        if(!expected.isEmpty()&&!expected.equalsIgnoreCase(sha1(temp))){temp.delete();throw new IOException("Downloaded file failed SHA-1 verification.");}
        if(target.exists())target.delete();
        if(!temp.renameTo(target))throw new IOException("Could not save downloaded file.");
        return target;
    }

    private String get(String address)throws Exception{
        HttpURLConnection c=(HttpURLConnection)new URL(address).openConnection();
        c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setRequestProperty("User-Agent","ImmortalLauncher/0.1.0");
        try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toString("UTF-8");
        }finally{c.disconnect();}
    }
    private String sha1(File file)throws Exception{
        MessageDigest d=MessageDigest.getInstance("SHA-1");
        try(InputStream in=new FileInputStream(file)){byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1)d.update(b,0,n);}
        StringBuilder s=new StringBuilder();for(byte b:d.digest())s.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return s.toString();
    }
    private String enc(String value)throws Exception{return URLEncoder.encode(value,"UTF-8");}
}
