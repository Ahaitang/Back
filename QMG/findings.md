# 研究发现

## 当前系统状态

### 后端接口现状
- 已删除的接口：getByAdmissionNumber, findByPage, countAll
- 可用的问卷接口：/list, /getByPatientId, /countByDayLast7Days, /save, /update, /delete
- 可用的患者接口：需要检查
- 可用的医生接口：需要检查

### 前端代码结构
- app 前端：qmg-app 目录
- admin 前端：qmg-admin 目录
- 需要检查两个前端的 API 调用

### 路由配置
- 需要检查 admin 的路由配置
- Dashboard 当前路径可能是 /dashboard
- 需要改为首页 /

### 图表库选择
- Element Plus 可能内置图表组件
- 或者使用 ECharts（需要安装）
- 或者使用 Chart.js（需要安装）

## 技术发现

### 已删除的后端接口
- QuestionnaireRecordController: getByAdmissionNumber
- QuestionnaireRecordService/Mapper: findByPage, countAll

### 前端可能使用的已删除接口
- getRecordsByAdmissionNumber
- getRecordsByPage

## 待解决问题

1. app 前端是否使用了已删除的接口
2. admin 前端是否还有其他无用代码
3. 选择哪个图表库来绘制曲线图
