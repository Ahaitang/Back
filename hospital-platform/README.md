# Hospital Platform

> 医院平台统一后端服务，整合 **QMG（重症肌无力评分系统）** 和 **Neuroimmune（神经免疫疾病随访系统）**。

---

## 目录

- [项目概述](#项目概述)
- [技术架构](#技术架构)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [环境配置](#环境配置)
- [认证机制](#认证机制)
- [API 规范](#api-规范)
- [QMG 模块](#qmg-模块)
- [Neuroimmune 模块](#neuroimmune-模块)
- [OCR 模块](#ocr-模块)
- [数据模型](#数据模型)
- [错误处理](#错误处理)
- [部署指南](#部署指南)
- [监控与健康检查](#监控与健康检查)

---

## 项目概述

### QMG - 重症肌无力定量评分系统

重症肌无力（Myasthenia Gravis, MG）是一种神经免疫疾病，本系统用于：
- **量化评分**：基于 13 项临床指标进行 QMG 评分（满分 39 分）
- **病情追踪**：记录患者历史评分，观察病情变化趋势
- **权限管理**：支持超级管理员、管理员、普通医生三级权限

### Neuroimmune - 神经免疫疾病随访系统

用于多种神经免疫疾病的长期随访管理：
- **疾病类型**：MS、NMOSD、MG、MOGAD、自身免疫性脑炎、GBS、CIDP 等
- **随访管理**：门诊/住院随访记录、用药方案
- **医患绑定**：医生-患者关系管理，支持扫码绑定

---

## 技术架构

| 层级 | 技术 |
|------|------|
| **框架** | Spring Boot 3.2.5 (Java 17) |
| **ORM** | MyBatis Plus 3.5.9 (Spring Boot 3 专用版本) |
| **数据库** | MySQL 8.0（三数据库：QMG + neuroimmune + session_audit） |
| **缓存** | Redis 7（Token 存储 + 业务缓存） |
| **认证** | JWT (jjwt 0.11.5) + Spring Security 6 |
| **文件存储** | MinIO（Neuroimmune 专用） |
| **OCR** | 百度 OCR API（身份证识别） |
| **日志** | Log4j2（替代 Logback） |
| **API 文档** | Knife4j 4.5.0（增强版 Swagger UI） |
| **构建** | Gradle 7.5.1 |

### Spring Boot 3 升级说明

- **Jakarta EE 命名空间**：使用 `jakarta.*` 替代 `javax.*`
- **Spring Security 6**：使用 `SecurityFilterChain` Bean 配置
- **MyBatis Plus**：使用 Spring Boot 3 专用 starter `mybatis-plus-spring-boot3-starter`
- **Thymeleaf Security**：升级至 `thymeleaf-extras-springsecurity6`

---

## 项目结构

```
hospital-platform/
├── hospital-common/          # 公共模块
│   ├── config/               # Redis、MyBatis Plus、CORS、Security 配置
│   ├── security/             # JWT 认证、Token 管理、踢下线功能
│   ├── model/                # Result、PageResult、ImportResult
│   ├── exception/            # BusinessException、ErrorCode、GlobalExceptionHandler
│   ├── audit/                # 审计日志 AOP
│   ├── util/                 # ExcelUtil、PasswordUtil
│   └── controller/           # TokenManagementController（会话管理）
│
├── hospital-qmg/             # QMG 业务模块
│   ├── entity/               # Doctor、Patient、QuestionnaireRecord 等
│   ├── mapper/               # MyBatis Mapper + XML
│   ├── service/              # 业务逻辑层
│   ├── controller/           # API 控制器
│   └── config/               # QMG 数据源配置
│
├── hospital-neuroimmune/     # Neuroimmune 业务模块
│   ├── entity/               # Patient、Doctor、FollowUp 等
│   ├── mapper/               # MyBatis Plus Mapper（注解为主）
│   ├── service/              # 业务逻辑层
│   ├── controller/           # API 控制器
│   └── config/               # Neuroimmune 数据源配置
│
├── hospital-ocr/             # OCR 服务模块
│   ├── service/              # 百度 OCR 服务
│   └── controller/           # OCR API
│
├── hospital-web/             # Web 入口模块
│   └── config/               # Knife4j OpenAPI 配置
│   └── resources/
│       ├── application.yaml       # 主配置
│       ├── application-dev.yaml   # 开发环境配置
│       └── log4j2-spring.xml      # Log4j2 日志配置
│
├── build.gradle              # 根构建脚本
└── .env.example              # 环境变量示例
```

---

## 快速开始

### 前置条件

- JDK 17+
- MySQL 8.0
- Redis 7
- Gradle（或使用 wrapper）

### 本地开发

1. **创建数据库**

```sql
CREATE DATABASE QMG CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE neuroimmune CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **启动 Redis**

```bash
redis-server
```

3. **运行项目**

```bash
cd hospital-platform
./gradlew bootRun
```

4. **访问服务**

- API 文档：http://localhost:8080/doc.html (Knife4j 增强版)
- 健康检查：http://localhost:8080/actuator/health

---

## 环境配置

### application.yaml 核心配置

```yaml
server:
  port: 8080

spring:
  profiles:
    active: dev
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
  cache:
    type: redis

jwt:
  secret: ${JWT_SECRET:hospital-platform-jwt-secret-key-2024-secure}
  expiration: ${JWT_EXPIRATION:86400000}  # 24小时
```

### application-dev.yaml 数据源配置

```yaml
spring:
  datasource:
    qmg:
      jdbc-url: jdbc:mysql://localhost:3306/QMG
      username: root
      password: 123456
    neuroimmune:
      jdbc-url: jdbc:mysql://localhost:3306/neuroimmune
      username: root
      password: 123456

  redis:
    host: localhost
    port: 6379
```

### 环境变量（生产环境）

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `JWT_SECRET` | JWT 密钥 | `hospital-platform-jwt-secret-key-2024-secure` |
| `JWT_EXPIRATION` | Token 有效期（毫秒） | `86400000` (24h) |
| `DB_QMG_URL` | QMG 数据库 URL | `jdbc:mysql://localhost:3306/QMG` |
| `DB_QMG_USER` | QMG 数据库用户 | `root` |
| `DB_QMG_PASSWORD` | QMG 数据库密码 | `123456` |
| `REDIS_HOST` | Redis 主机 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `MINIO_ENDPOINT` | MinIO 地址 | `http://localhost:9000` |
| `BAIDU_OCR_API_KEY` | 百度 OCR API Key | - |

---

## 认证机制

### JWT Token 结构

Token 存储以下信息：

```json
{
  "userId": 1,
  "username": "doctor001",
  "role": "doctor",
  "module": "qmg",
  "exp": 1715000000
}
```

### Token 存储（Redis）

```
Key:   hospital:token:{module}:{role}:{userId}
Value: Token 字符串
TTL:   24 小时（可配置）
```

### 认证流程

1. **登录**：验证用户名密码 → 生成 JWT → 存入 Redis
2. **请求**：携带 `Authorization: Bearer {token}` → 过滤器验证 → 解析用户信息
3. **踢下线**：删除 Redis 中 Token → 用户下次请求被拒绝

### 公开接口（无需认证）

以下路径无需 Token：

- `/api/v1/qmg/login`, `/api/v1/neuroimmune/login` - 登录接口
- `/api/v1/qmg/register`, `/api/v1/neuroimmune/register` - 注册接口
- `/api/v1/super-admin/login` - 超级管理员登录
- `/doc.html`, `/swagger-ui/**`, `/v3/api-docs/**` - Knife4j API 文档
- `/webjars/**` - Web 资源
- `/actuator/health`, `/actuator/info` - 健康检查

### Token 管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/admin/session/online` | POST | 查询在线用户 |
| `/api/v1/admin/session/count` | POST | 统计在线人数 |
| `/api/v1/admin/session/kick` | DELETE | 踢指定用户下线 |
| `/api/v1/admin/session/check` | POST | 检查用户是否在线 |

---

## API 规范

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

**QMG 模块特殊格式**：

```json
{
  "code": 1,        // 1=成功, 0=失败
  "message": "success",
  "data": { ... }
}
```

### 分页请求格式

```json
{
  "page": 1,
  "pageSize": 20,
  "patientId": 123,
  "keyword": "张"
}
```

### 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "hasMore": true
  }
}
```

### 请求方式约定

| 模块 | 约定 |
|------|------|
| **QMG** | 全部使用 `POST` 请求（包括查询） |
| **Neuroimmune** | RESTful 风格（GET/POST/PUT/DELETE） |

---

## QMG 模块

### API 路径前缀

```
/api/v1/qmg/
```

### 接口清单

#### 医生管理 `/api/v1/qmg/doctor`

| 接口 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/login` | POST | 医生登录 | `{username, password}` |
| `/logout` | POST | 医生登出 | `{id, level}` |
| `/getByUsername` | POST | 查询医生 | `{username}` |
| `/list` | POST | 医生列表 | `{currentUserLevel}` |
| `/update` | POST | 更新医生 | `{id, employeeNumber, username, password, level, currentUserLevel}` |
| `/register` | POST | 注册医生 | `{employeeNumber, username, password}` 或 `{doctors: [...]}` 批量 |

#### 患者管理 `/api/v1/qmg/patient`

| 接口 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/list` | POST | 患者列表 | `{currentDoctorId, currentUserLevel, scope}` |
| `/getById` | POST | 查询患者 | `{id, currentDoctorId, currentUserLevel}` |
| `/getByAdmissionNumber` | POST | 按住院号查询 | `{admissionNumber}` |
| `/search` | POST | 搜索患者 | `{keyword, currentDoctorId, currentUserLevel, scope}` |
| `/add` | POST | 新增患者 | `{name, gender, admissionNumber, phone, currentDoctorId}` |
| `/update` | POST | 更新患者 | `{id, name, gender, admissionNumber, phone, currentUserLevel}` |
| `/delete` | POST | 删除患者 | `{id, currentUserLevel}` |
| `/import` | POST | 批量导入 | `{patients: [...], currentDoctorId}` |

#### 问卷管理 `/api/v1/qmg/questionnaire`

| 接口 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/save` | POST | 保存问卷 | `{patientId, assessmentDate, selections, ...}` |
| `/getById` | POST | 查询问卷 | `{id}` |
| `/getByPatientId` | POST | 按患者查询 | `{patientId, startDate, endDate, currentDoctorId, currentUserLevel}` |
| `/list` | POST | 问卷列表 | `{patientName, startDate, endDate, page, pageSize, ...}` |
| `/update` | POST | 更新问卷 | `{id, selections, modifiedBy, ...}` |
| `/delete` | POST | 删除问卷 | `{id}` |
| `/countByDayLast7Days` | POST | 7天统计 | 无 |

#### 问卷配置 `/api/v1/qmg/questionnaireConfig`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/items` | POST | 获取评分项配置 |
| `/options` | POST | 获取选项配置 |

### 权限等级

| Level | 角色 | 权限 |
|-------|------|------|
| 0 | 超级管理员 | 全部数据可见、可修改权限等级 |
| 1 | 管理员 | 全部数据可见、不可修改超级管理员创建的数据 |
| 2 | 普通医生 | 仅查看自己关联的患者和问卷 |

### scope 参数说明

| 值 | 说明 |
|-----|------|
| 不传 | Web 端默认：管理员看全部，普通医生看自己的 |
| `mine` | App 端专用：所有人只看自己关联的数据 |

---

## Neuroimmune 模块

### API 路径前缀

```
/api/v1/neuroimmune/
```

### 接口清单

#### 认证 `/api/v1/neuroimmune`

| 接口 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/login` | POST | 统一登录 | `{username, password, role}` |
| `/logout` | POST | 登出 | - |
| `/admin/{id}/password` | PUT | 修改管理员密码 | `{password}` |
| `/patients/{id}/password` | PUT | 修改患者密码 | `{password}` |
| `/doctors/{id}/password` | PUT | 修改医生密码 | `{password}` |

**role 参数**：`admin`、`doctor`、`patient`

#### 患者管理 `/api/v1/neuroimmune/patients`

| 接口 | 方法 | 说明 | Headers |
|------|------|------|---------|
| `/` | GET | 患者列表 | `X-User-Role`, `X-User-Id` |
| `/my` | GET | 我的患者 | `X-User-Id` |
| `/{id}` | GET | 查询患者 | `X-User-Role`, `X-User-Id` |
| `/` | POST | 新增患者 | - |
| `/{id}` | PUT | 更新患者 | - |
| `/{id}` | DELETE | 删除患者 | - |

#### 医生管理 `/api/v1/neuroimmune/doctors`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 医生列表 |
| `/{id}` | GET | 查询医生 |
| `/` | POST | 新增医生 |
| `/{id}` | PUT | 更新医生 |
| `/{id}` | DELETE | 删除医生 |

#### 随访管理 `/api/v1/neuroimmune/followups`

| 接口 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/` | GET | 随访列表 | `page, pageSize, patientId, doctorId, status` |
| `/{id}` | GET | 查询随访 | - |
| `/` | POST | 新增随访 | FollowUp JSON |
| `/{id}` | PUT | 更新随访 | FollowUp JSON |
| `/{id}` | DELETE | 删除随访 | - |
| `/pending` | GET | 待处理随访 | `X-User-Id` |

#### 医患关系 `/api/v1/neuroimmune/relation`

| 接口 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/bind` | POST | 绑定关系 | `{patientId, doctorId}` |
| `/unbind` | POST | 解绑关系 | `{id}` |
| `/patient/{patientId}` | GET | 患者的医生列表 | - |
| `/doctor/{doctorId}` | GET | 医生的患者列表 | - |

#### 用药记录 `/api/v1/neuroimmune/medications`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 用药列表 |
| `/{id}` | GET | 查询用药 |
| `/` | POST | 新增用药 |
| `/{id}` | PUT | 更新用药 |
| `/{id}` | DELETE | 删除用药 |

#### 疾病发作 `/api/v1/neuroimmune/episodes`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 发作记录列表 |
| `/{id}` | GET | 查询发作记录 |
| `/` | POST | 新增发作记录 |
| `/{id}` | PUT | 更新发作记录 |
| `/{id}` | DELETE | 删除发作记录 |

#### 仪表盘 `/api/v1/neuroimmune/dashboard`

| 接口 | 方法 | 说明 | Headers |
|------|------|------|---------|
| `/stats` | GET | 统计数据 | `X-User-Role`, `X-User-Id` |

**统计数据**（按角色返回）：
- 管理员：`totalPatients`, `totalDoctors`, `pendingFollowUps`, `totalMedications`
- 医生：自己关联的患者数、待处理随访数
- 患者：就诊次数、用药方案数

#### 文件管理 `/api/v1/neuroimmune/file`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/upload` | POST | 上传文件（MinIO） |
| `/download/{filename}` | GET | 下载文件 |

#### 数据导入 `/api/v1/neuroimmune/import`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/template/patient` | GET | 下载患者导入模板 |
| `/template/followup` | GET | 下载随访导入模板 |
| `/patient` | POST | 导入患者 Excel |
| `/followup` | POST | 导入随访 Excel |

---

## OCR 模块

### API 路径前缀

```
/api/v1/ocr/
```

| 接口 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/idcard` | POST | 身份证识别 | `file` (MultipartFile) |

**返回格式**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "name": "张三",
    "idNumber": "123456789012345678",
    "gender": "男",
    "ethnic": "汉",
    "birth": "19900101",
    "address": "北京市..."
  }
}
```

---

## 数据模型

### QMG 数据库表

#### doctor（医生表）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | INT | 主键 |
| `employee_number` | VARCHAR(50) | 工号（唯一） |
| `username` | VARCHAR(100) | 用户名 |
| `password` | VARCHAR(255) | 密码（BCrypt） |
| `level` | INT | 权限等级：0/1/2 |
| `create_time` | DATETIME | 创建时间 |
| `update_time` | DATETIME | 更新时间 |

#### patient（患者表）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | INT | 主键 |
| `name` | VARCHAR(100) | 姓名 |
| `gender` | VARCHAR(10) | 性别：male/female |
| `admission_number` | VARCHAR(50) | 住院号（唯一） |
| `phone` | VARCHAR(20) | 手机号 |
| `create_time` | DATETIME | 创建时间 |
| `update_time` | DATETIME | 更新时间 |

#### patient_doctor（医患关系表）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | INT | 主键 |
| `patient_id` | INT | 患者ID |
| `doctor_id` | INT | 医生ID |
| `create_time` | DATETIME | 关联时间 |

#### questionnaire_record（问卷记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | INT | 主键 |
| `patient_id` | INT | 患者ID |
| `admission_number` | VARCHAR(50) | 住院号 |
| `assessment_date` | DATE | 测评日期 |
| `selections` | JSON | 选择的选项 |
| `item_scores` | JSON | 各项得分 |
| `total_score` | INT | 总分（满分39） |
| `category_scores` | JSON | 分类得分 |
| `doctor_id` | INT | 创建医生ID |
| `modified_by` | VARCHAR(100) | 最后修改人 |
| `create_time` | DATETIME | 创建时间 |
| `update_time` | DATETIME | 更新时间 |

### Neuroimmune 数据库表

#### patient（患者表）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `name` | VARCHAR(100) | 姓名 |
| `gender` | VARCHAR(10) | 性别 |
| `age` | INT | 年龄 |
| `phone` | VARCHAR(20) | 手机号 |
| `password` | VARCHAR(255) | 密码 |
| `avatar` | VARCHAR(255) | 头像URL |
| `id_card` | VARCHAR(18) | 身份证号 |
| `disease_type` | VARCHAR(50) | 疾病类型 |
| `doctor_id` | BIGINT | 绑定医生ID |
| `doctor_name` | VARCHAR(100) | 医生姓名 |
| `has_follow_up` | BOOLEAN | 是否有随访 |
| `is_real_auth` | BOOLEAN | 是否实名认证 |
| `create_time` | DATETIME | 创建时间 |

**疾病类型**：MS、NMOSD、MG、MOGAD、自身免疫性脑炎、GBS、CIDP、其它疾病

#### doctor（医生表）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `name` | VARCHAR(100) | 姓名 |
| `phone` | VARCHAR(20) | 手机号 |
| `password` | VARCHAR(255) | 密码 |
| `hospital` | VARCHAR(100) | 医院 |
| `department` | VARCHAR(100) | 科室 |
| `title` | VARCHAR(50) | 职称 |
| `avatar` | VARCHAR(255) | 头像 |
| `qr_code` | VARCHAR(255) | 扫码绑定二维码 |
| `create_time` | DATETIME | 创建时间 |

#### follow_up（随访表）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `patient_id` | BIGINT | 患者ID |
| `patient_name` | VARCHAR(100) | 患者姓名 |
| `doctor_id` | BIGINT | 医生ID |
| `doctor_name` | VARCHAR(100) | 医生姓名 |
| `date` | DATETIME | 随访日期 |
| `project` | VARCHAR(100) | 随访项目 |
| `type` | VARCHAR(50) | 类型：门诊/住院 |
| `status` | VARCHAR(20) | 状态：pending/completed/cancelled |
| `content` | TEXT | 随访内容 |
| `outpatient_time` | DATETIME | 门诊时间 |
| `hospitalization_time` | DATE | 住院时间 |
| `hospital` | VARCHAR(100) | 医院 |
| `department` | VARCHAR(100) | 科室 |
| `notes` | TEXT | 备注 |

#### medication（用药表）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `patient_id` | BIGINT | 患者ID |
| `patient_name` | VARCHAR(100) | 患者姓名 |
| `doctor_id` | BIGINT | 医生ID |
| `drug_name` | VARCHAR(100) | 药品名称 |
| `dosage` | VARCHAR(50) | 剂量 |
| `frequency` | VARCHAR(50) | 频率 |
| `start_date` | DATE | 开始日期 |
| `end_date` | DATE | 结束日期 |
| `notes` | TEXT | 备注 |

#### admin（管理员表）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `username` | VARCHAR(100) | 用户名 |
| `password` | VARCHAR(255) | 密码 |
| `name` | VARCHAR(100) | 姓名 |
| `level` | INT | 权限等级 |
| `create_time` | DATETIME | 创建时间 |

---

## 错误处理

### 错误码定义

| 分类 | Code | 说明 |
|------|------|------|
| **成功** | 200 | 操作成功 |
| **客户端错误** | 400 | 参数错误 |
| | 401 | 未认证/Token过期 |
| | 403 | 权限不足 |
| | 404 | 资源不存在 |
| | 409 | 资源冲突（重复） |
| **服务端错误** | 500 | 系统内部错误 |
| **业务错误** | IMPORT_ERROR | 导入失败 |

### 错误响应格式

```json
{
  "code": 401,
  "message": "Token已过期",
  "data": null
}
```

### QMG 错误格式

```json
{
  "code": 0,
  "message": "用户名或密码错误",
  "data": null
}
```

---

## 部署指南

### 服务端口

| 服务 | 端口 |
|------|------|
| Hospital Platform | 8080 |
| MySQL | 3306 |
| Redis | 6379 |
| MinIO API | 9100 |
| MinIO Console | 9101 |

---

## 监控与健康检查

### Actuator 端点

| 端点 | 说明 |
|------|------|
| `/actuator/health` | 健康状态（MySQL、Redis 连接状态） |
| `/actuator/info` | 应用信息 |

### 健康检查响应

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

### 审计日志

审计日志记录到独立文件：`logs/hospital-platform-audit.log`

**审计操作类型**：

| 类型 | 说明 |
|------|------|
| LOGIN | 登录 |
| LOGOUT | 登出 |
| CREATE | 创建 |
| UPDATE | 更新 |
| DELETE | 删除 |
| CHANGE_PASSWORD | 修改密码 |
| KICK_OFFLINE | 踢下线 |
| QUERY | 查询 |

**日志格式**：

```
2024-01-15 10:30:00.000|LOGIN|认证|医生登录|userId=1|username=doctor001|module=qmg|success=true
```

---

## 开发指南

### 构建命令

```bash
# 编译
./gradlew build

# 编译（跳过测试）
./gradlew build -x test

# 运行
./gradlew bootRun

# 清理
./gradlew clean
```

### 日志级别

| Logger | Level (dev) | Level (prod) |
|--------|-------------|--------------|
| `org.hospital` | DEBUG | INFO |
| `org.springframework` | INFO | INFO |
| `com.baomidou.mybatisplus` | INFO | INFO |

### IDEA 控制台颜色显示

安装 IDEA 插件 **Grep Console** 以显示 ANSI 颜色。

---

## API 文档

- **Knife4j 文档**：http://localhost:8080/doc.html（增强版 Swagger UI，中文界面）
- **Swagger UI**：http://localhost:8080/swagger-ui.html
- **API Docs JSON**：http://localhost:8080/v3/api-docs

**分组**：

| Group | 路径前缀 |
|-------|----------|
| QMG | `/api/v1/qmg` |
| Neuroimmune | `/api/v1/neuroimmune` |
| OCR | `/api/v1/ocr` |
| Admin | `/api/v1/admin` |

---

## 常见问题

### Q: Token 验证失败怎么办？

检查：
1. Token 是否过期（默认 24 小时）
2. Redis 中是否存在该 Token
3. Token 格式是否正确（`Bearer {token}`）

### Q: 如何踢用户下线？

调用 `/api/v1/admin/session/kick`：

```json
{
  "userId": 1,
  "role": "doctor",
  "module": "qmg"
}
```

### Q: 数据库连接失败？

检查：
1. MySQL 是否启动
2. 数据库是否创建（QMG、neuroimmune）
3. 用户名密码是否正确
4. 连接 URL 格式是否正确

---

## 技术支持

- 项目地址：`E:\CODE\BS\Back\hospital-platform`
- API 文档：http://localhost:8080/swagger-ui.html