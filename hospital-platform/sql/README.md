# SQL 数据库脚本说明

本目录包含医院平台所有数据库的统一初始化脚本。

## 文件说明

| 文件 | 说明 | 执行顺序 |
|------|------|----------|
| `00-init-databases.sql` | 创建所有数据库（QMG、neuroimmune、session_audit） | 第1个 |
| `01-qmg-schema.sql` | QMG 系统表结构（患者、医生、问卷等） | 第2个 |
| `02-qmg-data.sql` | QMG 问卷配置数据（13项评分项目） | 第3个 |
| `03-neuroimmune-schema.sql` | Neuroimmune 系统表结构（患者、医生、随访等） + 示例数据 | 第4个 |
| `04-session-audit-schema.sql` | 会话审计系统表结构 + 系统配置 | 第5个 |

## 执行方式

### 方式一：按顺序执行
```bash
mysql -u root -p < 00-init-databases.sql
mysql -u root -p < 01-qmg-schema.sql
mysql -u root -p < 02-qmg-data.sql
mysql -u root -p < 03-neuroimmune-schema.sql
mysql -u root -p < 04-session-audit-schema.sql
```

### 方式二：一次性执行所有脚本
```bash
# Windows PowerShell
Get-Content 00-init-databases.sql,01-qmg-schema.sql,02-qmg-data.sql,03-neuroimmune-schema.sql,04-session-audit-schema.sql | mysql -u root -p

# Linux/Mac
cat 00-init-databases.sql 01-qmg-schema.sql 02-qmg-data.sql 03-neuroimmune-schema.sql 04-session-audit-schema.sql | mysql -u root -p
```

## 数据库结构

### QMG 数据库（重症肌无力定量评分系统）
- `patient` - 患者信息表
- `doctor` - 医生表（含权限等级）
- `patient_doctor` - 患者-医生对应关系
- `questionnaire_item` - 问卷项目配置
- `questionnaire_option` - 问卷选项配置
- `questionnaire_record` - 问卷测评记录

### Neuroimmune 数据库（神经免疫疾病随访系统）
- `admin` - 管理员表
- `doctor` - 医生表
- `patient` - 患者信息表（含疾病分类）
- `follow_up` - 随访记录表
- `medication` - 用药记录表
- `medical_record` - 病历记录表
- `disease_episode` - 疾病发作记录表
- `patient_doctor_relation` - 患者-医生绑定关系表

### Session Audit 数据库（会话审计系统）
- `session_log` - 会话审计日志
- `black_list` - 黑名单表
- `system_config` - 系统配置表
- `super_admin_account` - 超级管理员账号表

## 注意事项

1. 执行前请确保 MySQL 服务已启动
2. 默认密码配置在 `application.yaml` 中，生产环境请修改
3. 示例数据仅用于测试，生产环境可删除对应的 INSERT 语句
4. 性能优化索引已包含在各 schema 文件中