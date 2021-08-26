/*
 * Copyright (C) 2019-2021  E FOUNDATION
 * Copyright (C) 2021 ECORP SAS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.application.model

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import foundation.e.apps.R
import foundation.e.apps.api.FDroidAppExistsRequest
import foundation.e.apps.api.SystemAppExistsRequest
import foundation.e.apps.application.model.data.FullData
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.PGPCompressedData
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection
import org.bouncycastle.openpgp.PGPSignatureList
import org.bouncycastle.openpgp.PGPUtil
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.security.Security

class IntegrityVerificationTask(
    private val applicationInfo: ApplicationInfo,
    private val fullData: FullData,
    private val integrityVerificationCallback: IntegrityVerificationCallback
) :

    AsyncTask<Context, Void, Context>() {
    private lateinit var systemJsonData: JSONObject
    private var verificationSuccessful: Boolean = false
    private var TAG = "IntegrityVerificationTask"

    override fun doInBackground(vararg context: Context): Context {
        try {
            var packageName = getAPK_PackageName(context[0]);
            verificationSuccessful = if (isSystemApplication(packageName.toString())) {
                verifyAPKSignature(context[0])
            } else if (isfDroidApplication(packageName.toString())) {
                verifyFdroidSignature(context[0])
            } else {
                checkGoogleApp(context[0])
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return context[0]
    }

    private fun checkGoogleApp(context: Context): Boolean {
        return verifyShaSum(context)
    }

    private fun verifyShaSum(context: Context): Boolean {
        if (!fullData.getLastVersion()!!.apkSHA.isNullOrEmpty()) {
            return getApkFileSha1(applicationInfo.getApkOrXapkFile(context, fullData, fullData.basicData)) ==
                fullData.getLastVersion()!!.apkSHA
        }
        return false
    }

    private fun verifySystemSignature(context: Context): Boolean {
        if (!fullData.getLastVersion()?.signature.isNullOrEmpty()) {
            return fullData.getLastVersion()?.signature ==
                getSystemSignature(context.packageManager)?.toCharsString()
        }
        return false
    }

    // get signature from apk and check
    private fun verifyAPKSignature(context: Context): Boolean {

        // get Signature from APK
        if (getAPKSignature(context) != null) {
            return getAPKSignature(context)?.toCharsString() ==
                getSystemSignature(context.packageManager)?.toCharsString()
        }
        return false
    }

    private fun getAPKSignature(context: Context): Signature? {
        try {
            val fullPath: String = applicationInfo.getApkFile(
                context,
                fullData.basicData
            ).absolutePath

            val releaseSig = context.packageManager.getPackageArchiveInfo(fullPath, PackageManager.GET_SIGNATURES)
            return getFirstSignature(releaseSig)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d(TAG, "Unable to find the package: android")
        }
        return null
    }

    private fun getAPK_PackageName(context: Context): String? {

        val pm: PackageManager = context.packageManager
        val fullPath: String = applicationInfo.getApkFile(
            context,
            fullData.basicData
        ).absolutePath
        val info = pm.getPackageArchiveInfo(fullPath, 0)
        if (info != null) {
            return info.packageName;
        }
        else
            return null;


    }

    private fun getFirstSignature(pkg: PackageInfo?): Signature? {
        return if (pkg?.signatures != null && pkg.signatures.isNotEmpty()) {
            pkg.signatures[0]
        } else null
    }

    private fun getSystemSignature(pm: PackageManager): Signature? {
        try {
            val sys = pm.getPackageInfo("android", PackageManager.GET_SIGNATURES)
            return getFirstSignature(sys)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d(TAG, "Unable to find the package: android")
        }
        return null
    }

    private fun verifyFdroidSignature(context: Context): Boolean {
        Security.addProvider(BouncyCastleProvider())
        return verifyAPKSignature(
            context,
            BufferedInputStream(
                FileInputStream(
                    applicationInfo.getApkFile(
                        context,
                        fullData.basicData
                    ).absolutePath
                )
            ),
            fullData.getLastVersion()!!.signature.byteInputStream(Charsets.UTF_8),
            context.assets.open("f-droid.org-signing-key.gpg")
        )
    }

    private fun isfDroidApplication(packageName: String): Boolean {
        var fDroidAppExistsResponse = 0
        FDroidAppExistsRequest(packageName)
            .request { applicationError, searchResult ->
                when (applicationError) {
                    null -> {
                        if (searchResult.size > 0) {
                            fDroidAppExistsResponse = searchResult[0]!!
                        }
                    }
                    else -> {
                        Log.e(TAG, applicationError.toString())
                    }
                }
            }
        return fDroidAppExistsResponse == 200
    }

    private fun isSystemApplication(packageName: String): Boolean {
        var jsonResponse = ""
        SystemAppExistsRequest(packageName)
            .request { applicationError, searchResult ->
                when (applicationError) {
                    null -> {
                        if (searchResult.size > 0) {
                            jsonResponse = searchResult[0].toString()
                        }
                    }
                    else -> {
                        Log.e(TAG, applicationError.toString())
                    }
                }
            }
        try {
            if (JSONObject(jsonResponse).has(packageName)) {
                systemJsonData = JSONObject(jsonResponse).getJSONObject(packageName)
                return true
            } else {
                return false
            }
            // return JSONObject(jsonResponse).has(packageName);
        } catch (e: Exception) {
            if (e is JSONException) {
                Log.d(TAG, "$packageName is not a system application")
            } else {
                e.printStackTrace()
            }
        }
        return false
    }

    override fun onPostExecute(context: Context) {
        integrityVerificationCallback.onIntegrityVerified(context, verificationSuccessful)
        if (!verificationSuccessful) {
            Toast.makeText(context, R.string.not_able_install, Toast.LENGTH_LONG).show()
        }
    }

    private fun getApkFileSha1(file: File): String? {
        val messageDigest = MessageDigest.getInstance("SHA-1")
        val fileInputStream = FileInputStream(file)
        var length = 0
        val buffer = ByteArray(8192)
        while (length != -1) {
            length = fileInputStream.read(buffer)
            if (length > 0) {
                messageDigest.update(buffer, 0, length)
            }
        }
        return byteArrayToHex(messageDigest.digest())
    }

    private fun byteArrayToHex(a: ByteArray): String? {
        val sb = StringBuilder(a.size * 2)
        for (b in a) sb.append(String.format("%02x", b))
        return sb.toString()
    }

    private fun verifyAPKSignature(
        context: Context,
        apkInputStream: BufferedInputStream,
        apkSignatureInputStream: InputStream,
        publicKeyInputStream: InputStream
    ): Boolean {
        try {

            var jcaPGPObjectFactory =
                JcaPGPObjectFactory(PGPUtil.getDecoderStream(apkSignatureInputStream))
            val pgpSignatureList: PGPSignatureList

            val pgpObject = jcaPGPObjectFactory.nextObject()
            if (pgpObject is PGPCompressedData) {
                jcaPGPObjectFactory = JcaPGPObjectFactory(pgpObject.dataStream)
                pgpSignatureList = jcaPGPObjectFactory.nextObject() as PGPSignatureList
            } else {
                pgpSignatureList = pgpObject as PGPSignatureList
            }

            val pgpPublicKeyRingCollection =
                PGPPublicKeyRingCollection(
                    PGPUtil.getDecoderStream(publicKeyInputStream),
                    JcaKeyFingerprintCalculator()
                )

            val signature = pgpSignatureList.get(0)
            val key = pgpPublicKeyRingCollection.getPublicKey(signature.keyID)

            signature.init(BcPGPContentVerifierBuilderProvider(), key)

            val buff = ByteArray(1024)
            var read = apkInputStream.read(buff)
            while (read != -1) {
                signature.update(buff, 0, read)
                read = apkInputStream.read(buff)
            }

            apkInputStream.close()
            apkSignatureInputStream.close()
            publicKeyInputStream.close()
            return signature.verify()
        } catch (e: Exception) {
            e.printStackTrace()

            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, context.resources.getString(R.string.Signature_verification_failed), Toast.LENGTH_LONG).show()
            }
        }

        return false
    }
}

interface IntegrityVerificationCallback {
    fun onIntegrityVerified(context: Context, verificationSuccessful: Boolean)
}
