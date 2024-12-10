# Contact App

一个功能完整的 Android 联系人应用，支持联系人管理、分组管理、短信和通话功能。基于 Material Design 设计规范，提供现代化的用户界面和流畅的用户体验。

<div align="center">
    <img src="screenshots/contacts.png" width="200" alt="联系人列表"/>
    <img src="screenshots/groups.png" width="200" alt="分组管理"/>
    <img src="screenshots/messages.png" width="200" alt="短信界面"/>
</div>

## 目录

- [功能特点](#功能特点)
- [技术特点](#技术特点)
- [开发环境](#开发环境)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [系统要求](#系统要求)
- [权限说明](#权限说明)
- [贡献指南](#贡献指南)
- [更新日志](#更新日志)
- [常见问题](#常见问题)
- [许可证](#许可证)

## 功能特点

### 联系人管理
- ✨ 添加、编辑、删除联系人
- 📝 支持详细联系人信息（姓名、电话、邮箱、公司等）
- 🔍 强大的搜索功能（支持姓名、拼音、电话号码搜索）
- 👥 灵活的分组管理

### 分组功能
- 📁 创建、编辑、删除分组
- 🎨 自定义分组颜色
- 📊 分组统计信息
- 🔄 批量移动联系人分组

### 短信功能
- 💬 发送和接收短信
- 📋 短信会话列表
- 📱 会话详情界面
- 👆 快速选择联系人

### 通话功能
- ☎️ 智能拨号盘
- 📞 通话记录管理
- ⚡ 一键拨号
- 📈 通话统计

## 技术特点

- 🎯 遵循 Material Design 3 设计规范
- 💾 使用 SQLite 数据库存储数据
- 📱 适配 Android 10.0 及以上版本
- 🌙 支持浅色/深色主题
- 🏗️ 采用 MVVM 架构模式
- 🔒 遵循 Android 最佳安全实践

## 开发环境

| 工具 | 版本 |
|------|------|
| Android Studio | Arctic Fox 或更高版本 |
| JDK | 11 或更高版本 |
| Android SDK | API 31 或更高版本 |
| Gradle | 7.0.2 或更高版本 |
| Build Tools | 31.0.0 或更高版本 |

## 快速开始

### 克隆项目

```bash
git clone https://github.com/yourusername/contact-app.git
cd contact-app
```

### 配置开发环境

1. 打开 Android Studio
2. 选择 "Open an existing Android Studio project"
3. 导航到克隆的项目目录并打开
4. 等待 Gradle 同步完成

### 运行项目

1. 连接 Android 设备或启动模拟器
2. 点击工具栏的 "Run" 按钮或使用快捷键 `Shift + F10`
3. 选择目标设备并确认

### 调试提示

- 使用 Logcat 查看日志输出
- 在 Debug 模式下运行以使用断点
- 使用 Android Profiler 分析性能

## 项目结构

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/contacts/
│   │   │   ├── activity/       # 界面活动
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── ContactDetailActivity.java
│   │   │   │   └── ...
│   │   │   ├── adapter/        # 列表适配器
│   │   │   │   ├── ContactsAdapter.java
│   │   │   │   ├── GroupAdapter.java
│   │   │   │   └── ...
│   │   │   ├── database/       # 数据库相关
│   │   │   │   ├── ContactDbHelper.java
│   │   │   │   └── ...
│   │   │   ├── fragment/       # 界面片段
│   │   │   │   ├── ContactsFragment.java
│   │   │   │   └── ...
│   │   │   ├── model/          # 数据模型
│   │   │   │   ├── Contact.java
│   │   │   │   └── ...
│   │   │   └── widget/         # 自定义控件
│   │   └── res/
│   │       ├── drawable/       # 图标资源
│   │       ├── layout/         # 布局文件
│   │       ├── menu/           # 菜单文件
│   │       └── values/         # 资源文件
│   └── test/                   # 测试文件
└── build.gradle                # 构建配置

```

## 系统要求

- Android 10.0 (API 29) 或更高版本
- 最小 RAM: 2GB
- 存储空间: 50MB 可用空间
- 权限: 联系人、电话、短信

## 权限说明

| 权限 | 用途 | 必需性 |
|------|------|--------|
| READ_CONTACTS | 读取联系人信息 | 必需 |
| WRITE_CONTACTS | 修改联系人信息 | 必需 |
| CALL_PHONE | 拨打电话功能 | 必需 |
| READ_CALL_LOG | 读取通话记录 | 可选 |
| SEND_SMS | 发送短信功能 | 可选 |
| READ_SMS | 读取短信功能 | 可选 |
| RECEIVE_SMS | 接收短信通知 | 可选 |

## 贡献指南

### 提交 Pull Request

1. Fork 项目
2. 创建特性分支
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. 提交更改
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
4. 推送到分支
   ```bash
   git push origin feature/AmazingFeature
   ```
5. 创建 Pull Request

### 代码规范

- 遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- 所有新代码必须包含适当的注释
- 提交信息要清晰明了
- 新功能需要包含测试用例

## 更新日志

### [1.0.0] - 2023-12-20
- 🎉 首次发布
- 📱 基本的联系人管理功能
- 👥 分组管理功能
- 💬 短信和通话功能

### [0.9.0] - 2023-12-10
- 🚀 Beta 版本发布
- 🐛 修复主要问题
- ✨ 性能优化

## 常见问题

### Q: 如何备份联系人数据？
A: 应用会自动同步到系统通讯录，您可以通过系统的备份功能进行备份。

### Q: 支持导入/导出联系人吗？
A: 支持标准的 vCard 格式导入导出，在设置中可以找到相关功能。

### Q: 如何恢复误删的联系人？
A: 可以通过系统通讯录的恢复功能找回最近删除的联系人。

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系我们

- 作者 - [@yourusername](https://twitter.com/yourusername)
- 项目地址 - [https://github.com/yourusername/contact-app](https://github.com/yourusername/contact-app)

---

如果您觉得这个项目有帮助，欢迎给个 star ⭐️
```


