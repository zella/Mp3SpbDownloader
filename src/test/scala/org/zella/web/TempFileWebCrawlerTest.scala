package org.zella.web

import java.util.logging.Level

import com.gargoylesoftware.htmlunit.{BrowserVersion, WebClient}
import com.google.common.truth.Truth._
import org.junit.{After, Before, Test}

/**
  * @author zella.
  */
class TempFileWebCrawlerTest {

  //TODO mp3spbcrawler
  val VALID_DOWNLOAD_LINK = "http://musicmp3spb.org/download/nude_city_life/67940862cca1c77354504829f3bfcb6f1480066258"
  var webClient: WebClient = _

  @Before def setup() {
    webClient = new WebClient(BrowserVersion.FIREFOX_45)
    webClient.getOptions.setThrowExceptionOnScriptError(false)
    webClient.getOptions.setThrowExceptionOnFailingStatusCode(false)
    java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF)
  }

  @Test
  def clickButton() {
    val webCrawler = new TempFileWebCrawler(webClient)
    //TODO move webClient.getPage(VALID_DOWNLOAD_LINK) to TempFileWebCrawler
    val page = webCrawler.clickButton(webClient.getPage(VALID_DOWNLOAD_LINK))
    assertThat(page.getTitleText).isEqualTo("TempFile.ru - временное хранение файлов до 200 Мб. Бесплатный хостинг картинок.")
  }

  @Test
  def clickDownLoadLink() {
    val webCrawler = new TempFileWebCrawler(webClient)

    val page = webCrawler.clickButton(webClient.getPage(VALID_DOWNLOAD_LINK))

    val link = webCrawler.clickDownloadLink(page)

    assertThat(link).startsWith("http://tempfile.ru/download/")
  }


  @After def clickDownloadLink() {
    webClient.close()
  }
}