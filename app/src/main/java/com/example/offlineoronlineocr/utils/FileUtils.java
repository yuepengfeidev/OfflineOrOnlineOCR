package com.example.offlineoronlineocr.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * FileUtils class
 *
 * @author yuepengfei
 * @date 2019/5/25
 * @description 文件处理工具
 */
public class FileUtils {
    /**
     * 检查文件
     *
     * @param dir      存放资源的文件
     * @param dataPath 字体库文件路径
     * @param lang     字体库 语种
     * @param context  context
     * @return 是否完成检查
     */
    public static boolean checkFile(File dir, String dataPath, String lang, Context context) {
        // 如果没有该文件则创建，然后复制
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles(dataPath, lang, context);
        }
        if (dir.exists()) {
            String dataFilePath = dataPath + "/tessdata/" + lang + ".traineddata";
            File dataFile = new File(dataFilePath);
            if (!dataFile.exists()) {
                copyFiles(dataPath, lang, context);
            }
        }
        return true;
    }

    /**
     * 将assets中的字体库 复制到 tess-two 指定读取的文件
     *
     * @param dataPath 字体库文件路径
     * @param lang     字体库 语种
     * @param context  context
     */
    private static void copyFiles(String dataPath, String lang, Context context) {
        AssetManager assetManager = context.getAssets();

        InputStream inputStream;
        OutputStream outputStream;
        String[] langArray = lang.split("\\+");

        for (String l : langArray) {
            try {
                String fileName = "tessdata/" + l + ".traineddata";
                // 打开 assets 中的资源
                inputStream = assetManager.open(fileName);
                String destFile = dataPath + "/" + fileName;
                outputStream = new FileOutputStream(destFile);

                // 读取复制
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                inputStream.close();
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 当系统大于24时，采用该种删除文件的方法
     *
     * @param context context
     * @param uri     文件uri
     */
    public static void deleteFile(Context context, Uri uri) {
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        assert data != null;
        File file = new File(data);
        // 删除文件
        if (!(file.exists() && file.delete())) {
            Log.d("FileUtils", "deleteFile: " + "无法删除文件！");
        }
    }
}
