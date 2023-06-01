package com.aslua;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.android.widget.ArrayListAdapter;
import com.luajava.JavaFunction;
import com.luajava.LuaError;
import com.luajava.LuaObject;
import com.luajava.LuaState;
import com.luajava.LuaStateFactory;
import com.yongle.aslua.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import dalvik.system.DexClassLoader;

public class LuaActivity extends AppCompatActivity implements LuaBroadcastReceiver.OnReceiveListener, LuaContext {

    private final static String ARG = "arg";  // 参数常量名
    private final static String DATA = "data";  // 数据常量名
    private final static String NAME = "name";  // 名称常量名
    private static final ArrayList<String> prjCache = new ArrayList<>();  // 项目缓存列表
    private String luaDir;  // Lua文件目录
    private Handler handler;  // 处理程序
    private TextView status;  // 状态文本视图
    private String luaCpath;  // Lua C路径
    private LuaDexLoader mLuaDexLoader;  // Lua Dex加载器
    private int mWidth;  // 屏幕宽度
    private int mHeight;  // 屏幕高度
    private ArrayListAdapter<String> adapter;  // 列表适配器
    private LuaState L;  // Lua状态
    private String luaPath;  // Lua文件路径
    private final StringBuilder toastbuilder = new StringBuilder();  // Toast构建器
    private Boolean isCreate = false;  // 是否已创建
    private Toast toast;  // Toast
    private LinearLayout layout;  // 线性布局
    private boolean isSetViewed;  // 是否设置视图
    private long lastShow;  // 上次显示时间
    private Menu optionsMenu;  // 菜单选项
    private LuaObject mOnKeyDown;  // onKeyDown回调
    private LuaObject mOnKeyUp;  // onKeyUp回调
    private LuaObject mOnKeyLongPress;  // onKeyLongPress回调
    private LuaObject mOnTouchEvent;  // onTouchEvent回调
    private String localDir;  // 本地目录
    private String odexDir;  // Odex目录
    private String libDir;  // 库目录
    private String luaExtDir;  // Lua扩展目录
    private LuaBroadcastReceiver mReceiver;  // 广播接收器
    private String luaLpath;  // Lua加载路径
    private String luaMdDir;  // LuaMd目录
    private boolean isUpdata;  // 是否更新
    private boolean mDebug = true;  // 调试模式
    private LuaResources mResources;  // Lua资源
    private Resources.Theme mTheme;  // 主题
    private ArrayList<LuaGcable> gclist = new ArrayList<>();  // GC对象列表
    private String pageName = "main";  // 页面名称
    private static String sKey;  // 静态键值
    private static final HashMap<String, LuaActivity> sLuaActivityMap = new HashMap<>();  // LuaActivity映射表
    private LuaObject mOnKeyShortcut;  // onKeyShortcut回调


    private static byte[] readAll(InputStream input) throws IOException {
        // 创建一个字节数组输出流，用于存储读取的数据
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        // 创建一个缓冲区数组
        byte[] buffer = new byte[4096];
        int n;
        // 从输入流中读取数据，直到读取到末尾
        while (-1 != (n = input.read(buffer))) {
            // 将读取的数据写入字节数组输出流
            output.write(buffer, 0, n);
        }
        // 将字节数组输出流中的数据转换为字节数组
        byte[] ret = output.toByteArray();
        output.close();
        // 返回读取的字节数组
        return ret;
    }


    @Override
    public ArrayList<ClassLoader> getClassLoaders() {

        return mLuaDexLoader.getClassLoaders();
    }

    public HashMap<String, String> getLibrarys() {
        // 返回库文件的映射关系（库文件名和路径）
        return mLuaDexLoader.getLibrarys();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        // 获取传递过来的参数
        String name = getIntent().getStringExtra("name");

        if (name != null) {
            // 设置标题
            setTitle(name);

            // 设置返回按钮
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        }


        //隐藏标题栏
        //getSupportActionBar().hide();


       // Intent intent=getIntent();
       // int theme=intent.getIntExtra("theme", android.R.style.Theme_Holo_Light_NoActionBar);

        // 获取Intent，可以从Intent中获取主题信息，这里注释掉了

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // 设置StrictMode线程策略，允许所有操作，用于临时禁用一些特定的线程策略限制

        super.onCreate(null);

        // 调用父类的onCreate方法，传入null作为参数

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mWidth = outMetrics.widthPixels;
        mHeight = outMetrics.heightPixels;

        // 获取窗口管理器和显示度量，并获取屏幕的宽度和高度

        layout = new LinearLayout(this);
        //layout.setBackgroundColor(Color.WHITE);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        status = new TextView(this);

        status.setTextColor(Color.BLACK);
        scroll.addView(status, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        status.setText("");
        status.setTextIsSelectable(true);
        // 创建列表适配器
        ListView list = new ListView(this);
        list.setFastScrollEnabled(true);



        adapter = new ArrayListAdapter<>(this, android.R.layout.simple_list_item_1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                if (convertView == null)
                    view.setTextIsSelectable(true);
                return view;
            }
        };

        list.setAdapter(adapter);
        layout.addView(list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));


        // 定义文件夹
        LuaApplication app = (LuaApplication) getApplication();

        if(app.getClass()!=LuaApplication.class){
            while (true){
                app.getClass();
            }
        }
        localDir = app.getLocalDir();
        odexDir = app.getOdexDir();
        libDir = app.getLibDir();
        luaMdDir = app.getMdDir();
        luaCpath = app.getLuaCpath();
        luaDir = localDir;
        luaLpath = app.getLuaLpath();
        luaExtDir = app.getLuaExtDir();

        // 获取应用程序的文件夹路径和其他相关路径

        handler = new MainHandler();

        // 创建Handler对象用于处理消息

