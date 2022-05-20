package sl.tiger.scraper.scraper.scrapers;

import org.springframework.beans.factory.annotation.Autowired;
import sl.tiger.scraper.business.CriteriaRepository;
import sl.tiger.scraper.business.ResultRepository;
import sl.tiger.scraper.controller.model.ScraperId;
import sl.tiger.scraper.controller.model.StatusMassages;
import sl.tiger.scraper.dto.Availability;
import sl.tiger.scraper.dto.Criteria;
import sl.tiger.scraper.dto.Result;
import sl.tiger.scraper.exception.CriteriaException;
import sl.tiger.scraper.scraper.Scraper;
import sl.tiger.scraper.util.ScrapHelper;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sl.tiger.scraper.util.ScrapHelper.getExceptionText;

@Component
public class KeystoneAutomotiveScraper extends Scraper {

    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private CriteriaRepository criteriaRepository;

    public static final String USERCODE = "160.719";
    public static final String PASSWORD = "Myauto#1080";
    Logger logger = LoggerFactory.getLogger(KeystoneAutomotiveScraper.class);
    private boolean isLoggedIn = false;

    public KeystoneAutomotiveScraper() {
        super("https://portal.lkqcorp.com/login", ScraperId.KEYSTONE_AUTOMOTIVE.id);
    }

    @Override
    public List<Result> search(Criteria criteria) throws CriteriaException {
        try {
            login();
            logger.info(StatusMassages.LOGIN_SUCCESS.status);

            selectCrashParts();

            chooseSelects(criteria);

            ArrayList<Result> results = new ArrayList<>();
            setResult(results, criteria);


            clickReset();
            webDriver.navigate().refresh();

            criteria.setDate(LocalDateTime.now());
            criteria.setSiteName(ScraperId.KEYSTONE_AUTOMOTIVE.id);

            criteriaRepository.save(criteria);
            resultRepository.saveAll(results);

            return results;

        } catch (Exception e) {
            logger.error("Keystone Scraper Search : \n", e);
            webDriver.navigate().refresh();

            if (e instanceof CriteriaException) {
                throw e;
            } else {
                throw new CriteriaException(StatusMassages.SOMETHING_WENT_WRONG.status);
            }
        }
    }

    @Override
    public List<Result> searchByPartNumber(String partNumber, boolean isAddToCart, Criteria criteria) throws CriteriaException {
        try {
            login();
            logger.info(StatusMassages.LOGIN_SUCCESS.status);

            findPartNuSearch(partNumber);
            logger.info(StatusMassages.PART_NO_SEARCH_SUCCESS.status);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("search-results-container")));

            ArrayList<Result> results = new ArrayList<>();

            WebElement mainForm = webDriver.findElement(By.id("main-content-area"));
            WebElement itemContainer = mainForm.findElement(By.className("app-container-item"));

            WebElement resultSort = itemContainer.findElement(By.className("aisle-wrapper")).findElement(By.className("results-sort-wrapper"));
            WebElement resultText = resultSort.findElement(By.className("results-text"));
            WebElement resultSize = resultText.findElements(By.tagName("span")).get(1);

            if (Integer.parseInt(resultSize.getText()) > 1) {
                partNuSetResult(results);
            } else if (Integer.parseInt(resultSize.getText()) == 0) {
                partNuSearchReset();
                throw new CriteriaException(StatusMassages.PART_NOT_AVAILABLE.status);
            } else {
                setResult(results, criteria);
            }

            if (isAddToCart) {
                WebElement items = itemContainer.findElement(By.id("search-results-container"));
                WebElement item = items.findElement(By.className("flex-card-item"));

                WebElement webElement = item.findElements(By.className("app-container-item")).get(0);
                WebElement productCost = webElement.findElement(By.className("product-costs"));
                WebElement element = productCost.findElement(By.className("picker-cart-wrapper"));

                WebElement button = element.findElement(By.className("cart-button"));
                wait.until(ExpectedConditions.elementToBeClickable(button));

                button.click();
                logger.info(StatusMassages.ADD_TO_CART_SUCCESS.status);
            }

