package sl.tiger.scraper.scraper.scrapers;

import sl.tiger.scraper.controller.model.ScraperId;
import sl.tiger.scraper.controller.model.StatusMassages;
import sl.tiger.scraper.dto.Availability;
import sl.tiger.scraper.dto.Criteria;
import sl.tiger.scraper.dto.LocationAvailability;
import sl.tiger.scraper.dto.Result;
import sl.tiger.scraper.exception.CriteriaException;
import sl.tiger.scraper.scraper.Scraper;
import sl.tiger.scraper.util.ScrapHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static sl.tiger.scraper.util.ScrapHelper.getExceptionText;

@Component
public class AltromScraper extends Scraper {

    public static final String USERNAME = "11-4830";
    public static final String PASSWORD = "4842";
    Logger logger = LoggerFactory.getLogger(AltromScraper.class);
    private boolean isLoggedIn = false;

    public AltromScraper() {
        super("https://www.altrom.com/indexnew.html", ScraperId.ALTROM.id);
    }

    @Override
    public List<Result> search(Criteria criteria) throws CriteriaException {

        try {

            login();
            logger.info(StatusMassages.LOGIN_SUCCESS.status);

            selectYear(criteria);

            if (!selectMake(criteria)) {
                throw new CriteriaException(StatusMassages.SELECT_MAKE_FAILED.status);
            }
            logger.info(StatusMassages.SELECT_MAKE_SUCCESS.status);

            if (!selectModel(criteria)) {
                throw new CriteriaException(StatusMassages.SELECT_MODEL_FAILED.status);
            }

            if (!selectCategory(criteria)) {
                throw new CriteriaException(StatusMassages.SELECT_CATEGORY_FAILED.status);
            }
            logger.info(StatusMassages.SELECT_CATEGORY_SUCCESS.status);

            List<Result> results = new ArrayList<>();
            setResults(results, criteria);

            webDriver.get("https://www.altrom.com/cgi-bin/altmake.sh");

            return results;

        } catch (Exception ex) {
            logger.error("Altrom Scraper Search : \n", ex);

            webDriver.get("https://www.altrom.com/cgi-bin/altmake.sh");
            if (ex instanceof CriteriaException) {
                throw ex;
            } else {
                throw new CriteriaException("Something went wrong");
            }
        }
    }

    @Override
    public List<Result> searchByPartNumber(String partNumber, boolean isAddToCart, Criteria criteria) throws CriteriaException {

        try {

            login();
            logger.info(StatusMassages.LOGIN_SUCCESS.status);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("findform")));

