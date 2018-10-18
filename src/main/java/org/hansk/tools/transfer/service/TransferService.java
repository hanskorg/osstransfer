package org.hansk.tools.transfer.service;

import org.hansk.tools.transfer.Config;
import org.hansk.tools.transfer.dao.OptionsMapper;
import org.hansk.tools.transfer.dao.TransferMapper;
import org.hansk.tools.transfer.domain.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by guohao on 2018/5/17.
 */
@Service
public class TransferService {
    @Autowired
    public TransferMapper transferMapper;
    @Autowired
    public OptionsMapper optionsMapper;
    @Autowired
    private Config config;

    public void preTransfer(String provider, String bucket, String targetProvider,  String targetBucket, String object, long objectSize){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:s");
        if(targetBucket == null || targetBucket.equals("")){
            targetBucket = bucket;
        }
        transferMapper.createTransfer(provider, bucket, targetProvider, targetBucket, object, objectSize, format.format(new Date()));
    }
    public void preTransferNotTransfer(String provider, String bucket, String targetProvider, String targetBucket, String objectName, long objectSize){

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:s");
        List<String> targets = null;
        if(targetProvider == null || targetProvider.isEmpty()){
            targets = config.getTarget();
        }else{
            targets = Arrays.asList(targetProvider.split(","));
        }
        for (String target: targets){
            if(transferMapper.transferCount(bucket, objectName, target) == 0) {
                transferMapper.createTransfer(provider, bucket, target, targetBucket, objectName, objectSize, format.format(new Date()));
            }
        }

    }

    public List<Transfer> getUnTransfers(String provider, String bucket, int limit){
        return transferMapper.getUnTransfer(provider, bucket, 0 , limit);
    }

    public List<Transfer>  getUnTransfers(int limit){
        return transferMapper.getUnTransfer(null, null,0 , limit);
    }

    public boolean isTransfered(String bucket, String objectName){
        return transferMapper.transferCount(bucket, objectName,null) == 0;
    }

    public boolean updateTransferStatus(int id, String targetProvider){
        transferMapper.updateTransferStatus( id, targetProvider, 1);
        return true;
    }

    public String getOption(String key){
        return optionsMapper.getValue(key);
    }

    public Boolean setOption(String key, String value){
        return optionsMapper.setValue(key, value);
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
