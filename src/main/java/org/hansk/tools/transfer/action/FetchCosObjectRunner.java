package org.hansk.tools.transfer.action;

import com.aliyun.oss.OSSClient;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.region.Region;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.model.FileListing;
import com.qiniu.util.Auth;
import org.bouncycastle.util.Strings;
import org.hansk.tools.transfer.Config;
import org.hansk.tools.transfer.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * Created by guohao on 2018/10/15.
 */
public class FetchCosObjectRunner implements ApplicationRunner {
    @Autowired
    public TransferService transferService;

    public void setConfig(Config config) {
        this.config = config;
    }

    @Autowired
    private Config config;
    private Logger logger = LoggerFactory.getLogger(FetchCosObjectRunner.class);
    private COSClient cosClient;
    private Runnable fetchRunner;
    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        COSCredentials cred = new BasicCOSCredentials(config.getCosSecretID(), config.getCosSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(config.getCosRegion()));
        cosClient = new COSClient(cred, clientConfig);

        fetchRunner = new Runnable(){

            @Override
            public void run() {

                for (Config.Bucket bucket : config.getBuckets()){
                    if(!Strings.toUpperCase(bucket.getOriginStorage()).equals("COS")){
                        continue;
                    }
                    if(bucket.getPrefix().isEmpty()){
                        bucket.getPrefix().add(null);
                    }
                    for(String prefix : bucket.getPrefix()){
                        String optionFlag = "next_marker-" + "cos" + "-"+bucket.getOriginBucket() + "-" + prefix;
                        String nextMarker = transferService.getOption(optionFlag);
                        if(nextMarker != null && nextMarker.equals("-1")){
                            logger.info("[Bucket: "+ bucket + ", Prefix: "+ prefix + "]已经遍历完毕 =====");
                            continue;
                        }
                        boolean isTruncated = false;

                        do {
                            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucket.getTargetBucket(), prefix, nextMarker, "", 1000);
                            ObjectListing objectListing = cosClient.listObjects(listObjectsRequest);
                            nextMarker = objectListing.getNextMarker();
                            if (objectListing.isTruncated()) {
                                transferService.setOption(optionFlag, "-1");
                                logger.info("[Bucket: " + bucket + ", Prefix: " + prefix + "]已经遍历完毕 =====");
                                isTruncated = true;
                            }else{
                                transferService.setOption(optionFlag, nextMarker);
                            }

                            for (COSObjectSummary object : objectListing.getObjectSummaries()) {
                                if (object.getLastModified().after(config.getTransferBefore())) {
                                    transferService.preTransferNotTransfer("cos", bucket.getOriginBucket(),bucket.getTargetStorage(),  bucket.getTargetBucket(), object.getKey(), object.getSize());
                                    logger.info("PreTransfer: [" + bucket.getOriginBucket() + " ," + object.getKey() + "]");
                                }
                            }

                        }while (isTruncated);
                    }


                }
            }
        };
        Thread thread = new Thread(fetchRunner);
        thread.setName("COS-Fetch");
        thread.start();
    }
}
