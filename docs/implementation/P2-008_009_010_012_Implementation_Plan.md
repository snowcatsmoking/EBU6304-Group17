# 实施计划：P2-008 / P2-009 / P2-010 / P2-012

> 技术栈：Java 17 + JavaFX + Jackson (JSON) + Maven  
> 所有新功能均在 Admin 端实现，数据本地 JSON 文件存储。

---

## 一、需求速览

| Story ID | 功能名称 | 用户角色 | 核心目标 |
|----------|----------|----------|----------|
| P2-008 | 工作量阈值预警 | Admin | 设置 TA 持仓上限，超限时在 Workload 页面显示文字警告 |
| P2-009 | 全院招募数据统计 | Admin | 按职位/MO 汇总招募完成率、TA 分布、招募进度（表格展示） |
| P2-010 | 全局数据导出（CSV） | Admin | 导出职位信息、TA 申请信息、招募统计三张表为 CSV 文件 |
| P2-012 | 手动备份 & 恢复数据 | Admin | 将 `data/` 目录打包为 ZIP 备份；从 ZIP 恢复覆盖当前数据 |

---

## 二、整体架构变更

### 2.1 Admin 侧边栏扩展

**改动文件**：`src/main/java/Admin/AdminDashboard.java`

当前导航项：Dashboard / User Management / Global Positions / Operation Logs  
**新增导航项**（按顺序插入 Operation Logs 后面）：

```
Workload Alert      → WorkloadView
Recruitment Stats   → RecruitmentStatsView
Data Export         → DataExportView
Backup / Restore    → BackupRestoreView
```

### 2.2 新增文件清单

| 文件路径 | 用途 |
|----------|------|
| `src/main/java/Admin/WorkloadView.java` | P2-008 界面 |
| `src/main/java/Admin/RecruitmentStatsView.java` | P2-009 界面 |
| `src/main/java/Admin/DataExportView.java` | P2-010 界面 |
| `src/main/java/Admin/BackupRestoreView.java` | P2-012 界面 |
| `src/main/java/data/WorkloadConfigManager.java` | P2-008 阈值持久化 |

---

## 三、P2-008：工作量阈值预警

### 3.1 需求拆解

- Admin 可以设置单学期 TA 最多被录取的职位数（默认 3）
- 页面展示所有 TA 当前已录取（`STATUS_APPROVED`）职位数
- 数量 == 阈值时：**警告（橙色）**；数量 > 阈值时：**超限（红色）**
- 阈值持久化存储

### 3.2 数据来源

- `TAApplicationRecordManager.getAllApplications()` → 过滤 `STATUS_APPROVED` → 按 `taStudentId` 分组统计
- `UserDataManager.getAllTAs()` → 获取 TA 姓名等信息

### 3.3 阈值存储

新建 `data/AdminData/workload_config.json`，格式：

```json
{
  "maxPositionsPerSemester": 3
}
```

由 `WorkloadConfigManager` 读写（使用 Jackson，与现有 Manager 保持一致）。

### 3.4 界面布局

```
[页面标题: Workload Alert]

阈值设置区（卡片）
┌────────────────────────────────────┐
│ Max positions per semester: [  3  ] [Save] │
└────────────────────────────────────┘

TA 工作量列表（卡片 + 表格）
┌──────────────────────────────────────────────────┐
│ Student ID │ Name │ Approved Positions │ Status   │
│ TAPan      │ Pan  │ 2                  │ ✅ Normal │
│ TAxxx      │ xxx  │ 3                  │ ⚠ Warning│
│ TAyyy      │ yyy  │ 4                  │ ❌ Overload│
└──────────────────────────────────────────────────┘
```

### 3.5 实现步骤

1. 创建 `WorkloadConfigManager`（读写阈值 JSON）
2. 创建 `WorkloadView`：
   - 顶部阈值设置区（TextField + Button）
   - 中部表格（从 ApplicationRecordManager 聚合数据）
   - 行颜色：正常=默认，==阈值=橙色背景，>阈值=红色背景
3. 修改 `AdminDashboard` 添加导航项

---

## 四、P2-009：全院招募数据统计

### 4.1 需求拆解

- 统计维度：
  - 每个职位（Job）：招募名额、申请数、录取数、完成率
  - 每个 MO：发布职位数、总申请数、总录取数
- 数据实时读取，表格展示

### 4.2 数据来源

- `JobDataManager.getAllJobs()` → 所有职位基础信息（`recruitmentCount`、`moStaffId`）
- `TAApplicationRecordManager.getAllApplications()` → 按 `jobId` / `moStaffId` 聚合

### 4.3 统计逻辑

```
完成率 = (approved 数 / recruitmentCount) × 100%，上限显示 100%
```

### 4.4 界面布局

```
[页面标题: Recruitment Statistics]

按职位统计（卡片 + 表格）
┌─────────────────────────────────────────────────────────────────────┐
│ Job Title │ Course │ MO │ Quota │ Applied │ Approved │ Completion   │
│ TA-CS101  │ CS101  │ MO1│  3    │   5     │    2     │ 67%          │
└─────────────────────────────────────────────────────────────────────┘

按 MO 统计（卡片 + 表格）
┌──────────────────────────────────────────────────────┐
│ MO Account │ Posted Jobs │ Total Applied │ Total Hired│
│ MOPan      │      2      │      8        │     4      │
└──────────────────────────────────────────────────────┘
```

### 4.5 实现步骤

