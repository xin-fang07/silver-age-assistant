# 基于LLM的银发智能生活助手系统

面向老年用户的AI生活助手平台，采用家属端 + 管理员端双端架构，帮助老年人便捷管理日常健康与生活事务，同时让家属远程关注老人状态。

## 技术栈

**后端**：Spring Boot 3 + MyBatis-Plus + MySQL 8.0 + Redis
**前端**：Vue 3 + Element Plus + Axios
**AI能力**：DeepSeek LLM + Prompt工程 + 结构化JSON输出解析
**测试工具**：Postman + MySQL + 浏览器开发者工具

## 核心功能

- **老人档案管理**：老人基本信息、健康档案、紧急联系人维护
- **健康监测**：血压、血糖、心率等指标记录与异常预警
- **智能问答**：基于DeepSeek大模型的老年生活咨询与健康建议
- **SOS紧急求助**：一键呼救，自动通知家属并逐级升级告警
- **生活提醒**：吃药提醒、日程提醒、家属端推送通知
- **家属端**：远程查看老人健康数据、接收预警通知、管理提醒事项
- **管理员端**：用户管理、系统监控、数据统计

## 项目亮点

### AI应用测试与容错设计
- 设计分层Prompt约束LLM输出结构化JSON，实现正则提取、JSON修复、降级模板等多层容错机制
- 针对空值、极端输入、边界场景进行AI输出稳定性测试，提升复杂输入场景下的输出可靠性
- 设计并执行30余条功能测试用例，覆盖正常流程、异常输入及边界场景
- 使用Postman进行RESTful接口测试，验证请求参数、响应数据、HTTP状态码及异常场景
- 发现并跟踪闭环缺陷10个，通过回归测试验证问题修复效果

### 工程化实践
- 敏感配置全部通过环境变量注入，配置与代码分离
- 数据库连接池HikariCP优化，支持高并发场景
- 接口限流防护，防止暴力登录和API滥用
- 全局异常处理与统一响应格式

## 快速启动

### 环境要求
- JDK 17+
- Node.js 16+
- MySQL 8.0+

### 后端启动
```bash
# 配置环境变量
export DB_URL="jdbc:mysql://localhost:3306/elder_ai"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
export DEEPSEEK_API_KEY="your_api_key"
export JWT_SECRET="your_jwt_secret"

# 启动后端
cd elder-ai-server
mvn spring-boot:run
前端启动
bash
cd elder-ai-web
npm install
npm run dev
作者
张馨方・张家界学院 计算机科学与技术
