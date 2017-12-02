package org.zella.ui;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import io.reactivex.schedulers.Schedulers;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import org.zella.core.DownloadQueue;
import org.zella.core.Engine;
import org.zella.io.FileDownloader;
import org.zella.web.IMp3SpbWebCrawler;
import org.zella.web.ITempFileWebCrawler;
import org.zella.web.Mp3SpbWebCrawler;
import org.zella.web.TempFileWebCrawler;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.prefs.Preferences;

public class Mp3SpbDownloader extends Application {

    private static final String PREF_FOLDER = "folder";

    private final Preferences prefs = Preferences.userNodeForPackage(Mp3SpbDownloader.class);

    private Button downloadButton;
    private TextField albumTextField;
    private TextField saveFolderTextField;
    private TableView<DownloadUiItem> downloadsTable;

    private String saveFolder = "";

    private final Engine engine = initEngine();

    private final ObservableList<DownloadUiItem> data =
            FXCollections.observableArrayList();


    /**
     * Инициализирует корневой макет.
     */
    public void initRootLayout(Stage primaryStage) {
        try {
            // Загружаем корневой макет из fxml файла.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Mp3SpbDownloader.class.getResource("/views/RootLayout.fxml"));
            AnchorPane rootLayout = (AnchorPane) loader.load();

            // Отображаем сцену, содержащую корневой макет.
            Scene scene = new Scene(rootLayout);

            downloadButton = (Button) scene.lookup("#downloadButton");
            albumTextField = (TextField) scene.lookup("#albumTextField");
            saveFolderTextField = (TextField) scene.lookup("#saveFolderTextField");
            downloadsTable = (TableView) scene.lookup("#downloadsTable");
            downloadsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("name"));
            downloadsTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("progress"));
            downloadsTable.setItems(data);

            saveFolder = prefs.get(PREF_FOLDER, System.getProperty("user.home") + File.separator + "mp3spb");
            saveFolderTextField.setText(saveFolder);

            downloadButton.setOnAction(event -> {

                downloadButton.setDisable(true);
                engine.albumInfo(albumTextField.getText())
                        .subscribeOn(Schedulers.io())
                        .doOnNext(albumInfo -> {
                            albumInfo.songs().forEach((num, songInfo) -> {
                                data.add(new DownloadUiItem(Engine.downloadId(albumInfo, num, songInfo), num.toString() + " " + songInfo.name(), "waiting..."));
                            });
                            downloadButton.setDisable(false);
                            albumTextField.clear();
                        })
                        .flatMap(albumInfo -> engine.downloadAlbum(albumInfo, Paths.get(saveFolderTextField.getText())))
                        .subscribe(infoAndProgress -> {
                                    DownloadQueue.DownloadInfo info = infoAndProgress._1();
                                    FileDownloader.DownloadProgress progress = infoAndProgress._2();

                                    if (progress instanceof FileDownloader.StartDownload) {

                                    } else if (progress instanceof FileDownloader.CompletedDownload) {
                                        find(data, info.downloadId()).setProgress("ok");
                                    } else if (progress instanceof FileDownloader.PartialDownload) {
                                        find(data, info.downloadId()).setProgress(percent((FileDownloader.PartialDownload) progress));
                                    } else if (progress instanceof FileDownloader.CancelDownload) {
                                        System.out.println(info.location() + " canceled");
                                    }

                                    downloadsTable.refresh();
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    downloadButton.setDisable(false);
                                });

            });

            primaryStage.setOnCloseRequest(e -> {
                if (!saveFolder.equals(saveFolderTextField.getText()))
                    prefs.put(PREF_FOLDER, saveFolderTextField.getText());
                Platform.exit();
                System.exit(0);
            });


            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Engine initEngine() {
        Engine engine;
        OkHttpClient httpClient = new OkHttpClient();

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setCssEnabled(false);
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        IMp3SpbWebCrawler mp3SpbWebCrawler = new Mp3SpbWebCrawler(webClient);
        ITempFileWebCrawler tempFileWebCrawler = new TempFileWebCrawler(webClient);

        FileDownloader fileDownloader = FileDownloader.apply(httpClient, 1024 * 32);

        DownloadQueue downloadQueue = DownloadQueue.apply(fileDownloader, Executors.newFixedThreadPool(2));

        engine = new Engine(webClient,
                mp3SpbWebCrawler,
                tempFileWebCrawler,
                downloadQueue
        );
        return engine;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        stage.setTitle("Mp3SpbDownloader");
        stage.getIcons().add(new Image(Mp3SpbDownloader.class.getResourceAsStream("/icon.jpg")));
        //TODO
//        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
//            java.awt.Image image = Toolkit.getDefaultToolkit().getImage(Mp3SpbDownloader.class.getResource("/icon.jpg"));
//            com.apple.eawt.Application.getApplication().setDockIconImage(image);
//        }
        initRootLayout(stage);
    }

    private String percent(FileDownloader.PartialDownload partialDownload) {
        return String.valueOf((int) ((float) partialDownload.bytesDownloaded() / (float) partialDownload.size() * 100f)) + " %";
    }

    private DownloadUiItem find(Collection<DownloadUiItem> items, String downloadId) {
        return items.stream()
                .filter(item -> item.downloadId.equals(downloadId))
                .findFirst()
                //TODO npe
                .get();
    }

    public static class DownloadUiItem {

        final String downloadId;

        private final SimpleStringProperty name;
        private final SimpleStringProperty progress;

        public String getName() {
            return name.get();
        }

        public String getProgress() {
            return progress.get();
        }

        public void setProgress(String progress) {
            this.progress.set(progress);
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public DownloadUiItem(String downloadId, String name, String progress) {
            this.downloadId = downloadId;

            this.name = new SimpleStringProperty(name);
            this.progress = new SimpleStringProperty(progress);
        }
    }

}