package org.hansk.tools.transfer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by guohao on 2018/5/17.
 */
@Configuration
@ConfigurationProperties(prefix="transfer")
public class Config {
    @Value("${transfer.oss.key}")
    private String ossKey;
    @Value("${transfer.oss.secret}")
    private String ossSecret;
    @Value("${transfer.oss.end_point}")
    private String ossEndPoint;
    @Value("${transfer.oss.timeout}")
    private int ossTimeout;
    @Value("${transfer.oss.access_domain}")
    private String ossAccessDomain;


    @Value("${transfer.cos.secret_id}")
    private String cosSecretID;
    @Value("${transfer.cos.secret_key}")
    private String cosSecretKey;
    @Value("${transfer.cos.app_id}")
    private String cosAppID;
    @Value("${transfer.cos.region}")
    private String cosRegion;

    @Value("${transfer.qiniu.access_key}")
    private String qiniuAccess;
    @Value("${transfer.qiniu.secret_key}")
    private String qiniuSecret;


    @Value("${transfer.max_trans_thread}")
    private int maxDownloadThread;
    @Value("${transfer.min_trans_thread}")
    private int coreDownloadThread;
    @Value("${transfer.max_check_thread}")
    private int maxCheckThread;

    private Date transferBefore;
    //复制目标
    private List<String> target;
    //@Value("${transfer.oss.buckets}")，不支持复杂结构注入，配置中此项提到根
    private List<Bucket> buckets;

    public static class Bucket{
        private String originStorage;
        private List<String> prefix = new ArrayList<>();
        private String originBucket;
        private String originRegion;
        private String originEndPoint;
        private String originCDNDomain;

        private String targetStorage;
        private String targetBucket;
        private String targetRegion;
        private String targetEndPoint;
        private String targetCDNDomain;


        public String getOriginStorage() {
            return originStorage;
        }

        public void setOriginStorage(String originStorage) {
            this.originStorage = originStorage;
        }

        public List<String> getPrefix() {
            return prefix;
        }

        public void setPrefix(List<String> prefix) {
            this.prefix = prefix;
        }

        public String getOriginBucket() {
            return originBucket;
        }

        public void setOriginBucket(String originBucket) {
            this.originBucket = originBucket;
        }

        public String getTargetStorage() {
            return targetStorage;
        }

        public void setTargetStorage(String targetStorage) {
            this.targetStorage = targetStorage;
        }

        public String getTargetBucket() {
            return targetBucket == null || targetBucket.equals("") ? originBucket : targetBucket;
        }

        public void setTargetBucket(String targetBucket) {
            this.targetBucket = targetBucket;
        }

        public String getOriginRegion() {
            return originRegion;
        }

        public void setOriginRegion(String originRegion) {
            this.originRegion = originRegion;
        }

        public String getOriginEndPoint() {
            return originEndPoint;
        }

        public void setOriginEndPoint(String originEndPoint) {
            this.originEndPoint = originEndPoint;
        }

        public String getTargetRegion() {
            return targetRegion;
        }

        public void setTargetRegion(String targetRegion) {
            this.targetRegion = targetRegion;
        }

        public String getTargetEndPoint() {
            return targetEndPoint;
        }

        public void setTargetEndPoint(String targetEndPoint) {
            this.targetEndPoint = targetEndPoint;
        }

        public String getOriginCDNDomain() {
            return originCDNDomain;
        }

        public void setOriginCDNDomain(String originCDNDomain) {
            this.originCDNDomain = originCDNDomain;
        }

        public String getTargetCDNDomain() {
            return targetCDNDomain;
        }

        public void setTargetCDNDomain(String targetCDNDomain) {
            this.targetCDNDomain = targetCDNDomain;
        }
    }
    private Status status = Status.STARTING;

    public enum Status{
        STARTING,
        RUNNING,
        SHUTTING,
        STOP,;
    };

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

    public String getOssEndPoint() {
        return ossEndPoint;
    }

    public void setOssEndPoint(String ossEndPoint) {
        this.ossEndPoint = ossEndPoint;
    }

    public int getOssTimeout() {
        return ossTimeout;
    }

    public void setOssTimeout(int ossTimeout) {
        this.ossTimeout = ossTimeout;
    }


    public String getCosSecretID() {
        return cosSecretID;
    }

    public void setCosSecretID(String cosSecretID) {
        this.cosSecretID = cosSecretID;
    }

    public String getCosSecretKey() {
        return cosSecretKey;
    }

    public void setCosSecretKey(String cosSecretKey) {
        this.cosSecretKey = cosSecretKey;
    }

    public String getCosAppID() {
        return cosAppID;
    }

    public void setCosAppID(String cosAppID) {
        this.cosAppID = cosAppID;
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

    public Status getStatus() {
        return status;
    }

    public Date getTransferBefore() {
        return transferBefore;
    }

    public void setTransferBefore(String transferBefore) {
        try {
            Date dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).parse(transferBefore);
            this.transferBefore = dateTime;
        } catch (ParseException e) {
            this.transferBefore = new Date();
        }
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCosRegion() {
        return cosRegion;
    }

    public void setCosRegion(String cosRegion) {
        this.cosRegion = cosRegion;
    }

    public List<String> getTarget() {
        return target;
    }

    public void setTarget(List<String> target) {
        this.target = target;
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<Bucket> buckets) {
        this.buckets = buckets;
    }

    public String getCosRegion(String bucketName) {
        for (Bucket bucket : buckets){
            if(bucket.getOriginBucket().equals(bucketName) && bucket.getOriginRegion() != null){
                return bucket.getOriginRegion();
            }
            if(bucket.getTargetBucket().equals(bucketName) && bucket.getTargetRegion() != null){
                return bucket.getTargetRegion();
            }
        }
        return this.getCosRegion();
    }

    public String getOssEndPoint(String bucketName) {
        for (Bucket bucket : buckets){
            if(bucket.getOriginBucket().equals(bucketName) && bucket.getOriginEndPoint() != null){
                return bucket.getOriginEndPoint();
            }
            if(bucket.getTargetBucket().equals(bucketName) && bucket.getTargetEndPoint() != null){
                return bucket.getTargetEndPoint();
            }
        }
        return this.getOssEndPoint();
    }

    public String getOssAccessDomain() {
        return ossAccessDomain;
    }

    public void setOssAccessDomain(String ossAccessDomain) {
        this.ossAccessDomain = ossAccessDomain;
    }

    public int getMaxCheckThread() {
        return maxCheckThread;
    }

    public void setMaxCheckThread(int maxCheckThread) {
        this.maxCheckThread = maxCheckThread;
    }

    @Override
    public String toString() {
        return "Config{" +
                "ossKey='" + ossKey + '\'' +
                ", ossSecret='" + ossSecret + '\'' +
                ", ossEndPoint='" + ossEndPoint + '\'' +
                ", ossTimeout=" + ossTimeout +
                ", ossAccessDomain='" + ossAccessDomain + '\'' +
                ", cosSecretID='" + cosSecretID + '\'' +
                ", cosSecretKey='" + cosSecretKey + '\'' +
                ", cosAppID='" + cosAppID + '\'' +
                ", cosRegion='" + cosRegion + '\'' +
                ", qiniuAccess='" + qiniuAccess + '\'' +
                ", qiniuSecret='" + qiniuSecret + '\'' +
                ", maxDownloadThread=" + maxDownloadThread +
                ", coreDownloadThread=" + coreDownloadThread +
                ", maxCheckThread=" + maxCheckThread +
                ", transferBefore=" + transferBefore +
                ", target=" + target +
                ", buckets=" + buckets +
                ", status=" + status +
                '}';
    }
}
