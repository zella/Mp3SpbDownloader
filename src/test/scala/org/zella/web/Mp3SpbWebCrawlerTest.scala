package org.zella.web

import java.util.logging.Level

import com.gargoylesoftware.htmlunit.{BrowserVersion, WebClient}
import com.google.common.truth.Truth._
import org.junit.{After, Before, Test}

/**
  * @author zella.
  */
class Mp3SpbWebCrawlerTest {

  val VALID_ALBUM_LINK = "http://musicmp3spb.org/album/rajaz_remastered_2007.html"

  var webClient: WebClient = _

  @Before def setup() {
    webClient = new WebClient(BrowserVersion.FIREFOX_45)
    webClient.getOptions.setThrowExceptionOnScriptError(false)
    webClient.getOptions.setThrowExceptionOnFailingStatusCode(false)
    java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF)
  }

  @Test
  def albumInfo() {
    val webCrawler = new Mp3SpbWebCrawler(webClient)
    val albumInfo = webCrawler.albumInfo(VALID_ALBUM_LINK)

    println(albumInfo)

    assertThat(albumInfo.artist).isEqualTo("Camel")
    assertThat(albumInfo.year).isEqualTo("1999")
    assertThat(albumInfo.title).isEqualTo("Rajaz (Remastered 2007)")
    assertThat(albumInfo.songs.size()).isEqualTo(8)


    assertThat(albumInfo.songs.get(1).name).isEqualTo("Three Wishes")
    assertThat(albumInfo.songs.get(1).downloadLink).startsWith("http://musicmp3spb.org/download/rajaz_remastered_2007_three_wishes/")
    assertThat(albumInfo.songs.get(1).downloadLink.length.asInstanceOf[Integer]).isGreaterThan(70)
    assertThat(albumInfo.songs.get(1).playLink).startsWith("http://musicmp3spb.org/download/rajaz_remastered_2007_three_wishes/play/")
    assertThat(albumInfo.songs.get(1).playLink.length.asInstanceOf[Integer]).isGreaterThan(70)
    // TODO ...

    assertThat(albumInfo.songs.get(8).name).isEqualTo("Lawrence")
    assertThat(albumInfo.songs.get(8).downloadLink).startsWith("http://musicmp3spb.org/download/lawrence/")
    assertThat(albumInfo.songs.get(8).downloadLink.length.asInstanceOf[Integer]).isGreaterThan(70)
    assertThat(albumInfo.songs.get(8).playLink).startsWith("http://musicmp3spb.org/download/lawrence/play/")
    assertThat(albumInfo.songs.get(8).playLink.length.asInstanceOf[Integer]).isGreaterThan(70)


  }


  @After def clickDownloadLink() {
    webClient.close()
  }
}