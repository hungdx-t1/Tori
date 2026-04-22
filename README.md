# 🦅 Tori Server

**Tori Server** is a high-performance, multi-tenant Discord Bot Framework built on top of JDA (Java Discord API). It is designed to run multiple isolated Discord bots within a single JVM instance, drastically reducing memory consumption and CPU overhead.

By leveraging **Java 21 Virtual Threads (Project Loom)** and a strict **API-Server separation** architecture, Tori Server is capable of hosting dozens of bots smoothly, even on low-end VPS environments.

---

## ✨ Key Features

* **Multi-Tenant Architecture:** Run multiple standalone Discord bots simultaneously on a single server instance.
* **Isolated Environments:** Each bot is loaded via its own `URLClassLoader`, ensuring strict class isolation and preventing dependency conflicts between bots.
* **Clean API Design:** The project is split into `tori-api` (interfaces & providers) and `tori-server` (core implementation). Bot developers only interact with the safe API layer.
* **Rich Console & Logging:** Powered by **Log4j2**, featuring ANSI color support, custom thread-based bot identification, and an interactive command console (`bots`, `stop`).
* **Auto-Configuration:** Automatically generates and manages `config.yml` on the first boot.

---

## 📂 Project Structure

The project follows a standard Maven multi-module architecture:

```text
tori/
├── tori-api/       # The API layer. Exposes interfaces, records, and the ToriProvider.
└── tori-server/    # The Core engine. Handles JDA, BotLoader, Scheduler, and Logging.
```
---
## 🚀 Getting Started

**Prerequisites**
* Java 21 or higher (Strictly required).
* Maven 3.8+

**Building the Server**

To build the project, run the following Maven command in the root directory:
```bash
mvn clean package
```

This will generate a fat-jar containing all core dependencies located at `tori-server/target/tori-server.jar`.

**Running the Server**
1. Move the tori-server.jar to your deployment folder.

2. Run the server using:
```bash
mvn clean package
```

3. On the first startup, the server will generate a config.yml file and a bots/ folder.

---

## 🛠️ Developing a Bot for Tori Server

Developing a bot for Tori Server is incredibly simple. Your bot project does not need to shade JDA or Log4j2, as Tori Server provides them.

1. Add the Tori API Dependency

In your bot's `pom.xml`, include `tori-api` with the `provided` scope:

```XML
<repositories>
    <repository>
        <id>jitpack</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.dianxin.tori</groupId>
        <artifactId>tori-api</artifactId>
        <version>26.4.226</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

```
2. Create the Bot Main Class
   
Extend the `JavaDiscordBot` abstract class:

```java
import com.dianxin.tori.api.bot.JavaDiscordBot;

public class MyAwesomeBot extends JavaDiscordBot {

    @Override
    protected String getBotToken() {
        return "YOUR_DISCORD_BOT_TOKEN_HERE";
    }

    @Override
    public void onEnable() {
        getLogger().info("Hello World from MyAwesomeBot!");
        // Register your commands/listeners here
    }
}
```
3. Create the `bot.yml` Metadata

Create a `bot.yml` file in your `src/main/resources/` directory:

```yaml
name: "MyAwesomeBot" # required
version: "1.0.0"
author: "YourName" # required
description: "A cool music and moderation bot"
main: "com.yourdomain.bot.MyAwesomeBot" # Path to your main class, required
contributors: ["ConName1", "ConName2"]
website: "https://example.com"
ownerId: "0000000000000000" # required
```

4. Deploy

Compile your bot into a standard `.jar` file and drop it into the `bots/` folder of your running Tori Server. Restart the server, and Tori will automatically detect, load, and run your bot in its own Thread!

---

## 🛑 Console Commands
Tori Server comes with an interactive console. Type these commands directly into the terminal:
* `bots` - Displays a list of all currently active bots, their versions, and authors.
* `stop` - Safely shuts down all JDA instances, saves data, and stops the server.

---

📄 License
This project is licensed under the MIT License - see the LICENSE file for details.