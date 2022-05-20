package sl.tiger.scraper.scraper.scrapers;

import org.springframework.beans.factory.annotation.Autowired;
import sl.tiger.scraper.business.CriteriaRepository;
import sl.tiger.scraper.business.ResultRepository;
import sl.tiger.scraper.controller.model.ScraperId;
import sl.tiger.scraper.controller.model.StatusMassages;
import sl.tiger.scraper.dto.Availability;
import sl.tiger.scraper.dto.Criteria;
import sl.tiger.scraper.dto.LocationAvailability;
import sl.tiger.scraper.dto.Result;
import sl.tiger.scraper.entity.ResultEntity;
import sl.tiger.scraper.exception.CriteriaException;
import sl.tiger.scraper.scraper.Scraper;
import sl.tiger.scraper.util.ScrapHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static sl.tiger.scraper.util.ScrapHelper.getExceptionText;

@Component
public class WorldAutoScraper extends Scraper {

    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private CriteriaRepository criteriaRepository;

    public static final String USERNAME = "WAW22295";
    public static final String PASSWORD = "MY4UT0P!";
    Logger logger = LoggerFactory.getLogger(WorldAutoScraper.class);
    private boolean isLoggedIn = false;

    public WorldAutoScraper() {
        super("http://www.iautoparts.biz/pronto/entrepot/waw", ScraperId.WORLD_AUTO.id);
    }

    @Override
    public List<Result> search(Criteria criteria) throws CriteriaException {
        try {

            login();
            logger.info(StatusMassages.LOGIN_SUCCESS.status);
            selectMainParams(criteria);

            webDriver.switchTo().frame("fraHeader");
            webDriver.findElement(By.id("hdrBtn")).click();
            webDriver.switchTo().defaultContent();

            selectCategoryParams(criteria);

            webDriver.switchTo().frame("fraHeader");
            webDriver.findElement(By.id("hdrBtn")).click();
            webDriver.switchTo().defaultContent();

            List<Result> results = new ArrayList<>();
            webDriver.switchTo().frame("fraBody");
            setResults(results, criteria);

            pageReset();

            criteria.setDate(LocalDateTime.now());

            criteriaRepository.save(criteria);
            resultRepository.saveAll(results);
            return results;

        } catch (Exception ex) {
            logger.error("WorldAuto Scraper Search : \n", ex);

            pageReset();
            webDriver.navigate().refresh();
            if (ex instanceof CriteriaException) {
                pageReset();
                throw ex;
            } else {
                pageReset();
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

            webDriver.switchTo().defaultContent();
            webDriver.switchTo().frame("fraBody");

            WebElement plTable = webDriver.findElement(By.id("plTable"));
            WebElement tbody = plTable.findElements(By.tagName("tbody")).get(1);
            WebElement tr = tbody.findElements(By.tagName("tr")).get(0);
            WebElement mainTd = tr.findElements(By.tagName("td")).get(2);

            List<Result> results = new ArrayList<>();

            if (mainTd.getText().trim().length() == 0) {
                pageReset();
                throw new CriteriaException(StatusMassages.PART_NOT_AVAILABLE.status);
            } else {
                setResults(results, criteria);
            }

            results = results.stream().filter(result -> partNumber.equalsIgnoreCase(criteria.getPartNumber())).collect(Collectors.toList());

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("plTable")));

            if (isAddToCart) {
                webDriver.switchTo().defaultContent();
                webDriver.switchTo().frame("fraBody");

                WebElement td = tr.findElements(By.tagName("td")).get(11);
                td.findElement(By.tagName("input")).click();

                webDriver.switchTo().defaultContent();
                webDriver.switchTo().frame("fraFooter");

                /* click add to quote after check */
                WebElement actionPage = webDriver.findElement(By.id("ActionPage"));
                actionPage.findElement(By.tagName("input")).click();

                webDriver.switchTo().defaultContent();
                webDriver.switchTo().frame("fraBody");

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("frmCart")));

                webDriver.switchTo().defaultContent();
                webDriver.switchTo().frame("fraFooter");

                WebElement actionBack = webDriver.findElement(By.className("ActionBack"));
                actionBack.findElements(By.tagName("input")).get(0).click();

