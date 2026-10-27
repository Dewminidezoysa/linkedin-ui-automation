package tests;

import java.nio.file.Files;
import java.nio.file.Path;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import base.BaseTest;

public class BrowserTest extends BaseTest {

    @Test
    public void loginPageLoads() throws Exception {
        try {
            driver.get("https://www.linkedin.com/login");

           WebElement usernameField = wait.until(browser -> {
    for (WebElement field : browser.findElements(
            By.cssSelector("input[type='email']"))) {

        if (field.isDisplayed() && field.isEnabled()) {
            return field;
        }
    }

    return null;
});

            Assert.assertTrue(
                usernameField.isDisplayed(),
                "The LinkedIn username field should be visible."
            );

            System.out.println("LinkedIn login page loaded successfully.");

        } finally {
            System.out.println("Page URL: " + driver.getCurrentUrl());
            System.out.println("Page title: " + driver.getTitle());
            System.out.println(
    "Email fields found: " +
    driver.findElements(By.cssSelector("input[type='email']")).size()
);

for (WebElement input : driver.findElements(By.tagName("input"))) {
    System.out.println(
        "INPUT: type=" + input.getAttribute("type")
        + ", id=" + input.getAttribute("id")
        + ", name=" + input.getAttribute("name")
        + ", visible=" + input.isDisplayed()
    );
}

System.out.println(
    "Frames found: " +
    driver.findElements(By.cssSelector("iframe, frame")).size()
);

            Path screenshot = Path.of(
                "evidence",
                "browser-check-" + System.currentTimeMillis() + ".png"
            );

            Files.createDirectories(screenshot.getParent());

            byte[] image = ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.BYTES);

            Files.write(screenshot, image);

            System.out.println(
                "Screenshot saved: " + screenshot.toAbsolutePath()
            );
        }
    }
}