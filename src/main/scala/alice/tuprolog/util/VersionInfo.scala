package alice.tuprolog.util

object VersionInfo {
  private val ENGINE_VERSION: String = "3.0"
  private val JAVA_SPECIFIC_VERSION: String = "0"

  def getEngineVersion: String = ENGINE_VERSION


  def getPlatform: String = {
    val vmName: String = System.getProperty("java.vm.name")
    if (vmName.contains("Java")){
      "Java"
    } else {
      throw new RuntimeException
    }
  }

  def getSpecificVersion: String = {
    val vmName: String = System.getProperty("java.vm.name")
    if (vmName.contains("Java")) {
      JAVA_SPECIFIC_VERSION
    } else {
      throw new RuntimeException
    }
  }

  def getCompleteVersion: String = getEngineVersion + "." + getSpecificVersion
}