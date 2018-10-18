package org.hansk.tools;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.internal.XmlResponsesSaxParser;
import com.qcloud.cos.model.BucketLifecycleConfiguration;
import com.qcloud.cos.model.ListBucketsRequest;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.region.Region;
import org.hansk.tools.transfer.Config;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@TestConfiguration
public class OsstransferApplicationTests {
	public void setConfig(Config config) {
		this.config = config;
	}

	@Autowired
	private Config config;
	@Test
	public void config(){
		System.out.println(config);
	}
	@Test
	public void contextLoads() {
		COSCredentials cred = new BasicCOSCredentials("AKIDXcdXKylI2KYgeZ4pcIpB2Mn4EE4jsVgM", "31ucItKcjfQ2UoyyoU3Z3OYXLzSkrnY8");
// 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
// clientConfig中包含了设置region, https(默认http), 超时, 代理等set方法, 使用可参见源码或者接口文档FAQ中说明
		ClientConfig clientConfig = new ClientConfig(new Region("ap-beijing"));
// 3 生成cos客户端
		COSClient cosClient = new COSClient(cred, clientConfig);
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
		listObjectsRequest.setBucketName("xyzb-noporn-1252772492");

//		ObjectListing objectListing = cosClient.listObjects(listObjectsRequest);
//		System.out.println(objectListing.getObjectSummaries());

//		File file = new File("C:\\Users\\guohao\\Videos\\2018-06-27 20-37-27.mp4");
//		cosClient.putObject("xxzb-noporn-1252772492","test",file);
//		BucketLifecycleConfiguration bucketLifecycleConfiguration= cosClient.getBucketLifecycleConfiguration("xxzb-noporn-1252772492");
//		System.out.println(bucketLifecycleConfiguration.getRules());
	}

}
