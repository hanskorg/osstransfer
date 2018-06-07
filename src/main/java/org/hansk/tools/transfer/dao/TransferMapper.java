package org.hansk.tools.transfer.dao;

import org.hansk.tools.transfer.domain.Transfer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by guohao on 2018/5/17.
 */
public interface TransferMapper {

    public List<Transfer> findFails(int start, int limit);

    public void createTransfer(@Param("bucket")String bucket, @Param("obj")String object, @Param("createTime")String  createTime);

    public int updateTransferStatus(@Param("id")int id, @Param("status")int status);

    public List<Transfer> getUnTransfer(@Param("bucket")String bucket, @Param("start")int start, @Param("limit")int limit);

    public List<Transfer> getUnTransfer( @Param("start")int start, @Param("limit")int limit);

    public int transferCount(@Param("objectName")String objcetName);
}

