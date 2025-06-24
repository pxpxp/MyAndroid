package com.example.myandroid.util;

import android.app.Activity;
import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author pxp
 * @description
 */
public class GlideUtil {

    public void deleteCacheFileByUrl(Context context, String url) {
        File cacheDir = Glide.getPhotoCacheDir(context);
        if (cacheDir!= null && cacheDir.isDirectory()) {
            File[] files = cacheDir.listFiles();
            if (files!= null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // 处理子目录
                        deleteCacheFileInSubdirectory(file, url);
                    } else {
                        // 根据文件名判断是否是要删除的文件
                        if (file.getName().contains(url)) {
                            file.delete();
                        }
                    }
                }
            }
        }
    }

    private void deleteCacheFileInSubdirectory(File directory, String url) {
        File[] subFiles = directory.listFiles();
        if (subFiles!= null) {
            for (File file : subFiles) {
                if (file.getName().contains(url)) {
                    file.delete();
                }
            }
        }
    }

//    public void deleteCacheFileByUrl(Activity context, String url) {
//        DiskCache.Factory diskCacheFactory = Glide.get(context).getDiskCacheFactory();
//        if (diskCacheFactory instanceof DiskLruCacheFactory) {
//            DiskLruCacheFactory lruCacheFactory = (DiskLruCacheFactory) diskCacheFactory;
//            DiskLruCache diskLruCache = lruCacheFactory.getDiskCache();
//
//            Key cacheKey = new OriginalKey(url, EmptySignature.obfuscatedKey());
//            try {
//                diskLruCache.remove(cacheKey);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
