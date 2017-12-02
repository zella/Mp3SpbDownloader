package org.zella.core

import java.nio.file.{Files, Paths}
import java.util.concurrent.Executors
import java.util.logging.Level

import com.gargoylesoftware.htmlunit.{BrowserVersion, WebClient}
import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import org.junit.rules.TemporaryFolder
import org.junit.{Rule, Test}
import org.zella.io.FileDownloader
import org.zella.web.{Mp3SpbWebCrawler, TempFileWebCrawler}

import scala.annotation.meta.getter

/**
  * @author zella.
  */
class EngineTest {

  @(Rule@getter)
  val tempFolder = new TemporaryFolder

  @Test
  def downloadAlbum() {

    val folder = tempFolder.newFolder("albums").toPath

    val engine: Engine = initEngine

    val album = engine.albumInfo("http://musicmp3spb.org/album/rajaz_remastered_2007.html").blockingSingle()

    engine.downloadAlbum(album, folder).blockingSubscribe()

    assertThat(Files.exists(folder)).isEqualTo(true)
    assertThat(Files.exists(folder.resolve("Camel"))).isEqualTo(true)
    assertThat(Files.exists(folder.resolve("Camel").resolve(
      "1999 Rajaz (Remastered 2007)"))).isEqualTo(true)
    assertThat(Files.exists(folder.resolve("Camel").resolve(
      "1999 Rajaz (Remastered 2007)").resolve("01 Three Wishes.mp3"))).isEqualTo(true)
    assertThat(Files.exists(folder.resolve("Camel").resolve(
      "1999 Rajaz (Remastered 2007)").resolve("02 Lost And Found.mp3"))).isEqualTo(true)
    assertThat(Files.exists(folder.resolve("Camel").resolve(
      "1999 Rajaz (Remastered 2007)").resolve("03 The Final Encore.mp3"))).isEqualTo(true)
    assertThat(Files.exists(folder.resolve("Camel").resolve(
      "1999 Rajaz (Remastered 2007)").resolve("04 Rajaz.mp3"))).isEqualTo(true)
    assertThat(Files.exists(folder.resolve("Camel").resolve(
      "1999 Rajaz (Remastered 2007)").resolve("05 Shout.mp3"))).isEqualTo(true)
    assertThat(Files.exists(folder.resolve("Camel").resolve(
      "1999 Rajaz (Remastered 2007)").resolve("06 Straight To My Heart.mp3"))).isEqualTo(true)
    assertThat(Files.exists(folder.resolve("Camel").resolve(
      "1999 Rajaz (Remastered 2007)").resolve("07 Sahara.mp3"))).isEqualTo(true)
    assertThat(Files.exists(folder.resolve("Camel").resolve(
      "1999 Rajaz (Remastered 2007)").resolve("08 Lawrence.mp3"))).isEqualTo(true)

    //no others file in dirs
    assertThat(Paths.get(folder.toString).toFile.listFiles().length).isEqualTo(1)
    assertThat(Paths.get(folder.toString, "Camel").toFile.listFiles().length).isEqualTo(1)
    assertThat(Paths.get(folder.toString, "Camel",
      "1999 Rajaz (Remastered 2007)").toFile.listFiles().length).isEqualTo(8)
  }

  @Test
  def issue6Github() {

    val folder = tempFolder.newFolder("albums").toPath

    val engine: Engine = initEngine

    val album = engine.albumInfo("http://musicmp3spb.org/album/brat_2.html").blockingSingle()

    engine.downloadAlbum(album, folder).blockingSubscribe()

    assertThat(Files.exists(folder)).isEqualTo(true)
    assertThat(Files.exists(folder.resolve("Soundtracks"))).isEqualTo(true)
    assertThat(Files.exists(folder.resolve("Soundtracks").resolve(
      "Брат-2"))).isEqualTo(true)
//    assertThat(Files.exists(folder.resolve("Camel").resolve(
//      "1999 Rajaz (Remastered 2007)").resolve("01 Three Wishes.mp3"))).isEqualTo(true)
//    assertThat(Files.exists(folder.resolve("Camel").resolve(
//      "1999 Rajaz (Remastered 2007)").resolve("02 Lost And Found.mp3"))).isEqualTo(true)
//    assertThat(Files.exists(folder.resolve("Camel").resolve(
//      "1999 Rajaz (Remastered 2007)").resolve("03 The Final Encore.mp3"))).isEqualTo(true)
//    assertThat(Files.exists(folder.resolve("Camel").resolve(
//      "1999 Rajaz (Remastered 2007)").resolve("04 Rajaz.mp3"))).isEqualTo(true)
//    assertThat(Files.exists(folder.resolve("Camel").resolve(
//      "1999 Rajaz (Remastered 2007)").resolve("05 Shout.mp3"))).isEqualTo(true)
//    assertThat(Files.exists(folder.resolve("Camel").resolve(
//      "1999 Rajaz (Remastered 2007)").resolve("06 Straight To My Heart.mp3"))).isEqualTo(true)
//    assertThat(Files.exists(folder.resolve("Camel").resolve(
//      "1999 Rajaz (Remastered 2007)").resolve("07 Sahara.mp3"))).isEqualTo(true)
//    assertThat(Files.exists(folder.resolve("Camel").resolve(
//      "1999 Rajaz (Remastered 2007)").resolve("08 Lawrence.mp3"))).isEqualTo(true)

    //no others file in dirs
    assertThat(Paths.get(folder.toString).toFile.listFiles().length).isEqualTo(1)
    assertThat(Paths.get(folder.toString, "Soundtracks").toFile.listFiles().length).isEqualTo(1)
    assertThat(Paths.get(folder.toString, "Soundtracks",
      "Брат-2").toFile.listFiles().length).isEqualTo(14)
  }

  private def initEngine = {
    val webClient = new WebClient(BrowserVersion.FIREFOX_45)
    webClient.getOptions.setJavaScriptEnabled(false)
    webClient.getOptions.setThrowExceptionOnScriptError(false)
    webClient.getOptions.setThrowExceptionOnFailingStatusCode(false)
    webClient.getOptions.setCssEnabled(false)
    java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF)

    val okHttp = new OkHttpClient()
    val fileDownloader = FileDownloader(okHttp)
    val downloadQueue = DownloadQueue(fileDownloader, Executors.newFixedThreadPool(2))
    val mp3SpbCrawler = new Mp3SpbWebCrawler(webClient)
    val tempFileCrawler = new TempFileWebCrawler(webClient)

    val engine = new Engine(webClient, mp3SpbCrawler, tempFileCrawler, downloadQueue)
    engine
  }
}
