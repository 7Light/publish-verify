# CI后端服务

CI后端服务是CI门户的核心服务，基于ServiceComb框架开发。所有接口的swagger文件都在ci-portal-schemas项目中，使用huawei-swagger-codegen-maven-plugin插件自动生成Java代码框架。
主要功能为：

- 提供CI门户查询接口
  - 门禁
  - 每日构建
  - 每日代码检查
  - 度量看板
  - 当前用户的committer权限
- 封装DevCloud CodeCheck相关接口，供事件处理服务调用
  - 触发门禁代码检查
  - 更新门禁事件状态
- 封装定时任务接口，供华为云应用魔方AppCube的定时任务服务调用
  - 每日代码检查
    - 定时任务1：每天 00:00 将偏移量置n为0
    - 定时任务2：每天00:15 启动，每30分钟触发一次,08:00结束；每次基于偏移量检查创建检查任务 [n,n+30)；任务启动后，偏移量n加30；
    - 定时任务3： 每天00:30 启动，每30分支触发一次,08:30结束；扫描未入库的任务；入库后，将任务状态修改为已入库；
  - 门禁代码检查
    - 每5分钟执行一次，查询未入库的门禁代码任务是否执行完毕；将执行完毕的代码检查任务结果入库，更新任务状态未已处理；

## 快速入门

### 获取代码

- 使用git clone命令拉取项目代码。
- 已在CodeHub上将ci-portal-schemas配置为ci-backend-service的子模块（submodule）。git clone和git pull命令不会自动更新submodule，需要执行git submodule update --remote 更新子模块。更新后使用git status可以看到ci-portal-schemas状态为有变更，需要依次使用git add、git commit、git push命令提交更新，以便ci-backend-service跟踪最新的ci-portal-schemas。

### 编译打包

- 将Maven的settings.xml文件配置为代码根目录下的settings.xml文件。settings.xml中配置了华为云镜像仓<https://mirrors.huaweicloud.com/home>和CI门户在申请的私有Maven库（ci-common-utils在私有Maven仓库中）。
- 执行mvn package进行打包。

  ```shell
  mvn package -s settings.xml
  ```

### 开发接口

- 在ci-portal-schemas中增加或修改yaml文件。
- provider/pom.xml中配置了huawei-swagger-codegen-maven-plugin插件，可以基于指定的yaml文件生成接口框架和bean。

## 权限管理

目前调用码云获取一个组织的仓库接口<https://gitee.com/api/v5/swagger#/getV5OrgsOrgRepos>，查询并保存OpenHarmony组织下所有仓库和成员。以下是响应中一个仓库的数据样例，assignee字段是Committer数组。

