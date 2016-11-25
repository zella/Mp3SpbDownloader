package org.zella.web

import java.util.concurrent.Callable

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.{HtmlAnchor, HtmlPage, HtmlSubmitInput}
import io.reactivex.Observable


/**
  * Контактирует с веб страницей TempFile
  *
  * @author zella.
  */
trait ITempFileWebCrawler {

  def clickButton(htmlPage: HtmlPage): HtmlPage

  def clickDownloadLink(page: HtmlPage): String

  def clickButtonAsync(htmlPage: HtmlPage): Observable[HtmlPage]

  def clickDownloadLinkAsync(page: HtmlPage): Observable[String]

}


/**
  * @author zella.
  */
class TempFileWebCrawler(webClient: WebClient) extends ITempFileWebCrawler {

  override def clickButton(page: HtmlPage): HtmlPage = {

    page
      .getByXPath("//*[@id=\"cntMainCenterText\"]/form/table/tbody/tr/td/input[2]")
      .get(0)
      .asInstanceOf[HtmlSubmitInput]
      .click()
      .asInstanceOf[HtmlPage]

  }

  override def clickDownloadLink(page: HtmlPage): String = {

    page
      .getByXPath("//*[@id=\"cntMainCenterText\"]/center/b/a")
      .get(0)
      .asInstanceOf[HtmlAnchor]
      .getHrefAttribute
  }

  override def clickButtonAsync(page: HtmlPage): Observable[HtmlPage] = {
    Observable.fromCallable(new Callable[HtmlPage] {
      override def call() = clickButton(page)
    })
  }

  override def clickDownloadLinkAsync(page: HtmlPage): Observable[String] = {
    Observable.fromCallable(new Callable[String] {
      override def call() = clickDownloadLink(page)
    })
  }
}
