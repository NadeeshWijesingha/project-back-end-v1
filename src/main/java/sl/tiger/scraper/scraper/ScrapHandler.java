package sl.tiger.scraper.scraper;

import sl.tiger.scraper.controller.model.StatusMassages;
import sl.tiger.scraper.dto.Criteria;
import sl.tiger.scraper.dto.PartNumberCriteria;
import sl.tiger.scraper.dto.Result;
import sl.tiger.scraper.exception.CriteriaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ScrapHandler {

    Logger logger = LoggerFactory.getLogger(ScrapHandler.class);

    static final int MIN_SCRAPERS = 1;
    static final int MAX_SCRAPERS = 5;
    static final int VALIDATION_INTERVAL = 5;

    private final Map<String, ConcurrentLinkedDeque<Scraper>> scrapers = new HashMap<>();
    private final Map<String, ScheduledExecutorService> executors = new HashMap<>();
    private final Map<String, Class<? extends Scraper>> scrapClasses = new HashMap<>();

    public void addScraper(Scraper scraper) {

        this.scrapers.put(scraper.scraperId, new ConcurrentLinkedDeque<>());
        this.scrapClasses.put(scraper.scraperId, scraper.getClass());

        this.scrapers.get(scraper.scraperId).add(scraper);
        this.addExecutor(scraper.scraperId);


    }

    public List<Result> searchByPartNumber(PartNumberCriteria criteria) throws CriteriaException {

        List<Result> results = new ArrayList<>();
        String[] scraperIds = criteria.getScrappers();
        if (scraperIds == null || scraperIds.length == 0) {
            scraperIds = this.scrapers.keySet().toArray(new String[0]);
        }

        for (String scraperId : scraperIds) {
            Scraper scraper = borrowScraper(scraperId);
            scraper.connect();
            try {
                results.addAll(scraper.searchByPartNumber(criteria.getSite(),
                        criteria.getPartNumber(), criteria.getAddToCart(), criteria.getCustomerName(),
                        criteria.getCustomerContactNumber(), criteria));
                returnScraper(scraper);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                returnScraper(scraper);

                if (ex instanceof CriteriaException) {
                    throw ex;
                } else {
                    throw new CriteriaException(StatusMassages.SOMETHING_WENT_WRONG.status);
                }
            }
        }

        return results;
    }

    public List<Result> search(Criteria criteria) throws CriteriaException {
        List<Result> results = new ArrayList<>();
        String[] scraperIds = criteria.getScrappers();
        if (scraperIds == null || scraperIds.length == 0) {
            scraperIds = this.scrapers.keySet().toArray(new String[0]);
        }

        for (String scraperId : scraperIds) {
            Scraper scraper = borrowScraper(scraperId);
            scraper.connect();
            try {
                results.addAll(scraper.search(criteria));
                returnScraper(scraper);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                returnScraper(scraper);

                if (ex instanceof CriteriaException) {
                    throw ex;
                } else {
                    throw new CriteriaException(StatusMassages.SOMETHING_WENT_WRONG.status);
                }
            }

        }

        return results;

    }

    public Scraper borrowScraper(String scraperId) {
        Scraper scraper;
        if ((scraper = scrapers.get(scraperId).pollLast()) == null) {
            scraper = getNewScraperInstance(scraperId);
        }
        return scraper;
    }

    public void returnScraper(Scraper scraper) {
        if (scraper == null) {
            return;
        }
        this.scrapers.get(scraper.scraperId).offer(scraper);
    }

    public void shutdown(String scraperId) {
        if (executors.get(scraperId) != null) {
            executors.get(scraperId).shutdown();
        }
    }


    private void addExecutor(String scraperId) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        this.executors.put(scraperId, executorService);

        executorService.scheduleWithFixedDelay(() -> {
            int size = scrapers.get(scraperId).size();

            if (size < MIN_SCRAPERS) {
                int sizeToBeAdded = MIN_SCRAPERS + size;
                for (int i = 0; i < sizeToBeAdded; i++) {
                    connectAndAddScraper(scraperId);
                }
            } else if (size > MAX_SCRAPERS) {
                int sizeToBeRemoved = size - MAX_SCRAPERS;
                for (int i = 0; i < sizeToBeRemoved; i++) {
                    scrapers.get(scraperId).poll();
                }
            }
        }, VALIDATION_INTERVAL, VALIDATION_INTERVAL, TimeUnit.SECONDS);
    }

    private Scraper getNewScraperInstance(String scraperId) {
        Scraper scraper = null;
        Constructor<?> ctor = null;
        try {
            ctor = scrapClasses.get(scraperId).getDeclaredConstructor();
            scraper = (Scraper) ctor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return scraper;
    }

    private void connectAndAddScraper(String scraperId) {
        Scraper scraper = getNewScraperInstance(scraperId);
        scrapers.get(scraperId).add(scraper);
    }

}
