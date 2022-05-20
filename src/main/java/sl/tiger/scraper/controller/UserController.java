package sl.tiger.scraper.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sl.tiger.scraper.business.UserRepository;
import sl.tiger.scraper.dto.LoginReqDTO;
import sl.tiger.scraper.dto.LoginRespDTO;
import sl.tiger.scraper.entity.User;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginReqDTO loginReqDTO) {
        User loginUser = userRepository.findByUsernameAndPassword(loginReqDTO.getUsername(), loginReqDTO.getPassword());
        if (loginUser != null) {
            LoginRespDTO loginRespDTO = new LoginRespDTO();
            loginRespDTO.setUsername(loginUser.getUsername());
            return new ResponseEntity<>(loginRespDTO, HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>("Username or Password Incorrect!", HttpStatus.UNAUTHORIZED);
        }
    }
}
