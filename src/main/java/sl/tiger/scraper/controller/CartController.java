package sl.tiger.scraper.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sl.tiger.scraper.business.PartNumberCriteriaRepository;
import sl.tiger.scraper.dto.PartNumberCriteria;

import java.util.List;

@RestController
@RequestMapping("api/v1")
public class CartController {

    @Autowired
    PartNumberCriteriaRepository  repository;

    @GetMapping("/cart")
    private List<PartNumberCriteria> getCartItems() {
        return repository.findAll();
    }

}
