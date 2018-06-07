# osstransfer
## Introduction
同步阿里云Object到七牛云

## Usage

### 修改JRE PATH
    wrapper/conf/wrapper.conf
    #line 48 set.JAVA_HOME=../../jre

### 修改配置 config/application.yml
```
spring:
  datasource:
   #mysql 配置
    url: jdbc:mysql://localhost:3306/oss_transfer
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
mybatis:
  mapper-locations: classpath:mapping/*.xml
  type-aliases-package: org.hansk.tools.transfer.domain
oss:
   # OSS 配置文件
  key: key
  secret: secret
  end_point: http://endpoint #endpooint 查看阿里云文档
  timeout: 1000
  buckets: # bucket列表
  - testbucket1
  - testbucket2
  # 最大下载数量
core_download: 2
max_download: 10

qiniu:
    # 七牛存储配置
  access_key: qiniukey
  secret_key: qiniusecret
```
### 运行程序
 > ./wrapper/bin/wrapper start # 启动
 
 > ./wrapper/bin/wrapper stop # 停止
 
 可以直接下载[osstransfer.zip](https://github.com/hanskorg/osstransfer/files/2080721/osstransfer.zip)运行
