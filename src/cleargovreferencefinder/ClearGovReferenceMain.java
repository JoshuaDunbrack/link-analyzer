/* *****************************************
 * ClearGov Website Analyzer
 *
 * Author: Josh Dunbrack
 * Date: Jan 7, 2019
 * Time: 12:49:22 PM
 *
 * Project: ClearGovReferenceFinder
 * Package: cleargovreferencefinder
 * File: ClearGovReferenceMain
 * Description: The main class which runs the program.
 *
 * ****************************************
 */
package cleargovreferencefinder;

import java.io.IOException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author Joshua Dunbrack
 */
public class ClearGovReferenceMain {

	/**
	 * @param args the command line arguments
	 * @throws java.io.IOException
	 */
	///*
	public static void main(String[] args) throws IOException {
		disableSSHSecurityCheck();
		ClearGovReferenceFinder finder = new ClearGovReferenceFinder(
				"data/ClientList_WithWebsites_20DEC18.csv");
		//		"data/Sample_File_ForJosh.csv");
		//		"data/break.csv");
	}

	//*/
	/**
	 * For fixing exceptions through closer inspection.
	 */
	/*
	public static void main(String[] args) throws MalformedURLException, IOException {

		Connection connection = Jsoup.connect("https://www.town.medfield.net/");
		Document doc = connection.get();
		System.out.println(doc.body());
		//*/
	private static void disableSSHSecurityCheck() {
		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
					//No need to implement.
				}

				public void checkServerTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
					//No need to implement.
				}
			}
		};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (Exception e) {
			System.out.println("");
		}
	}
}
