package com.huawei.publish.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PublishPO {
    private String gpgKeyUrl;
    private String keyFileName;
    private String rpmKey;
    private String fileKey;
    private String tempDir;
    private String publishDir;
    List<FilePO> files;
}
