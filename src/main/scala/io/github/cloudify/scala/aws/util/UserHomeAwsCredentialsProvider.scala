package io.github.cloudify.scala.aws.util

import com.amazonaws.auth.{PropertiesCredentials, AWSCredentials, AWSCredentialsProvider}
import java.io.File

/**
 * Implements a `AWSCredentialsProvider` that reads AWS credentials from a
 * properties file stored in the user's home directory.
 * Filename defaults to `.aws.properties`.
 */
case class UserHomeAwsCredentialsProvider(filename: String = ".aws.properties") extends AWSCredentialsProvider {

  def refresh() = { }

  def getCredentials: AWSCredentials = {
    val credentialsFile = new File(sys.env("HOME"), filename)
    new PropertiesCredentials(credentialsFile)
  }

}

