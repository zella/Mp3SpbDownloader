package org.zella.core;

import com.gargoylesoftware.htmlunit.WebClient;
import io.reactivex.Observable;
import org.zella.internals.RetryWithDelay;
import org.zella.io.FileDownloader;
import org.zella.web.AlbumInfo;
import org.zella.web.IMp3SpbWebCrawler;
import org.zella.web.ITempFileWebCrawler;
import org.zella.web.SongInfo;
import scala.Tuple2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author zella.
 */
public class Engine {

    private final WebClient webClient;
    private final IMp3SpbWebCrawler mp3SpbWebCrawler;
    private final ITempFileWebCrawler tempFileWebCrawler;
    private final DownloadQueue downloadQueue;

    public Engine(WebClient webClient, IMp3SpbWebCrawler mp3SpbWebCrawler, ITempFileWebCrawler tempFileWebCrawler, DownloadQueue downloadQueue) {
        this.webClient = webClient;
        this.mp3SpbWebCrawler = mp3SpbWebCrawler;
        this.tempFileWebCrawler = tempFileWebCrawler;
        this.downloadQueue = downloadQueue;
    }

    public Observable<AlbumInfo> albumInfo(String albumUrl) {
        return mp3SpbWebCrawler.albumInfoAsync(albumUrl);
    }

    public Observable<Tuple2<DownloadQueue.DownloadInfo, FileDownloader.DownloadProgress>> downloadAlbum(AlbumInfo albumInfo, Path downloadFolder) {
        return
                Observable.fromIterable(albumInfo.songs().entrySet())
                        .flatMap(numSongEntry -> tempFileWebCrawler
                                .clickButtonAsync(webClient.getPage(numSongEntry.getValue().downloadLink()))
                                .flatMap(tempFileWebCrawler::clickDownloadLinkAsync)
                                .flatMap(downloadLink -> {
                                    Tuple2<Path, Path> paths = tmpAndCompletePath(albumInfo, numSongEntry, downloadFolder);
                                    return downloadQueue.addDownload(
                                            downloadLink,
                                            paths._1(),
                                            paths._2(),
                                            downloadId(albumInfo, numSongEntry.getKey(), numSongEntry.getValue())
                                    );
                                }).retryWhen(new RetryWithDelay(6, 10000)));
    }

    public void close() {
        webClient.close();
    }

    public static String downloadId(AlbumInfo albumInfo, Integer num, SongInfo songInfo) {
        return albumInfo.title() + " " + num.toString() + " " + songInfo.name();
    }


    private Tuple2<Path, Path> tmpAndCompletePath(AlbumInfo albumInfo, Map.Entry<Integer, SongInfo> numSongEntry, Path downloadsFolder) throws IOException {

        AlbumPaths albumPaths = AlbumPaths.fromAlbumInfo(albumInfo);

        ensureFoldersExist(albumPaths, downloadsFolder);

        final Integer num = numSongEntry.getKey();
        final SongInfo songInfo = numSongEntry.getValue();

        final String tmpPath =
                albumPaths.artist
                        + File.separator
                        + albumPaths.album
                        + File.separator
                        // < 99 support
                        + (num < 10 ? "0" + num.toString() : num.toString()) + " " + songInfo.name()
                        .replaceAll("[\\\\/:*?\"<>|]", " ").trim();

        final String completePath = tmpPath + ".mp3";

        return new Tuple2<>(
                Paths.get(downloadsFolder.toAbsolutePath().toString(), tmpPath),
                Paths.get(downloadsFolder.toAbsolutePath().toString(), completePath)
        );
    }

    private void ensureFoldersExist(AlbumPaths albumPaths, Path downloadsFolder) throws IOException {
        if (!Files.exists(downloadsFolder))
            Files.createDirectory(downloadsFolder);

        if (!Files.exists(downloadsFolder.resolve(albumPaths.artist)))
            Files.createDirectory(downloadsFolder.resolve(albumPaths.artist));

        if (!Files.exists(downloadsFolder.resolve(albumPaths.artist).resolve(albumPaths.album)))
            Files.createDirectory(downloadsFolder.resolve(albumPaths.artist).resolve(albumPaths.album));
    }

    private static class AlbumPaths {
        private final String artist;
        private final String album;

        private AlbumPaths(String artist, String album) {
            this.artist = artist;
            this.album = album;
        }

        static AlbumPaths fromAlbumInfo(AlbumInfo albumInfo) {

            return new AlbumPaths(
                    albumInfo.artist().replaceAll("[\\\\/:*?\"<>|]", " ").trim(),
                    (albumInfo.year() + " " + albumInfo.title().replaceAll("[\\\\/:*?\"<>|]", " ").trim())
                            .trim()
            );
        }

    }


}
