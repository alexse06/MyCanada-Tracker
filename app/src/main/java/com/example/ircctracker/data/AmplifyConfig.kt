package com.example.ircctracker.data

import org.json.JSONObject

object AmplifyConfig {
    fun getConfiguration(context: android.content.Context): org.json.JSONObject {
        return JSONObject("""
            {
                "UserAgent": "aws-amplify-cli/2.0",
                "Version": "1.0",
                "auth": {
                    "plugins": {
                        "awsCognitoAuthPlugin": {
                            "UserAgent": "aws-amplify-cli/0.1.0",
                            "Version": "0.1.0",
                            "IdentityManager": {
                                "Default": {}
                            },
                            "Auth": {
                                "Default": {
                                    "authenticationFlowType": "USER_PASSWORD_AUTH"
                                }
                            },
                            "CognitoUserPool": {
                                "Default": {
                                    "PoolId": "${AppConfig.Cognito.USER_POOL_ID}",
                                    "AppClientId": "${AppConfig.Cognito.CLIENT_ID}",
                                    "Region": "${AppConfig.Cognito.REGION}"
                                }
                            }
                        }
                    }
                }
            }
        """.trimIndent())
    }
}