```json
{
    "id": 11218387, 
    "full_name": "openharmony/aafwk_aafwk_lite", 
    "human_name": "OpenHarmony/aafwk_aafwk_lite", 
    "url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite", 
    "namespace": {
        "id": 6486504, 
        "type": "group", 
        "name": "OpenHarmony", 
        "path": "openharmony", 
        "html_url": "https://gitee.com/openharmony"
    }, 
    "path": "aafwk_aafwk_lite", 
    "name": "aafwk_aafwk_lite", 
    "owner": {
        "id": 7928036, 
        "login": "openharmony_admin", 
        "name": "openharmony", 
        "avatar_url": "https://portrait.gitee.com/uploads/avatars/user/2642/7928036_openharmony_admin_1622551091.png", 
        "url": "https://gitee.com/api/v5/users/openharmony_admin", 
        "html_url": "https://gitee.com/openharmony_admin", 
        "followers_url": "https://gitee.com/api/v5/users/openharmony_admin/followers", 
        "following_url": "https://gitee.com/api/v5/users/openharmony_admin/following_url{/other_user}", 
        "gists_url": "https://gitee.com/api/v5/users/openharmony_admin/gists{/gist_id}", 
        "starred_url": "https://gitee.com/api/v5/users/openharmony_admin/starred{/owner}{/repo}", 
        "subscriptions_url": "https://gitee.com/api/v5/users/openharmony_admin/subscriptions", 
        "organizations_url": "https://gitee.com/api/v5/users/openharmony_admin/orgs", 
        "repos_url": "https://gitee.com/api/v5/users/openharmony_admin/repos", 
        "events_url": "https://gitee.com/api/v5/users/openharmony_admin/events{/privacy}", 
        "received_events_url": "https://gitee.com/api/v5/users/openharmony_admin/received_events", 
        "type": "User"
    }, 
    "description": "Ability framework. An ability is an abstraction of a functionality that an application can provide and is an essential component to OpenHarmony applications. | Ability是应用所具备能力的抽象，也是应用程序的重要组成部分", 
    "private": false, 
    "public": true, 
    "internal": false, 
    "fork": false, 
    "html_url": "https://gitee.com/openharmony/aafwk_aafwk_lite.git", 
    "ssh_url": "git@gitee.com:openharmony/aafwk_aafwk_lite.git", 
    "forks_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/forks", 
    "keys_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/keys{/key_id}", 
    "collaborators_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/collaborators{/collaborator}", 
    "hooks_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/hooks", 
    "branches_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/branches{/branch}", 
    "tags_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/tags", 
    "blobs_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/blobs{/sha}", 
    "stargazers_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/stargazers", 
    "contributors_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/contributors", 
    "commits_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/commits{/sha}", 
    "comments_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/comments{/number}", 
    "issue_comment_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/issues/comments{/number}", 
    "issues_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/issues{/number}", 
    "pulls_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/pulls{/number}", 
    "milestones_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/milestones{/number}", 
    "notifications_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/notifications{?since,all,participating}", 
    "labels_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/labels{/name}", 
    "releases_url": "https://gitee.com/api/v5/repos/openharmony/aafwk_aafwk_lite/releases{/id}", 
    "recommend": false, 
    "homepage": "", 
    "language": null, 
    "forks_count": 86, 
    "stargazers_count": 81, 
    "watchers_count": 10, 
    "default_branch": "master", 
    "open_issues_count": 0, 
    "has_issues": true, 
    "has_wiki": true, 
    "issue_comment": false, 
    "can_comment": false, 
    "pull_requests_enabled": true, 
    "has_page": false, 
    "license": "Apache-2.0", 
    "outsourced": false, 
    "project_creator": "open_harmony_admin", 
    "members": [
        "qiudengcheng", 
        "skyblackleon", 
        "openharmony_ci", 
        "autumn330", 
        "landwind"
    ], 
    "pushed_at": "2021-06-28T16:06:56+08:00", 
    "created_at": "2020-08-27T14:41:40+08:00", 
    "updated_at": "2021-06-28T16:06:56+08:00", 
    "parent": null, 
    "paas": null, 
    "assignees_number": 1, 
    "testers_number": 1, 
    "assignee": [
        {
            "id": 7809298, 
            "login": "autumn330", 
            "name": "autumn330", 
            "avatar_url": "https://portrait.gitee.com/uploads/avatars/user/2603/7809298_autumn330_1616768076.png", 
            "url": "https://gitee.com/api/v5/users/autumn330", 
            "html_url": "https://gitee.com/autumn330", 
            "followers_url": "https://gitee.com/api/v5/users/autumn330/followers", 
            "following_url": "https://gitee.com/api/v5/users/autumn330/following_url{/other_user}", 
            "gists_url": "https://gitee.com/api/v5/users/autumn330/gists{/gist_id}", 
            "starred_url": "https://gitee.com/api/v5/users/autumn330/starred{/owner}{/repo}", 
            "subscriptions_url": "https://gitee.com/api/v5/users/autumn330/subscriptions", 
            "organizations_url": "https://gitee.com/api/v5/users/autumn330/orgs", 
            "repos_url": "https://gitee.com/api/v5/users/autumn330/repos", 
            "events_url": "https://gitee.com/api/v5/users/autumn330/events{/privacy}", 
            "received_events_url": "https://gitee.com/api/v5/users/autumn330/received_events", 
            "type": "User"
        }
    ], 
    "testers": [
        {
            "id": 7387629, 
            "login": "openharmony_ci", 
            "name": "openharmony_ci", 
            "avatar_url": "https://portrait.gitee.com/uploads/avatars/user/2462/7387629_openharmony_ci_1623376846.png", 
            "url": "https://gitee.com/api/v5/users/openharmony_ci", 
            "html_url": "https://gitee.com/openharmony_ci", 
            "followers_url": "https://gitee.com/api/v5/users/openharmony_ci/followers", 
            "following_url": "https://gitee.com/api/v5/users/openharmony_ci/following_url{/other_user}", 
            "gists_url": "https://gitee.com/api/v5/users/openharmony_ci/gists{/gist_id}", 
            "starred_url": "https://gitee.com/api/v5/users/openharmony_ci/starred{/owner}{/repo}", 
            "subscriptions_url": "https://gitee.com/api/v5/users/openharmony_ci/subscriptions", 
            "organizations_url": "https://gitee.com/api/v5/users/openharmony_ci/orgs", 
            "repos_url": "https://gitee.com/api/v5/users/openharmony_ci/repos", 
            "events_url": "https://gitee.com/api/v5/users/openharmony_ci/events{/privacy}", 
            "received_events_url": "https://gitee.com/api/v5/users/openharmony_ci/received_events", 
            "type": "User"
        }
    ]
}
```

