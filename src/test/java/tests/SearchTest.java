package tests;

import base.BaseTest;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.SearchPage;

public class SearchTest extends BaseTest {

    private void loginToLinkedIn() {
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

        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login(email, password);

        new WebDriverWait(driver, Duration.ofSeconds(120))
            .until(ExpectedConditions.urlMatches(
                "^https://www\\.linkedin\\.com/feed(?:/|\\?|$).*"
            ));
    }

    private void captureScreenshot(String name) throws Exception {
        Path screenshot = Path.of(
            "evidence",
            name + "-" + System.currentTimeMillis() + ".png"
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

    @Test
    public void searchDisplaysResults() throws Exception {
        try {
            loginToLinkedIn();

            SearchPage searchPage = new SearchPage(driver);
            searchPage.searchFor("Microsoft");

            Assert.assertTrue(
                driver.getCurrentUrl().contains("/search/results/"),
                "The search results page should open."
            );

            Assert.assertTrue(
                searchPage.isResultVisible(),
                "A visible search result should appear."
            );

            System.out.println("Search test passed.");

        } finally {
            captureScreenshot("search-results");
        }
    }

    @Test
    public void firstResultOpensCompanyPage() throws Exception {
        try {
            loginToLinkedIn();

            SearchPage searchPage = new SearchPage(driver);
            searchPage.searchFor("Microsoft");

            Assert.assertTrue(
                searchPage.isResultVisible(),
                "A visible result should appear before clicking."
            );

            searchPage.openFirstResult();

            new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(browser -> {
                    URI url = URI.create(browser.getCurrentUrl());

                    return "www.linkedin.com".equals(url.getHost())
                        && url.getPath().matches(
                            "/company/microsoft(?:/.*)?"
                        );
                });

            Assert.assertTrue(
                URI.create(driver.getCurrentUrl()).getPath()
                    .matches("/company/microsoft(?:/.*)?"),
                "The first result should open Microsoft's company page."
            );

            System.out.println("Open-result test passed.");

        } finally {
            captureScreenshot("opened-company");
        }
    }
}