package sl.tiger.scraper.scraper.scrapers;


import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sl.tiger.scraper.controller.model.ScraperId;
import sl.tiger.scraper.controller.model.StatusMassages;
import sl.tiger.scraper.dto.Availability;
import sl.tiger.scraper.dto.Criteria;
import sl.tiger.scraper.dto.LocationAvailability;
import sl.tiger.scraper.dto.Result;
import sl.tiger.scraper.exception.CriteriaException;
import sl.tiger.scraper.scraper.Scraper;
import sl.tiger.scraper.util.ScrapHelper;

import java.util.ArrayList;
import java.util.List;

import static sl.tiger.scraper.util.ScrapHelper.getExceptionText;

@Component
public class TransbecScraper extends Scraper {

    // TODO read from config
    private static final String USERNAME = "faizal@myautoparts2.ca";
    private static final String PASSWORD = "myauto1080";
    private final Logger logger = LoggerFactory.getLogger(TransbecScraper.class);
    private boolean isLoggedIn = false;
    String lastUrl = "https://transbec.mypartfinder.com/?usersessionid=OGU1Mzk1ZmEtNDAzZi00NzUwLTg2ZDAtZDkzYTYwN2Q4Mzg1&lang=en";

    public TransbecScraper() {
        super("https://orders.transbec.ca/user-login", ScraperId.TRANSBEC.id);
    }

    @Override
    public List<Result> search(Criteria criteria) throws CriteriaException {
        login(); //cmdOk
        this.logger.info(StatusMassages.LOGIN_SUCCESS.status);
        try {
            selectParams(criteria);

            List<Result> results = new ArrayList<>();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("MuiTable-root")));

            setResults(results);

            // TODO get price

