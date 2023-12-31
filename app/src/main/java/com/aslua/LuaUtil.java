package com.aslua;

import static java.io.File.separator;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.HttpsURLConnection;

import dalvik.system.DexFile;

public class LuaUtil {
    /**
     * 截屏
     *
     */
    public static Bitmap captureScreen(Activity activity) {
// 获取屏幕大小：
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager WM = (WindowManager) activity
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = WM.getDefaultDisplay();
        display.getMetrics(metrics);
        int height = metrics.heightPixels; // 屏幕高
        int width = metrics.widthPixels; // 屏幕的宽
// 获取显示方式
        int pixelformat = display.getPixelFormat();
        PixelFormat localPixelFormat1 = new PixelFormat();
        PixelFormat.getPixelFormatInfo(pixelformat, localPixelFormat1);
        int deepth = localPixelFormat1.bytesPerPixel;// 位深
        byte[] piex = new byte[height * width * deepth];
        try {
            Runtime.getRuntime().exec(
                    new String[]{"/system/bin/su", "-c",
                            "chmod 777 /dev/graphics/fb0"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
// 获取fb0数据输入流
            InputStream stream = new FileInputStream("/dev/graphics/fb0");
            DataInputStream dStream = new DataInputStream(stream);
            dStream.readFully(piex);
        } catch (Exception e) {
            e.printStackTrace();
        }
// 保存图片
        int[] colors = new int[height * width];
        for (int m = 0; m < colors.length; m++) {
            int r = (piex[m * 4] & 0xFF);
            int g = (piex[m * 4 + 1] & 0xFF);
            int b = (piex[m * 4 + 2] & 0xFF);
            int a = (piex[m * 4 + 3] & 0xFF);
            colors[m] = (a << 24) + (r << 16) + (g << 8) + b;
        }
// piex生成Bitmap
        return Bitmap.createBitmap(colors, width, height,
                Bitmap.Config.ARGB_8888);
    }

    //读取asset文件

    public static byte[] readAsset(Context context, String name) throws IOException {
        AssetManager am = context.getAssets();
        InputStream is = am.open(name);
        byte[] ret = readAll(is);
        is.close();
        //am.close();
        return ret;
    }

    public static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[4096];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        byte[] ret = output.toByteArray();
        output.close();
        return ret;
    }

    //复制asset文件到sd卡
    public static void assetsToSD(Context context, String InFileName, String OutFileName) throws IOException {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(OutFileName);
        myInput = context.getAssets().open(InFileName);
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

    public static void copyFile(String from, String to) {
        try {
            copyFile(new FileInputStream(from), new FileOutputStream(to));
        } catch (IOException e) {
            Log.i("lua", Objects.requireNonNull(e.getMessage()));
        }
    }

    public static boolean copyFile(InputStream in, OutputStream out) {
        try {
            int byteread;
            byte[] buffer = new byte[4096];
            while ((byteread = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteread);
            }
        } catch (Exception e) {
            Log.i("lua", e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean copyDir(String from, String to) {
        return copyDir(new File(from), new File(to));
    }

    public static boolean copyDir(File from, File to) {
        boolean ret = true;
        File p = to.getParentFile();
        assert p != null;
        if (!p.exists())
            p.mkdirs();
        if (from.isDirectory()) {
            File[] fs = from.listFiles();
            if (fs != null && fs.length != 0) {
                for (File f : fs)
                    ret = copyDir(f, new File(to, f.getName()));
            } else {
                if (!to.exists())
                    ret = to.mkdirs();
            }
        } else {
            try {
                if (!to.exists())
                    to.createNewFile();
                ret = copyFile(new FileInputStream(from), new FileOutputStream(to));
            } catch (IOException e) {
                Log.i("lua", e.getMessage());
                ret = false;
            }
        }
        return ret;
    }

    public static boolean rmDir(File dir) {
        if (dir.isDirectory()) {
            File[] fs = dir.listFiles();
            assert fs != null;
            for (File f : fs)
                rmDir(f);
        }
        return dir.delete();
    }

    public static void rmDir(File dir, String ext) {
        if (dir.isDirectory()) {
            File[] fs = dir.listFiles();
            assert fs != null;
            for (File f : fs)
                rmDir(f, ext);
            dir.delete();
        }
        if (dir.getName().endsWith(ext))
            dir.delete();
    }

    public static byte[] readZip(String zippath, String filepath) throws IOException {
        ZipFile zip = new ZipFile(zippath);
        ZipEntry entey = zip.getEntry(filepath);
        InputStream is = zip.getInputStream(entey);
        return readAll(is);
    }

// 计算文件的 MD5 值

    public static String getFileMD5(String file) {
        return getFileMD5(new File(file));
    }

    public static String getFileMD5(File file) {
        try {
            return getFileMD5(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static String getFileMD5(InputStream in) {
        byte buffer[] = new byte[4096];
        int len;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // 计算文件的 SHA-1 值
    public static String getFileSha1(String file) {
        return getFileMD5(new File(file));
    }

    public static String getFileSha1(File file) {
        try {
            return getFileSha1(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static String getFileSha1(InputStream in) {
        byte buffer[] = new byte[4096];
        int len;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String[] getAllName(Context context, String path) {
        ArrayList<String> ret = new ArrayList<>();
        try {
            DexFile dex = new DexFile(context.getPackageCodePath());
            Enumeration<String> cls = dex.entries();
            while (cls.hasMoreElements()) {
                ret.add(cls.nextElement());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ZipFile zip = new ZipFile(path);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ret.add(entries.nextElement().getName().replaceAll("/", ".").replace(".class", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] arr = new String[ret.size()];
        ret.toArray(arr);
        return arr;
    }


    public static void unZip(String SourceDir) throws IOException {
        unZip(SourceDir, new File(SourceDir).getParent(), "");
    }

    public static void unZip(String SourceDir, boolean bool) throws IOException {
        if (!bool) {
            unZip(SourceDir);
            return;
        }
        String name = new File(SourceDir).getName();
        int i = name.lastIndexOf(".");
        if (i > 0) {
            name = name.substring(0, i);
        }
        i = name.indexOf("_");
        if (i > 0) {
            name = name.substring(0, i);
        }
        i = name.indexOf("(");
        if (i > 0) {
            name = name.substring(0, i);
        }
        unZip(SourceDir, new File(SourceDir).getParent() + separator + name, "");
    }

    public static void unZip(String SourceDir, String extDir) throws IOException {
        unZip(SourceDir, extDir, "");
    }

    public static void unZip(String SourceDir, String extDir, String fileExt) throws IOException {
        ZipFile zip = new ZipFile(SourceDir);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!name.startsWith(fileExt))
                continue;
            if (entry.isDirectory()) {
                File f = new File(extDir + separator + name);
                if (!f.exists())
                    f.mkdirs();
            } else {
                String fname = extDir + separator + name;
                File temp = new File(fname).getParentFile();
                assert temp != null;
                if (!temp.exists()) {
                    if (!temp.mkdirs()) {
                        throw new RuntimeException("create file " + temp.getName() + " fail");
                    }
                }

                FileOutputStream out = new FileOutputStream(extDir + separator + name);
                InputStream in = zip.getInputStream(entry);
                byte[] buf = new byte[4096];
                int count;
                while ((count = in.read(buf)) != -1) {
                    out.write(buf, 0, count);
                }
                out.close();
                in.close();
            }
        }
        zip.close();
    }


    private static final byte[] BUFFER = new byte[4096];

    public static boolean zip(String sourceFilePath) {
        return zip(sourceFilePath, new File(sourceFilePath).getParent());
    }

    public static boolean zip(String sourceFilePath, String zipFilePath) {
        File f = new File(sourceFilePath);
        return zip(sourceFilePath, zipFilePath, f.getName() + ".zip");
    }

    public static boolean zip(String sourceFilePath, String zipFilePath, String zipFileName) {
        boolean result = false;
        File source = new File(sourceFilePath);
        File zipFile = new File(zipFilePath, zipFileName);
        if (!Objects.requireNonNull(zipFile.getParentFile()).exists()) {
            if (!zipFile.getParentFile().mkdirs()) {
                return result;
            }
        }
        if (zipFile.exists()) {
            try {
                zipFile.createNewFile();
            } catch (IOException e) {
                return result;
            }
        }

        FileOutputStream dest;
        ZipOutputStream out = null;
        try {
            dest = new FileOutputStream(zipFile);
            CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            out = new ZipOutputStream(new BufferedOutputStream(checksum));
            //out.setMethod(ZipOutputStream.DEFLATED);
            compress(source, out, "");
            checksum.getChecksum().getValue();
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static void compress(File file, ZipOutputStream out, String mainFileName) {
        if (file.isFile()) {
            FileInputStream fi;
            BufferedInputStream origin = null;
            try {
                fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER.length);
                //int index=file.getAbsolutePath().indexOf(mainFileName);
                String entryName = mainFileName + file.getName();
                System.out.println(entryName);
                ZipEntry entry = new ZipEntry(entryName);
                out.putNextEntry(entry);
                //			byte[] data = new byte[BUFFER];
                int count;
                while ((count = origin.read(BUFFER, 0, BUFFER.length)) != -1) {
                    out.write(BUFFER, 0, count);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (origin != null) {
                    try {
                        origin.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (file.isDirectory()) {
            File[] fs = file.listFiles();
            if (fs != null && fs.length > 0) {
                for (File f : fs) {
                    if (f.isFile())
                        compress(f, out, mainFileName);
                    else
                        compress(f, out, mainFileName + f.getName() + "/");

                }
            }
        }
    }

    public static boolean unZipBase64(String source, String extDir) {
        ByteArrayInputStream zbuf = new ByteArrayInputStream(Base64.decode(source, Base64.NO_WRAP));
        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(zbuf));
        ZipEntry entry;
        try {
            while ((entry = zin.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory()) {
                    File f = new File(extDir + File.separator + name);
                    if (!f.exists())
                        f.mkdirs();
                } else {
                    String fname = extDir + File.separator + name;
                    File temp = new File(fname).getParentFile();
                    assert temp != null;
                    if (!temp.exists()) {
                        if (!temp.mkdirs()) {
                            throw new RuntimeException("create file " + fname + temp.getName() + " fail");
                        }
                    }
                    FileOutputStream out = new FileOutputStream(extDir + File.separator + name);
                    byte[] buf = new byte[8 * 1000];
                    int count;
                    while ((count = zin.read(buf)) != -1) {
                        out.write(buf, 0, count);
                    }
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String zipToBase64(String sourceFilePath) {
        String[] fs = new File(sourceFilePath).list();
        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        ZipOutputStream out = null;
        try {
            //CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            out.setLevel(9);
            assert fs != null;
            for (String s : fs) {
                compress(new File(sourceFilePath,s), out, "");
            }
            //checksum.getChecksum().getValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Base64.encodeToString(dest.toByteArray(), Base64.NO_WRAP);
    }

    public static boolean zip(String[] sourceFilePath, String zipFilePath, String zipFileName) {
        boolean result = false;
        //File source=new File(sourceFilePath);
        File zipFile = new File(zipFilePath, zipFileName);
        if (!Objects.requireNonNull(zipFile.getParentFile()).exists()) {
            if (!zipFile.getParentFile().mkdirs()) {
                return result;
            }
        }
        if (zipFile.exists()) {
            try {
                zipFile.createNewFile();
            } catch (IOException e) {
                return result;
            }
        }

        FileOutputStream dest;
        ZipOutputStream out = null;
        try {
            dest = new FileOutputStream(zipFile);
            CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            out = new ZipOutputStream(new BufferedOutputStream(checksum));
            //out.setMethod(ZipOutputStream.DEFLATED);
            for (String s : sourceFilePath) {
                compress(new File(s), out, "");
            }
            checksum.getChecksum().getValue();
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }



    public static final HashMap<String, String> mFileTypes = new HashMap<>();

    static {
        // images
        mFileTypes.put("FFD8FF", "jpg");
        mFileTypes.put("89504E47", "png");
        mFileTypes.put("47494638", "gif");
        mFileTypes.put("49492A00", "tif");
        mFileTypes.put("424D", "bmp");
        //other
        mFileTypes.put("41433130", "dwg"); // CAD
        mFileTypes.put("38425053", "psd");
        mFileTypes.put("7B5C727466", "rtf"); // 日记本
        mFileTypes.put("3C3F786D6C", "xml");
        mFileTypes.put("68746D6C3E", "html");
        mFileTypes.put("44656C69766572792D646174653A", "eml"); // 邮件
        mFileTypes.put("D0CF11E0", "doc");
        mFileTypes.put("5374616E64617264204A", "mdb");
        mFileTypes.put("252150532D41646F6265", "ps");
        mFileTypes.put("255044462D312E", "pdf");
        mFileTypes.put("504B0304", "docx");
        mFileTypes.put("52617221", "rar");
        mFileTypes.put("57415645", "wav");
        mFileTypes.put("41564920", "avi");
        mFileTypes.put("2E524D46", "rm");
        mFileTypes.put("000001BA", "mpg");
        mFileTypes.put("000001B3", "mpg");
        mFileTypes.put("6D6F6F76", "mov");
        mFileTypes.put("3026B2758E66CF11", "asf");
        mFileTypes.put("4D546864", "mid");
        mFileTypes.put("1F8B08", "gz");
    }

    public static String getFileType(String path) {
        try {
            return mFileTypes.get(getFileHeader(new FileInputStream(path)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "unknown";
    }


    public static String getFileType(File file) {
        try {
            return mFileTypes.get(getFileHeader(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    /**
     * 获取文件类型
     * ps:流会关闭
     *
     * @param inputStream
     * @return
     */
    public static String getFileType(InputStream inputStream) {
        return mFileTypes.get(getFileHeader(inputStream));
    }

    public static String getFileHeader(InputStream inputStream) {
        String value = null;
        try {
            byte[] b = new byte[4];
        /*int read() 从此输入流中读取一个数据字节。
        *int read(byte[] b) 从此输入流中将最多 b.length 个字节的数据读入一个 byte 数组中。
        * int read(byte[] b, int off, int len) 从此输入流中将最多 len 个字节的数据读入一个 byte 数组中。
        */
            inputStream.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }

    /**
     * 将要读取文件头信息的文件的byte数组转换成string类型表示
     *
     * @param src 要读取文件头信息的文件的byte数组
     * @return 文件头信息
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length == 0) {
            return null;
        }
        String hv;
        for (byte b : src) {
            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
            hv = Integer.toHexString(b & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }

    public BitmapDrawable toBlack(String path, float n, int h, int o) {
        Bitmap image = BitmapFactory.decodeFile(path);
        int width = image.getWidth();
        int height = image.getHeight();
        Bitmap imageRet = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        //int[][] colors = new int[width][height];
        int[] colors = new int[width * height];
        float[] vs = new float[width * height];
        float[] hsv = new float[3];
        float v = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color1 = image.getPixel(x, y);
                Color.colorToHSV(color1, hsv);
                vs[x + width * y] = hsv[2];
                v += hsv[2];
                //imageRet.setPixel(x,y, Color.rgb(v,v,v));
            }
        }
        float vv = v / (width * height) * n;
        int[][] color = new int[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = x + width * y;
                if (vs[i] > vv) {
                    colors[i] = -1;
                    color[x][y] = 1;
                } else {
                    colors[i] = 0xff000000;
                    color[x][y] = 0;
                }
            }
        }
        int ret;
        for (int x = width / 2; x < width - 10; x++) {
            for (int y = width / 3; y < width; y++) {
                if (check(x, y, color, h, o)) {
                    ret = x;
                    Log.i("find_color", String.valueOf(ret));
                    break;
                }
            }

        }
        return new BitmapDrawable(Bitmap.createBitmap(colors, width, height, Bitmap.Config.RGB_565));
    }

    private boolean check(int x, int y, int[][] color, int h, int o) {
        for (int i = 0; i < h; i++) {
            if (!(color[x][y + i] == 1 && color[x + o][y + i] == 0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 通过像素对比来计算偏差值
     *
     * @param path1 原图位置
     * @param path2 滑块图位置
     * @return 偏差值
     */
    public int getDifferenceValue(String path1, String path2) {
        int result = 0;
        File file = new File(path1);
        File file1 = new File(path2);
        try {
            Bitmap image = BitmapFactory.decodeFile(path1);
            Bitmap image1 = BitmapFactory.decodeFile(path2);

            int width = image.getWidth();
            int height = image.getHeight();
            int[][] colors = new int[width][height];
            for (int x = 1; x < width; x++) {
                for (int y = 1; y < height; y++) {
                    int color1 = image.getPixel(x, y);
                    int color2 = image1.getPixel(x, y);
                    if (color1 == color2) {
                        colors[x - 1][y - 1] = 0;
                    } else {
                        colors[x - 1][y - 1] = 1;
                    }
                }
            }
            int min = 999;
            int max = -1;
            for (int x = 0; x < colors.length; x++) {
                for (int y = 0; y < colors[x].length; y++) {
                    if (colors[x][y] == 1) {
                        colors[x][y] = checkPixel(x, y, colors);
                        if (colors[x][y] == 1) {
                            if (x > max) {
                                max = x;
                            } else if (x < min) {
                                min = x;
                            }
                        }
                    }
                }
            }
            result = (max + min) / 2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public int checkPixel(int x, int y, int[][] colors) {
        int result = colors[x][y];
        int num = 0;
        if ((y + 30) < colors[x].length) {
            for (int i = 1; i <= 30; i++) {
                int color = colors[x][y + i];
                if (color == 0) {
                    num += 1;
                }
            }
            if (num > 15) {
                return 0;
            }
        }
        return result;
    }

    public static Object makeRequest(String url){
        return makeRequest(url,null,null);
    }

    public static Object makeRequest(String url,Map<Object,Object> headers){
        return makeRequest(url,headers,null);
    }

    public static Object makeRequest(String url,String data){
        return makeRequest(url,null,data);
    }

    public static  Object makeRequest(String url,Map<Object,Object> headers,String data){
        if (headers == null) {
            headers = new HashMap<>();
        }
       return request(url, headers, data);
    }

    public static Object request(String url, Map<Object,Object> headers, String data) {
        InputStream in;
        try {
            Map<Object,Object> ret = new HashMap<>();
            URL urlUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlUrl.openConnection();
            for (Object le : headers.keySet()) {
                Object value = headers.get(le);
                if (value instanceof Map) {
                    addHeaders(le.toString(), (Map<Object,Object>) value, conn);
                } else {
                    assert value != null;
                    conn.addRequestProperty(le.toString(), value.toString());
                }
            }

            if (data != null) {
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                int len = data.length();
                if (conn.getRequestProperty("Content-Length") == null) {
                    conn.addRequestProperty("Content-Length", Integer.toString(len));
                }
                if (conn.getRequestProperty("Content-Type") == null) {
                    conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                }
                conn.connect();

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes(), 0, len);
                data = null;
                os.flush();
            } else {
                conn.connect();
            }
            ret.put("url", url);
            ret.put("requestMethod", conn.getRequestMethod());
            ret.put("code", conn.getResponseCode());
            ret.put("message", conn.getResponseMessage());
            ret.put("headers", getHeaders(conn));
            ret.put("contentEncoding", conn.getContentEncoding());
            ret.put("contentLength", conn.getContentLength());
            ret.put("contentType", conn.getContentType());
            ret.put("date", conn.getDate());
            ret.put("expiration", conn.getExpiration());
            ret.put("lastModified", conn.getLastModified());
            ret.put("usingProxy", conn.usingProxy());
            if (conn instanceof HttpsURLConnection) {
                ret.put("cipherSuite", ((HttpsURLConnection) conn).getCipherSuite());
            }
            try {
                in = conn.getInputStream();
            } catch (IOException e) {
                ret.put("error", true);
                in = conn.getErrorStream();
            }
            InputStream buff = new BufferedInputStream(in);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            while (true) {
                int length = buff.read(buffer);
                if (length == -1) {
                    ret.put("content", baos.toString());
                    conn.disconnect();
                    return ret;
                }
                baos.write(buffer, 0, length);
            }
        } catch (Throwable e2) {
            return "makeRequest fail for '" + url + "'; data = " + data + "; headers = " + headers+"|"+e2;
        }
    }

    private static void addHeaders(String name, Map<Object,Object> list, HttpURLConnection conn) {
        for (Object le : list.keySet()) {
            Object value = list.get(le);
            conn.addRequestProperty(name, String.valueOf(value));
        }
    }

    private static Map<Object,Object> getHeaders(HttpURLConnection conn) {
        Map<Object,Object> headers = new HashMap<>();
        Map<String, List<String>> map = conn.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                key = "null";
            }
            List<String> headerValues = entry.getValue();
            Map<Object,Object> values = new HashMap<>();
            int i = 1;
            for (String value : headerValues) {
                values.put(i, value);
                i++;
            }
            headers.put(key, values);
        }
        return headers;
    }

}
