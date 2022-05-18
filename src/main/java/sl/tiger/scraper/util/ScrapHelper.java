package sl.tiger.scraper.util;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sl.tiger.scraper.dto.Criteria;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ScrapHelper {

    private static final Logger logger = LoggerFactory.getLogger(ScrapHelper.class);

    private ScrapHelper() {

    }

    public static boolean selectTableCell(String tableId, String cellValue, WebDriver webDriver, WebDriverWait wait) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(tableId)));
        WebElement table = webDriver.findElement(By.id(tableId));
        boolean selected = false;
        List<WebElement> cells = table.findElements(By.tagName("td"));
        for (WebElement webElement : cells) {
            if (webElement.getText().equalsIgnoreCase(cellValue)) {
                webElement.click();
                selected = true;
                break;
            }
        }
        return selected;
    }

    public static boolean selectAnchorInContainer(String containerId, String value, WebDriver webDriver) {
        WebElement container = webDriver.findElement(By.id(containerId));
        boolean selected = false;
        List<WebElement> elements = container.findElements(By.tagName("a"));
        for (WebElement webElement : elements) {
            if (webElement.getText().equalsIgnoreCase(value)) {
                webElement.click();
                selected = true;
                break;
            }
        }
        return selected;
    }

    public static boolean selectTableCells(String tableId, String[] cellValues, WebDriver webDriver) {
        WebElement table = webDriver.findElement(By.id(tableId));
        boolean selected = false;
        List<WebElement> cells = table.findElements(By.tagName("td"));
        for (WebElement webElement : cells) {
            if (Arrays.asList(cellValues).contains(webElement.getText())) {
                webElement.click();
                selected = true;
                webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            }
        }
        return selected;
    }

    public static void waitUntilTableLoaded(String tableId, WebDriver webDriver, WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(tableId)));
        } catch (UnhandledAlertException f) {
            try {
                Alert alert = webDriver.switchTo().alert();
                alert.accept();
            } catch (NoAlertPresentException e) {
                e.printStackTrace();
            }
        }

        wait.until(driver -> {
            WebElement table = webDriver.findElement(By.id(tableId));
            int elementCount = table.findElements(By.tagName("td")).size();
            return elementCount > 1;
        });
    }

    public static boolean isEqualWithTrim(String st1, String st2) {
        return st1.trim().equalsIgnoreCase(st2.trim());
    }

    public static boolean isContainsWithTrim(String container, String checkValue) {
        return container.trim().toLowerCase().contains(checkValue.trim().toLowerCase());
    }

    public static boolean selectDropDownElement(String dropDownId, String dropDownValue, WebDriver webDriver) {
        Select dropDown = new Select(webDriver.findElement(By.id(dropDownId)));
        boolean selected = false;
        int index = 0;
        for (WebElement option : dropDown.getOptions()) {
            if (option.getText().equalsIgnoreCase(dropDownValue))
                break;
            index++;
        }
        if (index < dropDown.getOptions().size()) {
            dropDown.selectByIndex(index);
            selected = true;
        }
        return selected;
    }

    public static ExpectedCondition<Boolean> clickTillElementAttached(final By locator) {
        return new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                WebElement element = driver.findElement(locator);
                try {
                    element.click();
                    return true;
                } catch (StaleElementReferenceException | ElementClickInterceptedException ex) {
                    return false;
                }
            }

            public String toString() {
                return String.format("element '%s' still not attached to dom", locator);
            }
        };
    }

    public static String generateName(String fileName, Criteria criteria) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter nameDTF = DateTimeFormatter.ofPattern("yyyy-MM-dd - HH.mm.ss");

        String myPath = "../images/" + dtf.format(now) + "/";

        String output = myPath + fileName + " - " + criteria.screenshotName() + " - " + "[" + nameDTF.format(now) + "]" + ".jpeg";

        logger.info(criteria.toString());

        return output;

    }

    public static String getModelTrimString(Criteria criteria, String modelElement, String splitRegex, String spaceOrUnderscore) {
        String model = criteria.getModel();

        String[] criteriaModelSplit = model.split("\\s+");
        String[] modelElementSplit = modelElement.split(splitRegex);

        if (modelElementSplit.length >= 2 && criteriaModelSplit.length >= 2) {
            return criteriaModelSplit[0] + spaceOrUnderscore + criteriaModelSplit[1];
        }

        return criteria.getTrim() == null ? criteria.getModel() : criteria.getModel() + spaceOrUnderscore + criteria.getTrim();
    }

    public static void selectAllEngine(Criteria criteria, List<WebElement> cells) {

        if (cells.size() == 1) {
            cells.get(0).click();
            return;
        }

        if (criteria.getEngine() == null || criteria.getEngine().isEmpty()) {
            selectAllEngineOrZeroth(cells);
            return;
        }

        String[] engineSplit = criteria.getEngine().split("\\s+");
        String literPart = engineSplit[0];
        List<WebElement> filteredList = cells.stream()
                .filter(cell -> ScrapHelper.isContainsWithTrim(cell.getText(), literPart))
                .collect(Collectors.toList());

        if (filteredList.size() == 1) {
            filteredList.get(0).click();
            return;
        }

        String lastPart = engineSplit[engineSplit.length - 1];
        filteredList = filteredList.stream()
                .filter(cell -> ScrapHelper.isContainsWithTrim(cell.getText(), lastPart))
                .collect(Collectors.toList());


        filteredList.get(0).click();
    }

    private static void selectAllEngineOrZeroth(List<WebElement> cells) {
        boolean isEngineFound = false;

        cells.get(1).click();

        if (!isEngineFound) {
            cells.get(0).click();
        }
    }

    public static String getExceptionText(Exception ex) {
        int line = Thread.currentThread().getStackTrace()[2].getLineNumber();
        String className = Thread.currentThread().getStackTrace()[2].getFileName().split("\\.")[0];
        return className + ":" + line + ":" + ex.getClass().getSimpleName();
    }
}
