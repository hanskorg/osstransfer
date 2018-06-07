package org.hansk.tools.transfer.action;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import org.hansk.tools.transfer.Config;
import org.hansk.tools.transfer.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

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
                clientConfiguration.setConnectionTimeout(config.getTimeout());
                clientConfiguration.setRequestTimeout(config.getTimeout());

                ossClient = new OSSClient(config.getEndPoint(), config.getOssKey(), config.getOssSecret(), clientConfiguration);
                for (String bucket : config.getBuckets()){
                    String nextMarker = transferService.getOption("next_marker-" + bucket);
                    if(nextMarker != null && nextMarker.equals("-1")){
                        logger.info("==== "+ bucket + " 已经遍历完毕 =====");
                        continue;
                    }
                    logger.info("==== "+ bucket + " =====");
                    do {
                        try {
                            ObjectListing objectListing = ossClient.listObjects(new ListObjectsRequest(bucket).withMarker(nextMarker).withMaxKeys(1000));
                            List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
                            for (OSSObjectSummary s : sums) {
                                if(s.getLastModified().before(new Date(1527177600000L))){
                                    transferService.preTransferNotTransfer(s.getBucketName(), s.getKey());
                                    logger.info("[" + s.getBucketName() + " ,"+ s.getKey() + "]");
                                }
                            }
                            nextMarker  = objectListing.getNextMarker();
                            transferService.setOption("next_marker-" + bucket, nextMarker == null ? "-1" : nextMarker);
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
                ossClient.shutdown();

            }
        };
        fetchRunner.run();
    }

}
