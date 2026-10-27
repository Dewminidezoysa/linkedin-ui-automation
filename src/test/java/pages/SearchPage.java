package pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SearchPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By searchBox = By.cssSelector(
        "input[data-testid='typeahead-input'][placeholder='Search']"
    );

    private final By viewPageButton = By.xpath(
        "//button[normalize-space(.)='View page']"
    );

    public SearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
            driver, Duration.ofSeconds(30)
        );
    }

    // Finds a visible, enabled element without using another wait.
    private WebElement findVisibleElement(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            if (element.isDisplayed() && element.isEnabled()) {
                return element;
            }
        }

        return null;
    }

    private WebElement visibleElement(By locator) {
        return wait.until(browser -> {
            try {
                return findVisibleElement(locator);
            } catch (StaleElementReferenceException exception) {
                return null;
            }
        });
    }

    public void searchFor(String term) {
        // Clicking may cause LinkedIn to replace the search box.
        wait.until(browser -> {
            try {
                WebElement input = findVisibleElement(searchBox);

                if (input == null) {
                    return false;
                }

                input.click();
                return true;

            } catch (StaleElementReferenceException exception) {
                return false;
            }
        });

        // Locate the current search box again before typing.
        wait.until(browser -> {
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

        // Locate it again before pressing Enter.
        wait.until(browser -> {
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
    }

    public boolean isResultVisible() {
        return visibleElement(viewPageButton).isDisplayed();
    }

    public void openFirstResult() {
        wait.until(browser -> {
            try {
                WebElement button = findVisibleElement(viewPageButton);

                if (button == null) {
                    return false;
                }

                button.click();
                return true;

            } catch (StaleElementReferenceException exception) {
                return false;
            }
        });
    }
}