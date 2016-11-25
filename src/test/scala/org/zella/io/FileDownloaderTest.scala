package org.zella.io

import java.nio.file._

import com.google.common.truth.Truth.assertThat
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import okhttp3.OkHttpClient
import org.junit._
import org.zella.io.FileDownloader._

import scala.collection.JavaConversions._

/**
  * @author zella.
  */
class FileDownloaderTest {

  val LOCATION = Paths.get("chetaah.jpg")

  val SMALL_FILE_URL = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Cheetah_Kruger.jpg/1024px-Cheetah_Kruger.jpg";
  val SMALL_FILE_SIZE = 163691L

  val BIG_FILE_URL = "http://speedtest.ftp.otenet.gr/files/test100Mb.db"
  val BIg_FILE_SIZE = 104857600L

  @Test
  def downloadFile() {

    val emission = FileDownloader(new OkHttpClient())
      .downloadFile(SMALL_FILE_URL, LOCATION)
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
    assertThat(emission.count(e => e.isInstanceOf[PartialDownload])).isEqualTo(17)
    assertThat(emission.size).isEqualTo(19)

    assertThat(Files.size(LOCATION)).isEqualTo(SMALL_FILE_SIZE)

  }


  //FIXME request cancelation not implemented,
  //to test it, we need simulate long time request
  @Test
  @Ignore
  def downloadFileCancelRequestStage() {

    val disposable = FileDownloader(new OkHttpClient())
      .downloadFile(BIG_FILE_URL, LOCATION)
      .subscribeOn(Schedulers.newThread())
      .doOnError(new Consumer[Throwable] {
        override def accept(t: Throwable) {
          t.printStackTrace()
          Assert.fail()
        }
      })
      .subscribe()

    Thread.sleep(1)

    disposable.dispose()
    assertThat(Files.exists(LOCATION)).isEqualTo(false)

  }

  //FIXME can't determine flushes on disk (Files.size not returns actual downloaded bytes count),
  //so this test not useful. But this functionality is implemented and seems like working :)
  @Ignore
  @Test
  def downloadFileCancelWriteDiskStage() {

    val disposable = FileDownloader(new OkHttpClient())
      .downloadFile(BIG_FILE_URL, LOCATION)
      .subscribeOn(Schedulers.newThread())
      .doOnError(new Consumer[Throwable] {
        override def accept(t: Throwable) {
          t.printStackTrace()
          Assert.fail()
        }
      })
      .subscribe()

    Thread.sleep(3000)
    disposable.dispose()
    //time to flush changes to disk
    Thread.sleep(500)
    val sizeAtCancelation = Files.size(LOCATION)
    Thread.sleep(3000)
    val sizeAfterCancelation = Files.size(LOCATION)

    assertThat(sizeAfterCancelation).isEqualTo(sizeAtCancelation)

    assertThat(Files.size(LOCATION).asInstanceOf[java.lang.Long]).isLessThan(BIg_FILE_SIZE)

  }

  @Before
  @After
  def clean() {
    Files.deleteIfExists(LOCATION)
  }

}