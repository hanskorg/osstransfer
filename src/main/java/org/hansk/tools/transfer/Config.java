package org.hansk.tools.transfer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by guohao on 2018/5/17.
 */
@Component
@ConfigurationProperties(prefix="oss")
public class Config {
    @Value("${oss.key}")
    private String ossKey;
    @Value("${oss.secret}")
    private String ossSecret;
    @Value("${oss.end_point}")
    private String endPoint;
    @Value("${oss.timeout}")
    private int timeout;
    @Value("${qiniu.access_key}")
    private String qiniuAccess;
    @Value("${qiniu.secret_key}")
    private String qiniuSecret;
    @Value("${max_download}")
    private int maxDownloadThread;
    @Value("${core_download}")
    private int coreDownloadThread;

    private List<String> buckets;

    private Status status = Status.STARTING;

    public enum Status{
        STARTING,
        RUNNING,
        SHUTTING,
        STOP,;
    };



    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getOssKey() {
        return ossKey;
    }

    public void setOssKey(String ossKey) {
        this.ossKey = ossKey;
    }

    public String getOssSecret() {
        return ossSecret;
    }

    public void setOssSecret(String ossSecret) {
        this.ossSecret = ossSecret;
    }

    public String getQiniuAccess() {
        return qiniuAccess;
    }

    public void setQiniuAccess(String qiniuAccess) {
        this.qiniuAccess = qiniuAccess;
    }

    public String getQiniuSecret() {
        return qiniuSecret;
    }

    public void setQiniuSecret(String qiniuSecret) {
        this.qiniuSecret = qiniuSecret;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxDownloadThread() {
        return maxDownloadThread;
    }

    public void setMaxDownloadThread(int maxDownloadThread) {
        this.maxDownloadThread = maxDownloadThread;
    }

    public int getCoreDownloadThread() {
        return coreDownloadThread;
    }

    public void setCoreDownloadThread(int coreDownloadThread) {
        this.coreDownloadThread = coreDownloadThread;
    }

    public List<String> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<String> buckets) {
        this.buckets = buckets;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
