package com.huawei.publish;

import com.huawei.publish.model.FilePO;
import com.huawei.publish.model.PublishPO;
import com.huawei.publish.model.RepoIndex;
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

@RequestMapping(path = "/publish")
@RestController
public class PublishVerifyController {
    @Autowired
    private FileDownloadService fileDownloadService;
    private VerifyService verifyService;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        return result;
    }

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
                File targetPathDir = new File(file.getTargetPath());
                if (!targetPathDir.exists()) {
                    targetPathDir.mkdirs();
                }
                verifyService.execCmd("mv " + tempDirPath + "/" + fileName + " " + file.getTargetPath() + "/" + fileName);
            }
            verifyService.execCmd("rm -rf " + tempDirPath);

            if (!CollectionUtils.isEmpty(publishPO.getRepoIndexList())) {
                for (RepoIndex repoIndex : publishPO.getRepoIndexList()) {
                    if (repoIndex != null) {
                        if ("createrepo".equals(repoIndex.getIndexType())) {
                            verifyService.execCmd("createrepo -d " + repoIndex.getRepoPath());
                        }
                    }
                }
            }
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
        if ("asc".equals(file.getVerifyType())) {
            if (!verifyService.fileVerify(tempDirPath + fileName)) {
                return fileName + " digests signatures not OK.";
            }
        }
        if ("rpm".equals(file.getVerifyType())) {
            if (!verifyService.rpmVerify(tempDirPath + fileName)) {
                return fileName + " digests signatures not OK.";
            }
        }
        return "";
    }

    private String validate(PublishPO publishPO) {
        if (StringUtils.isEmpty(publishPO.getGpgKeyUrl())) {
            return "key url cannot be blank.";
        }

        if (CollectionUtils.isEmpty(publishPO.getFiles())) {
            return "files cannot be empty.";
        }

        for (FilePO file : publishPO.getFiles()) {
            if (StringUtils.isEmpty(file.getTargetPath())) {
                return "file target path can not be empty.";
            }
            File targetFile = new File(file.getTargetPath() + "/" + file.getName());
            if (targetFile.exists()) {
                return file.getName() + " already published.";
            }
        }
        return "";
    }
}
