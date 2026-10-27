package pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By emailField =
        By.cssSelector("input[type='email']");

    private final By passwordField =
        By.cssSelector("input[type='password']");

    private final By signInButton =
        By.xpath("//button[normalize-space(.)='Sign in']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
            driver, Duration.ofSeconds(30)
        );
    }

    public void open() {
        driver.get("https://www.linkedin.com/login/");
    }

    private WebElement visibleElement(By locator) {
        return wait.until(browser -> {
            try {
                for (WebElement element :
                        browser.findElements(locator)) {
                    if (element.isDisplayed()
                            && element.isEnabled()) {
                        return element;
                    }
                }
            } catch (StaleElementReferenceException exception) {
                return null;
            }

            return null;
        });
    }

    public void login(String email, String password) {
        WebElement emailInput = visibleElement(emailField);
        emailInput.clear();
        emailInput.sendKeys(email);

        WebElement passwordInput = visibleElement(passwordField);
        passwordInput.clear();
        passwordInput.sendKeys(password);

        visibleElement(signInButton).click();
    }
        public String getLoginErrorMessage() {
        By errorMessage = By.xpath(
            "//*[normalize-space(.)='Wrong email or password.'"
            + " and not(*)]"
        );

        return visibleElement(errorMessage).getText();
    }
}