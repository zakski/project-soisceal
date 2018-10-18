package com.szadowsz.gospel.core.utils

object VersionInfo {

  def getEngineVersion: String = "2.0.0"// TODO get working with intellij tests - getClass.getPackage.getImplementationVersion

  def getPlatform: String = {
    val vmName = System.getProperty("java.vm.name")
    if (vmName.contains("Java")) { //"Java HotSpot(TM) Client VM"
      "Java"
    }
    else throw new RuntimeException
  }
}