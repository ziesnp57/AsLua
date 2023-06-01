package com.aslua;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.luajava.LuaState;
import com.luajava.LuaTable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LuaApplication extends Application implements LuaContext {

    // LuaApplication类继承自Application类，并实现了LuaContext接口

    private static LuaApplication mApp;
    // 静态变量mApp用于存储LuaApplication的实例
    static private final HashMap<String, Object> data = new HashMap<>();
    // 静态哈希映射变量data用于存储数据
    protected String localDir;
    // 字符串变量localDir用于存储本地目录路径
    protected String odexDir;
    // 字符串变量odexDir用于存储odex目录路径
    protected String libDir;
    // 字符串变量libDir用于存储lib目录路径
    protected String luaMdDir;
    // 字符串变量luaMdDir用于存储luaMd目录路径
    protected String luaCpath;
    // 字符串变量luaCpath用于存储luaCpath路径
    protected String luaLpath;
    // 字符串变量luaLpath用于存储luaLpath路径
    protected String luaExtDir;
    // 字符串变量luaExtDir用于存储luaExt目录路径
    private boolean isUpdata;
    // 布尔变量isUpdata用于存储更新状态
    private SharedPreferences mSharedPreferences;
// SharedPreferences变量mSharedPreferences用于存储共享首选项

    public Uri getUriForPath(String path) {
        // 获取路径对应的Uri
        return FileProvider.getUriForFile(this, getPackageName(), new File(path));
    }

    public Uri getUriForFile(File path) {
        // 获取文件对应的Uri
        return FileProvider.getUriForFile(this, getPackageName(), path);
    }

    public String getPathFromUri(Uri uri) {
        // 从Uri获取路径
        String path = null;
        if (uri != null) {
            String[] p = {
                    getPackageName()
            };
            switch (Objects.requireNonNull(uri.getScheme())) {
                case "content" -> {
                    Cursor cursor = getContentResolver().query(uri, p, null, null, null);
                    if (cursor != null) {
                        int idx = cursor.getColumnIndexOrThrow(getPackageName());
                        if (idx < 0)
                            break;
                        path = cursor.getString(idx);
                        cursor.moveToFirst();
                        cursor.close();
                    }
                }
                case "file" -> path = uri.getPath();
            }
        }
        return path;
    }

    public static LuaApplication getInstance() {
        // 获取LuaApplication实例
        return mApp;
    }

    @Override
    public ArrayList<ClassLoader> getClassLoaders() {
        // 获取类加载器列表（未实现）
        return null;
    }

    @Override
    public void regGc(LuaGcable obj) {
        // 注册LuaGcable对象（未实现）
    }

    @Override
    public String getLuaPath() {
        // 获取Lua路径（未实现）
        return null;
    }

    @Override
    public String getLuaPath(String path) {
        // 获取Lua路径（带有path参数）
        return new File(getLuaDir(), path).getAbsolutePath();
    }

    @Override
    public String getLuaPath(String dir, String name) {
        // 获取Lua路径（带有dir和name参数）
        return new File(getLuaDir(dir), name).getAbsolutePath();
    }

    @Override
    public String getLuaExtPath(String path) {
        // 获取LuaExt路径（带有path参数）
        return new File(getLuaExtDir(), path).getAbsolutePath();
    }

    @Override
    public String getLuaExtPath(String dir, String name) {
        // 获取LuaExt路径（带有dir和name参数）
        return new File(getLuaExtDir(dir), name).getAbsolutePath();
    }

    public int getWidth() {
        // 获取屏幕宽度
        return getResources().getDisplayMetrics().widthPixels;
    }

    public int getHeight() {
        // 获取屏幕高度
        return getResources().getDisplayMetrics().heightPixels;
    }

    @Override
    public String getLuaDir(String dir) {
        // 获取Lua目录（带有dir参数）
        return localDir;
    }

    @Override
    public String getLuaExtDir(String name) {
        // 获取Lua扩展目录（带有name参数）
        File dir = new File(getLuaExtDir(), name);
        if (!dir.exists())
            if (!dir.mkdirs())
                return dir.getAbsolutePath();
        return dir.getAbsolutePath();
    }

    public String getLibDir() {
        // 获取lib目录
        return libDir;
    }

    public String getOdexDir() {
        // 获取odex目录
        return odexDir;
    }

    @Override
    public void onCreate() {
        // 应用创建时调用的方法
        super.onCreate();
        mApp = this;
        CrashHandler crashHandler = new CrashHandler();
        // 实例化CrashHandler
        crashHandler.init(getApplicationContext());
        // 初始化CrashHandler
        mSharedPreferences = getSharedPreferences(this);
        // 获取SharedPreferences实例

        // 初始化aslua工作目录
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            luaExtDir = sdDir + "/AsLua";
        } else {
            File[] fs = new File("/storage").listFiles();
            if (fs != null) {
                for (File f : fs) {
                    String[] ls = f.list();
                    if (ls == null)
                        continue;
                    if (ls.length > 5)
                        luaExtDir = f.getAbsolutePath() + "/AsLua";
                }
            }
            if (luaExtDir == null)
                luaExtDir = getDir("AsLua", Context.MODE_PRIVATE).getAbsolutePath();
        }

        File destDir = new File(luaExtDir);
        if (!destDir.exists()) {
            boolean b = destDir.mkdirs();
            if(!b){
                luaExtDir = Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath() + "/AsLua";
            }
        }
        destDir = new File(luaExtDir);

        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        localDir = getFilesDir().getAbsolutePath();
        odexDir = getDir("odex", Context.MODE_PRIVATE).getAbsolutePath();
        libDir = getDir("lib", Context.MODE_PRIVATE).getAbsolutePath();
        luaMdDir = getDir("lua", Context.MODE_PRIVATE).getAbsolutePath();

        // 初始化lua加载路径
        luaCpath = getApplicationInfo().nativeLibraryDir + "/lib?.so" + ";" + libDir + "/lib?.so";


        // 初始化lua工作目录
        luaLpath = luaMdDir + "/?.lua;" + luaMdDir + "/lua/?.lua;" + luaMdDir + "/?/init.lua;";

    }


    private static SharedPreferences getSharedPreferences(Context context) {
        // 获取SharedPreferences实例（根据API版本）
        Context deContext = context.createDeviceProtectedStorageContext();
        return PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNullElse(deContext, context));
    }

    @Override
    public String getLuaDir() {
        // 获取Lua目录
        return localDir;
    }

    @Override
    public void call(String name, Object[] args) {
        // 调用方法（未实现）
    }

    @Override
    public void set(String name, Object object) {
        // 设置变量（未实现）
        data.put(name, object);
    }

    @Override
    public Map getGlobalData() {
        // 获取全局数据
        return data;
    }


    @Override
    public Object getSharedData(String key) {
        // 获取共享数据（根据key）
        return mSharedPreferences.getAll().get(key);
    }

    @Override
    public Object getSharedData(String key, Object def) {
        // 获取共享数据（根据key和默认值）
        Object ret = mSharedPreferences.getAll().get(key);
        if (ret == null)
            return def;
        return ret;
    }

    @Override
    public boolean setSharedData(String key, Object value) {
        // 设置共享数据（根据key和value）
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        if (value == null)
            edit.remove(key);
        else if (value instanceof String)
            edit.putString(key, value.toString());
        else if (value instanceof Long)
            edit.putLong(key, (Long) value);
        else if (value instanceof Integer)
            edit.putInt(key, (Integer) value);
        else if (value instanceof Float)
            edit.putFloat(key, (Float) value);
        else if (value instanceof Set)
            edit.putStringSet(key, (Set<String>) value);
        else if (value instanceof LuaTable)
            edit.putStringSet(key, (HashSet<String>) ((LuaTable) value).values());
        else if (value instanceof Boolean)
            edit.putBoolean(key, (Boolean) value);
        else
            return false;
        edit.apply();
        return true;
    }

    public Object get(String name) {
        // 获取变量（根据name）
        return data.get(name);
    }

    public String getLocalDir() {
        // 获取本地目录
        return localDir;
    }


    public String getMdDir() {
        // 获取Md目录
        return luaMdDir;
    }

    @Override
    public String getLuaExtDir() {
        // 获取Lua扩展目录
        return luaExtDir;
    }

    @Override
    public void setLuaExtDir(String dir) {
        // 设置Lua扩展目录（根据dir）
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            luaExtDir = new File(sdDir , dir).getAbsolutePath();
        } else {
            File[] fs = new File("/storage").listFiles();
            assert fs != null;
            for (File f : fs) {
                String[] ls = f.list();
                if (ls == null)
                    continue;
                if (ls.length > 5)
                    luaExtDir = new File(f, dir).getAbsolutePath() ;
            }
            if (luaExtDir == null)
                luaExtDir = getDir(dir, Context.MODE_PRIVATE).getAbsolutePath();
        }
    }


    @Override
    public String getLuaCpath() {
        // 获取LuaCpath路径
        return luaCpath;
    }

    @Override
    public String getLuaLpath() {
        // 获取LuaLpath路径
        return luaLpath;
    }

    @Override
    public Context getContext() {
        // 获取上下文
        return this;
    }

    @Override
    public LuaState getLuaState() {
        // 获取LuaState
        return null;
    }

    @Override
    public Object doFile(String path, Object[] arg) {
        // 执行文件（根据path和arg）
        return null;
    }

    @Override
    public void sendMsg(String msg) {
        // 发送消息（根据msg）
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void sendError(String title, Exception msg) {
        // 发送错误（根据title和msg）
    }

}