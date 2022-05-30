package com.huawei.publish.model;

import java.util.List;


public class PublishPO {
    private String gpgKeyUrl;
    private String keyFileName;
    private String rpmKey;
    private String fileKey;
    private String tempDir;
    List<FilePO> files;
    private RepoIndex repoIndex;
    public String getGpgKeyUrl() {
        return gpgKeyUrl;
    }

    public void setGpgKeyUrl(String gpgKeyUrl) {
        this.gpgKeyUrl = gpgKeyUrl;
    }

    public String getKeyFileName() {
        return keyFileName;
    }

    public void setKeyFileName(String keyFileName) {
        this.keyFileName = keyFileName;
    }

    public String getRpmKey() {
        return rpmKey;
    }

    public void setRpmKey(String rpmKey) {
        this.rpmKey = rpmKey;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public List<FilePO> getFiles() {
        return files;
    }

    public void setFiles(List<FilePO> files) {
        this.files = files;
    }

    public RepoIndex getRepoIndex() {
        return repoIndex;
    }

    public void setRepoIndex(RepoIndex repoIndex) {
        this.repoIndex = repoIndex;
    }
}