                logger.info(StatusMassages.ADD_TO_CART_SUCCESS.status);
            }

            pageReset();

            criteria.setDate(LocalDateTime.now());

            criteriaRepository.save(criteria);
            resultRepository.saveAll(results);

            return results;
        } catch (Exception ex) {
            logger.error("WorldAuto Scraper Part Nu Search : \n", ex);

            webDriver.navigate().refresh();
            if (ex instanceof CriteriaException) {
                throw ex;
            } else {
                throw new CriteriaException(StatusMassages.PART_NOT_AVAILABLE.status);
            }
        }
    }

    public void login() {
        if (isLoggedIn && isResetTimePassed()) {
            logger.info("refreshing the page...... http://www.iautoparts.biz/pronto/entrepot/waw");
            webDriver.get("http://www.iautoparts.biz/pronto/entrepot/waw");
        }
        if (isLoggedIn) {
            webDriver.switchTo().frame("fraBody");
            if (!webDriver.findElements(By.name("username")).isEmpty()) {
                isLoggedIn = false;
            }
            webDriver.switchTo().defaultContent();
        }
        if (!isLoggedIn) {
            try {
                webDriver.switchTo().frame("fraBody");
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));

                webDriver.findElement(By.name("username")).sendKeys(USERNAME);
                webDriver.findElement(By.name("password")).sendKeys(PASSWORD);
                webDriver.findElement(By.className("ButtonForward")).click();
                isLoggedIn = true;
                webDriver.switchTo().defaultContent();
            } catch (NoSuchElementException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    public void selectMainParams(Criteria criteria) throws CriteriaException {
        webDriver.switchTo().defaultContent();
        try {
            webDriver.switchTo().frame("fraHeader");
            webDriver.findElement(By.id("idVehYMME")).findElement(By.tagName("a")).click();
            webDriver.switchTo().defaultContent();
            webDriver.switchTo().frame("fraBody");

            if (!ScrapHelper.selectAnchorInContainer("idYMMEBody", criteria.getYear(), webDriver)) {
                throw new CriteriaException(StatusMassages.SELECT_YEAR_FAILED.status);
            } else {
                logger.info(StatusMassages.SELECT_YEAR_SUCCESS.status);
            }

            if (!ScrapHelper.selectAnchorInContainer("idYMMEBody", criteria.getMake(), webDriver)) {
                throw new CriteriaException(StatusMassages.SELECT_MAKE_FAILED.status);
            }
            logger.info(StatusMassages.SELECT_MAKE_SUCCESS.status);


            if (!selectModel("idYMMEBody", criteria, webDriver)) {
                throw new CriteriaException(StatusMassages.SELECT_MODEL_FAILED.status);
            } else {
                logger.info(StatusMassages.SELECT_MODEL_SUCCESS.status);
            }

/*            WebElement main = webDriver.findElement(By.id("idVehYMME"));
            WebElement spanOne = main.findElements(By.tagName("span")).get(0);
            spanOne.findElements(By.tagName("a")).size();*/

            if (webDriver.findElement(By.id("idYMMEHdr")).getText().equals("Please select an Engine")) {
                selectEngine(criteria);
            }

            webDriver.switchTo().defaultContent();

        } catch (NoSuchElementException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void selectEngine(Criteria criteria) {
        WebElement table = webDriver.findElement(By.id("idYMMEBody"));
        List<WebElement> cells = table.findElements(By.tagName("a"));

        ScrapHelper.selectAllEngine(criteria, cells);
    }

    public boolean selectModel(String containerId, Criteria criteria, WebDriver webDriver) {
        WebElement container = webDriver.findElement(By.id(containerId));
        boolean selected = false;
        List<WebElement> elements = container.findElements(By.tagName("a"));
        for (WebElement webElement : elements) {
            if (webElement.getText().equalsIgnoreCase(ScrapHelper.getModelTrimString(criteria, webElement.getText(), "\\s+", " "))) {
                webElement.click();
                selected = true;
                break;
            }
        }
        return selected;
    }

    public void selectCategoryParams(Criteria criteria) throws CriteriaException {
        try {
            webDriver.switchTo().frame("fraBody");

            if (!selectDivInContainer("GrpBody_category", criteria.getCategoryNames(), webDriver)) {
                throw new CriteriaException(StatusMassages.SELECT_CATEGORY_FAILED.status);
            } else {
                logger.info(StatusMassages.SELECT_CATEGORY_SUCCESS.status);
            }

            if (!selectDivInContainer("GrpBody_group", criteria.getCategories()[0].getGroups(), webDriver)) {
                throw new CriteriaException(StatusMassages.SELECT_GROUP_FAILED.status);
            } else {
                logger.info(StatusMassages.SELECT_GROUP_SUCCESS.status);
            }

            webDriver.switchTo().defaultContent();

        } catch (NoSuchElementException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private Availability getAvailability(List<WebElement> rowsColumns) {
        Availability availability = new Availability();
        LocationAvailability locationAvailability = new LocationAvailability();

        String inStockValue = rowsColumns.get(13).findElement(By.tagName("div")).getText();
        String[] split = inStockValue.trim().split("\\s+");

        if (inStockValue.trim().contains("Call")) {
            availability.setAvailable(false);
            availability.setMassage("Call for more details");
            return availability;
        } else {
            if (split.length > 0) {
                if (inStockValue.trim().contains("avail")) {
                    availability.setAvailable(true);
                    availability.setMassage("Available");
                    locationAvailability.setLocation("");
                    locationAvailability.setQuantity(Integer.parseInt(split[0]));
                    availability.getLocationAvailability().add(locationAvailability);
                } else if (inStockValue.trim().equals("")) {
                    availability.setAvailable(false);
                    availability.setMassage("Out Of Stock");
                }
            }
        }
        return availability;
    }

    public void setResults(List<Result> results, Criteria criteria) {

        if (criteria.getMaxResultCount() != 0) {
            MAX_RESULT_COUNT = criteria.getMaxResultCount();
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("plTable")));
        WebElement resultTable = webDriver.findElement(By.id("plTable"));

        List<WebElement> rows = resultTable.findElements(By.tagName("tbody"));
        rows.remove(0);
        int count = 1;
        for (WebElement webElement : rows){
            if (count > MAX_RESULT_COUNT)
                break;
            Result result = new Result();
            try {

                WebElement mainTr = webElement.findElements(By.tagName("tr")).get(0);
                List<WebElement> rowsColumns = mainTr.findElements(By.tagName("td"));

                result.setDateTime(LocalDateTime.now());
                result.setSiteName(ScraperId.WORLD_AUTO.id);
                result.setPartNumber(rowsColumns.get(3).findElements(By.tagName("span")).get(1).getText());
                result.setDescription(rowsColumns.get(2).getText());
                if (criteria.isWithAvailability()) {
                    result.setAvailability(getAvailability(rowsColumns));
                }
                result.setYourPrice(rowsColumns.get(7).getText());
                result.setListPrice(rowsColumns.get(6).getText());
                result.setExtend(rowsColumns.get(8).getText());
                List<WebElement> imgRef = rowsColumns.get(1).findElements(By.tagName("img"));
                if (!imgRef.isEmpty()) {
                    result.setImageUrl(imgRef.get(0).getAttribute("src"));

                }
            } catch (NoSuchElementException | IndexOutOfBoundsException ex) {
                logger.error("Image is not present in the result.!");
                logger.error(ex.getMessage());
            }

            results.add(result);
            count++;
        }
        // webDriver.switchTo().defaultContent();
        logger.info(StatusMassages.SET_RESULT_SUCCESS.status);
    }

    public boolean selectDivInContainer(String containerId, String[] value, WebDriver webDriver) {
        WebElement container = webDriver.findElement(By.id(containerId));
        boolean selected = false;
        List<WebElement> elements = container.findElements(By.tagName("div"));
        for (WebElement webElement : elements) {
            for (String name : value) {
                if (name.equalsIgnoreCase(webElement.getText())) {
                    webElement.findElement(By.tagName("input")).click();
                    selected = true;
                    break;
                }
            }
        }
        return selected;
    }

    public void pageReset() {
        webDriver.switchTo().defaultContent();
        webDriver.switchTo().frame("fraHeader");
        webDriver.findElement(By.id("HomeIcon")).click();
        logger.info(StatusMassages.PAGE_RESET_SUCCESS.status);
    }

    public void findPartNuSearch(String partNumber, boolean isAddToCart) {
        webDriver.switchTo().defaultContent();
        webDriver.switchTo().frame("fraBody");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("partsSearchID")));
        WebElement body = webDriver.findElement(By.className("BodyFrame"));
        WebElement content = body.findElement(By.id("Content"));
        WebElement frmVehicleSelection = content.findElement(By.name("frmVehicleSelection"));
        WebElement searchField = frmVehicleSelection.findElement(By.id("partsSearchID"));

        searchField.sendKeys(partNumber);

        webDriver.findElement(By.xpath("//*[@id=\"divPage\"]/div/form/table/tbody/tr[1]/td[2]/table/tbody/tr[2]/td[2]")).click();
    }
}
