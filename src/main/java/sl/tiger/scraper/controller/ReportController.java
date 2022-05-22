package sl.tiger.scraper.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sl.tiger.scraper.business.CriteriaRepository;
import sl.tiger.scraper.dto.Criteria;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1")
public class ReportController {

    @Autowired
    CriteriaRepository criteriaRepository;

    @GetMapping("/getDetails")
    List<Criteria> getCounts() {

        System.out.println(criteriaRepository.findAll().size());
        return criteriaRepository.findAll();
    }
}
