package org.hansk.tools.transfer.storage;

import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.hansk.tools.transfer.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;

/**
 * 腾讯云存储客户端
 * @author guohao
 * @date 2018/10/16
 */
@Component("COS")
public class COSClient implements IStorage {

    private Config config;

    private com.qcloud.cos.COSClient cosClient;
    @Autowired
    public COSClient(Config config){
        this.config = config;
        COSCredentials cred = new BasicCOSCredentials(config.getCosSecretID(), config.getCosSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(config.getCosRegion()));
        cosClient = new com.qcloud.cos.COSClient(cred, clientConfig);
    }
    @Override
    public boolean putObject(InputStream objStream, String bucket, String key, long objectSize, String contentMD5, HashMap<String, String> metaData) throws Exception {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(objectSize);
        if(objectSize > 200 * 1024 * 1024 ){
            ObjectMetadata metadata =  cosClient.getObjectMetadata(bucket,key);
            if(metadata != null && metadata.getContentLength() == objectSize) {
                throw  new Exception("big size object exits");
            }
        }
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key,
                objStream,
                objectMetadata
        );
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        return true;
    }

    @Override
    public StorageObject getObject(String bucket, String objKey) throws Exception {
        StorageObject object = new StorageObject();
        COSObject cosObject = cosClient.getObject(bucket, objKey);
        object.content = cosObject.getObjectContent();
        object.setMetadata(new HashMap<>());
        object.getMetadata().put("Content-MD5",cosObject.getObjectMetadata().getContentMD5()) ;
        object.getMetadata().put("Content-Type",cosObject.getObjectMetadata().getContentType());
        object.getMetadata().put("Expires",cosObject.getObjectMetadata().getExpirationTime());
        object.getMetadata().put("Content-Length",cosObject.getObjectMetadata().getContentLength());
        object.getMetadata().put("Last-Modified",cosObject.getObjectMetadata().getLastModified());

        return object;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
