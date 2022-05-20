package sl.tiger.scraper.scraper.scrapers;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sl.tiger.scraper.business.CriteriaRepository;
import sl.tiger.scraper.business.ResultRepository;
import sl.tiger.scraper.controller.model.ScraperId;
import sl.tiger.scraper.controller.model.StatusMassages;
import sl.tiger.scraper.dto.Availability;
import sl.tiger.scraper.dto.Criteria;
import sl.tiger.scraper.dto.LocationAvailability;
import sl.tiger.scraper.dto.Result;
import sl.tiger.scraper.exception.CriteriaException;
import sl.tiger.scraper.scraper.Scraper;
import sl.tiger.scraper.util.ScrapHelper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component
public class MyPlaceForPartsScraper extends Scraper {

    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private CriteriaRepository criteriaRepository;

    // TODO read from config
    public static final String USERNAME = "vo91972";
    public static final String PASSWORD = "GMIHDJQU";
    Logger logger = LoggerFactory.getLogger(MyPlaceForPartsScraper.class);
    private boolean isLoggedIn = false;
    private boolean isPassMiddleScreen = false;

    public MyPlaceForPartsScraper() {
        super("https://myplaceforparts.com/", ScraperId.MY_PLACE_FOR_PARTS.id);
    }


    @Override
    public List<Result> search(Criteria criteria) throws CriteriaException {

        prepareToSearch();

        try {

            // wait till loading the home page
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("screenname")));

            selectParameters(criteria);

            selectSecondLevelParams(criteria);

            // search
            wait.until(ExpectedConditions.elementToBeClickable(By.id("cgs_go1")));
            webDriver.findElement(By.id("cgs_go1")).click();

