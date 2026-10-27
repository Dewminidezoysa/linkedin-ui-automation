package tests;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import base.BaseTest;
import pages.LoginPage;

public class LoginTest extends BaseTest {

    @Test
    public void validLogin() throws Exception {
        String email = System.getenv("LINKEDIN_EMAIL");
        String password = System.getenv("LINKEDIN_PASSWORD");

        Assert.assertNotNull(
            email, "Set LINKEDIN_EMAIL before running."
        );
        Assert.assertNotNull(
            password, "Set LINKEDIN_PASSWORD before running."
        );
        Assert.assertFalse(email.isBlank(), "Email is empty.");
        Assert.assertFalse(password.isBlank(), "Password is empty.");

        try {
            LoginPage loginPage = new LoginPage(driver);
            loginPage.open();
            loginPage.login(email, password);

            // Allows time for manual verification if LinkedIn requests it.
            new WebDriverWait(driver, Duration.ofSeconds(120))
                .until(ExpectedConditions.urlMatches(
                    "^https://www\\.linkedin\\.com/feed(?:/|\\?|$).*"
                ));

            Assert.assertTrue(
                driver.getCurrentUrl()
                    .startsWith("https://www.linkedin.com/feed"),
                "Successful login should open the feed."
            );

            System.out.println("Successful-login test passed.");

        } finally {
            Path screenshot = Path.of(
                "evidence",
                "login-" + System.currentTimeMillis() + ".png"
            );

            Files.createDirectories(screenshot.getParent());

            Files.write(
                screenshot,
                ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BYTES)
            );

            System.out.println(
                "Screenshot saved: " + screenshot.toAbsolutePath()
            );
        }
    }
        @Test
    public void invalidPasswordShowsError() throws Exception {
        String email = System.getenv("LINKEDIN_EMAIL");
        String validPassword = System.getenv("LINKEDIN_PASSWORD");
        String invalidPassword = "Incorrect-Test-Password-2026!";

        Assert.assertNotNull(
            email, "Set LINKEDIN_EMAIL before running."
        );
        Assert.assertFalse(email.isBlank(), "Email is empty.");

        Assert.assertNotEquals(
            invalidPassword,
            validPassword,
            "The test password must differ from your real password."
        );

        try {
            LoginPage loginPage = new LoginPage(driver);
            loginPage.open();
            loginPage.login(email, invalidPassword);

            String actualError = loginPage.getLoginErrorMessage();

            Assert.assertEquals(
                actualError,
                "Wrong email or password.",
                "LinkedIn should display the login error."
            );

            Assert.assertFalse(
                driver.getCurrentUrl()
                    .startsWith("https://www.linkedin.com/feed"),
                "An invalid password must not open the feed."
            );

            System.out.println("Invalid-password test passed.");

        } finally {
            Path screenshot = Path.of(
                "evidence",
                "invalid-login-" + System.currentTimeMillis() + ".png"
            );

            Files.createDirectories(screenshot.getParent());

            Files.write(
                screenshot,
                ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BYTES)
            );

            System.out.println(
                "Screenshot saved: " + screenshot.toAbsolutePath()
            );
        }
}
}