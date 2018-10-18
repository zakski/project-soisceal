package com.szadowsz.gospel.core.test.swi

import java.io.{BufferedReader, File, IOException, InputStreamReader}
import java.nio.file.{Files, Path}
import java.util.stream.Collectors

import scala.collection.JavaConverters._

object PrologModuleGetter {

  @throws[IOException]
  def getPrologFiles(path: String, classLoader: ClassLoader = Thread.currentThread().getContextClassLoader) = {
    val res = classLoader.getResource(path)
    val folder = new File(res.toURI)
    Files.walk(folder.toPath).collect(Collectors.toList()).asScala.toList.map(_.toFile).filter(_.getName.endsWith(".pl"))
  }
}
