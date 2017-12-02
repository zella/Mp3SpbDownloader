package org.zella.core

import java.nio.file.{Files, Paths}
import java.util.concurrent.Executors

import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import org.junit.rules.TemporaryFolder
import org.junit.{After, Before, Rule, Test}
import org.zella.io.FileDownloader

import scala.annotation.meta.getter

/**
  * @author zella.
  */
class DownloadQueueTest {

  @(Rule @getter)
  val tempFolder = new TemporaryFolder

  val URL_1 = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Cheetah_Kruger.jpg/1024px-Cheetah_Kruger.jpg"
  val URL_2 = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Cheetah_Kruger.jpg/1024px-Cheetah_Kruger.jpg"
  val URL_3 = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Cheetah_Kruger.jpg/1024px-Cheetah_Kruger.jpg"

  val FILE_SIZE_1 = 163691L
  val FILE_SIZE_2 = 163691L
  val FILE_SIZE_3 = 163691L


  @Test
  def addDownload() {

    val tmp1 = tempFolder.newFile("1.part").toPath
    val tmp2 = tempFolder.newFile("2.part").toPath
    val tmp3 = tempFolder.newFile("3.part").toPath
    val done1 = tempFolder.newFile("1.jpg").toPath
    val done2 = tempFolder.newFile("2.jpg").toPath
    val done3 = tempFolder.newFile("3.jpg").toPath

    val okHttp = new OkHttpClient()

    val fileDownloader = FileDownloader(okHttp)

    val downloadQueue = DownloadQueue(fileDownloader, Executors.newSingleThreadExecutor())
    val download1 = downloadQueue.addDownload(URL_1, tmp1, done1, "someId1")
    val download2 = downloadQueue.addDownload(URL_2, tmp2, done2, "someId2")
    val download3 = downloadQueue.addDownload(URL_3, tmp3, done3, "someId3")

    assertThat(downloadQueue.downloads.size()).isEqualTo(0)
    download1.subscribe()
    assertThat(downloadQueue.downloads.size()).isEqualTo(1)
    download2.subscribe()
    assertThat(downloadQueue.downloads.size()).isEqualTo(2)
    download3.subscribe()
    assertThat(downloadQueue.downloads.size()).isEqualTo(3)

    //TODO test obsevable properly, wait download
    Thread.sleep(4000)
    assertThat(downloadQueue.downloads.size()).isEqualTo(0)


  }



}
