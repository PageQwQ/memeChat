# memeChat

一个 Fabric 聊天表情包模组：在聊天中通过 `:名字:` 语法把任意文本替换为图片（PNG 静态图 / GIF 动图），支持全局文本显示、聊天补全、表情选择面板与多资源包管理。

## 仓库结构

| 仓库 | 说明 |
|---|---|
| [PageQwQ/mcmcChat-core](https://github.com/PageQwQ/mcmcChat-core) | 核心纯 Java 共享层（表情注册表、解析器、grouplist），不依赖 Minecraft |
| [PageQwQ/memeChat](https://github.com/PageQwQ/memeChat)（本仓库） | 模组本体；`main` 分支为总览与测试资源包，**各支持版本代码分分支存放** |

本仓库分支列表：

| 分支 | 版本 |
|---|---|
| `1.21.1` | 1.21.1（旧式字体） |
| `1.21.2` ~ `1.21.11` | 1.21.2 ~ 1.21.11（各版本独立分支） |
| `26.1.2` / `26.2` | 26.1.2 / 26.2 |

克隆对应分支后可直接构建：

```bash
git clone -b 1.21.9 https://github.com/PageQwQ/memeChat.git
cd memeChat && ./gradlew build
```

## 功能特性

- **聊天表情包**：在聊天框输入 `:名字:`，发送后任意文本位置（聊天消息、告示牌、书本、命令等）都会显示对应图片
- **GIF 动图**：支持 GIF 帧动画，按帧间隔循环播放
- **聊天补全**：输入 `:名字` 前缀时弹出候选列表，候选左侧显示表情预览图
- **转义语法**：`\:名字:` 以普通文本形式显示，不替换为图片
- **表情选择面板**：点击聊天框上方 ☺ 按钮打开面板，左侧选择资源包、右侧按分组浏览表情，点击即可插入 `:名字:`
- **多资源包支持**：多个资源包的表情独立分组显示，包名直接取材质包文件夹名
- **分组翻页**：分组过多时 tab 行自动分页，`<` `>` 箭头翻页
- **自定义分组显示名**：通过 `grouplist.txt` 把分组目录名映射为任意显示名（如中文），绕开 Minecraft 资源路径只允许 ASCII 的限制

## 使用说明

| 输入 | 效果 |
|---|---|
| `:beluga:` | 显示名为 beluga 的表情包图片 |
| `\:beluga:` | 显示字面文本 `:beluga:`（转义） |
| `:be`（输入中） | 弹出补全候选，候选左侧显示预览图 |
| ☺ 按钮（聊天框上方） | 打开表情选择面板 |

## 资源包格式

表情包通过任意资源包加载，目录结构如下：

```
材质包/
├── pack.mcmeta
└── assets/
    └── memechat/
        └── memes/
            ├── beluga.png          # 直接放在 memes 下 → 分组 "default"
            ├── animated.gif        # GIF 动图
            ├── grouplist.txt       # 可选：分组显示名映射
            ├── memegroup/          # 子目录 → 分组 "memegroup"
            │   ├── examplememe.png
            │   └── examplememe2.png
            └── group2/
                └── aaaa.gif
```

- 文件名（不含扩展名）即为表情包名字，用于 `:名字:` 语法
- 子目录名即为分组名，在面板中按组浏览
- `grouplist.txt` 语法（每行一条，逗号结尾）：

```
memegroup/ == "自定义名字",
group2/ == "组别2",
```

## 支持版本

| 版本 | 说明 |
|---|---|
| 1.21.1 | 独立项目 `mc-1-21-1/` |
| 1.21.2 ~ 1.21.11 | 单项目 `mc-1-21-x/`，构建时用 `-Pmc_version=X` 切换 |
| 26.1.2 / 26.2 | 独立项目 `mc-26-1-2/`、`mc-26-2-x/` |

## 构建

```bash
# 1.21.x（1.21.2 ~ 1.21.11）
cd mc-1-21-x
./gradlew build -Pmc_version=1.21.9          # 任意 1.21.2~1.21.11

# 1.21.1
cd mc-1-21-1
./gradlew build

# 26.x
cd mc-26-1-2 && ./gradlew build
cd mc-26-2-x && ./gradlew build
```

运行游戏验证：

```bash
# 1.21.1 ~ 1.21.8 需要 JDK 21
JAVA_HOME=<jdk21路径> ./gradlew runClient

# 1.21.9 ~ 1.21.11 与 26.x 需要 JDK 25（mixins.json 声明了 JAVA_25）
JAVA_HOME=<jdk25路径> ./gradlew runClient -Pmc_version=1.21.9
```

## 测试资源包

`test-pack/` 内含示例资源包（beluga.png、animated.gif、分组示例、grouplist.txt），可复制到游戏的 resourcepacks 目录后启用。注意：资源包目录名与文件路径只允许小写字母、数字、`_`、`-`、`.`，中文或特殊字符会被 Minecraft 忽略。
