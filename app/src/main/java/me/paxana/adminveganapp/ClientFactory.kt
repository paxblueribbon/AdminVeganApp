package me.paxana.adminveganapp

import android.content.Context
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.sigv4.BasicCognitoUserPoolsAuthProvider
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool

object ClientFactory {

    @Volatile private var client: AWSAppSyncClient? = null

    @Volatile var cognitoUserPoolsAuthProvider: CognitoUserPoolsAuthProvider? = null


    @Synchronized fun init(context: Context) {
        if (client == null)
        {
            val awsConfiguration = AWSConfiguration(context)
            cognitoUserPoolsAuthProvider = BasicCognitoUserPoolsAuthProvider(CognitoUserPool(context, awsConfiguration))
            client = AWSAppSyncClient.builder()
                .context(context)
                .awsConfiguration(awsConfiguration)
                .cognitoUserPoolsAuthProvider(cognitoUserPoolsAuthProvider).build()
        }
    }
    @Synchronized fun appSyncClient(): AWSAppSyncClient? {
        return client
    }
}