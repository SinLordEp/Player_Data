# 🚀 **项目简介 / Project Overview**

本项目是作为 "Acceso de Datos" 课程的一部分，它采用多层架构，包括控制层、数据层和 GUI 层。

This project was developed as part of the "Acceso de Datos" course. It follows a multi-layer architecture, including control, data, and GUI layers.

本项目是一个基于 Java 的多层结构应用程序，包含控制层、数据层和 GUI 层。主要用于管理玩家信息，并支持多种数据源和操作方式。

This project is a multi-layer Java application consisting of control, data, and GUI layers. It is mainly used for managing player information and supports multiple data sources and operations.

<br/>

# ✨ **功能特性 / Features**

- 支持多种数据源：文件、数据库、Hibernate、PHP、ObjectDB、BaseX、MongoDB。

- 提供基本的 CRUD 操作：读取、搜索、添加、修改、删除。

- 工厂模式管理数据操作，支持高扩展性。

- 控制层与数据层解耦，UI 与逻辑分离。

- Supports multiple data sources: File, Database, Hibernate, PHP, ObjectDB, BaseX, MongoDB.

- Provides basic CRUD operations: Read, Search, Add, Modify, Delete.

- Factory pattern for data operations, supporting high scalability.

- Decouples control and data layers, separating UI from business logic.

<br/>

# 🔧 **配置说明 / Configuration**

- 配置文件位于 `src/main/resources` 目录下。

- 可以根据不同环境修改数据库连接和数据源配置。

- Configuration files are located in `src/main/resources`.

- Modify database connection and data source settings for different environments.

<br/>

# 📚 **使用方法 / Usage**

- 启动程序后，可以通过图形界面进行玩家信息的管理，包括增删改查操作。

- 支持多数据源切换，可以在设置中选择数据来源。

- After starting the application, use the GUI to manage player information, including CRUD operations.

- Support switching between multiple data sources, selectable in settings.

<br/>

# 🗂 **目录结构 / Directory Structure**

```
control/        # 控制层，管理 UI 和数据交互
 └── PlayerControl.java

data/           # 数据层，封装数据操作和 CRUD 功能
 ├── CRUDFactory.java
 ├── DataOperation.java
 ├── DataSource.java
 ├── GeneralDAO.java
 ├── PlayerDAO.java
 ├── PlayerParser.java
 └── database/  # 数据库操作类
     ├── BaseXCRUD.java
     ├── DatabaseCRUD.java
     ├── HibernateCRUD.java
     ├── MongoCRUD.java
     ├── ObjectDBCRUD.java
     └── SqlDialect.java

exceptio...        # 配置文件
 ├── config.properties
 ├── control/
 │   └── logback.xml
 ├── data/
 │   ├── hibernate.cfg.xml
 │   ├── player_data.odb
 │   └── player_databaseInfo.yaml
 ├── data/php/
 │   ├── default_mysql.php
 │   ├── read_player.php
 │   ├── search_player.php
 │   ├── validate_data.php
 │   └── write_player.php
 ├── GUI/
 │   └── player_dialog.yaml
 └── import/
     ├── player_data.dat
     ├── player_txt.txt
     └── player_xml.xml
```
# 🤝 **开发与贡献 / Contribution**

- 欢迎提交 Issue 和 Pull Request。

- 请遵循代码风格和注释规范。

- Feel free to submit Issues and Pull Requests.

- Follow coding style and comment guidelines.

<br/>

# 📄 **许可协议 / License**

本项目遵循 MIT 许可协议。

This project is licensed under the MIT License.

