package io.github.cloudify.scala.aws.auth

import com.amazonaws.auth._
import java.io.File
import scala.util.{Success, Try}

/**
 * Provides helpful methods to deal with AWS Credentials
 */
object CredentialsProvider {

  /**
   * Implements a `AWSCredentialsProvider` that reads AWS credentials from a
   * properties file stored in the user's home directory.
   * Filename defaults to `.aws.properties`.
   */
  case class HomePropertiesFile(filename: String = ".aws.properties") extends AWSCredentialsProvider {

    def refresh() = { }

    def getCredentials: AWSCredentials = {
      val credentialsFile = new File(sys.env("HOME"), filename)
      new PropertiesCredentials(credentialsFile)
    }

  }

  /**
   * Provides an instance of the default `HomePropertiesFile` provider
   */
  lazy val DefaultHomePropertiesFile = HomePropertiesFile()

  /**
   * Provides an instance of the `InstanceProfile` provider
   */
  lazy val InstanceProfile = new InstanceProfileCredentialsProvider()

  /**
   * Provides an instance of the default `ClasspathPropertiesFileCredentialsProvider` provider.
   */
  lazy val DefaultClasspathPropertiesFile = new ClasspathPropertiesFileCredentialsProvider()

  /**
   * Tries the following provides in sequence:
   * `DefaultHomePropertiesFile`, `InstanceProfile`, `DefaultClasspathPropertiesFile`
   */
  lazy val DefaultHyerarchical = firstOf(List(
    DefaultHomePropertiesFile,
    InstanceProfile,
    DefaultClasspathPropertiesFile
  ))

  /**
   * Useful extensions to `AWSCredentialsProvider`
   */
  implicit class AWSCredentialsProviderOps(val credentialsProvider: AWSCredentialsProvider) extends AnyVal {

    /**
     * Tries to get the credentials from this provider, if it fails return the
     * other provider.
     */
    def orElse(otherCredentialsProvider: AWSCredentialsProvider): AWSCredentialsProvider =
      Try(credentialsProvider.getCredentials) match {
        case Success(_) => credentialsProvider
        case _ => otherCredentialsProvider
      }

  }

  /**
   * Returns the first successful `AWSCredentialsProvider` from the list
   * provided.
   */
  def firstOf(providers: Iterable[AWSCredentialsProvider]): AWSCredentialsProvider =
    providers.reduce(_.orElse(_))

}


