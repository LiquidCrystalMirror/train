# 环境变量配置说明

## 📋 概述

本项目使用 `.env` 文件管理敏感配置信息（数据库密码、JWT密钥等），避免将敏感信息硬编码在代码中或提交到版本控制系统。

## 🚀 快速开始

### 1. 创建 .env 文件

```bash
# 复制示例文件
cp .env.example .env
```

### 2. 配置环境变量

编辑 `.env` 文件，填入您的实际配置：

```env
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=sqlprogram
DB_USERNAME=root
DB_PASSWORD=your_actual_password

# JWT 配置
JWT_SECRET=your_jwt_secret

# MD5 Salt 配置
MD5_SALT=your_md5_salt
```

### 3. 启动应用

正常启动 Spring Boot 应用即可，`spring-dotenv` 会自动加载 `.env` 文件中的配置。

## 📁 文件说明

| 文件 | 说明 | 是否提交到Git |
|------|------|--------------|
| `.env` | 实际的环境变量配置文件 | ❌ **不提交** |
| `.env.example` | 环境变量配置模板 | ✅ **提交** |

## 🔒 安全注意事项

1. **永远不要**将 `.env` 文件提交到版本控制系统
2. **永远不要**在代码中硬编码敏感信息
3. 为不同环境（开发、测试、生产）使用不同的 `.env` 文件
4. 定期更换密码和密钥

## 📝 配置项说明

### 数据库配置

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_HOST` | 数据库主机地址 | localhost |
| `DB_PORT` | 数据库端口 | 3306 |
| `DB_NAME` | 数据库名称 | sqlprogram |
| `DB_USERNAME` | 数据库用户名 | root |
| `DB_PASSWORD` | 数据库密码 | - |

### JWT 配置

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `JWT_SECRET` | JWT签名密钥 | abc123 |

### MD5 配置

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `MD5_SALT` | MD5加密盐值 | 123(code) |

## 🔧 技术实现

项目使用 `spring-dotenv` 库自动加载 `.env` 文件中的环境变量，并在 Spring Boot 配置文件中通过 `${VAR_NAME:default_value}` 语法引用。

示例：
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:sqlprogram}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:052011}
```

## 💡 最佳实践

1. **开发环境**：在项目根目录创建 `.env` 文件
2. **团队协作**：只提交 `.env.example`，团队成员各自创建自己的 `.env`
3. **生产环境**：使用服务器环境变量或专业的密钥管理服务（如 AWS Secrets Manager、HashiCorp Vault）
4. **备份配置**：妥善保管 `.env` 文件的备份

## ⚠️ 常见问题

### Q: 修改 .env 后需要重启应用吗？
A: 是的，`.env` 文件在应用启动时加载，修改后需要重启应用才能生效。

### Q: 可以在代码中直接读取环境变量吗？
A: 可以，使用 `System.getenv("DB_HOST")` 或在 Spring 中使用 `@Value("${DB_HOST}")`。

### Q: 如果 .env 文件不存在会怎样？
A: 系统会使用配置文件中的默认值（冒号后面的值），例如 `${DB_HOST:localhost}` 会使用 `localhost`。
