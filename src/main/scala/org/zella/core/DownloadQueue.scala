package org.zella.core

import java.nio.file.{Files, Path}
import java.util
import java.util.Collections
import java.util.concurrent.{Executor, Executors}

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.{Action, Consumer}
import io.reactivex.schedulers.Schedulers
import org.zella.io.FileDownloader
import org.zella.io.FileDownloader.{CompletedDownload, DownloadProgress}

/**
  * @author zella.
  */
object DownloadQueue {

  lazy val defaultExecutor: Executor = Executors.newSingleThreadExecutor()

  def apply(
             downloader: FileDownloader,
             executor: Executor = defaultExecutor
           ): DownloadQueue = new DownloadQueue(downloader, executor)

  /**
    * Info about download
    *
    * @param downloadId unique download id
    * @param location   path to save file
    */
  case class DownloadInfo(downloadId: String, location: Path)

}


class DownloadQueue private(downloader: FileDownloader, executor: Executor = Executors.newSingleThreadExecutor()) {

  import org.zella.core.DownloadQueue.DownloadInfo

  private val downloadsById = Collections.synchronizedMap(new util.LinkedHashMap[String, DownloadInfo])

  /**
    * @return downloads, mapped by id
    */
  def downloads = downloadsById

  /**
    *
    * @param link              file url
    * @param temporaryLocation path to save file while download
    * @param completedLocation path to move file when completed
    */
  def addDownload(link: String, temporaryLocation: Path, completedLocation: Path, downloadId: String): Observable[(DownloadInfo, DownloadProgress)] = {

    //    val downloadId = UUID.randomUUID().toString

    val downloadInfo = DownloadInfo(downloadId, temporaryLocation)

    downloader.downloadFile(link, temporaryLocation)
      .subscribeOn(Schedulers.from(executor))
      .doOnComplete(new Action {
        override def run() = {
          if (Files.exists(completedLocation)) {
            Files.delete(completedLocation)
          }
          Files.move(temporaryLocation, completedLocation)
          println(s"$completedLocation downloaded")
        }
      })
      .doOnSubscribe(new Consumer[Disposable] {
        override def accept(d: Disposable) {
          downloadsById.put(downloadId, downloadInfo)
        }
      })
      .doOnTerminate(new Action {
        override def run() = downloadsById.remove(downloadId)
      })
      .map(new io.reactivex.functions.Function[DownloadProgress, (DownloadInfo, DownloadProgress)] {
        override def apply(progress: DownloadProgress): (DownloadInfo, DownloadProgress) = (
          downloadInfo,
          progress match {
            //TODO in this time, file not moved yet, but poher =)
            case CompletedDownload(bytes, size, path) => CompletedDownload(bytes, size, completedLocation)
            case default => default
          }
          )
      })
  }


}
