package org.zella.core

import java.nio.file.{Files, Paths}
import java.util.logging.Level

import com.gargoylesoftware.htmlunit.{BrowserVersion, WebClient}
import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import org.junit.Test
import org.zella.io.FileDownloader
import org.zella.web.{Mp3SpbWebCrawler, TempFileWebCrawler}

/**
  * @author zella.
  */
class EngineTest {

  //TODO Files.tempFolder
  val DOWNLOAD_FOLDER_STRING = "/Users/dru/tmp/downloader"
  val DOWNLOAD_FOLDER = Paths.get(DOWNLOAD_FOLDER_STRING)
  val ALBUM_LINK = "http://musicmp3spb.org/album/rajaz_remastered_2007.html"

  @Test
  def downloadAlbum() {
    val webClient = new WebClient(BrowserVersion.FIREFOX_45)
    webClient.getOptions.setThrowExceptionOnScriptError(false)
    webClient.getOptions.setThrowExceptionOnFailingStatusCode(false)
    java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF)

    val okHttp = new OkHttpClient()
    val fileDownloader = FileDownloader(okHttp)
    val downloadQueue = DownloadQueue(fileDownloader)
    val mp3SpbCrawler = new Mp3SpbWebCrawler(webClient)
    val tempFileCrawler = new TempFileWebCrawler(webClient)

    val engine = new Engine(webClient, mp3SpbCrawler, tempFileCrawler, downloadQueue)
    val album = engine.albumInfo(ALBUM_LINK).blockingSingle()

    engine.downloadAlbum(album, DOWNLOAD_FOLDER).blockingSubscribe()

    assertThat(Files.exists(DOWNLOAD_FOLDER)).isEqualTo(true)
    assertThat(Files.exists(Paths.get(DOWNLOAD_FOLDER_STRING, "Camel"))).isEqualTo(true)
    assertThat(Files.exists(Paths.get(DOWNLOAD_FOLDER_STRING, "Camel",
      "1999 Rajaz (Remastered 2007)"))).isEqualTo(true)
    assertThat(Files.exists(Paths.get(DOWNLOAD_FOLDER_STRING, "Camel",
      "1999 Rajaz (Remastered 2007)", "01 Three Wishes"))).isEqualTo(true)
    assertThat(Files.exists(Paths.get(DOWNLOAD_FOLDER_STRING, "Camel",
      "1999 Rajaz (Remastered 2007)", "02 Lost And Found"))).isEqualTo(true)
    assertThat(Files.exists(Paths.get(DOWNLOAD_FOLDER_STRING, "Camel",
      "1999 Rajaz (Remastered 2007)", "03 The Final Encore"))).isEqualTo(true)
    assertThat(Files.exists(Paths.get(DOWNLOAD_FOLDER_STRING, "Camel",
      "1999 Rajaz (Remastered 2007)", "04 Rajaz"))).isEqualTo(true)
    assertThat(Files.exists(Paths.get(DOWNLOAD_FOLDER_STRING, "Camel",
      "1999 Rajaz (Remastered 2007)", "05 Shout"))).isEqualTo(true)
    assertThat(Files.exists(Paths.get(DOWNLOAD_FOLDER_STRING, "Camel",
      "1999 Rajaz (Remastered 2007)", "06 Straight To My Heart"))).isEqualTo(true)
    assertThat(Files.exists(Paths.get(DOWNLOAD_FOLDER_STRING, "Camel",
      "1999 Rajaz (Remastered 2007)", "07 Sahara"))).isEqualTo(true)
    assertThat(Files.exists(Paths.get(DOWNLOAD_FOLDER_STRING, "Camel",
      "1999 Rajaz (Remastered 2007)", "08 Lawrence"))).isEqualTo(true)

    //no others file in dirs
    assertThat(Paths.get(DOWNLOAD_FOLDER_STRING).toFile.listFiles().length).isEqualTo(1)
    assertThat(Paths.get(DOWNLOAD_FOLDER_STRING, "Camel").toFile.listFiles().length).isEqualTo(1)
    assertThat(Paths.get(DOWNLOAD_FOLDER_STRING, "Camel",
      "1999 Rajaz (Remastered 2007)").toFile.listFiles().length).isEqualTo(8)



    //TODO that no others file

  }

}