            findPartNuSearch(partNumber, true);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }

            List<Result> results = new ArrayList<>();

            WebElement primaryContent = webDriver.findElement(By.id("primaryContent"));
            WebElement applisting = primaryContent.findElement(By.className("applisting"));
            WebElement cartUpdate = applisting.findElement(By.name("cartUpdate"));
            WebElement table = cartUpdate.findElement(By.tagName("table"));
            WebElement tbody = table.findElement(By.tagName("tbody"));
            WebElement tr = tbody.findElement(By.tagName("tr"));

            if (tr.getAttribute("class").equals("")) {
                webDriver.get("https://www.altrom.com/cgi-bin/altmake.sh");
                throw new CriteriaException(StatusMassages.PART_NOT_AVAILABLE.status);
            } else {
                setPartNuResult(results);
            }

            if (!tr.getAttribute("class").equals("")) {
                if (isAddToCart) {
                    List<WebElement> td = tr.findElements(By.tagName("td"));
                    td.get(5).click();
                    td.get(5).findElement(By.tagName("input")).sendKeys("1");

                    webDriver.findElements(By.className("addToCartContainer")).get(1).findElement(By.tagName("input")).click();

                    wait.until(ExpectedConditions.stalenessOf(applisting));

                    webDriver.findElement(By.id("shoppingCartTitle")).click();
                    System.out.println("clicked");

                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cartCheckout")));
                    System.out.println("waited");

                    WebElement cartCheckout = webDriver.findElement(By.id("cartCheckout"));
                    cartCheckout.findElements(By.tagName("input")).get(0).click();

                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalWindowWrapperSavedquote")));

                    WebElement mainPopUp = webDriver.findElement(By.id("modalWindowWrapperSavedquote"));
                    String descriptionName = criteria.getPartNumber() + " - " + Instant.now();
                    mainPopUp.findElement(By.id("SQD")).sendKeys(descriptionName);
                    mainPopUp.findElement(By.id("SQRB2")).click();

                    webDriver.findElement(By.name("SQForm")).findElements(By.tagName("input")).get(4).click();

                    wait.until(ExpectedConditions.stalenessOf(cartCheckout));

                    logger.info("add to cart success");
                }
            }

            webDriver.get("https://www.altrom.com/cgi-bin/altmake.sh");

            return results;

        } catch (Exception ex) {
            logger.error("Altrom Scraper Part nu Search : \n", ex);
            webDriver.get("https://www.altrom.com/cgi-bin/altmake.sh");
            if (ex instanceof CriteriaException) {
                throw ex;
            } else {
                throw new CriteriaException("Something went wrong");
            }
        }
    }

    public void login() {
        if (isLoggedIn && isResetTimePassed()) {
            logger.info("refreshing the page...... https://www.altrom.com/cgi-bin/altmake.sh");
            webDriver.get("https://www.altrom.com/cgi-bin/altmake.sh");
        }
        if (isLoggedIn && !webDriver.findElements(By.id("loginForm")).isEmpty()) {
            isLoggedIn = false;
        }
        if (!isLoggedIn) {
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lgn")));

                WebElement loginForm = webDriver.findElement(By.id("loginForm"));

                loginForm.findElement(By.id("lgn")).sendKeys(USERNAME);
                loginForm.findElement(By.id("pin")).sendKeys(PASSWORD);
                loginForm.findElement(By.className("btn")).click();
                isLoggedIn = true;

                Thread.sleep(3000);

                webDriver.get("https://www.altrom.com/cgi-bin/altmake.sh");

            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
                Thread.currentThread().interrupt();
            } catch (NoSuchElementException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    public void selectYear(Criteria criteria) throws CriteriaException {

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("findform")));
        if (!selectTableAndSelectCell("Parts by Year", criteria.getYear())) {
            throw new CriteriaException("select year failed");
        }
        logger.info(StatusMassages.SELECT_YEAR_SUCCESS.status);
    }

    public boolean selectMake(Criteria criteria) {

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("findform")));
        WebElement table = webDriver.findElement(By.id("modelTable"));
        boolean selected = false;
        List<WebElement> cells = table.findElements(By.tagName("a"));
        for (WebElement webElement : cells) {
            if (webElement.getText().equalsIgnoreCase(criteria.getMake())) {
                webElement.click();
                selected = true;
                break;
            }
        }
        return selected;
    }

    public boolean selectModel(Criteria criteria) {
        boolean selected = false;
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelTable")));
            WebElement mainTable = webDriver.findElement(By.id("modelTable"));
            List<WebElement> cells = mainTable.findElements(By.tagName("a"));

            String[] criteriaSplit = criteria.getModel().trim().split("-");
            String criteriaModel = criteriaSplit[0];

            List<WebElement> collect = cells.stream()
                    .filter(mainCell -> criteriaModel.trim().equalsIgnoreCase(mainCell.getText().trim()))
                    .collect(Collectors.toList());

            if (!collect.isEmpty()) {
                WebElement webElement = collect.get(0);
                List<WebElement> imgs = webElement.findElements(By.tagName("img"));

                /* If an Image Exist */
                if (!imgs.isEmpty()) {

                    String id = imgs.get(0).getAttribute("id");
                    String substringTableId = id.substring(id.length() - 8);
                    WebElement tableWithId = webDriver.findElement(By.id(substringTableId));

                    /* Check main model name with criteria model name */
                    if (webElement.getText().trim().equalsIgnoreCase(criteriaModel.trim())) {
                        webElement.click();

                        List<WebElement> engineRowsWithTrim = tableWithId.findElements(By.tagName("tr"));
                        boolean isSingleTrim = engineRowsWithTrim.get(0).findElements(By.tagName("td")).get(0).getText().trim().isEmpty();
                        if (isSingleTrim) {
                            selectEngine(criteria, substringTableId);
                        } else {
                            List<WebElement> filteredEngineRowsWithTrim = engineRowsWithTrim.stream().filter(row -> {
                                String tdTextTrim = row.findElements(By.tagName("td")).get(0).getText().trim();
                                return tdTextTrim.trim().equalsIgnoreCase(criteriaSplit[1].trim());
                            }).collect(Collectors.toList());

                            if (filteredEngineRowsWithTrim.size() == 1) {
                                filteredEngineRowsWithTrim.get(0).click();
                            } else {
                                ScrapHelper.selectAllEngine(criteria, filteredEngineRowsWithTrim);
                            }
                        }
                        selected = true;
                    }
                } else {
                    /* if an image not exist */
                    webElement.click();
                    selected = true;
                    logger.info(StatusMassages.SELECT_MODEL_SUCCESS.status);
                }
            }
        } catch (NoSuchElementException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return selected;
    }

    public void selectEngine(Criteria criteria, String engineContainerId) {

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(engineContainerId)));
        WebElement table = webDriver.findElement(By.id(engineContainerId));
        List<WebElement> cells = table.findElements(By.tagName("td"));

        ScrapHelper.selectAllEngine(criteria, cells);

    }

    public boolean selectCategory(Criteria criteria) {
        boolean selected = false;
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("resetCategoryContainer")));
            WebElement elementContainer = webDriver.findElement(By.id("resetCategoryContainer"));
            List<WebElement> cells = elementContainer.findElements(By.tagName("a"));
            for (WebElement webElement : cells) {
                if (ScrapHelper.isEqualWithTrim(webElement.getText(), criteria.getCategoryNames()[0])) {
                    shortWait.until(ExpectedConditions.elementToBeClickable(webElement));
                    webElement.click();
                    selected = true;
                    break;
                }
            }
        } catch (NoSuchElementException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return selected;
    }

    private Availability getAvailability(WebElement webElement) {
        Availability availability = new Availability();

        WebElement homstock = webElement.findElement(By.className("homstock"));

        if (homstock.getText().contains("Please")) {
            availability.setAvailable(false);
            availability.setMassage("Call For Availability");
            return availability;
        }

        WebElement altstock = webElement.findElement(By.className("altstock"));
        LocationAvailability inHouseAvailability = new LocationAvailability();

        if (Integer.parseInt(homstock.getText().trim()) >= 1) {
            inHouseAvailability.setLocation("Scarborough");
            inHouseAvailability.setQuantity(Integer.parseInt(homstock.getText().trim()));
            availability.setAvailable(true);
            availability.getLocationAvailability().add(inHouseAvailability);
        } else {
            inHouseAvailability.setLocation("Scarborough");
            inHouseAvailability.setQuantity(Integer.parseInt(homstock.getText().trim()));
            availability.setAvailable(false);
            availability.getLocationAvailability().add(inHouseAvailability);
        }
        LocationAvailability corporateAvailability = new LocationAvailability();
        if (altstock.getText().trim().equals("")) {
            corporateAvailability.setLocation("Corporate Availability");
            corporateAvailability.setQuantity(0);
            availability.getLocationAvailability().add(corporateAvailability);
        } else {
            corporateAvailability.setLocation("Corporate Availability");
            corporateAvailability.setQuantity(Integer.parseInt(altstock.getText().trim()));
            availability.getLocationAvailability().add(corporateAvailability);
        }

        return availability;
    }

    public void findPartNuSearch(String partNumber, boolean isAddToCart) {

        // Find text field
        WebElement searchForm = webDriver.findElement(By.id("findform"));
        WebElement table = searchForm.findElement(By.tagName("table"));
        WebElement element = table.findElement(By.name("compForm"));
        WebElement searchField = element.findElement(By.id("competitor"));

        searchField.sendKeys(partNumber);

        // Click find button
        element.findElements(By.tagName("input")).get(1).click();
    }

    public void setResults(List<Result> results, Criteria criteria) {

        if (criteria.getMaxResultCount() != 0) {
            MAX_RESULT_COUNT = criteria.getMaxResultCount();
        }

        WebElement resultTable = webDriver.findElement(By.className("dataTable"));

        List<WebElement> rows = resultTable.findElements(By.tagName("tr"));
        String title = "";
        Result result;
        int count = 1;

        for (WebElement webElement : rows) {
            if (count > MAX_RESULT_COUNT)
                break;

            if (webElement.getAttribute("class").contains("greenfader")) {
                title = webElement.getText();
            }
            if (webElement.getAttribute("class").contains("dataResults partline")
                    || webElement.getAttribute("class").contains("alternatingDataResults partline")) {
                result = new Result();

                try {
                    result.setTitle(title);
                    List<WebElement> cells = webElement.findElements(By.tagName("td"));
                    result.setDescription(cells.get(2).getText());
                    result.setImageUrl(cells.get(1).findElements(By.tagName("a")).get(1).getAttribute("href"));
                    result.setPartNumber(cells.get(1).getText());

                    if (criteria.isWithAvailability()) {
                        result.setAvailability(getAvailability(webElement));
                    }

                    List<WebElement> priceCells = cells.get(3).findElements(By.tagName("div"));
                    result.setListPrice(priceCells.get(0).findElements(By.tagName("span")).get(1).getText());
                    result.setYourPrice(priceCells.get(1).findElements(By.tagName("span")).get(1).getText());
                    if (priceCells.size() == 3) {
                        result.setCorePrice(priceCells.get(2).findElements(By.tagName("span")).get(1).getText());
                    }
                } catch (NoSuchElementException ex) {
                    logger.error(ex.getMessage(), ex);
                }
                results.add(result);
                count++;
            }
        }
        logger.info(StatusMassages.SET_RESULT_SUCCESS.status);
    }

    public void setPartNuResult(List<Result> results) {

        WebElement resultTable = webDriver.findElement(By.className("dataTable"));

        List<WebElement> rows = resultTable.findElements(By.tagName("tr"));
        String title = "";
        Result result;
        for (WebElement webElement : rows) {
            if (webElement.getAttribute("class").contains("greenfader")) {
                title = webElement.getText();
            }
            if (webElement.getAttribute("class").contains("dataResults partline")) {
                result = new Result();
                result.setTitle(title);
                List<WebElement> cells = webElement.findElements(By.tagName("td"));
                result.setDescription(cells.get(2).getText());
                result.setImageUrl(cells.get(1).findElements(By.tagName("a")).get(1).getAttribute("href"));
                result.setPartNumber(cells.get(1).getText());

                result.setAvailability(getAvailability(webElement));

                List<WebElement> priceCells = cells.get(3).findElements(By.tagName("div"));
                result.setListPrice(priceCells.get(0).findElements(By.tagName("span")).get(1).getText());
                result.setYourPrice(priceCells.get(1).findElements(By.tagName("span")).get(1).getText());
                if (priceCells.size() == 3) {
                    result.setCorePrice(priceCells.get(2).findElements(By.tagName("span")).get(1).getText());
                }
                results.add(result);
            }
        }
        logger.info(StatusMassages.SET_RESULT_SUCCESS.status);

    }

    private boolean selectTableAndSelectCell(String tableTitle, String cellText) {
        List<WebElement> tableContainers = webDriver.findElements(By.id("resetContCont"));
        boolean selected = false;
        for (WebElement tableContainer : tableContainers) {
            if (tableContainer.findElement(By.tagName("legend")).getText().equalsIgnoreCase(tableTitle)) {
                WebElement table = tableContainer.findElement(By.id("modelTable"));
                List<WebElement> cells = table.findElements(By.tagName("a"));
                for (WebElement webElement : cells) {
                    if (webElement.getText().equalsIgnoreCase(cellText)) {
                        webElement.click();
                        selected = true;
                        break;
                    }
                }
                break;
            }
        }
        return selected;
    }
}
