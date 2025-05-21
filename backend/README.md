# WOS 后端

## 介绍
这是上海交通大学图书馆WOS学术资源平台的后端代码部分。
主要使用Springboot 3.3.1 版本。

目前主要的开发者：
- 陈家树

## 如何运行代码
- **软件需求**：
    整个后端部分是由Maven构建的，所以你需要安装Maven。
    使用Maven加载项目，Maven可以自动下载在pom.xml中指定的依赖项。
- **数据库配置**：
    当前使用的是Mysql数据库做测试，最终版本将使用SQLServer作为数据库。
    数据库配置文件在`application.properties`中，你需要根据自己的数据库配置修改这个文件。
- **elasticSearch配置**：
    你需要安装elasticSearch，并且在`application.properties`中配置elasticSearch的地址。
- **运行代码**：
    你可以在IntelliJ IDEA中运行`BackEndApplication`类来启动整个项目，记得加载项目为Maven项目。

## 文件夹与文件
下面是这个项目的根文件夹的解释。
- **`src/main/java`** : 存储后端`.java`文件
  - **`config`** : 存储配置文件(`CORS`、`ElasticSearch`等)以及自定义数据格式（`DisciplinaryRequest`、`SearchFilter`等）
  - **`controller`** : 存储控制器文件
  - **`mapper`** : 存储`Mybatis`的`Mapper`文件
  - **`model`** : 存储实体文件
  - **`repository`** : 存储`JPA`的`Repository`文件(未使用)
  - **`provider`** : 存储`Mybatis`的`Provider`文件(SQL语句组合)
  - **`service`** : 存储服务文件
    - **`impl`** : 存储服务实现文件
      - **`DisciplinaryAnalysis`** : 存储学科分析服务实现文件
      - **`DownloadService`** : 存储下载服务实现文件
      - **`Main2022ServiceImpl`** : 存储主页服务(高级检索)实现文件
      - **`Main2022ElasticSearchServiceImpl`** : 存储主页服务(全文搜索)实现文件
      - **`NLPService`** : 存储自然语言处理文件
  - **`BackEndApplication`** : 存储主类文件

- **`src/main/resources`** : 存储配置文件
  - **`application.properties`** : 数据库配置文件

- **`pom.xml`** : Maven配置文件
