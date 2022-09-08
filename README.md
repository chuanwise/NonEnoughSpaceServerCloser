# 外存不足关服器 - NonEnoughSpaceServerCloser

当 Minecraft 服务器外存不足时，将会有插件在后台打印日志「java.io.IOException: 设备上没有空间」，此后服务器的操作（如玩家建筑等）都不会被保存。但玩家无法察觉此问题。

约两小时后，服务器才会崩溃。重启服务器后，**玩家在这两小时以内的全部劳动都会白费**。`CoreProtect` 之类的也无法记录，因而谈不上还原了。

椽子说，要在外存不足时立刻关闭服务器，于是 **外存不足关服器** 诞生了。

## 配置

到最新的 [RELEASE](https://github.com/Chuanwise/NonEnoughSpaceServerCloser) 处下载插件，将其放在 Bukkit 或兼容 Bukkit API 的服务器的 `plugins` 文件夹下，重启或通过 `plugman` 载入插件。

受限于 JVM，我们无法获取所有线程抛出的异常，也不能通过实时检查磁盘空间（Linux 服务器将会把文件内容暂存在缓冲区中，此时虽磁盘空间仍显示有余，但程序无法写入），只能通过日志检测实现功能。

内存不足的异常信息是本地化的，而且和操作系统有关，因此需要配置需要触发关服的异常信息（即你的平台上的那句「java.io.IOException: 设备上没有空间」）。

* Locale 为 CHINA 或 US 的 Linux 服务器无需进一步配置，插件将自动采用合适的配置。
* 其他服务器需要修改 config.yml 中的 message.error。

当任何一个插件打印了形如 `${message.error}` 或 `[xxx] [xxx] ... [xxx]: ${message.error}` 的日志时，插件将立刻踢出所有玩家，并向其显示配置中的 `message.kick`