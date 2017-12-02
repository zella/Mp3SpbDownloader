name := """mp3spb-downloader"""

version := "0.1.3-SNAPSHOT"

scalaVersion := "2.12.4"

mainClass in assembly := Some("org.zella.ui.Mp3SpbDownloader")

test in assembly := {}

// https://mvnrepository.com/artifact/com.novocode/junit-interface
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

// https://mvnrepository.com/artifact/com.google.truth/truth
libraryDependencies  += "com.google.truth" % "truth" % "0.30" % "test"

// https://mvnrepository.com/artifact/net.sourceforge.htmlunit/htmlunit
libraryDependencies += "net.sourceforge.htmlunit" % "htmlunit" % "2.23"

// https://mvnrepository.com/artifact/com.squareup.okio/okio
libraryDependencies += "com.squareup.okio" % "okio" % "1.13.0"

// https://mvnrepository.com/artifact/io.reactivex.rxjava2/rxjava
libraryDependencies += "io.reactivex.rxjava2" % "rxjava" % "2.1.7"

// https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
libraryDependencies += "com.squareup.okhttp3" % "okhttp" % "3.9.1"

