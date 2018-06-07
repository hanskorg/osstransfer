package org.hansk.tools.transfer.service;

import org.hansk.tools.transfer.dao.OptionsMapper;
import org.hansk.tools.transfer.dao.TransferMapper;
import org.hansk.tools.transfer.domain.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    public void preTransfer(String bucket, String object){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:s");
        transferMapper.createTransfer(bucket, object, format.format(new Date()));
    }
    public void preTransferNotTransfer(String bucket, String objectName){
        if(transferMapper.transferCount(objectName) == 0){
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:s");
            transferMapper.createTransfer(bucket, objectName, format.format(new Date()));
        }
    }

    public List<Transfer> getUnTransfers(String bucket, int limit){
        return transferMapper.getUnTransfer(bucket, 0 , limit);
    }

    public List<Transfer>  getUnTransfers(int limit){
        return transferMapper.getUnTransfer( null,0 , limit);
    }

    public boolean isTransfered(String objectName){
        return transferMapper.transferCount(objectName) == 0;
    }

    public boolean updateTransferStatus(int id){
        transferMapper.updateTransferStatus( id,1);
        return true;
    }

    public String getOption(String key){
        return optionsMapper.getValue(key);
    }

    public Boolean setOption(String key, String value){
        return optionsMapper.setValue(key, value);
    }

}
