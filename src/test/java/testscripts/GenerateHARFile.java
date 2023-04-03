package testscripts;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarResponse;
import net.lightbody.bmp.proxy.CaptureType;

public class GenerateHARFile {

	@Test
	public void generateAndParseHARFileTest() throws InterruptedException, IOException {
		// 1. Start proxy on some port
		BrowserMobProxy mobProxy = new BrowserMobProxyServer();
		mobProxy.start();

		// 2. Set SSL and HTTP proxy on Selenium Proxy
		Proxy proxy = new Proxy();
		proxy.setHttpProxy("localhost:" + mobProxy.getPort());
		proxy.setSslProxy("localhost:" + mobProxy.getPort());

		// 3. Add Capability for PROXY in DesiredCapabilities
		DesiredCapabilities capability = new DesiredCapabilities();
		capability.setCapability(CapabilityType.PROXY, proxy);
		// capability.acceptInsecureCerts();
		capability.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

		// 4. Set captureTypes
		EnumSet<CaptureType> captureTypes = CaptureType.getAllContentCaptureTypes();
		captureTypes.addAll(CaptureType.getCookieCaptureTypes());
		captureTypes.addAll(CaptureType.getHeaderCaptureTypes());
		captureTypes.addAll(CaptureType.getRequestCaptureTypes());
		captureTypes.addAll(CaptureType.getResponseCaptureTypes());

		// 5. setHarCaptureTypes with above captureTypes
		mobProxy.setHarCaptureTypes(captureTypes);

		// 6. HAR name
		mobProxy.newHar("MyHAR");

		// 7. Start browser and open URL
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		// options.addArguments("--remote-allow-origins=*");
		options.merge(capability);
		WebDriver driver = new ChromeDriver(options);

		driver.get("https://exactspace.co/");

		driver.manage().window().maximize();

		Thread.sleep(5000);

		Har har = mobProxy.getHar();

		File myHARFile = new File(System.getProperty("user.dir") + "/HARFolder/googleHAR1.har");
		har.writeTo(myHARFile);

		System.out.println("==> HAR details has been successfully written in the file.....");

		List<HarEntry> entries = har.getLog().getEntries();
		int totalStatusCodeCount = 0;
		int twoXXCount = 0;
		int fourXXCount = 0;
		int fiveXXCount = 0;
		for (HarEntry entry : entries) {
			HarResponse response = entry.getResponse();
			int statusCode = response.getStatus();
			if (statusCode >= 200 && statusCode < 300) {
				twoXXCount++;
			} else if (statusCode >= 400 && statusCode < 500) {
				fourXXCount++;
			} else if (statusCode >= 500 && statusCode < 600) {
				fiveXXCount++;
			}
			totalStatusCodeCount++;
		}

		// Display the status code counts
		System.out.println("Total status code count: " + totalStatusCodeCount);
		System.out.println("2XX count: " + twoXXCount);
		System.out.println("4XX count: " + fourXXCount);
		System.out.println("5XX count: " + fiveXXCount);

		driver.close();
	}
}
