<div align="center"><center>

# memeChat

A chat-meme mod: replace `:name:` syntax in chat with images (PNG stills / GIF animations) anywhere text renders. Includes chat completion, a meme picker panel, and multi-resource-pack support.

![Preview](/images/Preview.png)

</center></div>

## Repository Layout

| Branch | Contents |
|---|---|
| `main` | **Core code** — the pure-Java shared layer (`common/`, no Minecraft dependency): emoji registry, `:name:` parser, `grouplist.txt` parser, GIF decoder. Buildable standalone (`./gradlew build`). Also holds the test resource pack (`test-pack/`). |
| `1.21.x` | The mod for 1.21.1 – 1.21.11 (projects `mc-1-21-1/` and `mc-1-21-x/`) |
| `26.x` | The mod for 26.1.2 / 26.2 (projects `mc-26-1-2/` and `mc-26-2-x/`) |

## Features

- **Chat memes**: type `:name:` in chat; the image is rendered wherever the text appears (chat messages, signs, books, commands, etc.)
- **GIF animation**: animated GIFs play frame-by-frame at their stored frame delays
- **Chat completion**: typing `:name` shows candidate suggestions with the meme preview on the left
- **Escape syntax**: `\:name:` renders as literal text instead of an image
- **Meme picker panel**: click the ☺ button above the chat box to open the panel — pick a resource pack on the left, browse groups on the right, click a meme to insert `:name:`
- **Multi-pack support**: memes from different resource packs are listed independently; pack labels use the pack folder name
- **Group paging**: when there are too many groups, the tab row pages with `<` `>` arrows
- **Custom group display names**: map group folder names to any display name (e.g. Chinese) via `grouplist.txt`, working around Minecraft's ASCII-only resource path restriction

## Usage

| Input | Result |
|---|---|
| `:beluga:` | Renders the meme image named beluga |
| `\:beluga:` | Renders the literal text `:beluga:` (escaped) |
| `:be` (while typing) | Shows completion candidates with meme previews |
| ☺ button (above chat box) | Opens the meme picker panel |

## Resource Pack Format

Memes are loaded from any resource pack, using this layout:

```
pack/
├── pack.mcmeta
└── assets/
    └── memechat/
        └── memes/
            ├── beluga.png          # directly in memes/ → group "default"
            ├── animated.gif        # animated GIF
            ├── grouplist.txt       # optional: group display-name mapping
            ├── memegroup/          # subdirectory → group "memegroup"
            │   ├── examplememe.png
            │   └── examplememe2.png
            └── group2/
                └── aaaa.gif
```

- The file name (without extension) is the meme name used with `:name:`
- Each subdirectory is a group, browsed separately in the panel
- `grouplist.txt` syntax (one entry per line, comma-terminated):

```
memegroup/ == "Custom Name",
group2/ == "Group 2",
```

## Supported Versions

| Version | Branch | Project |
|---|---|---|
| 1.21.1 | `1.21.x` | `mc-1-21-1/` |
| 1.21.2 – 1.21.11 | `1.21.x` | `mc-1-21-x/` (switch with `-Pmc_version=X`) |
| 26.1.2 / 26.2 | `26.x` | `mc-26-1-2/` / `mc-26-2-x/` |

## Building

```bash
# Core library (main branch)
./gradlew build

# 1.21.x branch
git checkout 1.21.x
cd mc-1-21-x
./gradlew build -Pmc_version=1.21.9          # any version 1.21.2–1.21.11
cd ../mc-1-21-1 && ./gradlew build            # 1.21.1

# 26.x branch
git checkout 26.x
cd mc-26-1-2 && ./gradlew build
cd mc-26-2-x && ./gradlew build
```

Running the client for verification:

```bash
# 1.21.1 – 1.21.8 require JDK 21
JAVA_HOME=<jdk21 path> ./gradlew runClient

# 1.21.9 – 1.21.11 and 26.x require JDK 25 (mixins.json declares JAVA_25)
JAVA_HOME=<jdk25 path> ./gradlew runClient -Pmc_version=1.21.9
```

## Test Pack

`test-pack/` (on `main`) contains a sample resource pack (beluga.png, animated.gif, group examples, grouplist.txt). Copy it into your game's `resourcepacks` folder and enable it. Note: resource pack folder and file paths may only contain lowercase letters, digits, `_`, `-`, and `.` — Chinese or special characters are ignored by Minecraft.