package org.zella.web

import java.util
import java.util.concurrent.Callable

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.{HtmlAnchor, HtmlDivision, HtmlPage}
import io.reactivex.Observable

import scala.collection.JavaConverters._


/**
  * Контактирует с веб страницей TempFile
  *
  * @author zella.
  */
trait IMp3SpbWebCrawler {


  def albumInfo(albumUrl: String): AlbumInfo

  def albumInfoAsync(albumUrl: String): Observable[AlbumInfo]

}


case class AlbumInfo(artist: String, year: String, title: String, songs: java.util.Map[Integer, SongInfo])

case class SongInfo(name: String, downloadLink: String, playLink: String)

/**
  * @author zella.
  */
class Mp3SpbWebCrawler(webClient: WebClient) extends IMp3SpbWebCrawler {


  override def albumInfo(albumUrl: String): AlbumInfo = {
    val albumPage = webClient.getPage(albumUrl).asInstanceOf[HtmlPage]
    val nameDiv = albumPage.querySelector("#cntCenter > div.cntAlbumMusician > div.albCover > div")
      .asInstanceOf[HtmlDivision]

    val (name, year) = nameDiv.asText().split("\n") match {
      case Array(n, y) => (n.trim, y.trim)
    }

    val artist = albumPage.querySelector("#cntCenter > h1:nth-child(1) > a")
      .asInstanceOf[HtmlAnchor]
      .getAttribute("title")
      .replace(" mp3", "")

    val songs = albumPage.querySelector("#cntCenter > div.cntAlbumMusician > div.albSong")
      .asInstanceOf[HtmlDivision]
      .getChildElements
      .asScala
      .filter(_.isInstanceOf[HtmlDivision])
      .map(htmlDiv => {
        val anchors = htmlDiv.getChildElements.asScala
          .filter(_.isInstanceOf[HtmlAnchor])
          .toList
        val base = albumPage.getBaseURL.getProtocol + "://" + albumPage.getBaseURL.getHost
        val downloadLink = base + anchors(0).asInstanceOf[HtmlAnchor].getHrefAttribute
        val name = anchors(0).asInstanceOf[HtmlAnchor].getTextContent
        val playLink = base + anchors(2).asInstanceOf[HtmlAnchor].getHrefAttribute
        SongInfo(name, downloadLink, playLink)
      })

    val songsMap = new util.HashMap[Integer, SongInfo]()
    for ((song, i) <- songs.zipWithIndex) {
      songsMap.put(i + 1, song)
    }

    AlbumInfo(artist, year, name, songsMap)
  }

  override def albumInfoAsync(albumUrl: String): Observable[AlbumInfo] = {
    Observable.fromCallable(new Callable[AlbumInfo] {
      override def call() = albumInfo(albumUrl)
    })
  }
}
