package org.hansk.tools.transfer.storage;

import com.aliyun.oss.common.utils.IOUtils;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.BucketInfo;
import com.qiniu.util.Auth;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.util.Strings;
import org.hansk.tools.transfer.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 七牛存储客户端
 * @author guohao
 * @date 2018/10/16
 */
@Component("QINIU")
public class QiniuClient implements IStorage {

    private BucketManager bucketManager;
    private Auth auth;
    private UploadManager uploadManager;

    private Config config;
    private Map<String, String[]> domains;
    private Map<String, BucketInfo> bucketsInfo;
    @Autowired
    public QiniuClient(Config config){

        this.config = config;
        this.domains = new HashMap<>();
        this.bucketsInfo = new HashMap<>();
        auth = Auth.create(config.getQiniuAccess(), config.getQiniuSecret());
        Configuration cfg = new Configuration(Zone.zone1());
        uploadManager = new UploadManager(cfg);
        bucketManager = new BucketManager(auth,cfg);
    }
    @Override
    public boolean putObject(InputStream objStream, String bucket, String key, long objectSize, String contentMD5, HashMap<String, String> metaData) throws Exception {
        String upToken = auth.uploadToken(bucket, key);
        Response resp = uploadManager.put(IOUtils.readStreamAsByteArray(objStream), key, upToken);
        return resp.isOK();
    }

    @Override
    public StorageObject getObject(String bucket, String objKey) throws Exception {
        StorageObject object = new StorageObject();
        String domain = "";
        if(!domains.containsKey(bucket)){
            String[] domainList = bucketManager.domainList(bucket);
            domains.put(bucket, domainList);
        }
        if(!bucketsInfo.containsKey(bucket)){
            bucketsInfo.put(bucket, bucketManager.getBucketInfo(bucket));
        }
        BucketInfo bucketInfo = bucketsInfo.get(bucket);
        String[] bucketDomains =  domains.get(bucket);
        String fileUrl = "http://"+ bucketDomains[1] + "/" + objKey;

        if (bucketInfo.getPrivate() == 1){
            fileUrl = auth.privateDownloadUrl(fileUrl);
        }
        URL url = new URL(fileUrl);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setConnectTimeout(0);
        urlConnection.setReadTimeout(0);
        object.content = urlConnection.getInputStream();
        object.setMetadata(new HashMap<>());
        return object;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    private String specialSign(String objectKey){
        String key = "ac9b46abb8883e98aeb8e998b588129a1ea989a9";
        Integer timeStamp = (int)(System.currentTimeMillis() / 1000);
        String timestampHex = Integer.toHexString(timeStamp);
        String newObjectKey = objectKey;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(("/" + key + objectKey + timestampHex).getBytes());
            byte[] secretBytes = md.digest();
            String encodeStr = new BigInteger(1, secretBytes).toString(16);
            for (int i = 0; i < 32 - encodeStr.length(); i++) {
                encodeStr = "0" + encodeStr;
            }
            newObjectKey = String.format("%s?sign=%s&t=%s", objectKey, Strings.toLowerCase(encodeStr), timestampHex);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return newObjectKey;

    }
}
