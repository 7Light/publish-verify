package com.huawei.publish.service;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Component
public class FileDownloadService {

    /**
     *
     * @param url      文件地址
     * @param dir      存储目录
     * @param fileName 存储文件名
     * @return
     */
    public void downloadHttpUrl(String url, String dir, String fileName) {
        try {
            File dirfile = new File(dir);
            if (!dirfile.exists()) {
                dirfile.mkdirs();
            }
            HttpClient client = new HttpClient();
            GetMethod getMethod = new GetMethod(url);
            client.executeMethod(getMethod);
            InputStream is = getMethod.getResponseBodyAsStream();

            int cache = 10 * 1024;
            FileOutputStream fileout = new FileOutputStream(dir + "/" + fileName);
            byte[] buffer = new byte[cache];
            int ch = 0;
            while ((ch = is.read(buffer)) != -1) {
                fileout.write(buffer, 0, ch);
            }
            is.close();
            fileout.flush();
            fileout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
