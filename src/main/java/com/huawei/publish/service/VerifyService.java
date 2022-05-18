package com.huawei.publish.service;

import com.huawei.publish.model.PublishPO;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class VerifyService {
    private String gpgKeyUrl;
    private String keyFileName;
    private String rpmKey;
    private String fileKey;

    public VerifyService(String gpgKeyUrl, String keyFileName, String rpmKey, String fileKey) {
        this.gpgKeyUrl = gpgKeyUrl;
        this.keyFileName = keyFileName;
        this.rpmKey = rpmKey;
        this.fileKey = fileKey;
    }

    public VerifyService(PublishPO publishPO) {
        this.gpgKeyUrl = publishPO.getGpgKeyUrl();
        this.keyFileName = publishPO.getKeyFileName();
        this.rpmKey = publishPO.getRpmKey();
        this.fileKey = publishPO.getFileKey();
    }
//    B4D3CB9B1F2AE4B930DDF04D2B15B06184D0E37B
//    public static final String KEY_URL = "http://mirrors.163.com/centos/7/os/x86_64/RPM-GPG-KEY-CentOS-7";
//    https://repo.openeuler.org/openEuler-22.03-LTS/source/RPM-GPG-KEY-openEuler
//    public static final String KEY_FILE = "RPM-GPG-KEY-CentOS-7";
//    public static final String RPM_KEY = "gpg-pubkey-f4a80eb5-53a7ff4b";

    public String execCmd(String cmd) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process exec = runtime.exec(cmd);
        exec.waitFor();
        String output = getExecOutput(exec);
        // TODO log.debug
//        log.info(cmd + ":" + output);
        return output;
    }

    public boolean rpmVerify(String filePath) {
        try {
            if (!execCmd("rpm -q gpg-pubkey-*").contains(rpmKey)) {
                execCmd("wget " + gpgKeyUrl);
                execCmd("rpm --import " + keyFileName);
            }
            return execCmd("rpm -K " + filePath).contains("digests signatures OK");
        } catch (Exception e) {
//            log.error(e.getMessage());
        }
        return false;
    }

    public boolean checksum256Verify(String filePath, String sha256) {
        try {
            return execCmd("sha256sum " + filePath).contains(sha256);
        } catch (Exception e) {
//            log.error(e.getMessage());
        }
        return false;
    }

    public boolean fileVerify(String filePath) {
        try {
            if (!execCmd("gpg -k | grep " + fileKey).contains(fileKey)) {
                execCmd("wget " + gpgKeyUrl);
                execCmd("gpg --import " + keyFileName);
            }
            return execCmd("gpg --verify " + filePath).contains("Primary key fingerprint");
        } catch (Exception e) {
//            log.error("rpm verify error,file:{}, error:{}", filePath, e.getMessage());
        }
        return false;
    }

    private String getExecOutput(Process exec) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            while ((line = bufferedReader2.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
//            log.error(e.getMessage());
        }
        return sb + "";
    }
}
