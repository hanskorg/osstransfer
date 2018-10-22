package org.hansk.tools.transfer.storage;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by guohao on 2018/10/16.
 */
public interface IStorage {


    /**
     * 简单上传
     * @param objStream 文件流
     * @param bucket 目标bucket
     * @param key object名称
     * @param objectSize
     * @param contentMD5 对象md5
     * @param metaData
     * @return 是否成功
     * @throws Exception
     */
    public boolean putObject(InputStream objStream, String bucket, String key, long objectSize, String contentMD5, Map<String,String> metaData) throws Exception;

    /**
     * 获取对象
     * @param bucket
     * @param objKey
     * @return
     */
    public StorageObject getObject(String bucket, String objKey) throws Exception;

    public class StorageObject{
        HashMap<String, Object> metadata;
        InputStream content;

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(HashMap<String, Object> metadata) {
            this.metadata = metadata;
        }

        public InputStream getContent() {
            return content;
        }

        public void setContent(InputStream content) {
            this.content = content;
        }

        public String getContentMD5() {
            return (String)this.metadata.get("Content-MD5");
        }
        public String getContentType() {
            return (String)this.metadata.get("Content-Type");
        }
        public Date getExpirationTime() {
            return (Date) this.metadata.get("Expires");
        }
        public long getContentLength(){ return (long)this.metadata.get("Content-Length");}

    }
}