        try {
            status.setText("");
            adapter.clear();
            Intent intent = getIntent();
            Object[] arg = (Object[]) intent.getSerializableExtra(ARG);
            if (arg == null)
                arg = new Object[0];

            luaPath = getLuaPath();
            pageName = new File(luaPath).getName();
            int idx = pageName.lastIndexOf(".");
            if (idx > 0)
                pageName = pageName.substring(0, idx);

            luaLpath = (luaDir + "/?.lua;" + luaDir + "/lua/?.lua;" + luaDir + "/?/init.lua;") + luaLpath;
            initLua();

            mLuaDexLoader = new LuaDexLoader(this);
            mLuaDexLoader.loadLibs();
            //MultiDex.installLibs(this);
            sLuaActivityMap.put(pageName, this);
            doFile(luaPath, arg);
            isCreate = true;
            if (!pageName.equals("main"))
                runFunc("main", arg);
            runFunc(pageName, arg);
            runFunc("onCreate", savedInstanceState);
            if (!isSetViewed) {
                TypedArray array = getTheme().obtainStyledAttributes(new int[]{
                        android.R.attr.colorBackground,
                        android.R.attr.textColorPrimary,
                        android.R.attr.textColorHighlightInverse,
                });
                int backgroundColor = array.getColor(0, 0xFF00FF);
                int textColor = array.getColor(1, 0xFF00FF);
                array.recycle();
                status.setTextColor(textColor);
                layout.setBackgroundColor(backgroundColor);
                setContentView(layout);
            }
        } catch (Exception e) {
            sendMsg(e.getMessage());
            setContentView(layout);
            return;
        }

        // 初始化Lua环境和相关配置，并执行相应的Lua脚本

        mOnKeyShortcut = L.getLuaObject("onKeyShortcut");
        if (mOnKeyShortcut.isNil())
            mOnKeyShortcut = null;
        mOnKeyDown = L.getLuaObject("onKeyDown");
        if (mOnKeyDown.isNil())
            mOnKeyDown = null;
        mOnKeyUp = L.getLuaObject("onKeyUp");
        if (mOnKeyUp.isNil())
            mOnKeyUp = null;
        mOnKeyLongPress = L.getLuaObject("onKeyLongPress");
        if (mOnKeyLongPress.isNil())
            mOnKeyLongPress = null;
        mOnTouchEvent = L.getLuaObject("onTouchEvent");
        if (mOnTouchEvent.isNil())
            mOnTouchEvent = null;
        LuaObject onAccessibilityEvent = L.getLuaObject("onAccessibilityEvent");
        if (onAccessibilityEvent.isFunction())
            LuaAccessibilityService.onAccessibilityEvent = onAccessibilityEvent.getFunction();

        // 获取Lua脚本中定义的回调函数


        try {
            throw new RuntimeException("");
        } catch (Exception e) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            PrintStream p = new PrintStream(b);
            e.printStackTrace(p);
            String s = b.toString();
            String[] bs = s.split("\n");
            for (int i = 1; i < bs.length; i++) {
                String l = bs[i];
                if (l.contains("com.aslua") || l.contains("android.app") || l.contains("android.os") || l.contains("java.lang") || l.contains("com.android"))
                    continue;
                runFunc("onHook");
            /*LuaDialog d = new LuaDialog(this);
            d.setTitle("提示1");
            d.setMessage("你的手机运行环境不安全");
            d.setPosButton("确定");
            d.show();*/
                return;
            }
        }
    }



    public void setFragment(Fragment fragment) {
        isSetViewed = true;
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment).commit();
    }

// 设置Fragment并将其显示在当前Activity中

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        if (mOnKeyShortcut != null) {
            try {
                Object ret = mOnKeyShortcut.call(keyCode, event);
                if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret)
                    return true;
            } catch (LuaError e) {
                sendError("onKeyShortcut", e);
            }
        }
        return super.onKeyShortcut(keyCode, event);
    }

// 处理按键快捷方式事件，如果Lua脚本中定义了onKeyShortcut回调函数，则调用之并返回结果

    @Override
    public void regGc(LuaGcable obj) {
        // TODO: Implement this method
        gclist.add(obj);
    }

// 注册Lua对象的垃圾回收函数

    public Uri getUriForPath(String path) {
        return FileProvider.getUriForFile(this, getPackageName(), new File(path));
    }

// 根据文件路径获取Uri，用于文件共享

    public long test(String src, int n) {
        long t=System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            L.LdoString(src);
        }
        return System.currentTimeMillis()-t;
    }

// 在Lua环境中执行Lua代码，并计算执行时间

    public Uri getUriForFile(File path) {
        return FileProvider.getUriForFile(this, getPackageName(), path);
    }

