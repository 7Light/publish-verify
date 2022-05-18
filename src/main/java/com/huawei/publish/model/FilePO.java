package com.huawei.publish.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilePO {
    private String name;
    private String size;
    private String Date;
    private String parentDir = "";
    private String url;
    private String sha256;
}