将结果扁平化后，存储到MongoDB中。每条记录为某用户在某个仓库的角色，以及用户、仓库基本信息，例如：

```json
{
  "project_id": "11218387", 
  "full_name": "openharmony/aafwk_aafwk_lite", 
  "user_id": "7809298", 
  "login": "autumn330", 
  "name": "autumn330", 
  "url": "https://gitee.com/api/v5/users/autumn330",
  "type": "committer"
}

```

由于码云用户可以修改用户名和登录名，用户id可以保持不变，因此用户登录后需要使用id查询用户权限。

## 代码检查

代码检查调用华为云软件开发服务(DevCloud)中代码检查(CodeCheck)提供的API接口，相关接口文档参考<https://apiexplorer.developer.huaweicloud.com/apiexplorer/doc?product=CodeCheck&api=QueryTaskDetail>。

代码检查任务分为全量检查和增量检查：

- 每个仓库的每个分支都会有对应的全量检查任务，例如仓库aafwk_aafwk_lite的master和OpenHarmony_1.0.1_release分支，在CodeCheck中有两个对应的全量代码检查任务。
- 增量代码检查任务是全量检查的一个子任务，例如仓库aafwk_aafwk_lite有一个合入到master分支的增量检查代码任务，那么该任务是仓库aafwk_aafwk_lite master分支全量检查任务的子任务。

CI门户调用CodeCheck提供的接口查询检查结果、代码问题，将数据保存到MongoDB中，保存规则如下。

- 每日代码全量检查：每天只保留最近一次结果；最长保留31天。
- 增加代码检查：保留每一次任务的结果；最长保留31天。

### 代码问题状态修改

代码问题共有三种状态：未解决、已解决、已忽略。

- 不能修改“已解决”的问题状态。
- 可以将问题状态在“未解决”和“已忽略”之间切换。

修改代码检查问题的状态时，先调用codecheck接口进行更新，更新成功后更新对应details表中对应问题的状态，summary表不需要更新；如果codecheck更新失败，则不更新revision。
更新details表中指定问题的状态时，需要增加/更新revision属性，同时更新status字段。Revision属性包括：

- 新的状态 newStaus
- 修改前的状态 previousStaus
- 修改人id userId
- 修改人账号 userName
- 修改时间 timestamp

反复修改问题状态时，只保留最新的revision信息。

## 参数配置

- 华为云CSE（微服务引擎）会对testing、acceptance、production环境中微服务的接口schema进行校验（development不检查），版本号相同的微服务必须具备一致的schema，否则微服务启动失败。如果ci-portal-schemas的接口定义有变更（URL、参数等），必须更新provider/src/main/resources/application.yaml中的servicecomb.service.version版本号。
- 运行依赖
  - 配置中心地址: provider/src/main/resources/application.yaml中servicecomb.config.client字段
  - 注册中心地址：provider/src/main/resources/application.yaml中servicecomb.service.registry字段
  - 配置中心配置项（development、testing、production分别配置对应的值）：
    - MongoDB数据库：数据库地址、用户名、密码在配置中心配置,  
      - mongo.user
      - mongo.server
      - mongo.db
      - mongo.password
      - mongo.codecheck.user
      - mongo.codecheck.server
      - mongo.codecheck.password
      - mongo.codecheck.db
    - Redis: 配置中心配置
      - redis.password
      - redis.server
    - CodeCheck项目：配置中心配置
      - ak
      - sk
      - codecheck.region
      - codecheck.project_id

