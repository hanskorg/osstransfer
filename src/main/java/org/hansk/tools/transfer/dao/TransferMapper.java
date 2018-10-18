package org.hansk.tools.transfer.dao;

import org.hansk.tools.transfer.domain.Transfer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by guohao on 2018/5/17.
 */
public interface TransferMapper {

    public List<Transfer> findFails(int start, int limit);

    public void createTransfer(@Param("provider")String provider,@Param("bucket")String bucket, @Param("targetProvider")String targetProvider,@Param("targetBucket")String targetBucket, @Param("obj")String object, @Param("objectSize")long objectSize, @Param("createTime")String  createTime);

    public int updateTransferStatus(@Param("id")int id, @Param("targetProvider")String targetProvider, @Param("status")int status);

    public List<Transfer> getUnTransfer(@Param("provider")String provider, @Param("bucket")String bucket, @Param("start")int start, @Param("limit")int limit);

    public List<Transfer> getUnTransfer( @Param("start")int start, @Param("limit")int limit);

    public int transferCount(@Param("bucket") String bucket, @Param("objectName")String objcetName, @Param("targetProvider") String targetProvider);
}

