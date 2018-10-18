package org.hansk.tools.transfer.action;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import org.bouncycastle.util.Strings;
import org.hansk.tools.transfer.Config;
import org.hansk.tools.transfer.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by guohao on 2018/5/17.
 */
public class FetchOssObjectRunner implements ApplicationRunner {

    @Autowired
    public TransferService transferService;

    public void setConfig(Config config) {
        this.config = config;
    }

    @Autowired
    private Config config;
    private Logger logger = LoggerFactory.getLogger(FetchOssObjectRunner.class);
    private OSSClient ossClient;
    private Runnable fetchRunner;
    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {


        fetchRunner = new Runnable(){

            @Override
            public void run() {
                boolean isTruncated = false;
                ClientConfiguration clientConfiguration = new ClientConfiguration();
                clientConfiguration.setConnectionTimeout(config.getOssTimeout());
                clientConfiguration.setRequestTimeout(config.getOssTimeout());

                ossClient = new OSSClient(config.getOssEndPoint(), config.getOssKey(), config.getOssSecret(), clientConfiguration);
                for (Config.Bucket bucket : config.getBuckets()){
                    if(!Strings.toUpperCase(bucket.getOriginStorage()).equals("OSS") ){
                        continue;
                    }
                    String bucketName = bucket.getOriginBucket();
                    if(bucket.getPrefix().isEmpty()){
                        bucket.getPrefix().add(null);
                    }
                    for(String prefix : bucket.getPrefix()){
                        String optionFlag = "next_marker-" + "oss" + "-"+ bucketName + "-" + prefix;
                        String nextMarker = transferService.getOption(optionFlag);
                        if(nextMarker != null && nextMarker.equals("-1")){
                            logger.info("==== "+ bucket.getOriginStorage() +":" + prefix + " 已经遍历完毕 =====");
                            continue;
                        }
                        logger.info("==== "+ bucket.getOriginStorage() +":" + prefix + " =====");
                        do {
                            try {
                                ObjectListing objectListing = ossClient.listObjects(new ListObjectsRequest(bucketName).withPrefix(prefix).withMarker(nextMarker).withMaxKeys(1000));

                                List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
                                for (OSSObjectSummary s : sums) {
                                    if(s.getLastModified().before(config.getTransferBefore())){
                                        transferService.preTransferNotTransfer("OSS",bucket.getOriginBucket(), bucket.getTargetStorage(), bucket.getTargetBucket(), s.getKey(), s.getSize());
                                        logger.info("[" + s.getBucketName() + " ,"+ s.getKey() + "]");
                                    }
                                }
                                nextMarker  = objectListing.getNextMarker();
                                transferService.setOption(optionFlag, nextMarker == null ? "-1" : nextMarker);
                                isTruncated = objectListing.isTruncated();
                            }catch (OSSException ex){
                                logger.error("oss fail : " + ex.getMessage());
                            }catch (ClientException ex){
                                logger.error("client fail : " + ex.getMessage());
                            } finally {
                                //ossClient.shutdown();
                                //ossClient = new OSSClient(config.getEndPoint(), config.getOssKey(), config.getOssSecret(), clientConfiguration);
                            }

                        }while (isTruncated);
                    }

                }
                ossClient.shutdown();

            }
        };
        Thread thread = new Thread(fetchRunner);
        thread.setName("OSS-Fetch");
        thread.start();
    }

}