## 数据初始化

在系统部署后，需要进行数据初始化。

### 权限

调用接口，初始化权限信息。

### SIG组

调用接口，获取并保存SIG组与仓库的关系<https://gitee.com/openharmony/community/blob/master/sig/sigs.json>。

### 代码仓库

调用接口，获取并保存项目和仓库。

- 解析配置文件<https://gitee.com/wenjun_HW/ci_tool/blob/master/ci_conf/project4Kanban.json>，获取项目、构建阶段、manifest文件路径。
- 根据manifest文件<https://gitee.com/openharmony/manifest/blob/master/default.xml>，查询并保存仓库地址。

## 数据库设计

### 数据库实例

OpenHarmony CI/CD使用Mongo数据库，分为CI构建库和代码检查库。按照环境创建对应的数据库实例，各数据库实列中表、表结构一致。数据库实例如下：

- CI构建库：门禁事件、流水线、每日构建数据。
  - 开发/测试环境： test_ci_info
  - 生产环境： ci_info

- 代码检查库： 代码检查基础配置、任务、代码检查结果。
  - Alpha环境： ci_codechec_alpha
  - Beta环境： ci_codecheck_beta
  - 生产环境： ci_codecheck

### 表结构

#### branch_repository  仓库信息配置表

- project_name  项目名称
- manifest_branch_name  manifest类型的项目的分支名称
- repo_name  仓库名称
- repo_branch_name  仓库的分支
- repo_url  仓库git路径
- codecheck_task_name  代码检查任务名称
- codecheck_task_id  代码检查任务id
- execute_time  执行时间
- critical_issues  仓库配置的允许出现的严重问题数
- major_issues  仓库配置的允许出现的重要问题数
- general_issues  仓库配置的允许出现的普通问题数
- tips_issues  仓库配置的允许出现的提示问题数

#### code_check_task  codecheck全量代码检查任务表

- projectId 任务所在项目id
- taskId  任务id
- branch  检查分支
- gitUrl  仓库git路径
- isProcessing  任务是否正在执行

#### parent_task_inc  增量模式父任务表

- projectId 任务所在项目id
- taskId  任务id
- repoName  仓库名称
- gitUrl  仓库git路径
- branch  父任务分支

#### code_check_task_inc  mr增量检查任务表

- uuid  整个任务流程的唯一id，触发一次mr会生成一个
- taskId  任务的id
- parentTaskId 父任务id
- sourceBranch 合并代码的来源分支
- targetBranch  代码合并到的分支
- mrId 合并的pr号
- mrlUrl 合并请求的地址
- gitUrl  仓库的地址
- isProcessing  任务是否正在执行

#### project_branch 项目和分支的映射关系

- project_name  项目名称
- manifest_repo  manifest类型的仓库地址
- manifest_branch_name manifest类型的仓库分支名称
- device_level  设备级别

#### sig_info sig仓库信息和对应的语言

- repo_name_en 仓库英文名
- repo_name_cn 仓库中文名
- languages 仓库语言列表

#### task_inc_result_summary 增量检查结果概要表

- checkType 检查类型
- codeLine 代码有效行数
- codeLineTotal 代码总行数
- codeQuality 代码质量
- commentLines 注释行数
- commentRatio 注释比例
- complexityCount 复杂度
- createdAt 创建时间
- creatorId 创建者id
- duplicatedBlocks 重复块
- duplicatedLines 重复行数
- duplicationRatio 重复比例
- gitBranch 代码仓分支
- gitUrl 代码仓地址
- issueCount 问题数
- lastCheckTime 上一次检查时间
- lastExecTime 上次执行时间
- riskCoefficient 危险系数
- taskId 任务id
- task_name 任务名字
- uuid 唯一id

#### task_inc_result_details  增量检查结果明细表

