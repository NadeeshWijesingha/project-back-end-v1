package sl.tiger.scraper.scraper.scrapers;

import sl.tiger.scraper.controller.model.ScraperId;
import sl.tiger.scraper.controller.model.StatusMassages;
import sl.tiger.scraper.dto.Availability;
import sl.tiger.scraper.dto.Criteria;
import sl.tiger.scraper.dto.Result;
import sl.tiger.scraper.exception.CriteriaException;
import sl.tiger.scraper.scraper.Scraper;
import sl.tiger.scraper.util.ScrapHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sl.tiger.scraper.util.ScrapHelper.getExceptionText;

@Component
public class TigerAutoPartsScraper extends Scraper {

    public static final String USERCODE = "892574";
    public static final String PASSWORD = "892574";
    static String reDirectUrl = "https://scarborough.tigeronlineorder.com/admin/partslist2.php";
    Logger logger = LoggerFactory.getLogger(TigerAutoPartsScraper.class);
    private boolean isLoggedIn = false;

    public TigerAutoPartsScraper() {
        super("https://tigeronlineorder.com/", ScraperId.TIGER_AUTO_PARTS.id);
    }

    @Override
    public List<Result> search(Criteria criteria) throws CriteriaException {
        try {
            login();
            logger.info(StatusMassages.LOGIN_SUCCESS.status);

            selectMake(criteria);

            selectModel(criteria);

            selectYear(criteria);

            if (criteria.getSearchText() != null) {
                if (!inputSearchText(criteria)) {
                    throw new CriteriaException("text search failed");
                }
                logger.info(StatusMassages.TEXT_SEARCH_SUCCESS.status);
            }

            ArrayList<Result> results = new ArrayList<>();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("product_list_desktop")));

            if (webDriver.findElement(By.id("results")).getText().trim().equals("")) {
                clearSearchField();
                logger.error(StatusMassages.SOMETHING_WENT_WRONG.status);
                return new ArrayList<>();
            } else {
                setResult(results, criteria);
            }
            webDriver.get(reDirectUrl);