            partNuSearchReset();
            webDriver.navigate().refresh();

            criteria.setDate(LocalDateTime.now());
            criteria.setSiteName(ScraperId.KEYSTONE_AUTOMOTIVE.id);

            criteriaRepository.save(criteria);
            resultRepository.saveAll(results);

            return results;

        } catch (Exception e) {
            logger.error("Keystone Scraper Part Nu Search : \n", e);

            partNuSearchReset();

            webDriver.navigate().refresh();
            if (e instanceof CriteriaException) {
                throw e;
            } else {
                throw new CriteriaException(StatusMassages.PART_NOT_AVAILABLE.status);
            }
        }
    }

    public void login() {
        if (isLoggedIn && isResetTimePassed()) {
            logger.info("refreshing the page...... https://preview.orderkeystone.com/");
            webDriver.get("https://preview.orderkeystone.com/");
        }
        if (isLoggedIn && !webDriver.findElements(By.id("passwordTextBox")).isEmpty()) {
            isLoggedIn = false;
        }

        if (!isLoggedIn) {
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("main-content")));

                WebElement loginForm = webDriver.findElement(By.className("main-content"));

                loginForm.findElement(By.id("username")).sendKeys(USERCODE);
                loginForm.findElement(By.id("passwordTextBox")).sendKeys(PASSWORD);
                loginForm.findElement(By.className("accent-button")).click();
                isLoggedIn = true;

                selectAfterMarket();

            } catch (NoSuchElementException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        selectCrashParts();
    }

    public void selectAfterMarket() {

        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//*[@id=\"main-content-area\"]/app-landing")));

        List<WebElement> elements = webDriver.findElements(By.className("icon-bucket-wrapper"));
        WebElement button = elements.get(0).findElement(By.tagName("button"));
        button.click();

    }

    public void selectCrashParts() {


        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("/html/body/app-root/app-nav-bar/div/div[3]/div[2]/app-side-bar-search")));

        WebElement element = webDriver.findElement(By.className("sidebar-desktop"));
        WebElement panel = element.findElements(By.className("panel")).get(0);
        WebElement headerWrapper = panel.findElement(By.className("header-wrapper"));
        if (Arrays.asList(headerWrapper.getAttribute("class").split(" ")).contains("closed")) {
            wait.until(ExpectedConditions.elementToBeClickable(headerWrapper));
            wait.until(driver -> {
                headerWrapper.click();
                return !Arrays.asList(headerWrapper.getAttribute("class").split(" ")).contains("closed");
            });
        }


    }

    public void chooseSelects(Criteria criteria) throws CriteriaException {

        WebElement webElement = webDriver.findElements(By.className("panel-contents")).get(0);
        WebElement element = webElement.findElement(By.className("ymm-wrapper"));
        WebElement searchButton = webElement.findElements(By.className("accent-button")).get(1);
        List<WebElement> elements = element.findElements(By.className("ng-select"));

        // Select Year
        if (!selectNgSelect(elements.get(0), criteria.getYear())) {
            throw new CriteriaException(StatusMassages.SELECT_YEAR_FAILED.status);
        } else {
            logger.info(StatusMassages.SELECT_YEAR_SUCCESS.status);
        }

        // Select Make
        if (!selectNgSelect(elements.get(1), criteria.getMake())) {
            throw new CriteriaException(StatusMassages.SELECT_MAKE_FAILED.status);
        } else {
            logger.info(StatusMassages.SELECT_MAKE_SUCCESS.status);
        }

        // Select Model
        String model = criteria.getTrim() == null ? criteria.getModel() : criteria.getModel() + "_" + criteria.getTrim();
        if (!selectNgSelect(elements.get(2), model)) {
            throw new CriteriaException(StatusMassages.SELECT_MODEL_FAILED.status);
        } else {
            logger.info(StatusMassages.SELECT_MODEL_SUCCESS.status);
        }

        if (criteria.getCategoryNames().length > 0) {
            //        Select Category
            if (!selectNgSelect(elements.get(3), criteria.getCategoryNames()[0])) {
                throw new CriteriaException(StatusMassages.SELECT_CATEGORY_FAILED.status);
            } else {
                logger.info(StatusMassages.SELECT_CATEGORY_SUCCESS.status);
            }
        }

        wait.until(ExpectedConditions.elementToBeClickable(searchButton));
        searchButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(searchButton));

    }

    public boolean selectNgSelect(WebElement ngSelectElement, String checkValue) {
        wait.until(ExpectedConditions.elementToBeClickable(ngSelectElement));


        wait.until(driver -> {
            try {
                shortWait.until(ExpectedConditions.elementToBeClickable(ngSelectElement));
                ngSelectElement.click();
            } catch (ElementClickInterceptedException ex) {
                logger.error(ex.getMessage(), ex);
            }
            int optionCount = ngSelectElement.findElements(By.className("ng-option")).size();
            return optionCount > 1;
        });


        boolean selected = false;
        List<WebElement> options = ngSelectElement.findElements(By.className("ng-option"));
        for (WebElement web : options) {
            if (web.getText().equalsIgnoreCase(checkValue)) {
                web.click();
                selected = true;
                break;
            }
        }
        return selected;
    }

    public boolean selectCategory(int i, String[] cellValue, WebDriver webDriver) {
        WebElement webElement = webDriver.findElements(By.className("panel-contents")).get(0);
        WebElement element = webElement.findElement(By.className("ymm-wrapper"));
        boolean selected = false;
        List<WebElement> elements = element.findElements(By.className("ng-select"));
        List<WebElement> options = elements.get(i).findElements(By.className("ng-option"));
        for (String s : cellValue) {
            for (WebElement web : options) {
                if (web.getText().equalsIgnoreCase(s)) {
                    web.click();
                    selected = true;
                    break;
                }
            }
        }
        return selected;
    }

    public void clickReset() {
        WebElement reset = webDriver.findElement(By.xpath("/html/body/app-root/app-nav-bar/div/div[3]/div[2]/app-side-bar-search/div[1]/div[2]/button[4]"));
        reset.click();
        logger.info(StatusMassages.PAGE_RESET_SUCCESS.status);
    }

    public void partNuSearchReset() {
        webDriver.findElement(By.xpath("/html/body/app-root/app-nav-bar/div/div[3]/div[2]/app-side-bar-search/div[1]/div[2]/button[2]")).click();
        logger.info(StatusMassages.PAGE_RESET_SUCCESS.status);
    }


    public void findPartNuSearch(String partNumber) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("panel-contents")));
        WebElement webElement = webDriver.findElements(By.className("panel-contents")).get(0);
        WebElement searchField = webElement.findElement(By.className("search-input"));
        WebElement searchButton = webElement.findElement(By.className("accent-button"));

        searchField.sendKeys(partNumber);

        wait.until(ExpectedConditions.elementToBeClickable(searchButton));
        searchButton.click();
    }

    public void partNuSetResult(List<Result> results) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-nav-bar/div/div[3]/div[3]/app-aisle/div[2]/div/div/app-product-card[1]")));

        WebElement mainForm = webDriver.findElement(By.id("main-content-area"));
        WebElement itemContainer = mainForm.findElement(By.className("app-container-item"));

        WebElement items = itemContainer.findElement(By.id("search-results-container"));
        WebElement item = items.findElement(By.className("flex-card-item"));

        Result result = new Result();

        WebElement searchedResult = item.findElements(By.className("app-container-item")).get(0);
        try {
            result.setDateTime(LocalDateTime.now());
            result.setSiteName(ScraperId.KEYSTONE_AUTOMOTIVE.id);
            result.setPartNumber(searchedResult.findElement(By.className("lkq-link")).findElement(By.tagName("a")).getText());
            result.setImageUrl(searchedResult.findElement(By.className("flex-image")).findElement(By.tagName("img")).getAttribute("src"));
            result.setTitle(searchedResult.findElement(By.className("product-title")).getText());
            result.setDescription(searchedResult.findElement(By.className("part-description")).getText());
            result.setListPrice(searchedResult.findElement(By.className("product-costs")).findElements(By.className("cost-details")).get(1).findElements(By.tagName("span")).get(1).getText());
            result.setYourPrice(searchedResult.findElement(By.className("product-costs")).findElements(By.className("cost-details")).get(2).findElements(By.tagName("span")).get(1).getText());

            result.setAvailability(getAvailability(searchedResult));
        } catch (NoSuchElementException e) {
            logger.error(e.getMessage(), e);
        }

        results.add(result);
        logger.info(StatusMassages.PAGE_RESET_SUCCESS.status);
    }

    private Availability getAvailability(WebElement webElement) {
        Availability availability = new Availability();

        WebElement availabilityInfo = webElement.findElement(By.className("availability-info"));
        WebElement availabilityDescription = availabilityInfo.findElement(By.className("eda-description"));

        if (availabilityDescription.getText().trim().contains("Call for")) {
            availability.setAvailable(false);
            availability.setMassage("Call for availability");
        } else {
            availability.setAvailable(true);
            availability.setMassage(availabilityDescription.getText().trim());
        }

        return availability;
    }

    public void setResult(List<Result> results, Criteria criteria) {

        if (criteria.getMaxResultCount() != 0) {
            MAX_RESULT_COUNT = criteria.getMaxResultCount();
        }

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-nav-bar/div/div[3]/div[3]/app-aisle/div[2]/div/div/app-product-card[1]")));

        WebElement mainForm = webDriver.findElement(By.id("main-content-area"));
        WebElement itemContainer = mainForm.findElement(By.className("app-container-item"));
        WebElement items = itemContainer.findElement(By.id("search-results-container"));
        WebElement item = items.findElement(By.className("flex-card-item"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("app-container-item")));
        List<WebElement> products = item.findElements(By.className("app-container-item"));

        int count = 1;

        for (WebElement webElement: products) {
            if (count > MAX_RESULT_COUNT)
                break;
            Result result = new Result();
            try {

                WebElement productInfo = webElement.findElement(By.className("product-info"));
                WebElement productName = productInfo.findElement(By.className("product-title"));
                WebElement productDesc = productInfo.findElement(By.className("part-description"));

                WebElement partNus = productInfo.findElement(By.className("lkq-link"));
                WebElement partNu = partNus.findElement(By.tagName("a"));

                WebElement productCosts = webElement.findElement(By.className("product-costs"));
                WebElement costs = productCosts.findElement(By.className("your-cost"));
                List<WebElement> cost = costs.findElements(By.tagName("span"));

                WebElement listPrice = productCosts.findElements(By.className("cost-details")).get(1).findElements(By.tagName("span")).get(1);


                WebElement image = webElement.findElement(By.className("flex-image"));
                result.setDateTime(LocalDateTime.now());
                result.setSiteName(ScraperId.KEYSTONE_AUTOMOTIVE.id);
                result.setImageUrl(image.findElement(By.tagName("img")).getAttribute("src"));
                result.setTitle(productName.getText());
                result.setDescription(productDesc.getText());
                result.setListPrice(listPrice.getText());
                result.setYourPrice(cost.get(1).getText());
                result.setPartNumber(partNu.getText());
                if (criteria.isWithAvailability()) {
                    result.setAvailability(getAvailability(webElement));
                }
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                logger.error(e.getMessage(), e);
            }
            results.add(result);
            count++;
        }
        logger.info(StatusMassages.SET_RESULT_SUCCESS.status);
    }

}
