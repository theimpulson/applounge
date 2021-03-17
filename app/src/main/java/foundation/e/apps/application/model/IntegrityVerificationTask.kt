/*
    Copyright (C) 2019  e Foundation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.application.model

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import foundation.e.apps.R
import foundation.e.apps.application.model.data.FullData
import foundation.e.apps.utils.Constants
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.PGPCompressedData
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection
import org.bouncycastle.openpgp.PGPSignatureList
import org.bouncycastle.openpgp.PGPUtil
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.security.Security

class IntegrityVerificationTask(
        private val applicationInfo: ApplicationInfo,
        private val fullData: FullData,
        private val integrityVerificationCallback: IntegrityVerificationCallback) :
        AsyncTask<Context, Void, Context>() {
    private var verificationSuccessful: Boolean = false

    override fun doInBackground(vararg context: Context): Context {
        if (fullData.packageName == Constants.MICROG_PACKAGE) {
            verificationSuccessful = true
        } else {
            verificationSuccessful = if (!fullData.getLastVersion()!!.apkSHA.isNullOrEmpty()) {
                getApkFileSha1(applicationInfo.getApkOrXapkFile(context[0], fullData, fullData.basicData)) ==
                        fullData.getLastVersion()!!.apkSHA
            } else {
                Security.addProvider(BouncyCastleProvider())
                verifyAPKSignature(
                        context[0],
                        BufferedInputStream(FileInputStream(
                                applicationInfo.getApkFile(context[0],
                                        fullData.basicData).absolutePath)),
                        fullData.getLastVersion()!!.signature.byteInputStream(Charsets.UTF_8),
                        context[0].assets.open("f-droid.org-signing-key.gpg"))
            }
        }
        return context[0]
    }

    override fun onPostExecute(context: Context) {
        integrityVerificationCallback.onIntegrityVerified(context, verificationSuccessful)
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
            publicKeyInputStream: InputStream): Boolean {
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
                            JcaKeyFingerprintCalculator())

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
            Toast.makeText(context, context.resources.getString(R.string.Signature_verification_failed), Toast.LENGTH_LONG).show();
            return signature.verify()
        } catch (e: Exception) {
            e.printStackTrace()
            //Toast message if we want to show message to user
            Toast.makeText(context, context.resources.getString(R.string.Signature_verification_failed), Toast.LENGTH_LONG).show();
        }
        return false;

    }
}

interface IntegrityVerificationCallback {
    fun onIntegrityVerified(context: Context, verificationSuccessful: Boolean)
}
