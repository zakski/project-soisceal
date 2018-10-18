package com.szadowsz.gospel.core.db.libs

import java.io.{BufferedReader, File, FileInputStream, FileReader}

import com.szadowsz.gospel.core.event.io.OutputEvent
import com.szadowsz.gospel.core.listener.OutputListener
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Theory}
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ISOIOLibrarySpec extends FlatSpec with BaseEngineSpec {

  private val file = new File(".")
  private val writePath = file.getCanonicalPath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "writeFile.txt"
  private val readPath = file.getCanonicalPath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "readFile.txt"
  private val binPath = file.getCanonicalPath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "binFile.bin"


  override protected def init(): PrologEngine = new PrologEngine(Array("com.szadowsz.gospel.core.db.libs.BasicLibrary", "com.szadowsz.gospel.core.db.libs.ISOIOLibrary"))

  private def getStringDataWritten(filePath: String) = {
    val reader = new BufferedReader(new FileReader(filePath))
    val dataRead = reader.readLine
    reader.close()
    dataRead
  }

  private def getByteDataWritten(filePath: String) = {
    val fins = new FileInputStream(filePath)
    val dataRead = fins.read
    fins.close()
    dataRead
  }

  behavior of "ISO IO Library"

  it should "open an existing file" in {
    val info = prolog.solve("open('" + writePath + "','write',X,[alias('editor'), type(text)]).")
    info.isSuccess shouldBe true
  }

  it should "not be able to open a non-existant file" in {
    val info = prolog.solve("open('" + writePath.replace(".txt", ".myext") + "','write',X,[alias('editor'), type(text)]).")
    info.isSuccess shouldBe false
  }

  it should "not be able to take in a list instead of a variable" in {
    val info = prolog.solve("open('" + writePath + "','read',[]).")
    info.isSuccess shouldBe false
  }

  it should "not be able to take in a variable instead of a list" in {
    val info = prolog.solve("open('" + writePath + "','read',X,X).")
    info.isSuccess shouldBe false
  }

  it should "not be able to pass a property into an illicit list" in {
    val info = prolog.solve("open('" + writePath + "','read',X,[ciao(caramelle)]).")
    info.isSuccess shouldBe false
  }

  it should "in this way also the close and the flush and then also the auxiliary functions" in {
    val theoryText = "test:- open('" + writePath + "','write',X),close(X,force(true)).\n"
    prolog.setTheory(new Theory(theoryText))
    val info = prolog.solve("test.")
    info.isSuccess shouldBe false
  }

  it should "complete test 2 succesfully" in {
    val dataToWrite = "B"
    val theory = "test2:-" + "open('" + writePath + "','write',X,[alias(ciao),type(text),eof_action(reset),reposition(true)]),write_term('ciao','" + dataToWrite + "',[quoted(true)]),close(X)."
    prolog.setTheory(new Theory(theory))
    val info = prolog.solve("test2.")
    info.isSuccess shouldBe true
    getStringDataWritten(writePath) shouldBe dataToWrite
  }

  it should "complete test 3 succesfully" in {
    val dataToWrite1 = "term."
    val dataToWrite2 = "ciao."
    val theory = "test3:- " + "open('" + readPath + "','write',X,[alias(ciao, computer, casa, auto),type(text),eof_action(reset),reposition(true)])," + "open('" + writePath + "','write',Y,[alias(telefono, rosa),type(text),eof_action(reset),reposition(true)])," + "write_term('telefono','" + dataToWrite1 + "',[quoted(true)])," + "write_term('auto','" + dataToWrite2 + "',[quoted(true)])," + "close(X)," + "close(Y)."
    prolog.setTheory(new Theory(theory))
    val info = prolog.solve("test3.")
    info.isSuccess shouldBe true
    getStringDataWritten(writePath) shouldBe dataToWrite1
    getStringDataWritten(readPath) shouldBe dataToWrite2
  }

  it should "complete test 4 succesfully" in {
    val dataToWrite = "term."
    val theory = "test4:-" + "open('" + writePath + "','write',Y,[alias(telefono, casa),type(text),eof_action(reset),reposition(true)])," + "write_term('telefono','" + dataToWrite + "',[quoted(true)])," + "flush_output('casa')," + "close(Y)."
    prolog.setTheory(new Theory(theory))
    val info = prolog.solve("test4.")
    info.isSuccess shouldBe true
    getStringDataWritten(writePath) shouldBe dataToWrite
  }

  it should "complete test 5 succesfully" in {
    val dataToRead = "ciao"
    // Per beccare l'output
    //TODO Da rivedere
    val listener = new OutputListener() {
      override def onOutput(e: OutputEvent): Unit = {
        e.getMsg shouldBe dataToRead
      }
    }
    prolog.addOutputListener(listener)
    val theory = "test5:-" + "open('" + readPath + "','read',X,[alias(reading, nome),type(text),eof_action(reset),reposition(true)])," + "read_term(X,I,[])," + "write('user_output', I)," + "close('reading')."
    prolog.setTheory(new Theory(theory))
    val info = prolog.solve("test5.")
    info.isSuccess shouldBe true
    prolog.removeOutputListener(listener)
  }

  it should "complete test 6 succesfully" in {
    val dataToRead = Array("c", "\n", "iao")
    val listener = new OutputListener() {
      private var count = 0

      override def onOutput(e: OutputEvent): Unit = {
        e.getMsg shouldBe dataToRead(count)
        count += 1
      }
    }
    prolog.addOutputListener(listener)
    val theory = "test6:-" + "open('" + readPath + "','read',X,[alias(reading, nome),type(text),eof_action(reset),reposition(true)])," + "get_char('reading',M)," + "read_term(X,J,[])," + "write(M)," + "nl('user_output')," + "write(J)," + "close(X)."
    prolog.setTheory(new Theory(theory))
    val info = prolog.solve("test6.")
    info.isSuccess shouldBe true
    prolog.removeOutputListener(listener)
  }

  it should "complete test 7 succesfully" in {
    val dataToRead = "c"
    val listener = new OutputListener() {
      override def onOutput(e: OutputEvent): Unit = {
        e.getMsg shouldBe dataToRead
      }
    }
    prolog.addOutputListener(listener)
    val theory = "test7:- put_char('user_output',c)."
    prolog.setTheory(new Theory(theory))
    val info = prolog.solve("test7.")
    info.isSuccess shouldBe true
    prolog.removeOutputListener(listener)
  }

  it should "complete test 8 succesfully" in {
    val dataToRead = 51
    val listener = new OutputListener() {
      override def onOutput(e: OutputEvent): Unit = {
        e.getMsg shouldBe dataToRead.toString
      }
    }
    prolog.addOutputListener(listener)
    val theory = "test8:-" + "open('" + binPath + "','read',X,[alias(readCode, nome),type(binary),eof_action(reset),reposition(true)])," + "peek_byte('nome', PB)," + "write(PB)," + "close(X)."
    prolog.setTheory(new Theory(theory))
    val info = prolog.solve("test8.")
    info.isSuccess shouldBe true
    prolog.removeOutputListener(listener)
  }

  it should "complete test 9 succesfully" in {
    val dataToWrite = 51
    val theory = "test9:-" + "open('" + binPath + "','write',X,[alias(readCode, nome),type(binary),eof_action(reset),reposition(true)])," + "put_byte('nome'," + dataToWrite + ")," + "flush_output('nome')," + "close(X)."
    prolog.setTheory(new Theory(theory))
    val info = prolog.solve("test9.")
    info.isSuccess shouldBe true
    getByteDataWritten(binPath) shouldBe dataToWrite
  }

  it should "complete test 10 succesfully" in {
    val dataToRead = Array(99, 105, 105)
    // 'c', 'i', 'i'
    val listener = new OutputListener() {
      private var count = 0
      override def onOutput(e: OutputEvent): Unit = {
        e.getMsg shouldBe dataToRead(count).toString
        count += 1
      }
    }
    prolog.addOutputListener(listener)
    val theory = "test10:-" + "open('" + readPath + "','read',X,[alias(reading, nome),type(text),eof_action(reset),reposition(true)])," + "get_code('reading',M)," + "peek_code('nome',N)," + "peek_code(X,O)," + "write(M)," + "write(N)," + "write(O)," + "close(X)."
    prolog.setTheory(new Theory(theory))
    val info = prolog.solve("test10.")
    info.isSuccess shouldBe true
    prolog.removeOutputListener(listener)
  }

}
