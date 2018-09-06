package cn.wilmar.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SpringBootApplication
public class RestfulApiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestfulApiDemoApplication.class, args);
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
class User {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    long id;
    @NonNull @NotNull @Column(unique = true)
    String username;
    @NonNull @NotNull
    String password;
}

@RepositoryRestResource
interface UserRestRepository extends JpaRepository<User, Long> {
    Optional<User> getUserByUsername(String username);
}

@Component
class UserCLI implements CommandLineRunner {
    private final UserRestRepository repository;

    UserCLI(UserRestRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        Stream.of("yinguowei", "liangjian", "jon", "socheer").forEach(
                name -> repository.save(new User(name, "111111"))
        );
        repository.findAll().forEach(System.out::println);
    }
}

@RestController
@RequestMapping("/api")
class UserController {

    private static final String APPLICATION_NAME = "apidemo";
    private final UserRestRepository repository;

    UserController(UserRestRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> queryUsers(@RequestParam("limit") int limit, @RequestParam("offset") int offset, @RequestParam("sortby") String sortby, @RequestParam("order") String order) {
        repository.findAll()
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> get(@PathVariable Long id) {
        if (id == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return repository.findById(id)
                .map(user1 -> new ResponseEntity<>(user1, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/users")
    public ResponseEntity<User> save(@Valid @RequestBody User user) {
        Optional<User> userOpt = repository.getUserByUsername(user.getUsername());
        if (userOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>(repository.save(user), HttpStatus.CREATED);
        }
    }

    @PatchMapping("/users/{id}}")
    public ResponseEntity<User> patchUser(@PathVariable Long id, @RequestBody User user) {
        Optional<User> userOpt = repository.findById(id);
        if (!userOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // TODO: mapper
        User userOrig = userOpt.get();
        if (user.getUsername() != null) {
            userOrig.setUsername(user.getUsername());
        }
        if (user.getPassword() != null) {
            userOrig.setPassword(user.getPassword());
        }
        // TODO: modified judge
        return new ResponseEntity<>(repository.save(userOrig), HttpStatus.CREATED);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> update(@PathVariable Long id,@Valid  @RequestBody User user) {
        Optional<User> userOpt = repository.findById(id);
        if (!userOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // TODO: mapper
        User userOrig = userOpt.get();
        userOrig.setUsername(user.getUsername());
        userOrig.setPassword(user.getPassword());
        // TODO: modified judge
        return new ResponseEntity<>(repository.save(userOrig), HttpStatus.CREATED);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<User> userOpt = repository.findById(id);
        if (!userOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        repository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public static HttpHeaders createAlert(String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-" + APPLICATION_NAME + "-alert", message);
        headers.add("X-" + APPLICATION_NAME + "-params", param);
        return headers;
    }
}

@Api(value = "/user", description = "用户相关操作 API")
@RestController
@RequestMapping("/api/users")
class UserController2 {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation(value = "查询所有用户")
//    或者写成    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> get(@PathVariable Long id) {
        User user = userService.getUser(id).isPresent();
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<User> save(@RequestBody User user) {
        User user0 = userService.getUserByUsername(user.getUsername());
        if (user0 != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>(userService.saveOrUpdateUser(user), HttpStatus.OK);
        }
    }

    @PatchMapping("/users/{id}}")
    public ResponseEntity<User> patchUser(@RequestBody User user, @PathVariable("id") Long id) {
        User orgUser = userService.getUser(id).get();
        if (user.getPassword() != null) {
            orgUser.setPassword(user.getPassword());
        }
        return userService.saveOrUpdateUser(user);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User user, @RequestParam("limit") int limit) {
        User user0 = userService.getUser(id);
        if (user0 == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        user0.setEmail(user.getEmail());
        user0.setFullname(user.getFullname());
        user0.setGender(user.getGender());
        user0.setUsername(user.getUsername());
        return new ResponseEntity<>(userService.saveOrUpdateUser(user0), HttpStatus.OK);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = userService.getUser(id);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }


    @ApiOperation(value = "查询用户", notes = "返回所有用户的查询方法，无参数")
    @GetMapping("/user")
    public ModelAndView userList() {
        return new ModelAndView("user/userList", "users", userRepository.findAll());
    }


    @ApiOperation(value = "创建用户的表单", notes = "返回空用户")
    @GetMapping("/user/new")
    public ModelAndView newUserForm() {
        return new ModelAndView("user/userForm", "user", new User());
    }

    @ApiOperation(value = "提交新建用户", notes = "返回用户查询界面Action")
    @PostMapping("/user/new")
    public String createUser(
            @ApiParam(name = "user", value = "用户对象", required = true)
            @Valid User user) {
        userRepository.save(user);
        return "redirect:/user";
    }

    @ApiOperation(value = "打开编辑用户的表单", notes = "返回要编辑的用户对象")
    @GetMapping("/user/{id}/edit")
    public ModelAndView editUserForm(
            @ApiParam(name = "id", value = "用户id", required = true)
            @PathVariable("id") Long id) {
        return new ModelAndView("user/userForm", "user", userRepository.findOne(id));
    }

    @ApiOperation(value = "保存编辑后的用户", notes = "返回用户查询界面Action")
    @PostMapping("/user/{id}/edit")
    public String updateUser(
            @ApiParam(name = "id", value = "用户id", required = true)
            @PathVariable("id") Long id,
            @ApiParam(name = "user", value = "用户对象", required = true)
            @Valid User user) {
        User orgUser = userRepository.findOne(id);
        orgUser.setFullname(user.getFullname());
        orgUser.setUsername(user.getUsername());
        if (!StringUtils.isEmpty(user.getPassword())) {
            orgUser.setPassword(user.getPassword());
        }
        userRepository.save(orgUser);
        return "redirect:/user";
    }

    @ApiOperation(value = "删除用户", notes = "返回用户查询界面Action")
    @DeleteMapping("/user/{id}")
    public String deleteUser(
            @ApiParam(name = "id", value = "用户id", required = true)
            @PathVariable Long id) {
        userRepository.delete(id);
        return "redirect:/user";
    }
}