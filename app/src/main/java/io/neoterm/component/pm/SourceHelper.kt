package io.neoterm.component.pm

import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.frontend.logging.NLog
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author kiva
 */
object SourceHelper {
  fun syncSource() {
    val sourceManager = ComponentManager.getComponent<PackageComponent>().sourceManager
    syncSource(sourceManager)
  }

  fun syncSource(sourceManager: SourceManager) {
    val content = buildString {
      this.append("# Generated by NeoTerm-Preference\n")
      sourceManager.getEnabledSources()
        .joinTo(this, "\n") { "deb [trusted=yes] ${it.url} ${it.repo}\n" }
    }
    kotlin.runCatching {
      Files.write(Paths.get(NeoTermPath.SOURCE_FILE), content.toByteArray())
    }
  }

  fun detectSourceFiles(): List<File> {
    val sourceManager = ComponentManager.getComponent<PackageComponent>().sourceManager
    val sourceFiles = ArrayList<File>()
    try {
      val prefixes = sourceManager.getEnabledSources()
        .map { detectSourceFilePrefix(it) }
        .filter { it.isNotEmpty() }

      File(NeoTermPath.PACKAGE_LIST_DIR)
        .listFiles()
        .filterTo(sourceFiles) { file ->
          prefixes.filter { file.name.startsWith(it) }
            .count() > 0
        }
    } catch (e: Exception) {
      sourceFiles.clear()
      NLog.e("PM", "Failed to detect source files: ${e.localizedMessage}")
    }

    return sourceFiles
  }

  fun detectSourceFilePrefix(source: Source): String {
    try {
      val url = URL(source.url)
      val builder = StringBuilder(url.host)
      if (url.port != -1) {
        builder.append(":${url.port}")
      }

      val path = url.path
      if (path != null && path.isNotEmpty()) {
        builder.append("_")
        val fixedPath = path.replace("/", "_").substring(1) // skip the last '/'
        builder.append(fixedPath)
      }
      builder.append("_dists_${source.repo.replace(" ".toRegex(), "_")}_binary-")
      return builder.toString()
    } catch (e: Exception) {
      NLog.e("PM", "Failed to detect source file prefix: ${e.localizedMessage}")
      return ""
    }
  }
}