            return results;

        } catch (Exception ex) {
            logger.error("Tiger Scraper Search : \n", ex);

            webDriver.get(reDirectUrl);
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
            logger.info(StatusMassages.LOGIN_SUCCESS.status);
            findPartNuSearch(partNumber, isAddToCart);
            ArrayList<Result> results = new ArrayList<>();

            wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(By.id("div_products"), By.tagName("div")));

            WebElement resultElement = webDriver.findElement(By.id("results"));
            String tableText = resultElement.getText();

            if (tableText.trim().equals("")) {
                clearSearchField();
                throw new CriteriaException(StatusMassages.PART_NOT_AVAILABLE.status);
            } else {
                setResult(results, criteria);
            }

            if (isAddToCart) {
                WebElement element = webDriver.findElement(By.id("res-wrap"));
                WebElement tr = element.findElements(By.tagName("tr")).get(2);
                WebElement td = tr.findElements(By.tagName("td")).get(5);
                WebElement button = td.findElements(By.tagName("input")).get(1);
                button.click();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                    Thread.currentThread().interrupt();
                }

                WebElement itemsmycart = webDriver.findElement(By.id("itemsincartlink"));
                itemsmycart.click();

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tabs")));

                WebElement element1 = webDriver.findElement(By.id("frm-scarb"));
                WebElement element2 = element1.findElement(By.className("div-quote"));
                element2.findElement(By.className("newquote")).click();

                selectShop();

                logger.info(StatusMassages.ADD_TO_CART_SUCCESS.status);
            } else {
                clearSearchField();
            }

            webDriver.get(reDirectUrl);

            return results;
        } catch (Exception ex) {
            logger.error("Tiger Scraper Part Nu Search : \n", ex);

            webDriver.get(reDirectUrl);
            if (ex instanceof CriteriaException) {
                System.out.println("--- criteria exception ---");
                throw ex;
            } else {
                System.out.println("--- something went wrong exception ---" + ex.getMessage());
                throw new CriteriaException(StatusMassages.SOMETHING_WENT_WRONG.status);
            }
        }
    }

    public void login() {
        if (isLoggedIn && isResetTimePassed()) {
            logger.info("refreshing the page...... https://scarborough.tigeronlineorder.com/admin/partslist2.php");
            webDriver.get("https://scarborough.tigeronlineorder.com/admin/partslist2.php");
        }
        if (isLoggedIn && !webDriver.findElements(By.name("username")).isEmpty()) {
            isLoggedIn = false;
        }

        if (!isLoggedIn) {

            selectLocation();

            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));

                WebElement loginForm = webDriver.findElement(By.className("login"));

                loginForm.findElement(By.name("username")).sendKeys(USERCODE);
                loginForm.findElement(By.name("password")).sendKeys(PASSWORD);
                loginForm.findElement(By.id("submit")).click();
                isLoggedIn = true;

                selectShop();

            } catch (NoSuchElementException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    public void clearSearchField() {
        WebElement search = webDriver.findElement(By.id("floating-search"));
        WebElement searchField = search.findElement(By.tagName("input"));
        searchField.clear();
    }

    private void selectLocation() {
        Select select = new Select(webDriver.findElement(By.name("location")));
        select.selectByVisibleText("Scarborough");
    }

    private void selectShop() {
        WebElement shop = webDriver.findElement(By.className("first"));
        shop.click();
    }

    private void selectMake(Criteria criteria) throws CriteriaException {
        if (!ScrapHelper.selectDropDownElement("makes", criteria.getMake(), webDriver)) {
            throw new CriteriaException(StatusMassages.SELECT_MAKE_FAILED.status);
        } else {
            logger.info(StatusMassages.SELECT_MAKE_SUCCESS.status);
        }
    }

    private void selectModel(Criteria criteria) throws CriteriaException {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"models\"]/option[2]")));
            Select dropDown = new Select(webDriver.findElement(By.id("models")));
            int index = 0;
            for (WebElement option : dropDown.getOptions()) {
                if (option.getText().equalsIgnoreCase(ScrapHelper.getModelTrimString(criteria, option.getText(), "_", "_")))
                    break;
                index++;
            }
            if (index < dropDown.getOptions().size()) {
                dropDown.selectByIndex(index);
                logger.info(StatusMassages.SELECT_MODEL_SUCCESS.status);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CriteriaException(StatusMassages.SELECT_MODEL_FAILED.status);
        }
    }

    private void selectYear(Criteria criteria) throws CriteriaException {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"years\"]/option[2]")));
        if (!ScrapHelper.selectDropDownElement("years", criteria.getYear(), webDriver)) {
            throw new CriteriaException(StatusMassages.SELECT_YEAR_FAILED.status);
        } else {
            logger.info(StatusMassages.SELECT_YEAR_SUCCESS.status);
        }
    }

    public boolean inputSearchText(Criteria criteria) {
        boolean clicked = false;
        try {
            WebElement search = webDriver.findElement(By.id("floating-search"));
            WebElement searchField = search.findElement(By.id("qsku"));
            searchField.clear();
            Thread.sleep(1000);
            searchField.sendKeys(criteria.getSearchText());
            search.findElement(By.id("btn-go1")).click();
            clicked = true;
            Thread.sleep(1000);
        } catch (NoSuchElementException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();

        }
        return clicked;
    }

    private Availability getAvailability(WebElement webElement) {
        Availability availability = new Availability();
        List<WebElement> rowsColumns = webElement.findElements(By.tagName("td"));
        try {
            List<WebElement> input = rowsColumns.get(5).findElements(By.tagName("input"));

            if (input.size() > 0) {
                availability.setAvailable(true);
                availability.setMassage("Available");
            } else {
                availability.setAvailable(false);
                availability.setMassage("Not Available");
            }
        } catch (NoSuchElementException e) {
            logger.error("No Such Element", e);
        }

        return availability;
    }

    public void setResult(List<Result> results, Criteria criteria) {

        if (criteria.getMaxResultCount() != 0) {
            MAX_RESULT_COUNT = criteria.getMaxResultCount();
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("product_list_desktop")));

        WebElement resultTable = webDriver.findElement(By.className("product_list_desktop"));

        List<WebElement> resultSet1 = resultTable.findElements(By.className("alt1"));
        List<WebElement> resultSet2 = resultTable.findElements(By.className("alt2"));

        List<WebElement> combinedList = Stream.of(resultSet1, resultSet2)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        webDriver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        int count = 1;

        for (WebElement webElement : combinedList) {
            if (count > MAX_RESULT_COUNT)
                break;
            Result result = new Result();
            try {
                List<WebElement> rowsColumns = webElement.findElements(By.tagName("td"));

                if (rowsColumns.get(0).findElements(By.tagName("a")).size() < 1) {
                    result.setImageUrl("image not available in this product");
                } else {
                    result.setImageUrl(rowsColumns.get(0).findElements(By.tagName("a")).get(1).getAttribute("href"));
                }
                result.setPartNumber(rowsColumns.get(1).getText());
                result.setTitle(rowsColumns.get(2).getText());
                result.setListPrice(rowsColumns.get(3).getText());
                result.setYourPrice(rowsColumns.get(4).getText());
                if (criteria.isWithAvailability()) {
                    result.setAvailability(getAvailability(webElement));
                }

            } catch (NoSuchElementException ex) {
                logger.error(ex.getMessage(), ex);
            }

            results.add(result);
            count++;
        }
        webDriver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_TIME, TimeUnit.SECONDS);
        logger.info(StatusMassages.SET_RESULT_SUCCESS.status);
    }

    public void findPartNuSearch(String partNumber, boolean isAddToCart) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("qsku")));
        WebElement field = webDriver.findElement(By.id("qsku"));
        field.sendKeys(partNumber);

        webDriver.findElement(By.id("btn-go1")).click();
        logger.info(StatusMassages.PART_NO_SEARCH_SUCCESS.status);
    }
}