            try {
                webDriver.switchTo().alert().accept();
                logger.info("popup occurred");
                webDriver.findElement(By.id("cgs_go1")).click();
            } catch (NoAlertPresentException | UnhandledAlertException e) {

                logger.error("no popup appeared");
            }

            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                Thread.currentThread().interrupt();

            }

            secondMiddleScreen();

            ScrapHelper.waitUntilTableLoaded("parts_display_table", webDriver, wait);

            List<Result> results = new ArrayList<>();
            setResults(results);

            //vehNavNewLookup
            wait.until(ExpectedConditions.elementToBeClickable(By.id("vehNavNewLookup")));
            webDriver.findElement(By.id("vehNavNewLookup")).click();

            criteria.setDate(LocalDateTime.now());
            criteria.setSiteName(ScraperId.MY_PLACE_FOR_PARTS.id);

            criteriaRepository.save(criteria);
            resultRepository.saveAll(results);
            return results;
        } catch (Exception ex) {

            logger.error("MyPlace4Parts Scraper Search : \n", ex);

            //vehNavNewLookup
            wait.until(ExpectedConditions.elementToBeClickable(By.id("vehNavNewLookup")));
            webDriver.findElement(By.id("vehNavNewLookup")).click();

            if (ex instanceof CriteriaException) {
                throw ex;
            } else {
                throw new CriteriaException(StatusMassages.SOMETHING_WENT_WRONG.status);
            }
        }

    }

    private void prepareToSearch() {
        login();
        logger.info(StatusMassages.LOGIN_SUCCESS.status);
        passMiddleScreen();
        logger.info("middle screen pass success");
        closeAdIfExist();
    }

    @Override
    public List<Result> searchByPartNumber(String partNumber, boolean isAddToCart, Criteria criteria) throws CriteriaException {

        try {

            prepareToSearch();

            findPartNuSearch(partNumber, isAddToCart);
            List<Result> results = new ArrayList<>();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("parts_row")));
            WebElement parts_row = webDriver.findElement(By.id("parts_row"));
            WebElement item_desc = parts_row.findElement(By.className("item_desc"));

            if (item_desc.getText().trim().equals("")) {
                resetSearch();
                throw new CriteriaException(StatusMassages.PART_NOT_AVAILABLE.status);
            } else {
                setResults(results);
            }

            if (isAddToCart) {
                webDriver.findElement(By.id("_mpp_parts_display_WAR_mpp_parts_displayportlet_INSTANCE_6Xnw_2_0_addImage")).click();

                webDriver.findElement(By.className("gr_button")).click();

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("gr_save_button")));

                WebElement webElement = webDriver.findElements(By.className("gr_save_button")).get(0);
                webElement.findElement(By.tagName("span")).findElement(By.tagName("span")).click();

                wait.until(ExpectedConditions.alertIsPresent());

                webDriver.switchTo().alert().accept();

                wait.until(ExpectedConditions.alertIsPresent());

                webDriver.switchTo().alert().accept();

                resetSearch();

                try {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newLookUpbuttonDiv")));
                    WebElement newLookUpbuttonDiv = webDriver.findElement((By.id("newLookUpbuttonDiv")));
                    //Continue Shopping
                    for (WebElement button : newLookUpbuttonDiv.findElements(By.tagName("button"))) {
                        if (button.getText().equals("Continue Shopping")) {
                            button.click();
                            logger.info(StatusMassages.ADD_TO_CART_SUCCESS.status);
                            break;
                        }
                    }
                } catch (TimeoutException ex) {
                    logger.error(ex.getMessage(), ex);
                }

            } else {
                resetSearch();
            }

            criteria.setDate(LocalDateTime.now());
            criteria.setSiteName(ScraperId.MY_PLACE_FOR_PARTS.id);

            criteriaRepository.save(criteria);
            resultRepository.saveAll(results);
            return results;
        } catch (Exception ex) {
            logger.error("MyPlace4Parts Scraper Part Nu Search : \n", ex);

            resetSearch();
            //vehNavNewLookup
            wait.until(ExpectedConditions.elementToBeClickable(By.id("vehNavNewLookup")));
            webDriver.findElement(By.id("vehNavNewLookup")).click();

            if (ex instanceof CriteriaException) {
                logger.error("--- criteria exception ---" + ex.getMessage());
                throw ex;
            } else {
                logger.error("--- something went wrong exception ---" + ex.getMessage());
                throw new CriteriaException(StatusMassages.SOMETHING_WENT_WRONG.status);
            }
        }
    }

    public void login() {
        boolean refreshed = false;
        if (isLoggedIn && isResetTimePassed()) {
            logger.info("refreshing the page...... https://myplaceforparts.com/");
            webDriver.get("https://myplaceforparts.com/");
            refreshed = true;
        }
        if (isLoggedIn && !webDriver.findElements(By.id("password")).isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (!webDriver.findElements(By.id("password")).isEmpty()) {
                isLoggedIn = false;
                isPassMiddleScreen = false;
            }

        }
        if (isLoggedIn && refreshed) {
            isPassMiddleScreen = false;
        }
        if (!isLoggedIn) {
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));

                webDriver.findElement(By.id("username")).sendKeys(USERNAME);
                webDriver.findElement(By.id("password")).sendKeys(PASSWORD);
                webDriver.findElement(By.id("submit-button")).click();
                isLoggedIn = true;
            } catch (NoSuchElementException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    public void resetSearch() {
        //vehNavNewLookup
        wait.until(ScrapHelper.clickTillElementAttached(By.id("vehNavNewLookup")));
    }

    public void passMiddleScreen() {
        if (!isPassMiddleScreen) {
            try {
                // wait till loading the middle screen
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("btn-signout")));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("userLogo")));

                // got to home page
                webDriver.findElement(By.className("userLogo")).click();
                isPassMiddleScreen = true;
            } catch (NoSuchElementException | TimeoutException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    public void secondMiddleScreen() {


        List<WebElement> specificConds = webDriver.findElements(By.id("specificConds"));
        if (specificConds.size() == 0) {
            return;
        }
        try {
            List<WebElement> div = webDriver.findElements(By.tagName("div"));
            if (div.get(1).getText().trim().startsWith("Specific")) {
                WebElement main_div = webDriver.findElement(By.id("specificConds"));
                List<WebElement> options = main_div.findElements(By.tagName("div"));

                if (options.size() == 1) {
                    List<WebElement> td = main_div.findElements(By.tagName("tr"));
                    // click dont know
                    td.get(4).findElement(By.tagName("input")).click();
                    logger.info("option 1 selected");
                    // click go
                    webDriver.findElement(By.id("cgs_go3")).click();
                } else {
                    // click dont know
                    options.get(0).findElements(By.tagName("tr")).get(3).findElement(By.tagName("input")).click();
                    logger.info("option 1 selected");
                    options.get(1).findElements(By.tagName("tr")).get(3).findElement(By.tagName("input")).click();
                    logger.info("option 2 selected");

                    // click go
                    webDriver.findElement(By.id("cgs_go3")).click();
                }
            }
        } catch (Exception e) {
            logger.error("Middle Screen");
        }
    }

    public void selectParameters(Criteria criteria) throws CriteriaException {

        /*year*/
        if (!ScrapHelper.selectTableCell("_mpp_vehicle_selector_WAR_mpp_vehicle_selectorportlet_INSTANCE_mtX2_year", criteria.getYear(), webDriver, shortWait)) {
            throw new CriteriaException(StatusMassages.SELECT_YEAR_FAILED.status);
        }
        logger.info(StatusMassages.SELECT_YEAR_SUCCESS.status);
        selectFromTable("_mpp_vehicle_selector_WAR_mpp_vehicle_selectorportlet_INSTANCE_mtX2_year", criteria.getYear(), StatusMassages.SELECT_YEAR_FAILED, StatusMassages.SELECT_YEAR_SUCCESS);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();

        }

        /*make*/
        if (!ScrapHelper.selectTableCell("_mpp_vehicle_selector_WAR_mpp_vehicle_selectorportlet_INSTANCE_mtX2_make", criteria.getMake(), webDriver, shortWait)) {
            throw new CriteriaException(StatusMassages.SELECT_MAKE_FAILED.status);
        }
        logger.info(StatusMassages.SELECT_MAKE_SUCCESS.status);
        selectFromTable("_mpp_vehicle_selector_WAR_mpp_vehicle_selectorportlet_INSTANCE_mtX2_make", criteria.getMake(), StatusMassages.SELECT_MAKE_FAILED, StatusMassages.SELECT_MAKE_SUCCESS);


        /*model*/
        if (!selectModel("_mpp_vehicle_selector_WAR_mpp_vehicle_selectorportlet_INSTANCE_mtX2_model", criteria, webDriver)) {
            throw new CriteriaException(StatusMassages.SELECT_MODEL_FAILED.status);
        }
        logger.info(StatusMassages.SELECT_MODEL_SUCCESS.status);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("_mpp_vehicle_selector_WAR_mpp_vehicle_selectorportlet_INSTANCE_mtX2_engine")));
        WebElement engineMain = webDriver.findElement(By.id("_mpp_vehicle_selector_WAR_mpp_vehicle_selectorportlet_INSTANCE_mtX2_engine"));
        String engineText = engineMain.findElements(By.tagName("tr")).get(0).getText();
        if (engineText.equals("All Engine")) {
            selectEngine(criteria);
        }
    }

    private void selectFromTable(String mpp_vehicle_selector_war_mpp_vehicle_selectorportlet_instance_mtX2_year, String year, StatusMassages selectYearFailed, StatusMassages selectYearSuccess) throws CriteriaException {
        if (!ScrapHelper.selectTableCell(mpp_vehicle_selector_war_mpp_vehicle_selectorportlet_instance_mtX2_year, year, webDriver, shortWait)) {
            throw new CriteriaException(selectYearFailed.status);
        }
        logger.info(selectYearSuccess.status);
    }

    private void selectEngine(Criteria criteria) {
        /*engine*/
        WebElement table = webDriver.findElement(By.id("_mpp_vehicle_selector_WAR_mpp_vehicle_selectorportlet_INSTANCE_mtX2_engine"));
        List<WebElement> cells = table.findElements(By.tagName("td"));

        ScrapHelper.selectAllEngine(criteria, cells);

    }

    private void closeAdIfExist() {
        try {
            webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            List<WebElement> addContainers = webDriver.findElements(By.id("selector_year_container"));
            webDriver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_TIME, TimeUnit.SECONDS);
            if (!addContainers.isEmpty()) {
                WebElement continueBtn = webDriver.findElement(By.id("continueBtn"));
                continueBtn.findElement(By.tagName("a")).click();
                logger.info("Ad closed successfully.!");
            } else {
                logger.info("No advertisement found.!");
            }
        } catch (NoSuchElementException | ElementNotInteractableException ex) {
            logger.info("No advertisement found.!");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public boolean selectModel(String tableId, Criteria criteria, WebDriver webDriver) {
        shortWait.until(ExpectedConditions.visibilityOfElementLocated(By.id(tableId)));
        WebElement table = webDriver.findElement(By.id(tableId));
        boolean selected = false;
        List<WebElement> cells = table.findElements(By.tagName("td"));
        for (WebElement webElement : cells) {
            if (webElement.getText().equalsIgnoreCase(ScrapHelper.getModelTrimString(criteria, webElement.getText(), "\\s+", " "))) {
                webElement.click();
                selected = true;
                break;
            }
        }
        return selected;
    }

    public void selectSecondLevelParams(Criteria criteria) throws CriteriaException {


        // Categories
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cat_container")));
        if (!ScrapHelper.selectTableCells("cat_container", criteria.getCategoryNames(), webDriver)) {
            throw new CriteriaException(StatusMassages.SELECT_CATEGORY_FAILED.status);
        }
        logger.info(StatusMassages.SELECT_CATEGORY_SUCCESS.status);

        WebElement allSegmentsCheckbox = webDriver.findElement(By.id("AllSegments"));
        if (!allSegmentsCheckbox.isSelected()) {
            allSegmentsCheckbox.click();
        }

        //  wait for loader to finished
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("mpfp21-waiting")));
        ScrapHelper.waitUntilTableLoaded("groups_container", webDriver, wait);

        // Groups
        if (!ScrapHelper.selectTableCells("groups_container", criteria.getCategories()[0].getGroups(), webDriver)) {
            throw new CriteriaException(StatusMassages.SELECT_GROUP_FAILED.status);
        }
        logger.info(StatusMassages.SELECT_GROUP_SUCCESS.status);

        //  wait for loader to finished
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("mpfp21-waiting")));

        ScrapHelper.waitUntilTableLoaded("segments_container", webDriver, wait);
    }

    public void findPartNuSearch(String partNumber, boolean isAddToCart) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("partInquiryBox")));
        WebElement partInquiryBox = webDriver.findElement(By.id("partInquiryBox"));
        partInquiryBox.sendKeys(partNumber);
        webDriver.findElement(By.id("_mpp_part_inquiry_WAR_mpp_part_inquiryportlet_INSTANCE_gIm3_partInquirySearch")).click();
        logger.info(StatusMassages.PART_NO_SEARCH_SUCCESS.status);
    }

    private Availability getAvailability(WebElement webElement) {
        Availability availability = new Availability();
        availability.setAvailable(false);
        try {
            WebElement select = webElement.findElement(By.className("selectWidth"));
            List<WebElement> options = select.findElements(By.tagName("option"));
            for (WebElement option : options) {
                LocationAvailability locationAvailability = new LocationAvailability();

                String[] split = option.getText().trim().split("\\s+");
                if (!split[0].trim().equalsIgnoreCase("0")) {
                    availability.setAvailable(true);
                }
                locationAvailability.setQuantity(Integer.parseInt(split[0]));
                locationAvailability.setLocation(split[1]);
                availability.getLocationAvailability().add(locationAvailability);
            }
        } catch (NoSuchElementException e) {
            logger.error(e.getMessage());
            availability.setAvailable(false);
            availability.setMassage("Call for availability");
        }
        return availability;
    }

    public void setResults(List<Result> results) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("parts_display_table")));
        WebElement resultTable = webDriver.findElement(By.id("parts_display_table"));

        List<WebElement> rows = resultTable.findElements(By.id("parts_row"));
        rows.forEach(webElement -> {
            Result result = new Result();
            List<WebElement> rowsColumns = webElement.findElements(By.tagName("td"));
            try {
                List<WebElement> titleDiv = rowsColumns.get(0).findElements(By.tagName("div"));
                result.setTitle(titleDiv.get(0).getText());
                List<WebElement> partData = rowsColumns.get(0).findElements(By.className("partData"));
                if (!partData.isEmpty()) {
                    result.setDescription(partData.get(0).getText());
                }
                result.setDateTime(LocalDateTime.now());
                result.setSiteName(ScraperId.MY_PLACE_FOR_PARTS.id);
                result.setPartNumber(rowsColumns.get(2).findElement(By.className("part_num_wdith")).getText());
                result.setAvailability(getAvailability(webElement));
                result.setYourPrice(rowsColumns.get(4).findElement(By.tagName("label")).getText());
                result.setListPrice(rowsColumns.get(6).findElements(By.tagName("div")).get(0).getText());

                List<WebElement> imgRef = rowsColumns.get(1).findElements(By.tagName("img"));
                if (!imgRef.isEmpty()) {
                    result.setImageUrl(imgRef.get(0).getAttribute("src"));

                }
            } catch (NoSuchElementException ex) {
                logger.error(ex.getMessage(), ex);
            }

            if (result.getTitle() != null) {
                results.add(result);
            }
        });
        logger.info(StatusMassages.SET_RESULT_SUCCESS.status);

    }

    public Map<String, List<String>> getCategories() {
        HashMap<String, List<String>> categoryListHashMap = new HashMap<>();

        List<String> beltsAndCooling = List.of("Belts", "Hoses", "Radiator, Cap, Fan Motor & Switch", "Thermostat, Gasket & Housing", "Water Pump, Gasket, Fan & Clutch");
        categoryListHashMap.put("Belts & Cooling", beltsAndCooling);

        List<String> BrakesAndWheelBearings = List.of("Front Brake Hardware", "Front Brake Hydraulics", "Front Brake Pads & Shoes, Rotors & Drums", "Master Cylinder, Brake Light Switch, Power Booster & ABS", "Rear Brake Hardware & Cables", "Rear Brake Hydraulics", "Rear Brake Pads & Shoes, Rotors & Drums", "Wheel Bearings & Seals)");
        categoryListHashMap.put("Brakes & Wheel Bearings", BrakesAndWheelBearings);

        /*
        ToDo: implement other categories
         */

        return categoryListHashMap;
    }

/*    public void passContinueScreen() {
        if (ScrapHelper.isElementExist("continueBtn", webDriver)) {
            webDriver.findElement(By.id("continueBtn")).click();
        }
    }*/

}
