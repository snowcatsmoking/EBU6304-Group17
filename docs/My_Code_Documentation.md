# 我的代码文档

## 目录
- [项目概述](#项目概述)
- [我负责的功能模块](#我负责的功能模块)
- [LoginScreen 模块](#loginscreen-模块)
- [ZiqianCao TA功能模块](#ziqiancao-ta功能模块)
- [文件说明](#文件说明)

---

## 项目概述

这是一个助教招聘系统（TA Recruitment System），使用Java + JavaFX开发，数据存储在JSON文件中。

### 技术栈
- **语言**: Java 21
- **UI框架**: JavaFX
- **JSON处理**: Jackson
- **构建工具**: Maven

---

## 我负责的功能模块

| 模块 | 对应Backlog | 优先级 | Sprint |
|------|-------------|--------|--------|
| [LoginScreen](#loginscreen-模块) | 登录/注册 | P0-009 | 1 |
| [个人资料](#profileviewjava) | P0-001, P1-001 | P0/P1 | 1/2 |
| [岗位列表](#tapositionlistuijava) | P0-002, P1-011 | P0/P1 | 1/2 |
| [申请管理](#taaplicationmanagerjava) | P0-003 | P0 | 1 |
| [简历上传](#fileuploaderjava) | P1-003 | P1 | 2 |

---

## LoginScreen 模块

### 1. LoginView.java
**路径**: `src/main/java/LoginScreen/LoginView.java`

**功能**:
- 登录界面UI（登录/注册标签页切换）
- 处理用户登录请求
- 根据用户角色跳转到不同页面：
  - TA → TAPositionListUI
  - MO → MODashboard
  - ADMIN → AdminDashboard

**关键方法**:
| 方法 | 说明 |
|------|------|
| `buildLoginScene()` | 构建登录场景 |
| `createLoginPanel()` | 创建登录面板 |
| `createRegisterPanel()` | 创建注册面板 |
| `switchToLogin()` / `switchToRegister()` | 切换登录/注册标签 |

**UI组件**:
- 账号输入框
- 密码输入框
- 角色选择下拉框（TA/MO/Admin）
- Admin授权码输入框（仅注册Admin时显示）

---

### 2. UserManager.java
**路径**: `src/main/java/LoginScreen/UserManager.java`

**功能**:
- 用户注册（register）
- 用户登录（login）
- 按角色存储用户数据到不同目录

**目录结构**:
```
data/
├── TAData/      # TA用户
├── MOData/      # MO用户
└── AdminData/   # Admin用户
```

**关键方法**:
| 方法 | 说明 |
|------|------|
| `register(account, password, role, authCode)` | 注册新用户 |
| `login(account, password)` | 验证用户登录 |
| `getDirectoryByRole(role)` | 根据角色获取存储目录 |

**Admin授权码**: `BUPTAdmin`

---

### 3. User.java
**路径**: `src/main/java/LoginScreen/User.java`

**功能**: 用户实体类

**字段**:
| 字段 | 类型 | 说明 |
|------|------|------|
| `account` | String | 账号（学号/工号） |
| `password` | String | 密码 |
| `role` | String | 角色（TA/MO/ADMIN） |
| `name` | String | 姓名 |

---

## ZiqianCao TA功能模块

### 4. ProfileView.java
**路径**: `src/main/java/ZiqianCao/java/ProfileView.java`

**功能** (对应 P0-001, P1-001):
- 显示和编辑TA个人资料
- 有活跃申请时锁定资料（除邮箱外）
- 集成FileUploader上传简历

**资料字段**:
- 姓名
- 学号
- 专业
- 电话
- 邮箱
- 可任职时间
- 技能

**关键方法**:
| 方法 | 说明 |
|------|------|
| `getView()` | 获取资料页面 |
| `loadUserData(studentId)` | 加载用户数据 |
| `checkActiveApplication(studentId)` | 检查是否有活跃申请 |
| `saveProfile()` | 保存资料到JSON |

**数据存储**: `data/TAData/{学号}.json`

---

### 5. TAPositionListUI.java
**路径**: `src/main/java/ZiqianCao/java/TAPositionListUI.java`

**功能** (对应 P0-002, P1-011):
- 显示助教岗位列表
- 岗位过滤（按课程名、截止日期、招聘人数）
- 分页显示（每页3条）
- 申请岗位

**关键组件**:

- 侧边栏导航（Dashboard/Positions/My Applications/Profile）
- 岗位卡片（显示职位名、课程、招聘人数、要求、截止日期、发布者）
- 分页控件（上一页/下一页）

**关键方法**:
| 方法 | 说明 |
|------|------|
| `createPositionListView()` | 创建岗位列表页面 |
| `applyFilters()` | 应用过滤条件 |
| `refreshPositionList()` | 刷新岗位列表 |
| `createPaginationBox()` | 创建分页控件 |
| `createPositionBox(job)` | 创建单个岗位卡片 |

**岗位状态**:
| 状态 | 说明 |
|------|------|
| 正常 | 可申请 |
| Expired | 已过期（截止日期已过） |
| Closed | 已关闭（MO手动关闭） |

---

### 6. TAApplicationManager.java

**路径**: `src/main/java/ZiqianCao/java/TAApplicationManager.java`

**功能** (对应 P0-003):
- 创建TA申请
- 校验必填字段
- 校验学号唯一性

**关键方法**:
| 方法 | 说明 |
|------|------|
| `createTAApplication(application)` | 创建申请 |
| `validateRequiredFields(app)` | 校验必填字段 |
| `validateStudentIdUnique(studentId)` | 校验学号唯一性 |

**必填字段**:
- 姓名
- 学号
- 专业
- 电话
- 邮箱
- 可任职时间
- 技能

---

### 7. FileUploader.java
**路径**: `src/main/java/ZiqianCao/java/FileUploader.java`

**功能** (对应 P1-003):
- 文件选择器
- 文件上传（Word/PDF，最大10MB）
- 文件列表显示
- 文件删除

**支持格式**: `.doc`, `.docx`, `.pdf`

**存储路径**: `resources/Data/Uploads/{学号}/`

**关键方法**:
| 方法 | 说明 |
|------|------|
| `getUploadComponent()` | 获取上传组件 |
| `openFileChooser()` | 打开文件选择器 |
| `saveFile(sourceFile)` | 保存文件 |
| `deleteFile(fileName)` | 删除文件 |

---

### 8. 其他相关文件

| 文件 | 说明 |
|------|------|
| `TAApplication.java` | TA申请实体类 |
| `TAApplicationRecord.java` | 申请记录实体类 |
| `TAApplicationRecordManager.java` | 申请记录管理 |
| `TAJob.java` | 岗位实体类 |
| `DashboardView.java` | TA仪表板 |
| `MyApplicationsView.java` | 我的申请页面 |
| `ApplicationDetailView.java` | 申请详情页面 |
| `TAApplicationFormView.java` | 申请表单 |

---

## 文件说明

### LoginScreen 模块文件
| 文件 | 行数 | 主要职责 |
|------|------|----------|
| [LoginView.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/LoginScreen/LoginView.java) | 384 | 登录界面UI，登录/注册逻辑 |
| [UserManager.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/LoginScreen/UserManager.java) | 145 | 用户管理（注册/登录） |
| [User.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/LoginScreen/User.java) | 49 | 用户实体类 |
| [LoginMain.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/LoginScreen/LoginMain.java) | - | 登录主入口 |
| [LoginLauncher.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/LoginScreen/LoginLauncher.java) | - | 登录启动器 |

### ZiqianCao TA功能模块文件
| 文件 | 行数 | 主要职责 | 对应Story |
|------|------|----------|-----------|
| [ProfileView.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/ProfileView.java) | 244 | 个人资料编辑 | P0-001, P1-001 |
| [TAPositionListUI.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/TAPositionListUI.java) | 521+ | 岗位列表+分页 | P0-002, P1-011 |
| [TAApplicationManager.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/TAApplicationManager.java) | 116 | 申请管理 | P0-003 |
| [FileUploader.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/FileUploader.java) | 194 | 简历上传 | P1-003 |
| [TAApplication.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/TAApplication.java) | - | TA申请实体 | - |
| [TAApplicationRecord.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/TAApplicationRecord.java) | - | 申请记录实体 | - |
| [TAApplicationRecordManager.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/TAApplicationRecordManager.java) | - | 申请记录管理 | - |
| [TAJob.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/TAJob.java) | - | 岗位实体 | - |
| [DashboardView.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/DashboardView.java) | - | TA仪表板 | - |
| [MyApplicationsView.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/MyApplicationsView.java) | - | 我的申请 | - |
| [ApplicationDetailView.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/ApplicationDetailView.java) | - | 申请详情 | - |
| [TAApplicationFormView.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/TAApplicationFormView.java) | - | 申请表单 | - |
| [TAApplicationLauncher.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/TAApplicationLauncher.java) | - | 启动器 | - |
| [TAApplicationTest.java](file:///d:/java项目/EBU6304-Group17/EBU6304-Group17/src/main/java/ZiqianCao/java/TAApplicationTest.java) | - | 测试 | - |

---

## 数据流程

```
用户登录 (LoginView)
    ↓
UserManager.login() 验证
    ↓
根据角色跳转:
├─ TA → TAPositionListUI
│   ├─ DashboardView
│   ├─ ProfileView (编辑资料)
│   ├─ TAPositionListUI (浏览岗位 + 申请)
│   └─ MyApplicationsView (查看申请)
├─ MO → MODashboard
└─ Admin → AdminDashboard
```

---

## 数据存储结构

```
data/
├── TAData/              # TA用户资料
│   └── {学号}.json
├── MOData/              # MO用户
│   └── {工号}.json
├── AdminData/           # Admin用户
│   └── {账号}.json
├── JobData/             # 岗位数据
│   └── {jobId}.json
├── ApplicationData/     # 申请记录
│   └── {applicationId}.json
├── Logs/                # 操作日志
│   └── {日期}_{用户}.json
└── session.json         # 会话数据

resources/Data/Uploads/  # 上传的文件
└── {学号}/
    ├── resume.pdf
    └── ...
```

---

## 开发笔记

### 注意事项
1. **有活跃申请时，TA资料被锁定**（除邮箱外）
2. **同一岗位不能重复申请**
3. **Admin注册需要授权码**: `BUPTAdmin`
4. **文件上传限制**: 最大10MB，仅支持doc/docx/pdf
5. **分页**: 每页显示3条岗位

### 依赖
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.2</version>
</dependency>
```
