package org.zella.io

import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

import io.reactivex._
import io.reactivex.functions.Cancellable
import okhttp3._
import okio._

/**
  * Download files api
  *
  * @author zella.
  */
object FileDownloader {

  def apply(
             okHttpClient: OkHttpClient,
             progressPeriodBytes: Long = 10000,
             internalBufferBytes: Long = 2048
           ): FileDownloader = new FileDownloader(okHttpClient, progressPeriodBytes, internalBufferBytes)


  trait DownloadProgress

  case class StartDownload(path: Path) extends DownloadProgress

  case class CancelDownload(path: Path) extends DownloadProgress

  case class PartialDownload(bytesDownloaded: Long, size: Long, path: Path) extends DownloadProgress

  case class CompletedDownload(bytesDownloaded: Long, size: Long, path: Path) extends DownloadProgress

}


class FileDownloader private(okHttpClient: OkHttpClient, progressPeriodBytes: Long, internalBufferBytes: Long) {

  import org.zella.io.FileDownloader._

  /**
    * Download file with progress, rxjava 2 api
    *
    * @param link     http download link
    * @param location path to save file on file system
    * @return Observable, that emits PartialDownload with progress and CompletedDownload as last event
    */
  def downloadFile(link: String, location: Path): Observable[DownloadProgress] = {
    Observable.create(new ObservableOnSubscribe[DownloadProgress] {
      override def subscribe(emitter: ObservableEmitter[DownloadProgress]) {

        val isCanceled = new AtomicBoolean(false)

        val request: Request = new Request.Builder().url(link).get().build()
        val call = okHttpClient.newCall(request)

        val response = call.execute()

        if (!response.isSuccessful) {
          emitter.onError(new IOException("Unexpected code " + response.message()))
        }

        emitter.onNext(StartDownload(location))

        emitter.setCancellable(new Cancellable {
          override def cancel() {
            isCanceled.set(true)
            emitter.onNext(CancelDownload(location))
          }
        })

        val sink = Okio.buffer(Okio.sink(location))

        handleWrites(sink, response.body(), emitter, location, isCanceled)

        emitter.onNext(CompletedDownload(
          response.body().contentLength(),
          response.body().contentLength(),
          location))
        emitter.onComplete()
      }
    }

    )
  }

  private def handleWrites(fileSink: BufferedSink, body: ResponseBody, emitter: ObservableEmitter[DownloadProgress], location: Path, isCanceled: AtomicBoolean) {
    val contentLength = body.contentLength()

    var totalBytes = 0L
    var readBytes = 0L
    var progressLimitBytes = 0L

    while (readBytes != -1) {
      if (isCanceled.get()) {
        body.close()
        fileSink.close()
        return
      }

      readBytes = body.source().read(fileSink.buffer(), internalBufferBytes)
      totalBytes += readBytes
      if (totalBytes > progressLimitBytes) {
        progressLimitBytes += progressPeriodBytes
        fileSink.flush()
        emitter.onNext(PartialDownload(totalBytes, contentLength, location))
      }
    }

    body.close()
    fileSink.close()

  }

}
