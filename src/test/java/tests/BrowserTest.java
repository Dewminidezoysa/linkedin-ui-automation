package tests;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import base.BaseTest;

public class BrowserTest extends BaseTest {

    @Test
    public void loginPageLoads() throws Exception {
        try {
            driver.get("https://www.linkedin.com/login/");

            System.out.println(
                "Waiting for the LinkedIn login form. "
                + "If verification is requested, complete it manually."
            );

            WebElement usernameField =
                new WebDriverWait(driver, Duration.ofSeconds(60))
                    .withMessage(() ->
                        "The login email field did not become visible. "
                        + "Page title: " + driver.getTitle()
                        + ". URL: " + driver.getCurrentUrl()
                    )
                    .until(browser -> {
                        try {
                            for (WebElement field :
                                    browser.findElements(
                                        By.cssSelector(
                                            "input[type='email'], "
                                            + "input#username"
                                        )
                                    )) {

                                if (field.isDisplayed()
                                        && field.isEnabled()) {
                                    return field;
                                }
                            }
                        } catch (
                            StaleElementReferenceException exception
                        ) {
                            // The page changed; check again.
                            return null;
                        }

                        return null;
                    });

            Assert.assertTrue(
                usernameField.isDisplayed(),
                "The LinkedIn username field should be visible."
            );

            System.out.println(
                "Browser-check test passed: login field is visible."
            );

        } finally {
            System.out.println(
                "Page URL: " + driver.getCurrentUrl()
            );
            System.out.println(
                "Page title: " + driver.getTitle()
            );

            Path screenshot = Path.of(
                "evidence",
                "browser-check-"
                    + System.currentTimeMillis()
                    + ".png"
            );

            Files.createDirectories(screenshot.getParent());

            Files.write(
                screenshot,
                ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BYTES)
            );

            System.out.println(
                "Screenshot saved: "
                    + screenshot.toAbsolutePath()
            );
        }
    }
}