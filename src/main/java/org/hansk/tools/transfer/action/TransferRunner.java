package org.hansk.tools.transfer.action;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.hansk.tools.transfer.Config;
import org.hansk.tools.transfer.domain.Transfer;
import org.hansk.tools.transfer.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.io.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by guohao on 2018/5/18.
 */
public class TransferRunner implements ApplicationRunner {

    private Logger logger = LoggerFactory.getLogger(TransferRunner.class);
    @Autowired
    private Config config;
    @Autowired
    public TransferService transferService;

    private OSSClient ossClient;

    private UploadManager uploadManager;

    private ScheduledExecutorService scheduledThreadPoolExecutor;
    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        ossClient = new OSSClient(config.getEndPoint(), config.getOssKey(), config.getOssSecret());
        Auth auth = Auth.create(config.getQiniuAccess(), config.getQiniuSecret());
        Configuration cfg = new Configuration(Zone.zone1());
        uploadManager = new UploadManager(cfg);

        


        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler rejectionHandler = new RejectedExecutionHandler(){

            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                synchronized (this) {
                    logger.warn(r.toString() + " is rejected");
                    try {
                        scheduledThreadPoolExecutor.awaitTermination(500,TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        ThreadPoolExecutor executorPool = new ThreadPoolExecutor(config.getCoreDownloadThread(),
                config.getMaxDownloadThread(),
                10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2), threadFactory, rejectionHandler);
        scheduledThreadPoolExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                List<Transfer> unTransferList =  transferService.getUnTransfers(config.getMaxDownloadThread());
                for (Transfer transfer : unTransferList){

                    executorPool.execute( new Runnable(){
                        @Override
                        public void run() {
                            String upToken = auth.uploadToken(transfer.getBucket(), transfer.getObject());
                            OSSObject ossObject = ossClient.getObject(new GetObjectRequest(transfer.getBucket(),transfer.getObject()));

                            try {
                                Response response = uploadManager.put(IOUtils.readStreamAsByteArray(ossObject.getObjectContent()),transfer.getObject(),upToken);
                                if( response.isOK() ) {
                                    logger.info("file upload success [ " + transfer.getBucket() + " ;"+ transfer.getObject() + " ]");
                                    transferService.updateTransferStatus(transfer.getId());
                                }else{
                                    logger.error("file upload fail [ "+ transfer.getObject() + " ] , exception: " + response.error);
                                }
                                ossObject.close();
                            } catch (QiniuException e) {
                                logger.error("file upload fail [ "+ transfer.getObject() + " ] , exception: " + e.toString());
                            } catch (IOException e) {
                                logger.error("file download fail , IO error [ "+ transfer.getObject() + " ] , exception: " + e.toString());
                            }
                        }
                    });
                }
            }
        },5000, 500,TimeUnit.MILLISECONDS);



    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public ScheduledExecutorService getScheduledThreadPoolExecutor() {
        return scheduledThreadPoolExecutor;
    }
}