1. 创建 `RecruitmentStatsView`：
   - 构建两个聚合 Map（按 jobId、按 moStaffId）
   - 表格使用与现有 `DashboardView` 相同的 HBox-per-row 风格
   - 完成率 ≥ 100% 时显示绿色，< 50% 显示橙色
2. 修改 `AdminDashboard` 添加导航项

---

## 五、P2-010：全局数据导出（CSV）

### 5.1 需求拆解

- 三类数据可分别导出：
  1. **职位信息**（`JobData/`）
  2. **TA 申请记录**（`ApplicationData/`）
  3. **招募统计**（P2-009 的聚合结果）
- 格式：UTF-8 CSV，用 `FileChooser` 让 Admin 选择保存路径
- 不引入第三方 CSV 库，纯 Java 字符串拼接

### 5.2 CSV 列定义

**职位信息 CSV**：
```
JobId, PositionName, CourseName, CourseCode, RecruitmentCount, Publisher, MoStaffId, Deadline, IsActive
```

**申请记录 CSV**：
```
ApplicationId, TaStudentId, StudentName, JobId, PositionName, CourseName, Status, ApplicationDate, Reviewer, ReviewComment
```

**招募统计 CSV**（来自 P2-009 的逻辑）：
```
JobId, PositionName, MoStaffId, Quota, Applied, Approved, CompletionRate
```

### 5.3 界面布局

```
[页面标题: Data Export]

┌────────────────────────────────────────────────────────────────────┐
│ Export Jobs Data           [Export CSV]                            │
│ All job postings                                                   │
├────────────────────────────────────────────────────────────────────┤
│ Export Application Records [Export CSV]                            │
│ All TA application records                                         │
├────────────────────────────────────────────────────────────────────┤
│ Export Recruitment Stats   [Export CSV]                            │
│ Aggregated per-job stats                                           │
└────────────────────────────────────────────────────────────────────┘
```

### 5.4 实现步骤

1. 创建 `DataExportView`：
   - 三行导出卡片，每行一个 "Export CSV" 按钮
   - 点击触发 `FileChooser.showSaveDialog()`
   - 用 `PrintWriter`（UTF-8）写 CSV，特殊字符用 `"..."` 包裹处理
   - 成功/失败后弹出 Alert 提示
2. 修改 `AdminDashboard` 添加导航项

---

## 六、P2-012：手动备份 & 恢复数据

### 6.1 需求拆解

- **备份**：将 `data/` 下所有子目录的 JSON 文件打包为一个 ZIP 文件，由 Admin 选择保存路径
- **恢复**：Admin 选择一个 ZIP 备份文件，解压后覆盖当前 `data/` 目录中的对应文件
- 恢复前必须显示确认对话框（"This will overwrite current data. Are you sure?"）
- 操作结果给出明确提示

### 6.2 技术方案

- 使用 Java 标准库 `java.util.zip.ZipOutputStream` / `ZipInputStream`，无需第三方依赖
- 备份覆盖范围：`DataConfig` 中定义的所有目录（TA/MO/Admin/Job/Application/Favorite/Log/Matching）
- 恢复时保持目录结构，按原路径还原

### 6.3 界面布局

```
[页面标题: Backup & Restore]

备份区（卡片）
┌────────────────────────────────────────────────────────────────────┐
│ Backup System Data                                                 │
│ Export all users, jobs, and application data as a ZIP file.        │
│                                          [Backup Now]              │
└────────────────────────────────────────────────────────────────────┘

恢复区（卡片）
┌────────────────────────────────────────────────────────────────────┐
│ Restore System Data                                                │
│ Select a backup ZIP file to restore. This will overwrite current   │
│ data and cannot be undone.                                         │
│                                          [Restore from Backup]     │
└────────────────────────────────────────────────────────────────────┘
```

### 6.4 实现步骤

1. 创建 `BackupRestoreView`：
   - **备份**：遍历 `DataConfig` 所有目录，递归添加文件到 ZIP；`FileChooser` 保存
   - **恢复**：`FileChooser` 选 ZIP → 弹出 `Alert.CONFIRMATION` → 确认后解压覆盖
   - 操作成功/失败均弹出 `Alert.INFORMATION` / `Alert.ERROR`
2. 修改 `AdminDashboard` 添加导航项

---

## 七、开发顺序建议

| 优先级 | Story | 理由 |
|--------|-------|------|
| 1 | P2-012 备份恢复 | 独立性最高，无数据依赖，风险低 |
| 2 | P2-009 招募统计 | 纯数据聚合，逻辑复用于 P2-010 |
| 3 | P2-010 数据导出 | 依赖 P2-009 的统计逻辑 |
| 4 | P2-008 工作量预警 | 需要额外的配置持久化，单独处理 |

---

## 八、验收标准核对

### P2-008
- [ ] Admin 可在界面输入并保存阈值，刷新后保留
- [ ] 列表展示所有 TA 已录取职位数
- [ ] 达到阈值显示橙色警告，超过显示红色警告

### P2-009
- [ ] 按职位展示：招募名额、申请数、录取数、完成率
- [ ] 按 MO 展示：发布职位数、总申请数、总录取数
- [ ] 数据实时读取（每次打开界面重新计算）

### P2-010
- [ ] 三类数据可独立导出为 CSV
- [ ] 导出文件用 Excel 可正常打开
- [ ] Admin 可自选保存路径
- [ ] 导出成功/失败有明确提示

### P2-012
- [ ] 备份生成 ZIP 文件，包含 data/ 下所有 JSON
- [ ] 恢复前有确认对话框
- [ ] 恢复后数据与备份一致
- [ ] 操作结果有明确提示
