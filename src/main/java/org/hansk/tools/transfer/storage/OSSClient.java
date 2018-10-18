package org.hansk.tools.transfer.storage;

import com.aliyun.oss.model.*;
import org.hansk.tools.transfer.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

/**
 * 阿里云存储客户端
 * @author guohao
 * @date 2018/10/16
 */
@Component("OSS")
public class OSSClient implements IStorage {

    private Config config;

    private com.aliyun.oss.OSSClient ossClient;
    @Autowired
    public OSSClient(Config config){
        this.config = config;
        ossClient = new com.aliyun.oss.OSSClient(config.getOssEndPoint(), config.getOssKey(), config.getOssSecret());
    }
    @Override
    public boolean putObject(InputStream objStream, String bucket, String key, long objectSize, String contentMD5, HashMap<String, String> metaData) throws Exception {
        boolean isSuccess = false;
        //200M以下简单传输
        if(objectSize < 200 * 1024 * 1024L){
            com.aliyun.oss.model.PutObjectResult result = ossClient.putObject( bucket, key, objStream);
            isSuccess = result.getResponse() != null && result.getResponse().isSuccessful();
        }else{
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucket, key);
            InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(request);
            String uploadId = result.getUploadId();
            List<PartETag> partETags =  new ArrayList<PartETag>();
            // 1MB
            final long partSize = 1 * 1024 * 1024L;
            int partCount = (int) (objectSize / partSize);
            if (objectSize % partSize != 0) {
                partCount++;
            }
            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == partCount) ? (objectSize - startPos) : partSize;
                // 跳过已经上传的分片。
                byte[] partBytes = new byte[(int) curPartSize];
                objStream.read(partBytes);
                InputStream partInputStream = new ByteArrayInputStream(partBytes);
//               objStream.skip(startPos);

                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucket);
                uploadPartRequest.setKey(key);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(partInputStream);

                // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100KB。
                uploadPartRequest.setPartSize(curPartSize);
                // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
                uploadPartRequest.setPartNumber( i + 1);
                // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);

                // 每次上传分片之后，OSS的返回结果会包含一个PartETag。PartETag将被保存到partETags中。
                partETags.add(uploadPartResult.getPartETag());
            }
            Collections.sort(partETags, new Comparator<PartETag>() {
                @Override
                public int compare(PartETag p1, PartETag p2) {
                    return p1.getPartNumber() - p2.getPartNumber();
                }
            });
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(bucket, key, uploadId, partETags);
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            if(completeMultipartUploadResult.getLocation() !=null && !completeMultipartUploadResult.getLocation().isEmpty()){
                isSuccess = true;
            }
        }

        return isSuccess;
    }

    // 首先是将流缓存到byteArrayOutputStream中
    public void inputStreamCacher(ByteArrayOutputStream byteArrayOutputStream, InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > -1 ) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
    }

    @Override
    public StorageObject getObject(String bucket, String objKey) throws Exception {
        StorageObject object = new StorageObject();
        OSSObject ossObject = ossClient.getObject(new GetObjectRequest(bucket, objKey));
        object.content = ossObject.getObjectContent();
        object.setMetadata(new HashMap<>());
        object.getMetadata().put("Content-MD5",ossObject.getObjectMetadata().getContentMD5()) ;
        object.getMetadata().put("Content-Type",ossObject.getObjectMetadata().getContentType());
//        object.getMetadata().put("Expires",ossObject.getObjectMetadata().getExpirationTime());
        object.getMetadata().put("Content-Length",ossObject.getObjectMetadata().getContentLength());
        object.getMetadata().put("Last-Modified",ossObject.getObjectMetadata().getLastModified());
        return object;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setOssClient(com.aliyun.oss.OSSClient ossClient) {
        this.ossClient = ossClient;
    }
}
