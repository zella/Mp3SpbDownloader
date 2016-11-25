package org.zella.core

import java.nio.file.{Files, Paths}

import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import org.junit.{After, Before, Test}
import org.zella.io.FileDownloader

/**
  * @author zella.
  */
class DownloadQueueTest {

  val TMP_LOCATION_1 = Paths.get("1.part")
  val TMP_LOCATION_2 = Paths.get("2.part")
  val TMP_LOCATION_3 = Paths.get("3.part")

  val LOCATION_1 = Paths.get("1.jpg")
  val LOCATION_2 = Paths.get("2.jpg")
  val LOCATION_3 = Paths.get("3.jpg")

  val URL_1 = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Cheetah_Kruger.jpg/1024px-Cheetah_Kruger.jpg"
  val URL_2 = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Cheetah_Kruger.jpg/1024px-Cheetah_Kruger.jpg"
  val URL_3 = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Cheetah_Kruger.jpg/1024px-Cheetah_Kruger.jpg"


  val FILE_SIZE_1 = 163691L
  val FILE_SIZE_2 = 163691L
  val FILE_SIZE_3 = 163691L


  @Test
  def addDownload() {

    val okHttp = new OkHttpClient()

    val fileDownloader = FileDownloader(okHttp)

    val downloadQueue = DownloadQueue(fileDownloader)
    val download1 = downloadQueue.addDownload(URL_1, TMP_LOCATION_1, LOCATION_1, "someId1")
    val download2 = downloadQueue.addDownload(URL_2, TMP_LOCATION_2, LOCATION_2, "someId2")
    val download3 = downloadQueue.addDownload(URL_3, TMP_LOCATION_3, LOCATION_3, "someId3")

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

  //TODO test cancelation etc

  @Before
  @After
  def clean() {
    Files.deleteIfExists(LOCATION_1)
    Files.deleteIfExists(LOCATION_2)
    Files.deleteIfExists(LOCATION_3)
  }


}