// 根据文件对象获取Uri，用于文件共享

    public String getPathFromUri(Uri uri) {
        String path = null;
        if (uri != null) {
            String[] p = {
                    MediaStore.Images.Media.DATA
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

// 根据Uri获取文件路径，用于从Uri中解析出文件路径

    public void initMain() {
        prjCache.add(getLocalDir());
    }

// 初始化主要的Lua项目

    public String getLuaPath() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        String path;
        if (uri == null)
            return null;

        path = uri.getPath();
        assert path != null;
        if (!new File(path).exists() && new File(getLuaPath(path)).exists())
            path = getLuaPath(path);

        luaPath = path;
        File f = new File(path);

        luaDir = new File(luaPath).getParent();
        if (f.getName().equals("runcode/main.lua") && new File(luaDir, "runcode/init.lua").exists()) {
            if (!prjCache.contains(luaDir))
                prjCache.add(luaDir);
        } else {
            String parent = luaDir;
            while (parent != null) {
                if (prjCache.contains(parent)) {
                    luaDir = parent;
                    break;
                } else {
                    if (new File(parent, "runcode/main.lua").exists() && new File(parent, "runcode/init.lua").exists()) {
                        luaDir = parent;
                        if (!prjCache.contains(luaDir))
                            prjCache.add(luaDir);
                        break;
                    }
                }
                parent = new File(parent).getParent();
            }
        }
        return path;
    }

// 获取Lua脚本的路径，并初始化相关的Lua项目

    public String getQuery(String name) {
        Uri uri = getIntent().getData();
        if (uri == null)
            return null;
        return uri.getQueryParameter(name);
    }

// 获取Uri中指定名称的查询参数值

    public Object getArg(int idx) {
        Object[] arg = (Object[]) getIntent().getSerializableExtra(ARG);
        if (arg != null && arg.length > idx && idx >= 0) {
            return arg[idx];
        }
        return null;
    }


    /**
     * 获取Lua脚本的绝对路径
     */
    @Override
    public String getLuaPath(String path) {
        return new File(getLuaDir(), path).getAbsolutePath();
    }

    /**
     * 获取指定目录下Lua脚本的绝对路径
     */
    @Override
    public String getLuaPath(String dir, String name) {
        return new File(getLuaDir(dir), name).getAbsolutePath();
    }

    /**
     * 获取Lua扩展脚本的绝对路径
     */
    @Override
    public String getLuaExtPath(String path) {
        return new File(getLuaExtDir(), path).getAbsolutePath();
    }

    /**
     * 获取指定目录下Lua扩展脚本的绝对路径
     */
    @Override
    public String getLuaExtPath(String dir, String name) {
        return new File(getLuaExtDir(dir), name).getAbsolutePath();
    }

    /**
     * 获取Lua加载路径
     */
    @Override
    public String getLuaLpath() {
        return luaLpath;
    }

    /**
     * 获取Lua C库加载路径
     */
    @Override
    public String getLuaCpath() {
        return luaCpath;
    }

    /**
     * 获取上下文Context
     */
    @Override
    public Context getContext() {
        return this;
    }

    /**
     * 获取Lua状态机LuaState
     */
    @Override
    public LuaState getLuaState() {
        return L;
    }

    /**
     * 获取窗口的DecorView
     */
    public View getDecorView() {
        return getWindow().getDecorView();
    }

    /**
     * 获取本地目录路径
     */
    public String getLocalDir() {
        return localDir;
    }

    /**
     * 获取Lua扩展目录路径
     */
    @Override
    public String getLuaExtDir() {
        return luaExtDir;
    }

    /**
     * 设置Lua扩展目录路径
     */
    @Override
    public void setLuaExtDir(String dir) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            luaExtDir = new File(sdDir, dir).getAbsolutePath();
        } else {
            File[] fs = new File("/storage").listFiles();
            assert fs != null;
            for (File f : fs) {
                String[] ls = f.list();
                if (ls == null)
                    continue;
                if (ls.length > 5)
                    luaExtDir = new File(f, dir).getAbsolutePath();
            }
            if (luaExtDir == null)
                luaExtDir = getDir(dir, Context.MODE_PRIVATE).getAbsolutePath();
        }
        File d = new File(luaExtDir);
        if (!d.exists())
            d.mkdirs();
    }

    /**
     * 获取Lua扩展目录下指定名称的子目录路径
     */
    @Override
    public String getLuaExtDir(String name) {
        File dir = new File(getLuaExtDir(), name);
        if (!dir.exists())
            if (!dir.mkdirs())
                return null;
        return dir.getAbsolutePath();
    }

    /**
     * 获取Lua目录路径
     */
    @Override
    public String getLuaDir() {
        return luaDir;
    }

    /**
     * 设置Lua目录路径
     */
    public void setLuaDir(String dir) {
        luaDir = dir;
    }

    /**
     * 获取Lua目录下指定名称的子目录路径
     */
    @Override
    public String getLuaDir(String name) {
        File dir = new File(luaDir + "/" + name);
        if (!dir.exists())
            if (!dir.mkdirs())
                return null;
        return dir.getAbsolutePath();
    }


    /**
     * 解压Assets中的文件
     *
     * @param assetName       压缩包文件名
     * @param outputDirectory 输出目录
     */
    public void unZipAssets(String assetName, String outputDirectory) throws IOException {
        //创建解压目标目录
        File file = new File(outputDirectory);
        //如果目标目录不存在，则创建
        if (!file.exists()) {
            file.mkdirs();
        }
        InputStream inputStream;
        //打开压缩文件
        try {
            inputStream = this.getAssets().open(assetName);
        } catch (IOException e) {
            return;
        }


        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        //读取一个进入点
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        //使用1Mbuffer
        byte[] buffer = new byte[4096];
        //解压时字节计数
        int count;
        //如果进入点为空说明已经遍历完所有压缩包中文件和目录
        while (zipEntry != null) {
            //如果是一个目录
            if (zipEntry.isDirectory()) {
                file = new File(outputDirectory + File.separator + zipEntry.getName());
                file.mkdir();
            } else {
                //如果是文件
                file = new File(outputDirectory + File.separator
                        + zipEntry.getName());
                //创建该文件
                file.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                while ((count = zipInputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, count);
                }
                fileOutputStream.close();
            }
            //定位到下一个文件入口
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
    }

    public DexClassLoader loadApp(String path) throws LuaError {
        return mLuaDexLoader.loadApp(path);
    }

    public DexClassLoader loadDex(String path) throws LuaError {
        return mLuaDexLoader.loadDex(path);
    }

    public void loadResources(String path) {
        mLuaDexLoader.loadResources(path);
    }

    @Override
    public AssetManager getAssets() {
        if (mLuaDexLoader != null && mLuaDexLoader.getAssets() != null)
            return mLuaDexLoader.getAssets();
        return super.getAssets();
    }

    public LuaResources getLuaResources() {
        Resources superRes = super.getResources();
        if (mLuaDexLoader != null && mLuaDexLoader.getResources() != null)
            superRes = mLuaDexLoader.getResources();
        mResources = new LuaResources(getAssets(), superRes.getDisplayMetrics(),
                superRes.getConfiguration());
        mResources.setSuperResources(superRes);
        return mResources;
    }

    public Resources getSuperResources() {
        return super.getResources();
    }

    @Override
    public Resources getResources() {
        if (mLuaDexLoader != null && mLuaDexLoader.getResources() != null)
            return mLuaDexLoader.getResources();
        if (mResources != null)
            return mResources;
        return super.getResources();
    }

    @Override
    public Resources.Theme getTheme() {
        if (mLuaDexLoader != null && mLuaDexLoader.getTheme() != null)
            return mLuaDexLoader.getTheme();
        return super.getTheme();
    }

    public Object loadLib(String name) throws LuaError {
        int i = name.indexOf(".");
        String fn = name;
        if (i > 0)
            fn = name.substring(0, i);
        File f = new File(libDir + "/lib" + fn + ".so");
        if (!f.exists()) {
            f = new File(luaDir + "/lib" + fn + ".so");
            if (!f.exists())
                throw new LuaError("can not find lib " + name);
            LuaUtil.copyFile(luaDir + "/lib" + fn + ".so", libDir + "/lib" + fn + ".so");
        }
        LuaObject require = L.getLuaObject("require");
        return require.call(name);
    }

    public void createShortcut(String text, String name) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setClassName(getPackageName(), LuaActivity.class.getName());
        intent.setData(Uri.parse(text));

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        ShortcutManager scm = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);
        ShortcutInfo si = new ShortcutInfo.Builder(this, text)
                .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                .setShortLabel(name)
                .setIntent(intent)
                .build();
        try {
            scm.requestPinShortcut(si, null);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "添加快捷方式出错", Toast.LENGTH_SHORT).show();
        }
    }

    public void createShortcut(String text, String name, String icon) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setClassName(getPackageName(), LuaActivity.class.getName());
        intent.setData(Uri.parse(text));

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        ShortcutManager scm = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);
        ShortcutInfo si = new ShortcutInfo.Builder(this, text)
                .setIcon(Icon.createWithFilePath(icon))
                .setShortLabel(name)
                .setIntent(intent)
                .build();
        try {
            scm.requestPinShortcut(si, null);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "添加快捷方式出错", Toast.LENGTH_SHORT).show();
        }
    }

    public void shareFile(String path) {
        Intent share = new Intent(Intent.ACTION_SEND);
        File file = new File(path);
        share.setType(getType(file));
        share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.putExtra(Intent.EXTRA_STREAM, getUriForFile(file));
        startActivity(Intent.createChooser(share, file.getName()));
    }

    private String getType(@NonNull File file) {
        int lastDot = file.getName().lastIndexOf(46);
        if (lastDot >= 0) {
            String extension = file.getName().substring(lastDot + 1);
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }
        return "application/octet-stream";
    }

    public void installApk(String path) {
        Intent share = new Intent(Intent.ACTION_VIEW);
        File file = new File(path);
        share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.setDataAndType(getUriForFile(file), getType(file));
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(share, file.getName()));
    }

    public Intent registerReceiver(LuaBroadcastReceiver receiver, IntentFilter filter) {
        // TODO: Implement this method
        return super.registerReceiver(receiver, filter);
    }

    public Intent registerReceiver(LuaBroadcastReceiver.OnReceiveListener ltr, IntentFilter filter) {
        // TODO: Implement this method
        LuaBroadcastReceiver receiver = new LuaBroadcastReceiver(ltr);
        return super.registerReceiver(receiver, filter);
    }

    public Intent registerReceiver(IntentFilter filter) {
        // TODO: Implement this method
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = new LuaBroadcastReceiver(this);
        return super.registerReceiver(mReceiver, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: Implement this method
        runFunc("onReceive", context, intent);
    }

    @Override
    public void onContentChanged() {
        // TODO: Implement this method
        super.onContentChanged();
        isSetViewed = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        runFunc("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        runFunc("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        runFunc("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        runFunc("onStop");
    }

    public static LuaActivity getActivity(String name) {
        return sLuaActivityMap.get(name);
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null)
            unregisterReceiver(mReceiver);

        for (LuaGcable obj : gclist) {
            obj.gc();
        }
        sLuaActivityMap.remove(pageName);
        runFunc("onDestroy");
        super.onDestroy();
        System.gc();
        L.gc(LuaState.LUA_GCCOLLECT, 1);
        //L.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: Implement this method
        if (data != null) {
            String name = data.getStringExtra(NAME);
            if (name != null) {
                Object[] res = (Object[]) data.getSerializableExtra(DATA);
                if (res == null) {
                    runFunc("onResult", name);
                } else {
                    Object[] arg = new Object[res.length + 1];
                    arg[0] = name;
                    System.arraycopy(res, 0, arg, 1, res.length);
                    Object ret = runFunc("onResult", arg);
                    if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret)
                        return;
                }
            }
        }
        runFunc("onActivityResult", requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mOnKeyDown != null) {
            try {
                Object ret = mOnKeyDown.call(keyCode, event);
                if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret)
                    return true;
            } catch (LuaError e) {
                sendError("onKeyDown", e);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mOnKeyUp != null) {
            try {
                Object ret = mOnKeyUp.call(keyCode, event);
                if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret)
                    return true;
            } catch (LuaError e) {
                sendError("onKeyUp", e);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (mOnKeyLongPress != null) {
            try {
                Object ret = mOnKeyLongPress.call(keyCode, event);
                if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret)
                    return true;
            } catch (LuaError e) {
                sendError("onKeyLongPress", e);
            }
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnTouchEvent != null) {
            try {
                Object ret = mOnTouchEvent.call(event);
                if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret)
                    return true;
            } catch (LuaError e) {
                sendError("onTouchEvent", e);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO: Implement this method
        optionsMenu = menu;
        runFunc("onCreateOptionsMenu", menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // 获取传递过来的参数
        String name = getIntent().getStringExtra("name");

        if (name != null) {
            if (item.getItemId() == android.R.id.home) {
                this.finish();
            }
        }

        Object ret = null;
        if (!item.hasSubMenu())
            ret = runFunc("onOptionsItemSelected", item);
        if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret)
            return true;
        return super.onOptionsItemSelected(item);
    }

    public Menu getOptionsMenu() {
        return optionsMenu;
    }

/*    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        if (!item.hasSubMenu())
            runFunc("onMenuItemSelected", featureId, item);
        return super.onMenuItemSelected(featureId, item);
    }*/

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // TODO: Implement this method
        runFunc("onCreateContextMenu", menu, v, menuInfo);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO: Implement this method
        runFunc("onContextItemSelected", item);
        return super.onContextItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO: Implement this method
        super.onConfigurationChanged(newConfig);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        //wm.getDefaultDisplay().getRealMetrics(outMetrics);
        mWidth = outMetrics.widthPixels;
        mHeight = outMetrics.heightPixels;
        runFunc("onConfigurationChanged", newConfig);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    @Override
    public Map getGlobalData() {
        return ((LuaApplication) getApplication()).getGlobalData();
    }

    @Override
    public Object getSharedData(String key) {
        return LuaApplication.getInstance().getSharedData(key);
    }

    @Override
    public Object getSharedData(String key, Object def) {
        return LuaApplication.getInstance().getSharedData(key, def);
    }

    @Override
    public boolean setSharedData(String key, Object value) {
        return LuaApplication.getInstance().setSharedData(key, value);
    }

    public boolean bindService(int flag) {
        ServiceConnection conn = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName comp, IBinder binder) {
                // TODO: Implement this method
                runFunc("onServiceConnected", comp, ((LuaService.LuaBinder) binder).getService());
            }

            @Override
            public void onServiceDisconnected(ComponentName comp) {
                // TODO: Implement this method
                runFunc("onServiceDisconnected", comp);
            }
        };
        return bindService(conn, flag);
    }

    public boolean bindService(ServiceConnection conn, int flag) {
        // TODO: Implement this method
        Intent service = new Intent(this, LuaService.class);
        service.putExtra("luaDir", luaDir);
        service.putExtra("luaPath", luaPath);
        return super.bindService(service, conn, flag);
    }

    public boolean stopService() {
        return stopService(new Intent(this, LuaService.class));
    }

    public ComponentName startService() {
        return startService(null, null);
    }

    public ComponentName startService(Object[] arg) {
        return startService(null, arg);
    }

    public ComponentName startService(String path) {
        return startService(path, null);
    }

    public ComponentName startService(String path, Object[] arg) {
        // TODO: Implement this method
        Intent intent = new Intent(this, LuaService.class);
        intent.putExtra("luaDir", luaDir);
        intent.putExtra("luaPath", luaPath);
        if (path != null) {
            if (path.charAt(0) != '/')
                intent.setData(Uri.parse("file://" + luaDir + "/" + path + ".lua"));
            else
                intent.setData(Uri.parse("file://" + path));
        }

        if (arg != null)
            intent.putExtra(ARG, arg);

        return super.startService(intent);
    }

    public void newActivity(String path, boolean newDocument) throws FileNotFoundException {
        newActivity(1, path, null, newDocument);
    }

    public void newActivity(String path, Object[] arg, boolean newDocument) throws FileNotFoundException {
        newActivity(1, path, arg, newDocument);
    }

    public void newActivity(int req, String path, boolean newDocument) throws FileNotFoundException {
        newActivity(req, path, null, newDocument);
    }

    public void newActivity(String path) throws FileNotFoundException {
        newActivity(1, path, null);
    }

    public void newActivity(String path, Object[] arg) throws FileNotFoundException {
        newActivity(1, path, arg);
    }

    public void newActivity(int req, String path) throws FileNotFoundException {
        newActivity(req, path, null);
    }

    public void newActivity(int req, String path, Object[] arg) throws FileNotFoundException {
        newActivity(req, path, arg, false);
    }

    public void newActivity(int req, String path, Object[] arg, boolean newDocument) throws FileNotFoundException {
        Intent intent = new Intent(this, LuaActivity.class);

        intent.putExtra(NAME, path);
        if (path.charAt(0) != '/')
            path = luaDir + "/" + path;
        File f = new File(path);
        if (f.isDirectory() && new File(path + "/runcode/main.lua").exists())
            path += "/runcode/main.lua";
        else if ((f.isDirectory() || !f.exists()) && !path.endsWith(".lua"))
            path += ".lua";
        if (!new File(path).exists())
            throw new FileNotFoundException(path);

        if (newDocument) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }

        intent.setData(Uri.parse("file://" + path));

        if (arg != null)
            intent.putExtra(ARG, arg);
        if (newDocument)
            startActivity(intent);
        else
            startActivityForResult(intent, req);
        //overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public void newActivity(String path, int in, int out, boolean newDocument) throws FileNotFoundException {
        newActivity(1, path, in, out, null, newDocument);
    }

    public void newActivity(String path, int in, int out, Object[] arg, boolean newDocument) throws FileNotFoundException {
        newActivity(1, path, in, out, arg, newDocument);
    }

    public void newActivity(int req, String path, int in, int out, boolean newDocument) throws FileNotFoundException {
        newActivity(req, path, in, out, null, newDocument);
    }

    public void newActivity(String path, int in, int out) throws FileNotFoundException {
        newActivity(1, path, in, out, null);
    }

    public void newActivity(String path, int in, int out, Object[] arg) throws FileNotFoundException {
        newActivity(1, path, in, out, arg);
    }

    public void newActivity(int req, String path, int in, int out) throws FileNotFoundException {
        newActivity(req, path, in, out, null);
    }

    public void newActivity(int req, String path, int in, int out, Object[] arg) throws FileNotFoundException {
        newActivity(req, path, in, out, arg, false);
    }

    public void newActivity(int req, String path, int in, int out, Object[] arg, boolean newDocument) throws FileNotFoundException {
        Intent intent = new Intent(this, LuaActivity.class);

        intent.putExtra(NAME, path);
        if (path.charAt(0) != '/')
            path = luaDir + "/" + path;
        File f = new File(path);
        if (f.isDirectory() && new File(path + "/runcode/main.lua").exists())
            path += "/runcode/main.lua";
        else if ((f.isDirectory() || !f.exists()) && !path.endsWith(".lua"))
            path += ".lua";
        if (!new File(path).exists())
            throw new FileNotFoundException(path);
        intent.setData(Uri.parse("file://" + path));

        if (newDocument) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }


        if (arg != null)
            intent.putExtra(ARG, arg);
        if (newDocument)
            startActivity(intent);
        else
            startActivityForResult(intent, req);
        overridePendingTransition(in, out);

    }

    public void finish(boolean finishTask) {
        if (!finishTask) {
            super.finish();
            return;
        }
        Intent intent = getIntent();
        if (intent != null && (intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_DOCUMENT) != 0)
            finishAndRemoveTask();
        else
            super.finish();
    }

    public LuaAsyncTask newTask(LuaObject func) throws LuaError {
        return newTask(func, null, null);
    }

    public LuaAsyncTask newTask(LuaObject func, LuaObject callback) throws LuaError {
        return newTask(func, null, callback);
    }

    public LuaAsyncTask newTask(LuaObject func, LuaObject update, LuaObject callback) throws LuaError {
        return new LuaAsyncTask(this, func, update, callback);
    }

    public LuaThread newThread(LuaObject func) throws LuaError {
        return newThread(func, null);
    }

    public LuaThread newThread(LuaObject func, Object[] arg) throws LuaError {
        return new LuaThread(this, func, true, arg);
    }

    public LuaTimer newTimer(LuaObject func) throws LuaError {
        return newTimer(func, null);
    }

    public LuaTimer newTimer(LuaObject func, Object[] arg) throws LuaError {
        return new LuaTimer(this, func, arg);
    }

    public LuaAsyncTask task(long delay, LuaObject func) throws LuaError {
        return task(delay, null, null);
    }

    public LuaAsyncTask task(long delay, Object[] arg, LuaObject func) throws LuaError {
        LuaAsyncTask task = new LuaAsyncTask(this, delay, func);
        task.execute(arg);
        return task;
    }

    public LuaAsyncTask task(LuaObject func) throws LuaError {
        return task(func, null, null, null);
    }

    public LuaAsyncTask task(LuaObject func, Object[] arg) throws LuaError {
        return task(func, arg, null, null);
    }

    public LuaAsyncTask task(LuaObject func, Object[] arg, LuaObject callback) throws LuaError {
        return task(func, null, null, callback);
    }

    public LuaAsyncTask task(LuaObject func, LuaObject update, LuaObject callback) throws LuaError {
        return task(func, null, update, callback);
    }

    public LuaAsyncTask task(LuaObject func, Object[] arg, LuaObject update, LuaObject callback) throws LuaError {
        LuaAsyncTask task = new LuaAsyncTask(this, func, update, callback);
        task.execute(arg);
        return task;
    }

    public LuaThread thread(LuaObject func) throws LuaError {
        LuaThread thread = newThread(func, null);
        thread.start();
        return thread;
    }

    public LuaThread thread(LuaObject func, Object[] arg) throws LuaError {
        LuaThread thread = new LuaThread(this, func, true, arg);
        thread.start();
        return thread;
    }

    public LuaTimer timer(LuaObject func, long period) throws LuaError {
        return timer(func, 0, period, null);
    }

    public LuaTimer timer(LuaObject func, long period, Object[] arg) throws LuaError {
        return timer(func, 0, period, arg);
    }

    public LuaTimer timer(LuaObject func, long delay, long period) throws LuaError {
        return timer(func, delay, period, null);
    }

    public LuaTimer timer(LuaObject func, long delay, long period, Object[] arg) throws LuaError {
        LuaTimer timer = new LuaTimer(this, func, arg);
        timer.start(delay, period);
        return timer;
    }

    public Ticker ticker(final LuaObject func, long period) throws LuaError {
        Ticker timer = new Ticker();
        timer.setOnTickListener(() -> {
            try {
                func.call();
            } catch (LuaError e) {
                e.printStackTrace();
                sendError("onTick", e);
            }
        });
        timer.start();
        timer.setPeriod(period);
        return timer;
    }

    public Bitmap loadBitmap(String path) throws IOException {
        return LuaBitmap.getBitmap(this, path);
    }

    public void setContentView(String layout) throws LuaError {
        setContentView(layout, null);
    }

    public void setContentView(String layout, LuaObject env) throws LuaError {
        // TODO: Implement this method
        LuaObject loadlayout = L.getLuaObject("loadlayout");
        View view = (View) loadlayout.call(layout, env);
        super.setContentView(view);
    }

    public void setContentView(LuaObject layout) throws LuaError {
        setContentView(layout, null);
    }

    public void setContentView(LuaObject layout, LuaObject env) throws LuaError {
        // TODO: Implement this method
        LuaObject loadlayout = L.getLuaObject("loadlayout");
        View view;
        if (layout.isString())
            view = (View) loadlayout.call(layout.getString(), env);
        else if (layout.isTable())
            view = (View) loadlayout.call(layout, env);
        else
            throw new LuaError("布局可以是表或字符串.");
        super.setContentView(view);
    }

    public void result(Object[] data) {
        Intent res = new Intent();
        res.putExtra(NAME, getIntent().getStringExtra(NAME));
        res.putExtra(DATA, data);
        setResult(0, res);
        finish();
    }

    //初始化lua使用的Java函数
    private void initLua() throws Exception {
        L = LuaStateFactory.newLuaState();
        L.openLibs();
        L.pushJavaObject(this);
        L.setGlobal("activity");
        L.getGlobal("activity");
        L.setGlobal("this");
        L.pushContext(this);
        L.getGlobal("luajava");
        L.pushString(luaExtDir);
        L.setField(-2, "luaextdir");
        L.pushString(luaDir);
        L.setField(-2, "luadir");
        L.pushString(luaPath);
        L.setField(-2, "luapath");
        L.pop(1);
        initENV();

        JavaFunction print = new LuaPrint(this, L);
        print.register("print");

        L.getGlobal("package");
        L.pushString(luaLpath);
        L.setField(-2, "path");
        L.pushString(luaCpath);
        L.setField(-2, "cpath");
        L.pop(1);

        JavaFunction set = new JavaFunction(L) {
            @Override
            public int execute() throws LuaError {
                LuaThread thread = (LuaThread) L.toJavaObject(2);

                thread.set(L.toString(3), L.toJavaObject(4));
                return 0;
            }
        };
        set.register("set");

        JavaFunction call = new JavaFunction(L) {
            @Override
            public int execute() throws LuaError {
                LuaThread thread = (LuaThread) L.toJavaObject(2);

                int top = L.getTop();
                if (top > 3) {
                    Object[] args = new Object[top - 3];
                    for (int i = 4; i <= top; i++) {
                        args[i - 4] = L.toJavaObject(i);
                    }
                    thread.call(L.toString(3), args);
                } else if (top == 3) {
                    thread.call(L.toString(3));
                }

                return 0;
            }

        };
        call.register("call");

    }

    public void setDebug(boolean isDebug) {
        mDebug = isDebug;
    }

    private void initENV() throws LuaError {
        if (!new File(luaDir + "/runcode/init.lua").exists())
            return;

        try {
            int ok = L.LloadFile(luaDir + "/runcode/init.lua");
            if (ok == 0) {
                L.newTable();
                LuaObject env = L.getLuaObject(-1);
                L.setUpValue(-2, 1);
                ok = L.pcall(0, 0, 0);
                if (ok == 0) {
                    LuaObject title = env.getField("appname");
                    if (title.isString())
                        setTitle(title.getString());
                    title = env.getField("app_name");
                    if (title.isString())
                        setTitle(title.getString());

                    LuaObject debug = env.getField("debugmode");
                    if (debug.isBoolean())
                        mDebug = debug.getBoolean();
                    debug = env.getField("debug_mode");
                    if (debug.isBoolean())
                        mDebug = debug.getBoolean();

                    LuaObject theme = env.getField("theme");

                    if (theme.isNumber())
                        setTheme((int) theme.getInteger());
                    else if (theme.isString())
                        setTheme(android.R.style.class.getField(theme.getString()).getInt(null));
                    return;
                }
            }
            throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
        } catch (Exception e) {
            sendMsg(e.getMessage());
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        ActivityManager.TaskDescription tDesc;

        tDesc = new ActivityManager.TaskDescription(title.toString());

        setTaskDescription(tDesc);
    }


    //运行lua脚本
    public Object doFile(String filePath) {
        return doFile(filePath, new Object[0]);
    }

    public Object doFile(String filePath, Object[] args) {
        int ok = 0;
        try {
            if (filePath.charAt(0) != '/')
                filePath = luaDir + "/" + filePath;

            L.setTop(0);
            ok = L.LloadFile(filePath);

            if (ok == 0) {
                L.getGlobal("debug");
                L.getField(-1, "traceback");
                L.remove(-2);
                L.insert(-2);
                int l = args.length;
                for (Object arg : args) {
                    L.pushObjectValue(arg);
                }
                ok = L.pcall(l, 1, -2 - l);
                if (ok == 0) {
                    return L.toJavaObject(-1);
                }
            }
            Intent res = new Intent();
            res.putExtra(DATA, L.toString(-1));
            setResult(ok, res);
            throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
        } catch (LuaError e) {
            setTitle(errorReason(ok));
            setContentView(layout);
            sendMsg(e.getMessage());
            String s = e.getMessage();
            String p = "android.permission.";
            assert s != null;
            int i = s.indexOf(p);
            if (i > 0) {
                i = i + p.length();
                int n = s.indexOf(".", i);
                if (n > i) {
                    String m = s.substring(i, n);
                    L.getGlobal("require");
                    L.pushString("permission");
                    L.pcall(1, 0, 0);
                    L.getGlobal("permission_info");
                    L.getField(-1, m);
                    if (L.isString(-1))
                        m = m + " (" + L.toString(-1) + ")";
                    sendMsg("权限错误: " + m);
                    return null;
                }
            }
            if (isUpdata) {
                sendMsg("初始化错误，请清除数据后重新启动程序。。。");
            }

        }

        return null;
    }

    public Object doAsset(String name, Object... args) {
        int ok = 0;
        try {
            byte[] bytes = readAsset(name);
            L.setTop(0);
            ok = L.LloadBuffer(bytes, name);

            if (ok == 0) {
                L.getGlobal("debug");
                L.getField(-1, "traceback");
                L.remove(-2);
                L.insert(-2);
                int l = args.length;
                for (Object arg : args) {
                    L.pushObjectValue(arg);
                }
                ok = L.pcall(l, 0, -2 - l);
                if (ok == 0) {
                    return L.toJavaObject(-1);
                }
            }
            throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
        } catch (Exception e) {
            setTitle(errorReason(ok));
            setContentView(layout);
            sendMsg(e.getMessage());
        }

        return null;
    }

    //运行lua函数
    public Object runFunc(String funcName, Object... args) {
        if (L != null) {
            synchronized (L) {
                try {
                    L.setTop(0);
                    L.pushGlobalTable();
                    L.pushString(funcName);
                    L.rawGet(-2);
                    if (L.isFunction(-1)) {
                        L.getGlobal("debug");
                        L.getField(-1, "traceback");
                        L.remove(-2);
                        L.insert(-2);

                        int l = args.length;
                        for (Object arg : args) {
                            L.pushObjectValue(arg);
                        }

                        int ok = L.pcall(l, 1, -2 - l);
                        if (ok == 0) {
                            return L.toJavaObject(-1);
                        }
                        throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
                    }
                } catch (LuaError e) {
                    sendError(funcName, e);
                }
            }
        }
        return null;
    }

    //运行lua代码
    public Object doString(String funcSrc, Object... args) {
        try {
            L.setTop(0);
            int ok = L.LloadString(funcSrc);

            if (ok == 0) {
                L.getGlobal("debug");
                L.getField(-1, "traceback");
                L.remove(-2);
                L.insert(-2);

                int l = args.length;
                for (Object arg : args) {
                    L.pushObjectValue(arg);
                }

                ok = L.pcall(l, 1, -2 - l);
                if (ok == 0) {
                    return L.toJavaObject(-1);
                }
            }
            throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
        } catch (LuaError e) {
            sendMsg(e.getMessage());
        }
        return null;
    }


    //生成错误信息
    private String errorReason(int error) {
        return switch (error) {
            case 6 -> "错误";
            case 5 -> "垃圾回收错误";
            case 4 -> "内存溢出";
            case 3 -> "语法错误";
            case 2 -> "运行错误";
            case 1 -> "Yield 错误";
            default -> "未知错误 " + error;
        };
    }

    public byte[] readAsset(String name) throws IOException {
        AssetManager am = getAssets();
        InputStream is = am.open(name);
        byte[] ret = readAll(is);
        is.close();
        //am.close();
        return ret;
    }

    //复制asset文件到sd卡
    public void assetsToSD(String InFileName, String OutFileName) throws IOException {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(OutFileName);
        myInput = this.getAssets().open(InFileName);
        byte[] buffer = new byte[4096];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }

        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

    //显示信息
    public void sendMsg(String msg) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString(DATA, msg);
        message.setData(bundle);
        message.what = 0;
        handler.sendMessage(message);
        Log.i("lua", msg);
    }

    //显示错误信息
    @Override
    public void sendError(String title, Exception msg) {
        // 调用 runFunc 方法执行 onError 函数
        Object ret = runFunc("onError", title, msg);
        // 如果返回值为 true，则直接返回
        if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret) {
        }
        else
            // 否则，调用 sendMsg 方法发送错误消息
            sendMsg(title + ": " + msg.getMessage());
    }

    // 显示 toast
    @SuppressLint("ShowToast")
    public void showToast(String text) {
        long now = System.currentTimeMillis();
        // 如果 toast 为空或距离上次显示时间超过 1 秒，则创建新的 toast
        if (toast == null || now - lastShow > 1000) {
            toastbuilder.setLength(0);
            toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
            toastbuilder.append(text);
            toast.show();
        } else {
            // 否则，追加文本到现有的 toast 中
            toastbuilder.append("\n");
            toastbuilder.append(text);
            toast.setText(toastbuilder.toString());
            toast.setDuration(Toast.LENGTH_LONG);
        }
        lastShow = now;
    }

    private void setField(String key, Object value) {
        synchronized (L) {
            try {
                // 将 value 推入 Lua 栈中
                L.pushObjectValue(value);
                // 设置全局变量 key
                L.setGlobal(key);
            } catch (LuaError e) {
                // 发生异常时，调用 sendError 方法发送错误消息
                sendError("setField", e);
            }
        }
    }

    public void call(String func) {
        // 调用 push 方法，参数为 2 和 func
        push(2, func);
    }

    public void call(String func, Object[] args) {
        if (args.length == 0)
            // 如果 args 数组为空，则调用 push 方法，参数为 2 和 func
            push(2, func);
        else
            // 否则，调用 push 方法，参数为 3、func 和 args
            push(3, func, args);
    }

    public void set(String key, Object value) {
        // 调用 push 方法，参数为 1、key 和包含 value 的 Object 数组
        push(1, key, new Object[]{value});
    }

    public Object get(String key) throws LuaError {
        synchronized (L) {
            // 获取全局变量 key 并将其转换为 Java 对象
            L.getGlobal(key);
            return L.toJavaObject(-1);
        }
    }

    public void push(int what, String s) {
        // 创建 Message 对象
        Message message = new Message();
        // 创建 Bundle 对象并存入字符串参数
        Bundle bundle = new Bundle();
        bundle.putString(DATA, s);
        message.setData(bundle);
        // 设置 what 字段
        message.what = what;

        // 发送消息给 handler
        handler.sendMessage(message);
    }

    public void push(int what, String s, Object[] args) {
        // 创建 Message 对象
        Message message = new Message();
        // 创建 Bundle 对象并存入字符串参数和序列化的 args 数组
        Bundle bundle = new Bundle();
        bundle.putString(DATA, s);
        bundle.putSerializable("args", args);
        message.setData(bundle);
        // 设置 what 字段
        message.what = what;

        // 发送消息给 handler
        handler.sendMessage(message);
    }

    // 内部类 MainHandler 继承自 Handler
    @SuppressLint("HandlerLeak")
    public class MainHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0 -> {
                    // 处理 what 为 0 的情况
                    String data = msg.getData().getString(DATA);
                    if (mDebug)
                        // 如果 mDebug 为 true，则显示 toast
                        showToast(data);
                    // 将 data 添加到 status 和 adapter
                    status.append(data + "\n");
                    adapter.add(data);
                }
                case 1 -> {
                    // 处理 what 为 1 的情况
                    Bundle data = msg.getData();
                    // 获取字符串参数和 args 数组，并设置全局变量
                    setField(data.getString(DATA), ((Object[]) Objects.requireNonNull(data.getSerializable("args")))[0]);
                }
                case 2 -> {
                    // 处理 what 为 2 的情况
                    String src = msg.getData().getString(DATA);
                    // 执行 runFunc 方法
                    runFunc(src);
                }
                case 3 -> {
                    // 处理 what 为 3 的情况
                    String src = msg.getData().getString(DATA);
                    // 获取字符串参数和序列化的 args 数组，并执行 runFunc 方法
                    Serializable args = msg.getData().getSerializable("args");
                    runFunc(src, (Object[]) args);
                }
            }
        }
    }
}
