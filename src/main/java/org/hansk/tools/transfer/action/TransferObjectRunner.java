package org.hansk.tools.transfer.action;

import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.model.OSSObject;
import com.qcloud.cos.model.COSObject;
import com.qiniu.common.QiniuException;
import org.bouncycastle.util.Strings;
import org.hansk.tools.transfer.Config;
import org.hansk.tools.transfer.domain.Transfer;
import org.hansk.tools.transfer.service.TransferService;
import org.hansk.tools.transfer.storage.IStorage;
import org.hansk.tools.transfer.storage.OSSClient;
import org.hansk.tools.transfer.storage.QiniuClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import sun.nio.ch.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by guohao on 2018/10/15.
 */
public class TransferObjectRunner implements ApplicationRunner {

    private Logger logger = LoggerFactory.getLogger(TransferObjectRunner.class);
    @Autowired
    private Config config;
    @Autowired
    public TransferService transferService;

    @Autowired
    private QiniuClient qiniuClient;
    @Autowired
    private org.hansk.tools.transfer.storage.COSClient cosClient;
    @Autowired
    private org.hansk.tools.transfer.storage.OSSClient ossClient;
    @Autowired
    private ApplicationContext applicationContext;

    private ScheduledExecutorService scheduledThreadPoolExecutor;
    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {

        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler rejectionHandler = new RejectedExecutionHandler(){

            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                synchronized (this) {
                    //logger.warn(r.toString() + " is rejected");
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
                            IStorage.StorageObject storageObject = null;
                            IStorage uploadClient = null;
                            IStorage downClient = null;
                            downClient = (IStorage) applicationContext.getBean(Strings.toUpperCase(transfer.getProvider()));
                            try {
                                storageObject = downClient.getObject(transfer.getBucket(), transfer.getObject());
                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.error(transfer.getProvider() + " object get  error" + e.toString());
                                return;
                            }
                            List<String> targets = transfer.getTargetProvider() == null || transfer.getTargetProvider().isEmpty()
                                    ? config.getTarget()
                                    : Arrays.asList(transfer.getTargetProvider().split(","));
                            for (String target : targets){
                                uploadClient = (IStorage) applicationContext.getBean(Strings.toUpperCase(target));
                                boolean isOk = false;
                                try {

                                    isOk = uploadClient.putObject(storageObject.getContent()
                                            ,transfer.getTargetBucket()
                                            ,transfer.getObject()
                                            ,transfer.getObjectSize()
                                            ,storageObject.getContentMD5()
                                            ,null
                                            );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if( isOk ) {
                                    logger.info("file upload success ["+ transfer.getProvider() + ":"+ transfer.getBucket() + " " + target + ":" + transfer.getTargetBucket()+" ;"+ transfer.getObject() + " ]");
                                    transferService.updateTransferStatus(transfer.getId(), transfer.getTargetProvider());
                                }else{
                                    logger.error("file upload fail ["+ transfer.getProvider() + "; "+ transfer.getBucket() + " ;"+ transfer.getObject() + " ]");
                                }
                            }
                            if (storageObject != null && storageObject.getContent() != null){
                                try {
                                    storageObject.getContent().close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
        },5000, 500, TimeUnit.MILLISECONDS);



    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public ScheduledExecutorService getScheduledThreadPoolExecutor() {
        return scheduledThreadPoolExecutor;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