            for (Result result : results) {
                webDriver.get("https://orders.transbec.ca/en");
                WebElement quickSearch = webDriver.findElement(By.id("txtHeaderQuickSearch"));
                quickSearch.sendKeys(result.getPartNumber());
                quickSearch.submit();

                if (webDriver.getCurrentUrl().contains("products-search")) {
                    // multiple results match for part number
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ListingProducts")));
                    List<WebElement> searchResults = webDriver.findElement(By.id("ListingProducts")).findElements(By.className("ejs-productitem"));
                    for (WebElement searchResult : searchResults) {
                        if (searchResult.findElement(By.className("product-code")).getText().split(":")[1].trim().equalsIgnoreCase(result.getPartNumber())) {
                            WebElement title = searchResult.findElement(By.className("product-title"));
                            title.findElement(By.tagName("a")).click();
                            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("price-current")));
                            result.setYourPrice(webDriver.findElement(By.className("price-current")).getText());
                            result.setAvailability(getAvailability());
                            break;
                        }
                    }
                } else {
                    // single result match for part number
                    result.setYourPrice(webDriver.findElement(By.className("price")).getText());
                    result.setAvailability(getAvailability());
                }
            }

            webDriver.get(lastUrl);

            return results;

        } catch (Exception ex) {
            logger.error("Transbec Scraper Search : \n", ex);

            webDriver.get(lastUrl);
            if (ex instanceof CriteriaException) {
                throw ex;
            } else {
                throw new CriteriaException(StatusMassages.SOMETHING_WENT_WRONG.status);
            }
        }
    }

    @Override
    public List<Result> searchByPartNumber(String partNumber, boolean isAddToCart, Criteria criteria) throws CriteriaException {
        try {
            login();
            webDriver.get("https://orders.transbec.ca/en");
            findPartNuSearch(partNumber);
            List<Result> results = new ArrayList<>();

            if (webDriver.getCurrentUrl().contains("products-search")) {
                /* multiple results match for part number */
                List<WebElement> searchResults = webDriver.findElement(By.id("ListingProducts")).findElements(By.className("ejs-productitem"));
                for (WebElement searchResult : searchResults) {
                    if (searchResult.findElement(By.className("product-code")).getText().split(":")[1].trim().equalsIgnoreCase(criteria.getPartNumber())) {
                        WebElement title = searchResult.findElement(By.className("product-title"));
                        title.findElement(By.tagName("a")).click();
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("price-current")));
                        setPartNuResult(results);
                        break;
                    }
                }
            } else {
                /* single result match for part number */
                setPartNuResult(results);
            }

            if (webDriver.findElements(By.className("input-mini")).size() > 0) {
                if (isAddToCart) {
                    WebElement qtyInput = webDriver.findElement(By.className("input-mini"));
                    qtyInput.sendKeys("1");
                    webDriver.findElement(By.className("addToCartButtonGroup")).findElement(By.tagName("button")).click();
                    logger.info(StatusMassages.ADD_TO_CART_SUCCESS.status);
                }
            }

            webDriver.get("https://partcat.com/transbec?usersessionid=YmM2NjAwZmItZDhkMi00OTI0LThkNjEtOTliZWQzNjY1NzI2&lang=en");
            logger.info(StatusMassages.SET_RESULT_SUCCESS.status);
            return results;

        } catch (Exception ex) {

            logger.error("Transbec Scraper Part Nu Search : \n", ex);

            webDriver.get("https://partcat.com/transbec?usersessionid=YmM2NjAwZmItZDhkMi00OTI0LThkNjEtOTliZWQzNjY1NzI2&lang=en");

            if (ex instanceof CriteriaException) {
                throw ex;
            } else {
                throw new CriteriaException(StatusMassages.SOMETHING_WENT_WRONG.status);
            }
        }
    }

    public void login() {
        if (isLoggedIn && isResetTimePassed()) {
            logger.info("refreshing the page...... https://partcat.com/transbec");
            webDriver.get("https://orders.transbec.ca/dashboard");
        }
        if (isLoggedIn && !webDriver.findElements(By.id("UserName")).isEmpty()) {
            isLoggedIn = false;
        }
        if (!isLoggedIn) {
            try {
                webDriver.findElement(By.id("UserName")).sendKeys(USERNAME);
                webDriver.findElement(By.id("Password")).sendKeys(PASSWORD);
                webDriver.findElement(By.id("cmdSignIn")).click();
                isLoggedIn = true;
            } catch (NoSuchElementException ex) {
                logger.error(ex.getMessage(), ex);
            }

            // set address dialog
            wait.until(ExpectedConditions.elementToBeClickable(By.id("cmdOk")));
            webDriver.findElement(By.id("cmdOk")).click();
            // go to path finder
            wait.until(ScrapHelper.clickTillElementAttached(By.className("custom-header-content")));
        }
    }

    public void selectParams(Criteria criteria) throws CriteriaException {
        try {
//            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("application")));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("response-tab-new_tab")));

            // select year
            if (!ScrapHelper.selectDropDownElement("year-select", criteria.getYear(), webDriver)) {
                throw new CriteriaException(StatusMassages.SELECT_YEAR_FAILED.status);
            } else {
                logger.info(StatusMassages.SELECT_YEAR_SUCCESS.status);
            }

            // select make
            wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(By.id("year-select"), By.tagName("option")));
            if (!ScrapHelper.selectDropDownElement("make-select", criteria.getMake(), webDriver)) {
                throw new CriteriaException(StatusMassages.SELECT_MAKE_FAILED.status);
            } else {
                logger.info(StatusMassages.SELECT_MAKE_SUCCESS.status);
            }

            // select model

            if (!selectModel("model-select", criteria, webDriver)) {
                throw new CriteriaException(StatusMassages.SELECT_MODEL_FAILED.status);
            } else {
                logger.info(StatusMassages.SELECT_MODEL_SUCCESS.status);
            }

            // select engine
            try {
                Select select = new Select(webDriver.findElement(By.id("engine-select")));
                List<WebElement> options = select.getOptions();
                ScrapHelper.selectAllEngine(criteria, options);
            } catch (Exception e) {
                logger.error(StatusMassages.SELECT_ENGINE_FAILED.status, e);
                throw new CriteriaException(StatusMassages.SELECT_ENGINE_FAILED.status);
            }

            // select product type
            if (!ScrapHelper.selectDropDownElement("product-type-select", criteria.getCategories()[0].getCategory(), webDriver)) {
                throw new CriteriaException(StatusMassages.SELECT_CATEGORY_FAILED.status);
            } else {
                logger.info(StatusMassages.SELECT_CATEGORY_SUCCESS.status);
            }

            // select product line
            if (!ScrapHelper.selectDropDownElement("product-line-select", criteria.getCategories()[0].getGroups()[0], webDriver)) {
                throw new CriteriaException(StatusMassages.SELECT_GROUP_FAILED.status);
            } else {
                logger.info(StatusMassages.SELECT_GROUP_SUCCESS.status);
            }

        } catch (NoSuchElementException ex) {
            logger.error(ex.getMessage(), ex);
        }

    }

    public boolean selectModel(String dropDownId, Criteria criteria, WebDriver webDriver) {
        Select dropDown = new Select(webDriver.findElement(By.id(dropDownId)));
        boolean selected = false;
        int index = 0;
        for (WebElement option : dropDown.getOptions()) {
            if (option.getText().equalsIgnoreCase(ScrapHelper.getModelTrimString(criteria, option.getText(), "\\s+", " "))) break;
            index++;
        }
        if (index < dropDown.getOptions().size()) {
            dropDown.selectByIndex(index);
            selected = true;
        }
        return selected;
    }

    private Availability getAvailability() {

        WebElement qtyByWarehouseTable = webDriver.findElement(By.className("qtyByWarehouseTable"));
        WebElement tbody = qtyByWarehouseTable.findElement(By.tagName("tbody"));
        List<WebElement> tr = tbody.findElements(By.tagName("tr"));

        Availability availability = new Availability();
        availability.setAvailable(false);

        for (WebElement tableRows : tr) {

            LocationAvailability locationAvailability = new LocationAvailability();

            WebElement warehouseName = tableRows.findElement(By.className("warehouseName"));
            WebElement warehouseQty = tableRows.findElement(By.className("warehouseQty"));
            WebElement warehouseAvail = tableRows.findElement(By.className("warehouseAvail"));

            if (warehouseQty.getText().trim().equals("-")) {
                locationAvailability.setLocation(warehouseName.getText().trim());
            } else if (warehouseQty.getText().trim().equals("-") && warehouseAvail.findElement(By.tagName("img")).getAttribute("alt").equals("In Stock")) {
                availability.setAvailable(true);
                locationAvailability.setLocation(warehouseName.getText().trim());
            } else if (warehouseQty.getText().trim().equals("0")) {
                locationAvailability.setLocation(warehouseName.getText().trim());
            } else {
                availability.setAvailable(true);
                locationAvailability.setLocation(warehouseName.getText().trim());
                locationAvailability.setQuantity(Integer.parseInt(warehouseQty.getText().trim()));
            }

            availability.getLocationAvailability().add(locationAvailability);
        }

        return availability;
    }

    public void setResults(List<Result> results) {

        WebElement resultTable = webDriver.findElement(By.id("MuiTable-root")).findElement(By.tagName("tbody"));

        List<WebElement> rows = resultTable.findElements(By.tagName("tr"));
        rows.forEach(webElement -> {
            Result result = new Result();
            List<WebElement> rowColumns = webElement.findElements(By.tagName("td"));
            try {
                result.setPartNumber(rowColumns.get(0).getText());
                result.setProductLine(rowColumns.get(1).getText());
                result.setDescription(rowColumns.get(2).getText());
                result.setPosition(rowColumns.get(3).getText());
                result.setAttributes(rowColumns.get(4).getText());
                result.setApplicationNotes(rowColumns.get(5).getText());
            } catch (NoSuchElementException ex) {
                logger.error(ex.getMessage(), ex);
            }

            this.logger.info(StatusMassages.SET_RESULT_SUCCESS.status);
            results.add(result);
        });

    }

    public void findPartNuSearch(String partNumber) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("frmWarehouseSelector")));
        WebElement searchField = webDriver.findElement(By.id("txtHeaderQuickSearch"));
        searchField.sendKeys(partNumber);
        webDriver.findElements(By.className("btn-go")).get(1).click();
    }

    public void setPartNuResult(List<Result> results) {
        try {
            Result result = new Result();

            WebElement imageElement = webDriver.findElement(By.className("product-image"));
            String imageURL = imageElement.findElement(By.tagName("img")).getAttribute("src");

            result.setPartNumber(webDriver.findElement(By.className("product-details-code")).getText().split(":")[1].trim());
            result.setDescription(webDriver.findElement(By.className("product-details-desc")).getText());
            result.setYourPrice(webDriver.findElement(By.className("price-current")).getText());
            result.setAvailability(getAvailability());
            result.setImageUrl(imageURL);

            results.add(result);
            logger.info(StatusMassages.SET_RESULT_SUCCESS.status);
        } catch (NoSuchElementException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
