# osstransfer
## Introduction
阿里云OSS、腾讯云COS、七牛对象存储 互传。


## Usage

### 下载
#### 选择版本
 [osstransfer.tar.gz](https://github.com/hanskorg/google-authenticator-rust/releases)
##### 1、with jre版本
 java version 1.8.0_131
##### 2、without jre版本
```
wget https://github.com/hanskorg/strorage-transfer/releases/download/v0.1.0/transfer-0.1.0-without-jre.tar.gz
tar zxvf transfer-0.1.0-without-jre.tar.gz ./transfer
cd transfer
ln -s {path_to_jre} jre
./transfer start
```
### 修改配置 config/application.yml
```
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/transfer
    username: root
    password: 123456
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    filters: stat
    maxActive: 20
    initialSize: 1
    maxWait: 60000
    minIdle: 1
    timeBetweenEvictionRunsMillis: 60000
    minEvictableIdleTimeMillis: 300000
    validationQuery: select 1
    testWhileIdle: true
    testOnBorrow: false
    testOnReturn: false
    poolPreparedStatements: true
    maxOpenPreparedStatements: 2
    useSSL: false
mybatis:
  mapper-locations: classpath:mapping/*.xml
  type-aliases-package: org.hansk.tools.transfer.domain
#迁移相关相关配置
transfer:
  oss:
    key: key
    secret: secret
    end_point: http://endpoint
    timeout: 1000
    access_domain: localhost

  qiniu:
    access_key: 
    secret_key: 

  cos:
    secret_id: 
    secret_key: 
    app_id: 
    region: 

  buckets:
  -
    #源存储,必填
    originStorage: 
    #源存储Bucket ,必填
    originBucket: 
    #源Region COS有效 非必填, 默认值 transfer.cos.region
    originRegion:
    #源EndPoint OSS有效 非必填 默认值 transfer.oss.end_point
    originEndPoint: 
    #需要迁移的对象前缀 非必填 默认值 /
    prefix:

    #目标存储 多值,分割，默认值为transfer.target
    targetStorage: cos
    #目标bucket 非必填 默认与源相同
    targetBucket: 
    #目标bucket 非必填 transfer.cos.region
    targetRegion: 
     #目标bucket 非必填 transfer.oss.end_point
    targetEndPoint:

  -
    originStorage: qiniu
    originBucket: test
    prefix:
      - test/image
    targetBucket: test_cos/image
  #默认迁移存储目标cos、qiniu、oss
  target: 
  - cos

  #最小网络线程
  core_download: 2
  #最大网络线程
  max_download: 50
  #迁移2018-10-19 00:00:00之前的对象
  transferBefore: 2018-10-19 00:00:00
```
./transfer restart

### 运行

 > ./transfer start # 启动
 
 > ./transfer stop # 停止
 
