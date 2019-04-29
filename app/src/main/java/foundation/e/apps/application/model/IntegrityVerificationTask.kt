package foundation.e.apps.application.model

import android.content.Context
import android.os.AsyncTask
import foundation.e.apps.application.model.data.FullData
import org.apache.commons.codec.binary.Hex
import java.security.MessageDigest
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator
import org.bouncycastle.openpgp.PGPUtil
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection
import org.bouncycastle.openpgp.PGPSignatureList
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory
import org.bouncycastle.openpgp.PGPCompressedData
import java.io.*

class IntegrityVerificationTask(
        private val applicationInfo: ApplicationInfo,
        private val fullData: FullData,
        private val integrityVerificationCallback: IntegrityVerificationCallback) :
        AsyncTask<Context, Void, Context>() {
    private var verificationSuccessful: Boolean = false
    override fun doInBackground(vararg context: Context): Context {
        verificationSuccessful = if (!fullData.getLastVersion()!!.apkSHA.isNullOrEmpty()) {
            getApkFileSha1(applicationInfo.getApkFile(context[0], fullData.basicData)) ==
                    fullData.getLastVersion()!!.apkSHA
        } else {
            Security.addProvider(BouncyCastleProvider())
            verifyAPKSignature(
                    BufferedInputStream(FileInputStream(
                            applicationInfo.getApkFile(context[0],
                                    fullData.basicData).absolutePath)),
                    fullData.getLastVersion()!!.signature.byteInputStream(Charsets.UTF_8),
                    context[0].assets.open("f-droid.org-signing-key.gpg"))
        }
        return context[0]
    }

    override fun onPostExecute(context: Context) {
        integrityVerificationCallback.onIntegrityVerified(context, verificationSuccessful)
    }

    private fun getApkFileSha1(file: File): String {
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
        return String(Hex.encodeHex(messageDigest.digest()))
    }

    private fun verifyAPKSignature(
            apkInputStream: BufferedInputStream,
            apkSignatureInputStream: InputStream,
            publicKeyInputStream: InputStream): Boolean {
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

        signature.init(JcaPGPContentVerifierBuilderProvider().setProvider("BC"), key)

        var character = apkInputStream.read()
        while (character >= 0) {
            signature.update(character.toByte())
            character = apkInputStream.read()
        }

        apkInputStream.close()
        apkSignatureInputStream.close()
        publicKeyInputStream.close()

        return signature.verify()
    }
}

interface IntegrityVerificationCallback {
    fun onIntegrityVerified(context: Context, verificationSuccessful: Boolean)
}
