name := """mp3spb-downloader"""

version := "0.1.1"

scalaVersion := "2.11.7"

mainClass in assembly := Some("org.zella.ui.Mp3SpbDownloader")

test in assembly := {}

// https://mvnrepository.com/artifact/com.novocode/junit-interface
libraryDependencies in test += "com.novocode" % "junit-interface" % "0.11"

// https://mvnrepository.com/artifact/com.google.truth/truth
libraryDependencies  in test += "com.google.truth" % "truth" % "0.30"

// https://mvnrepository.com/artifact/net.sourceforge.htmlunit/htmlunit
libraryDependencies += "net.sourceforge.htmlunit" % "htmlunit" % "2.23"

// https://mvnrepository.com/artifact/com.squareup.okio/okio
libraryDependencies += "com.squareup.okio" % "okio" % "1.11.0"

// https://mvnrepository.com/artifact/io.reactivex.rxjava2/rxjava
libraryDependencies += "io.reactivex.rxjava2" % "rxjava" % "2.0.0"
// https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
libraryDependencies += "com.squareup.okhttp3" % "okhttp" % "3.4.2"

