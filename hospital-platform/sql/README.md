# 数据库建表脚本说明

## 文件说明

| 文件 | 说明 |
|------|------|
| `schema-only.sql` | 纯建表脚本（无示例数据） |

## 使用方法

```bash
mysql -u root -p < schema-only.sql
```

## 数据库说明

脚本创建以下数据库：

| 数据库名 | 说明 |
|---------|------|
| `QMG` | 重症肌无力定量评分系统 |
| `neuroimmune` | 祫经免疫疾病随访系统 |
| `session_audit` | 会话审计系统 |

## 表结构概览

### QMG 数据库（6张表）
- `patient` - 患者信息表
- `doctor` - 医生表
- `patient_doctor` - 患者-医生对应表
- `questionnaire_item` - 问卷项目配置表
- `questionnaire_option` - 问卷选项配置表
- `questionnaire_record` - 问卷结果表

### Neuroimmune 数据库（11张表）
- `admin` - 管理员表
- `doctor` - 医生表
- `doctor_role` - 医生角色关联表
- `patient` - 患者表
- `patient_disease` - 者疾病关联表
- `patient_doctor_relation` - 患者-医生绑定关系表
- `dict_common` - 通用字典表
- `follow_up` - 随访记录表
- `medication` - 用药记录表
- `medical_record` - 病历记录表
- `disease_episode` - 疾病发作记录表

### Session Audit 数据库（4张表）
- `session_log` - 会话审计日志
- `black_list` - 黑名单表
- `system_config` - 系统配置表
- `super_admin_account` - 超级管理员账号表

## 版本信息

- MySQL版本要求: 5.7+ 或 8.0+
- 字符集: utf8mb4