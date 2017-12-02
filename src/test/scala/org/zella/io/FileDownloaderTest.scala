package org.zella.io

import java.nio.file._

import com.google.common.truth.Truth.assertThat
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import okhttp3.OkHttpClient
import org.junit._
import org.junit.rules.TemporaryFolder
import org.zella.io.FileDownloader._

import scala.annotation.meta.getter
import scala.collection.JavaConversions._

/**
  * @author zella.
  */
class FileDownloaderTest {

  @(Rule @getter)
  val tempFolder = new TemporaryFolder

  val SMALL_FILE_URL = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Cheetah_Kruger.jpg/1024px-Cheetah_Kruger.jpg";
  val SMALL_FILE_SIZE = 155462L

  val BIG_FILE_URL = "http://speedtest.ftp.otenet.gr/files/test100Mb.db"
  val BIg_FILE_SIZE = 104857600L

  @Test
  def downloadFile() {

    val file = tempFolder.newFile("chetaah.jpg")

    val emission = FileDownloader(new OkHttpClient())
      .downloadFile(SMALL_FILE_URL, file.toPath)
      .doOnError(new Consumer[Throwable] {
        override def accept(t: Throwable) {
          t.printStackTrace()
          Assert.fail()
        }
      })
      .blockingIterable().toList

    val testSubscriber = new TestSubscriber[DownloadProgress]()

    assertThat(emission.head.isInstanceOf[StartDownload]).isEqualTo(true)
    assertThat(emission.last.isInstanceOf[CompletedDownload]).isEqualTo(true)
    //TODO test bytes count in PartialDownload
    assertThat(emission.count(e => e.isInstanceOf[FileDownloader.CompletedDownload])).isEqualTo(1)
    assertThat(emission.count(e => e.isInstanceOf[PartialDownload])).isEqualTo(4)

    assertThat(Files.size(file.toPath)).isEqualTo(SMALL_FILE_SIZE)

  }

}