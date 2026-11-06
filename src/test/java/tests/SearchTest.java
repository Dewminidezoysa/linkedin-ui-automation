package tests;

import base.BaseTest;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
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

        System.out.println("STEP: Login completed.");
    }

    private void captureScreenshot(String name) {
        try {
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

        } catch (Exception exception) {
            System.err.println(
                "Screenshot could not be saved: "
                + exception.getMessage()
            );
        }
    }

    private boolean isMicrosoftCompanyPage() {
        URI url = URI.create(driver.getCurrentUrl());

        return "www.linkedin.com".equals(url.getHost())
            && url.getPath().matches(
                "/company/microsoft(?:/.*)?"
            );
    }

    private void waitForCompanyContent() {
        By companyName = By.xpath(
            "//*[self::h1 or self::h2 or @role='heading']"
            + "[normalize-space(.)='Microsoft']"
            + " | //main//p[normalize-space(.)='Microsoft']"
        );

        new WebDriverWait(driver, Duration.ofSeconds(60))
            .withMessage(
                "Microsoft's URL opened, but its company name "
                + "did not become visible in the page content."
            )
            .until(browser -> {
                if (!isMicrosoftCompanyPage()) {
                    return false;
                }

                try {
                    for (WebElement element :
                            browser.findElements(companyName)) {

                        if (element.isDisplayed()) {
                            return true;
                        }
                    }

                } catch (StaleElementReferenceException exception) {
                    return false;
                }

                return false;
            });

        System.out.println(
            "STEP: Microsoft company content is visible."
        );
    }

    @Test
    public void searchDisplaysResults() {
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
                "A visible company result should appear."
            );

            System.out.println("Search test passed.");

        } finally {
            captureScreenshot("search-results");
        }
    }

    @Test
    public void firstResultOpensCompanyPage() {
        try {
            System.out.println(
                "TASK 4: Starting result-opening test."
            );

            loginToLinkedIn();

            SearchPage searchPage = new SearchPage(driver);
            searchPage.searchFor("Microsoft");

            Assert.assertTrue(
                searchPage.isResultVisible(),
                "A visible company result should appear before clicking."
            );

            captureScreenshot("task4-before-click");

            searchPage.openFirstResult();

            new WebDriverWait(driver, Duration.ofSeconds(30))
                .withMessage(
                    "The clicked result did not open "
                    + "Microsoft's company URL."
                )
                .until(browser -> isMicrosoftCompanyPage());

            // Wait for rendered company content, not just a URL change.
            waitForCompanyContent();

            Assert.assertTrue(
                isMicrosoftCompanyPage(),
                "Expected Microsoft's company URL, but opened: "
                + driver.getCurrentUrl()
            );

            System.out.println(
                "TASK 4 PASSED: Microsoft company URL "
                + "and visible company content verified."
            );

        } finally {
            if (driver != null) {
                System.out.println(
                    "TASK 4 final URL: " + driver.getCurrentUrl()
                );

                captureScreenshot("opened-company");
            }
        }
    }
}