- taskId 任务id
- uuid  唯一id
- result 执行结果
- subCodeCheckName 检查的名称 统一是“codeCheck”
- filePath 问题所在的文件路径
- ruleName  规则名称
- lineNumber 行号
- defectLevel 缺陷等级
- createAt 创建时间

#### task_result_summary  全量检查结果概要

- date 日期
- sigNameCn sig中文名
- sigNameEn sig英文名
- repoNameCn 仓库中文名
- repoNameEn 仓库英文名
- checkType 检查类型
- codeLine      代码有效行数
- codeLineTotal 代码总行数
- codeQuality 代码质量
- commentLines 注释行数
- commentRatio  注释比例
- complexityCount 复杂度
- createdAt 创建时间
- creatorId 创建者id
- duplicatedBlocks 重复块
- duplicatedLines 重复行数
- duplicationRatio 重复比例
- gitBranch 代码仓分支
- gitUrl 代码仓地址
- issueCount 问题数
- lastCheckTime 上一次检查时间
- lastExecTime   上次执行时间
- riskCoefficient   危险系数
- taskId 任务id
- task_name 任务名字

#### task_result_details

- taskId  任务id
- filePath 问题所在的文件路径
- ruleName  规则名称
- lineNumber 代码行
- defectLevel 问题级别
- date 日期
- createAt 创建时间
- fileName 文件名
- fragment  代码片段
- fragment.lineContent 代码行内容
- fragment.lineNum 行号
- fragment.startOffset 缺陷开始列号
- fragment.endOffset  缺陷结束列号

## 代码说明

