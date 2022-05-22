package sl.tiger.scraper.scraper;

import sl.tiger.scraper.dto.Criteria;
import sl.tiger.scraper.dto.PartNumberCriteria;
import sl.tiger.scraper.dto.Result;
import sl.tiger.scraper.exception.CriteriaException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class Scraper {

    @Autowired
    ScrapHandler handler;

    protected static final int MAX_WAIT_TIME = 30;
    protected static final int MAX_SHORT_WAIT_TIME = 2;
    protected static final int IMPLICIT_WAIT_TIME = 20;
    protected static int MAX_RESULT_COUNT = 10;

    protected int RESET_TIME_GAP = 0;

    public String scraperId;
    public String url;
    public Logger scraperLogger = LoggerFactory.getLogger(Scraper.class);
    protected WebDriver webDriver;
    protected WebDriverWait wait;
    protected WebDriverWait shortWait;
    protected ChromeOptions chromeOptions;
    private boolean isConnected = false;


    private Instant lastCalledTime = Instant.now();

    protected Scraper(String url, String id) {
        this.scraperId = id;
        this.url = url;
        this.chromeOptions = new ChromeOptions();
        if (!"false".equalsIgnoreCase(System.getProperty("scrapper.headless"))) {
//            chromeOptions.addArguments("--headless");
        }
        chromeOptions.addArguments("--window-size=1920,1080");

    }

    public void connect() throws CriteriaException {
        try {
            if (!isConnected) {
                webDriver = new ChromeDriver(chromeOptions);
                wait = new WebDriverWait(webDriver, MAX_WAIT_TIME);
                shortWait = new WebDriverWait(webDriver, MAX_SHORT_WAIT_TIME);
                webDriver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_TIME, TimeUnit.SECONDS);
                scraperLogger.info(String.format("Connecting to %s", url));
                webDriver.get(url);
                isConnected = true;
            }
        } catch (WebDriverException ex) {
            for (String s : Arrays.asList(ex.getMessage(), "Cannot connect to the site..........")) {
                scraperLogger.error(s);
            }
            throw new CriteriaException(ex.getMessage());
        }
    }

    public void disConnect() {
        webDriver.quit();
    }

    public boolean isResetTimePassed() {
        Instant now = Instant.now();
        Duration timeElapsed = Duration.between(this.lastCalledTime, now);
        this.lastCalledTime = now;
        return timeElapsed.getSeconds() / 60 >= this.RESET_TIME_GAP;
    }

    public abstract List<Result> search(Criteria criteria) throws CriteriaException;

    public abstract List<Result> searchByPartNumber(String site, String partNumber,
                                                    boolean addToCart, String customerName,
                                                    String customerContact,  PartNumberCriteria criteria) throws CriteriaException;


    @PostConstruct
    public void init() {
        handler.addScraper(this);
    }

}
