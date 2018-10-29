package io.eelo.appinstaller.application.model

class Version(val downloadFlag: String,
              val downloadLink: String,
              val minAndroid: String,
              val apkSHA: String,
              val createdOn: String,
              val version: String,
              val signature: String,
              val fileSize: String,
              val updateDate: String,
              val sourceDownload: String,
              val whatsNew: String?,
              val updateName : String)