关键目录和文件：
│  .gitignore
│  .gitmodules
│  pom.xml
│  README.md
│  settings.xml  # Maven配置文件
├─ci-portal-schemas # 接口定义
│  └─yaml
│          admin.yaml
│          buildresult.yaml
│          codecheck.yaml
│          dailybuild.yaml
│          event.yaml
│          file.yaml
│          project.yaml
│          webhookcodecheck.yaml
│          webhookgitee.yaml
│
├─consumer
│  │  pom.xml
│  │
│  └─src
│     └─main
│         ├─java
│         │  └─com
│         │      └─huawei
│         │          └─ci
│         │              └─consumer
│         │                      ConsumerApplication.java
│         │
│         └─resources
│                 microservice.yaml
│
│
└─provider
    │  pom.xml
    │
    └─src
       └─main
           ├─java
           │  └─com
           │      └─huawei
           │          └─ci
           │              └─portal
           │                  └─provider
           │                      │  ProviderApplication.java # 启动类
           │                      │
           │                      ├─config
           │                      │      CiReadMongoConfig.java
           │                      │      CiWriteMongoConfig.java
           │                      │      CodeCheckMongoConfig.java
           │                      │      RedisConfig.java
           │                      │      ScanConfig.java
           │                      │
           │                      ├─controller # 自动生成
           │                      │      AdminController.java
           │                      │      BuildresultController.java
           │                      │      CodecheckController.java
           │                      │      DailybuildController.java
           │                      │      EventController.java
           │                      │      FileController.java
           │                      │      ProjectController.java
           │                      │      WebhookcodecheckController.java
           │                      │      WebhookController.java
           │                      │      WebhookgiteeController.java
           │                      │
           │                      ├─delegate # 自动生成
           │                      │      AdminDelegate.java
           │                      │      BuildresultDelegate.java
           │                      │      CodecheckDelegate.java
           │                      │      DailybuildDelegate.java
           │                      │      EventDelegate.java
           │                      │      FileDelegate.java
           │                      │      ProjectDelegate.java
           │                      │      WebhookcodecheckDelegate.java
           │                      │      WebhookDelegate.java
           │                      │      WebhookgiteeDelegate.java
           │                      │
           │                      ├─entity
           │                      │  ├─model # 自动生成
           │                      │  │      BuildModel.java
           │                      │  │      BuildResultModel.java
           │                      │  │      BuildTypeModel.java
           │                      │  │      DailyBuildModel.java
           │                      │  │      EventListModel.java
           │                      │  │      EventModel.java
           │                      │  │      MrParamModel.java
           │                      │  │      PipelineModel.java
           │                      │  │      PrMsg.java
           │                      │  │      ProjectModel.java
           │                      │  │      QueryDetailModel.java
           │                      │  │      QuerySummaryModel.java
           │                      │  │      TestDataModel.java
           │                      │  │
           │                      │  ├─mongodb
           │                      │  │      BranchRepo.java
           │                      │  │      ProjectBranch.java
           │                      │  │      SigInfo.java
           │                      │  │
           │                      │  ├─util
           │                      │  │      MultiResponse.java
           │                      │  │
           │                      │  └─vo
           │                      │          BuildResults.java
           │                      │          CodeCheckParams.java
           │                      │          CodeCheckParentTaskIncVo.java
           │                      │          CodeCheckResultDetailsVo.java
           │                      │          CodeCheckResultSummaryVo.java
           │                      │          CodeCheckResultVo.java
           │                      │          CodeCheckTaskIncVo.java
           │                      │          CodeCheckTaskVo.java
           │                      │          DailyBuilds.java
           │                      │          DailyBuildVo.java
           │                      │          DefectVo.java
           │                      │          EventResultVo.java
           │                      │          EventVo.java
           │                      │          PipelineQuery.java
           │                      │          PipelineVo.java
           │                      │          PrMsgVo.java
           │                      │          TestDataVo.java
           │                      │
           │                      ├─enums
           │                      │      CiCollectionName.java
           │                      │      CiConstants.java
           │                      │      CodeCheckCollectionName.java
           │                      │      CodeCheckConstants.java
           │                      │      CodeCheckStatus.java
           │                      │
           │                      ├─handler
           │                      │      AuthHandler.java
           │                      │      MrCodeCheckHandler.java
           │                      │
           │                      ├─impl # 业务实现
           │                      │      BuildresultDelegateImpl.java
           │                      │      CodecheckDelegateImpl.java
           │                      │      CodeCheckModel.java
           │                      │      DailybuildDelegateImpl.java
           │                      │      EventDelegateImpl.java
           │                      │      FileDelegateImpl.java
           │                      │      GiteeDelegateImpl.java
           │                      │      ProjectDelegateImpl.java
           │                      │      WebhookcodecheckDelegateImpl.java
           │                      │      WebhookgiteeDelegateImpl.java
           │                      │
           │                      ├─model
           │                      │      QuerySummaryModel.java
           │                      │
           │                      ├─operation
           │                      │  ├─ci
           │                      │  │  ├─read
           │                      │  │  │      CodeCheckOperation.java
           │                      │  │  │      DailyBuildOperation.java
           │                      │  │  │      EventOperation.java
           │                      │  │  │      GiteeRepoOperation.java
           │                      │  │  │      PipelineOperation.java
           │                      │  │  │
           │                      │  │  └─write
           │                      │  │          CodeCheckDevCloudOperation.java
           │                      │  │
           │                      │  └─codecheck
           │                      │          BranchRepoOperation.java
           │                      │          CiCodeCheckOperation.java
           │                      │          EventCodeCheckOperation.java
           │                      │          IncResultDetailsOperation.java
           │                      │          IncResultSummaryOperation.java
           │                      │          ParentTaskIncOperation.java
           │                      │          ProjectBranchOperation.java
           │                      │          ResultDetailsOperation.java
           │                      │          ResultSummaryOperation.java
           │                      │          SigInfoOperation.java
           │                      │          TaskIncOperation.java
           │                      │          TaskOperation.java
           │                      │
           │                      ├─schedule
           │                      │      CreateAndExecTask.java
           │                      │      DynamicCornScheduler.java
           │                      │      SaveCodeCheckIncTaskResultSchedule.java
           │                      │      SaveCodeCheckTaskResultSchedule.java
           │                      │
           │                      └─utils
           │                              RedisOperateUtils.java
           │                              ValidatorUtil.java
           │
           └─resources
               │  application.yaml
               │
               ├─config
               │      cse.handler.xml
               │
               ├─i18n
               │      messages.properties
               │      messages_en_US.properties
               │      messages_zh_CN.properties
               │
               └─sig
                       sig_info.properties
                       sig_info_2.properties
