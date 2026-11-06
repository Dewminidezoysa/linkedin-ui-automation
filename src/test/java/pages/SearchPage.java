package pages;

import java.net.URI;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SearchPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private String clickedCompanyPath;

    private final By searchBox = By.cssSelector(
        "input[data-testid='typeahead-input'][placeholder='Search']"
    );

    private final By companiesFilter = By.xpath(
        "//label[normalize-space(.)='Companies']"
    );

    private final By companyLinks = By.cssSelector(
        "main a[href*='/company/']"
    );

    public SearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
            driver, Duration.ofSeconds(30)
        );
    }

    private WebElement findVisibleElement(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            if (element.isDisplayed() && element.isEnabled()) {
                return element;
            }
        }

        return null;
    }

    private boolean isCompanyPath(String path) {
        return path != null
            && path.matches("/company/[^/]+/?");
    }

    private WebElement findFirstCompanyLink() {
        for (WebElement link : driver.findElements(companyLinks)) {
            if (!link.isDisplayed() || !link.isEnabled()) {
                continue;
            }

            String href = link.getAttribute("href");

            if (href == null) {
                continue;
            }

            URI url = URI.create(href);

            if ("www.linkedin.com".equals(url.getHost())
                    && isCompanyPath(url.getPath())) {
                return link;
            }
        }

        return null;
    }

    private void clickVisible(By locator) {
        wait.withMessage(
            "Could not click a visible element: " + locator
        ).until(browser -> {
            try {
                WebElement element = findVisibleElement(locator);

                if (element == null) {
                    return false;
                }

                ((JavascriptExecutor) browser).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});",
                    element
                );

                element.click();
                return true;

            } catch (StaleElementReferenceException
                     | ElementClickInterceptedException exception) {
                return false;
            }
        });
    }

    public void searchFor(String term) {
        clickVisible(searchBox);

        wait.withMessage(
            "Could not enter the search term."
        ).until(browser -> {
            try {
                WebElement input = findVisibleElement(searchBox);

                if (input == null) {
                    return false;
                }

                input.sendKeys(
                    Keys.chord(Keys.CONTROL, "a"),
                    term
                );

                return term.equals(input.getDomProperty("value"));

            } catch (StaleElementReferenceException exception) {
                return false;
            }
        });

        wait.withMessage(
            "Could not submit the search term."
        ).until(browser -> {
            try {
                WebElement input = findVisibleElement(searchBox);

                if (input == null
                        || !term.equals(input.getDomProperty("value"))) {
                    return false;
                }

                input.sendKeys(Keys.ENTER);
                return true;

            } catch (StaleElementReferenceException exception) {
                return false;
            }
        });

        wait.until(
            ExpectedConditions.urlContains("/search/results/")
        );

        System.out.println(
            "STEP: Selecting the Companies search filter."
        );

        clickVisible(companiesFilter);

        wait.withMessage(
            "The Companies search results did not open."
        ).until(
            ExpectedConditions.urlContains(
                "/search/results/companies"
            )
        );

        wait.withMessage(
            "No visible company result link was found."
        ).until(browser -> {
            try {
                return findFirstCompanyLink() != null;
            } catch (StaleElementReferenceException exception) {
                return false;
            }
        });

        System.out.println(
            "STEP: Company search results displayed."
        );
    }

    public boolean isResultVisible() {
        return wait.withMessage(
            "No visible company result link was found."
        ).until(browser -> {
            try {
                return findFirstCompanyLink() != null;
            } catch (StaleElementReferenceException exception) {
                return false;
            }
        });
    }

    private boolean matchesClickedCompany() {
        URI current = URI.create(driver.getCurrentUrl());

        if (!"www.linkedin.com".equals(current.getHost())
                || clickedCompanyPath == null) {
            return false;
        }

        String path = current.getPath();

        return path.equals(clickedCompanyPath)
            || path.startsWith(clickedCompanyPath + "/");
    }

    public void openFirstResult() {
        String originalWindow = driver.getWindowHandle();

        Set<String> originalWindows =
            new HashSet<>(driver.getWindowHandles());

        wait.withMessage(
            "The first company result could not be clicked."
        ).until(browser -> {
            try {
                WebElement link = findFirstCompanyLink();

                if (link == null) {
                    return false;
                }

                String href = link.getAttribute("href");

                clickedCompanyPath = URI.create(href)
                    .getPath()
                    .replaceAll("/+$", "");

                System.out.println(
                    "STEP: Clicking first company result: " + href
                );

                ((JavascriptExecutor) browser).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});",
                    link
                );

                link.click();
                return true;

            } catch (StaleElementReferenceException
                     | ElementClickInterceptedException exception) {
                return false;
            }
        });

        wait.withMessage(
            "The clicked company's destination page did not open."
        ).until(browser -> {
            for (String handle : browser.getWindowHandles()) {
                if (handle.equals(originalWindow)
                        || !originalWindows.contains(handle)) {

                    browser.switchTo().window(handle);

                    if (matchesClickedCompany()) {
                        return true;
                    }
                }
            }

            browser.switchTo().window(originalWindow);
            return false;
        });

        System.out.println(
            "STEP: Company page opened: " + driver.getCurrentUrl()
        );
    }

    public String waitForCompanyHeading(String expectedName) {
        return wait.withMessage(
            "The expected company heading was not visible: "
            + expectedName
        ).until(browser -> {
            try {
                for (WebElement heading :
                        browser.findElements(By.tagName("h1"))) {

                    String text = heading.getText().trim();

                    if (heading.isDisplayed()
                            && expectedName.equalsIgnoreCase(text)) {
                        return text;
                    }
                }

            } catch (StaleElementReferenceException exception) {
                return null;
            }

            return null;
        });
    }
}