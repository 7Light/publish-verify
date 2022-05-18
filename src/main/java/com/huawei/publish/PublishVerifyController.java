package com.huawei.publish;

import com.huawei.publish.model.FilePO;
import com.huawei.publish.model.PublishPO;
import com.huawei.publish.service.FileDownloadService;
import com.huawei.publish.service.VerifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping(path = "/", produces = {"application/json"}, consumes = {"application/json"})
@RestController
public class PublishVerifyController {

    @Autowired
    private FileDownloadService fileDownloadService;
    private VerifyService verifyService;

    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    public Map<String, Object> publish(@RequestBody PublishPO publishPO) {
        Map<String, Object> result = new HashMap<>();
        String validate = validate(publishPO);
        if (!StringUtils.isEmpty(validate)) {
            result.put("result", "fail");
            result.put("message", "Validate failed, " + validate);
            return result;
        }
        verifyService = new VerifyService(publishPO);
        List<FilePO> files = publishPO.getFiles();
        String tempDirPath = publishPO.getTempDir();
        String publishDirPath = publishPO.getPublishDir();
        try {
            File tempDir = new File(tempDirPath);
            if (!tempDir.exists()) {
                verifyService.execCmd("mkdir " + tempDirPath);
            }
            for (FilePO file : files) {
                String fileName = file.getName();
                fileDownloadService.downloadHttpUrl(file.getUrl(), tempDirPath, fileName);
                String verifyMessage = verify(tempDirPath, file, fileName);
                if (!StringUtils.isEmpty(verifyMessage)) {
                    result.put("result", "fail");
                    result.put("message", verifyMessage);
                    return result;
                }
            }
            File publishDir = new File(publishDirPath);
            if (!publishDir.exists()) {
                verifyService.execCmd("mkdir " + publishDirPath);
            }
            for (FilePO file : files) {
                String fileName = file.getName();
                verifyService.execCmd("mv " + tempDirPath + fileName + " " + publishDirPath + fileName);
            }
            verifyService.execCmd("rm -rf " + tempDirPath);
        } catch (IOException | InterruptedException e) {
            result.put("result", "fail");
            result.put("message", "publish failed, " + e.getMessage());
            return result;
        }
        result.put("result", "success");
        return result;
    }

    private String verify(String tempDirPath, FilePO file, String fileName) {
        if (!StringUtils.isEmpty(file.getSha256())) {
            if (!verifyService.checksum256Verify(tempDirPath + fileName, file.getSha256())) {
                return fileName + " checksum check failed.";
            }
        }
        if (fileName.endsWith(".sha256sum")) {
            if (!verifyService.fileVerify(tempDirPath + fileName)) {
                return fileName + " digests signatures not OK.";
            }
        }
        if (fileName.endsWith(".rpm")) {
            if (!verifyService.rpmVerify(tempDirPath + fileName)) {
                return fileName + " digests signatures not OK.";
            }
        }
        return "";
    }

    private String validate(PublishPO publishPO) {
        if (StringUtils.isEmpty(publishPO.getTempDir()) || StringUtils.isEmpty(publishPO.getPublishDir())) {
            return "publish dir cannot be blank.";
        }
        if (StringUtils.isEmpty(publishPO.getGpgKeyUrl())) {
            return "key url cannot be blank.";
        }

        if (CollectionUtils.isEmpty(publishPO.getFiles())) {
            return "files cannot be empty.";
        }

        for (FilePO file : publishPO.getFiles()) {
            File targetFile = new File(publishPO.getPublishDir() + file.getName());
            if (targetFile.exists()) {
                return file.getName() + " already published.";
            }
        }
        return "";
    }
}
