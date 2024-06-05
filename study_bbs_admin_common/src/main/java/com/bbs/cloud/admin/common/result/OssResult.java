package com.bbs.cloud.admin.common.result;

/**
 * 云存储：例如上传图片可能会返回url、名字两种样子
 * OSS结果
 */
public class OssResult {

    private String viewUrl;

    private String fileName;

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
