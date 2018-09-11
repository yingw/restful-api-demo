package cn.wilmar.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.*;
import lombok.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 主程序入口
 */
@EnableSwagger2
@EnableJpaAuditing // 开启 JPA 审计功能
@SpringBootApplication
public class RestfulApiDemoApplication {
    private static final String SWAGGER_SCAN_BASE_PACKAGE = "cn.wilmar";
    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        SpringApplication.run(RestfulApiDemoApplication.class, args);
    }

    /**
     * Swagger 配置
     */
    @Bean
    public Docket restApiDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(SWAGGER_SCAN_BASE_PACKAGE)) // 指定 package
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Restful API Demo 接口文档")//设置文档的标题
                .description("更多内容请关注：http://www.wilmar.cn")//设置文档的描述->1.Overview
                .version(VERSION)//设置文档的版本信息-> 1.1 Version information
                .contact(new Contact("Yin Guo Wei", "http://www.wilmar.cn", "yinguowei@cn.wilmar-intl.com"))//设置文档的联系方式->1.2 Contact information
                .termsOfServiceUrl("http://www.wilmar.cn")//设置文档的License信息->1.3 License information
                .license("Apache 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0")
                .build();
    }

}

/**
 * User 实体对象，映射数据库 user 表，对应资源 /api/users
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@ApiModel(description = "数据库用户对象")
@EntityListeners(AuditingEntityListener.class)
@Entity
class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @ApiModelProperty(value = "用户 ID", example = "1")
    Long id;
    @NonNull @NotNull @ApiModelProperty(value = "用户姓名", example = "Yin Guo Wei")
    String name;
    @NonNull @NotNull @Column(unique = true) @ApiModelProperty(value = "登录账号", example = "yinguowei")
    String login;
    @JsonIgnore @ApiModelProperty(value = "登录密码", example = "111111")
    String password;
    @NonNull @ApiModelProperty(value = "邮箱", example = "yinguowei@cn.wilmar-intl.com")
    String email;

    @ManyToMany(fetch = FetchType.EAGER) @ApiModelProperty(value = "用户的角色", dataType = "Role")
    Set<Role> roles = new HashSet<>();

    @CreatedDate @JsonIgnore
    @Column(name = "created_date", nullable = false, updatable = false)
    LocalDateTime createDate; // 审计字段：创建时间
    @CreatedBy @JsonIgnore
    @Column(name = "created_by", nullable = false, length = 50, updatable = false)
    String createdBy; // 审计字段：创建人
    @LastModifiedDate @JsonIgnore
    @Column(name = "last_modified_date")
    LocalDateTime lastModifiedDate; // 审计字段：最后修改时间
    @LastModifiedBy @JsonIgnore
    @Column(name = "last_modified_by", length = 50)
    String lastModifiedBy; // 审计字段：最后修改人
}

/**
 * Role 实体对象
 */
@Data
@ApiModel(description = "数据库角色对象")
@Entity
class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @ApiModelProperty(value = "角色 ID", example = "1")
    Long id;
    @NonNull @NotNull @ApiModelProperty(value = "角色名称", example = "Administrator")
    String name;
    @NonNull @NotNull @Enumerated(EnumType.STRING) @ApiModelProperty(value = "角色代码", example = "ADMIN")
    RoleCode code;
}

/**
 * 角色代码枚举类型
 */
enum RoleCode {
    /**
     * 管理员
     */
    ADMIN,
    /**
     * 普通用户
     */
    USER
}

/**
 * 审计人默认实现
 */
@Component
class DefaultAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("system");  // TODO: load from principal
    }
}

/**
 * User 对象资源管理对象（DTO）
 */
interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 根据登录账号查询用户
     *
     * @return User Optional 对象
     */
    Optional<User> getUserByLogin(String login);

    /**
     * 根据多个条件查询用户
     *
     * @param pageable 用户指定的分页信息
     * @return 返回 Page 对象包含用户集合和分页信息
     */
    Page<User> findByLoginLikeOrNameLikeOrEmailLike(Pageable pageable, String login, String name, String email);

    // TODO: implementation
//    @Query("select User from User where login like \"%\" {keyword} + \"%\" and name like \"%\" + {keyword}")
//    Page<User> findByKeyword(Pageable pageable, String keyword);
}

/**
 * Role 对象资源管理对象（DTO）
 */
interface RoleRepository extends CrudRepository<Role, Long> {
}

/**
 * 测试数据初始化工具
 */
@Component
class InitDataLoader implements CommandLineRunner {

    /**
     * 角色：Admin、User
     */
    private final Role defaultRole = new Role("Default User", RoleCode.USER);
    private final Role adminRole = new Role("Administrator", RoleCode.ADMIN);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    InitDataLoader(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        roleRepository.save(defaultRole);
        roleRepository.save(adminRole);

        /**
         * 测试用户
         */
        Stream.of("Yin Guo Wei", "Liang Jian", "Wan Jon Yew", "Kwek So Cheer").forEach(
                name -> userRepository.save(new User(
                        name,
                        name.replaceAll(" ", "").toLowerCase(),
//                        "111111", // TODO: default & encode
                        name.replaceAll(" ", "").toLowerCase() + "@cn.wilmar-intl.com"
                ))
        );
        /**
         * 给用户添加默认角色：Default User
         */
        List<User> users = userRepository.findAll();
        for (User user : users) {
            user.getRoles().add(defaultRole);
            if (user.getId() == 1) {
                user.getRoles().add(adminRole); // ID 为 1 的用户设置为超级管理员
            }
            userRepository.save(user);
        }
        userRepository.findAll().forEach(System.out::println);
    }

}

/**
 * 集合了 Spring Pageable 参数的 Swagger 注解
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                value = "第几页 (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                value = "每页的记录数"),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                value = "排序，书写格式：property(,asc|desc). 默认 asc. 支持多个排序.")})
@interface ApiPageable {
}

/**
 * User 控制器，管理所有路径
 */
@Api(value = "/api", description = "用户相关操作 API")
@RestController
@RequestMapping("/api")
class UserController {

    private static final String APPLICATION_NAME = "RestfulApiDemo";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    UserController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

//    @ApiOperation(value = "查询所有用户")
//    @GetMapping("/users")
//    public ResponseEntity<List<User>> getAllUsers() {
//        return new ResponseEntity<>(userRepository.findAll(), HttpStatus.OK);
//    }

    @ApiOperation(value = "根据用户名查询用户")
    @ApiResponses(@ApiResponse(code = 200, message = "查询信息成功"))
    @GetMapping("/users")
    @ApiPageable
//    public ResponseEntity<List<User>> queryUsers(@RequestParam("limit") int limit, @RequestParam("offset") int offset, @RequestParam("sortby") String sortby, @RequestParam("order") String order) {
//        userRepository.findAll()
//    }
    public ResponseEntity<List<User>> queryUsers(
            @PageableDefault Pageable page,
            @ApiParam(value = "查询关键字", allowableValues = "range[1,5]", defaultValue = "Yin")
            @RequestParam(value = "keyword", defaultValue = "") String keyword) throws URISyntaxException {
        Page<User> users = userRepository.findByLoginLikeOrNameLikeOrEmailLike(page, "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%");
//        userRepository.findAll();
        return new ResponseEntity<>(users.getContent(), HttpStatus.OK);
//        return ResponseEntity.created(new URI("/api/users")).header("").body(users.getContent());
    }

    @ApiOperation(value = "根据用户id返回资源对象")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取信息成功"),
            @ApiResponse(code = 404, message = "没有找到用户id"),
            @ApiResponse(code = 400, message = "请求参数可能有误")
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<User> get(
            @ApiParam(name = "id", value = "用户id", required = true, defaultValue = "1")
            @PathVariable Long id) {
        if (id == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return userRepository.findById(id)
                .map(user1 -> new ResponseEntity<>(user1, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "新建用户", notes = "提交新建用户")
    @ApiResponses({
            @ApiResponse(code = 201, message = "资源创建成功"),
            @ApiResponse(code = 409, message = "用户账号已经存在冲突")
    })
    @PostMapping("/users")
    public ResponseEntity<User> save(
            @ApiParam(name = "user", value = "用户对象", required = true)
            @Valid @RequestBody User user) throws URISyntaxException {
        Optional<User> userOpt = userRepository.getUserByLogin(user.getLogin());
        if (userOpt.isPresent()) {
//            return new ResponseEntity<>(HttpStatus.CONFLICT);
            return ResponseEntity.noContent()
                    .header("X-" + APPLICATION_NAME + "-error", "用户账号已经存在冲突")
                    .build();
        } else {
//            return new ResponseEntity<>(userRepository.save(user), HttpStatus.CREATED);
            return ResponseEntity.created(new URI("/api/users"))
                    .header("X-" + APPLICATION_NAME + "-alert", "资源创建成功") // TODO: mess code
                    .body(userRepository.save(user));
        }
    }

    @ApiOperation(value = "更新用户信息", notes = "更新用户部分数据")
    @ApiResponses({
            @ApiResponse(code = 200, message = "更新成功"),
            @ApiResponse(code = 404, message = "没有找到用户id")
    })
    @PatchMapping("/users/{id}")
    public ResponseEntity<User> patchUser(
            @ApiParam(name = "id", value = "用户id", required = true, defaultValue = "1")
            @PathVariable Long id,
            @ApiParam(name = "user", value = "用户对象", required = true)
            @RequestBody User user) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // TODO: mapper, and can't set Login
        User userOrig = userOpt.get();
        if (user.getName() != null) {
            userOrig.setName(user.getName());
        }
        if (user.getEmail() != null) {
            userOrig.setEmail(user.getEmail());
        }
        if (user.getPassword() != null) {
            userOrig.setPassword(user.getPassword());
        }
        // TODO: modified judge
        return new ResponseEntity<>(userRepository.save(userOrig), HttpStatus.OK);
    }

    @ApiOperation(value = "更新用户", notes = "保存编辑后的用户")
    @ApiResponses({
            @ApiResponse(code = 200, message = "更新成功"),
            @ApiResponse(code = 404, message = "没有找到用户id")
    })
    @PutMapping("/users/{id}")
    public ResponseEntity<User> update(
            @ApiParam(name = "id", value = "用户id", required = true, defaultValue = "1")
            @PathVariable Long id,
            @ApiParam(name = "user", value = "用户对象", required = true)
            @Valid @RequestBody User user) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // TODO: merge
//        new AssertTrue(userOpt.get().getLogin().equals(user.getLogin()), "Error login");
        // TODO: mapper
        // TODO: modified judge
        return new ResponseEntity<>(userRepository.save(
                userOpt.map(userOrig -> {
                    userOrig.setEmail(user.getEmail());
                    userOrig.setName(user.getName());
                    userOrig.setPassword(user.getPassword());
                    return userOrig;
                }).get()
        ), HttpStatus.OK);
    }

    // updatePassword

    @ApiOperation(value = "删除用户", notes = "删除用户")
    @ApiResponses({
            @ApiResponse(code = 204, message = "删除成功，没有返回内容"),
            @ApiResponse(code = 404, message = "没有找到用户id")
    })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(
            @ApiParam(name = "id", value = "用户id", required = true, defaultValue = "4")
            @PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @ApiOperation(value = "给指定用户新增角色")
    @PostMapping("/users/{userId}/roles/{roleId}")
    @ApiResponses({
            @ApiResponse(code = 201, message = "用户角色创建成功"),
            @ApiResponse(code = 404, message = "没有找到用户id或角色id")
    })
    public ResponseEntity<User> addRole(
            @ApiParam(name = "userId", value = "用户id", required = true, defaultValue = "2")
            @PathVariable("userId") final Long userId,
            @ApiParam(name = "roleId", value = "角色id", required = true, defaultValue = "2")
            @PathVariable("roleId") final Long roleId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            ResponseEntity.notFound().header("...").build();
        }
        userOpt.get().getRoles().forEach(role -> {
            if (role.getId().equals(roleId)) {
                // TODO:
            }
        });
        userOpt.get().getRoles().add(roleOpt.get());
        userRepository.save(userOpt.get());
        return new ResponseEntity<>(userOpt.get(), HttpStatus.CREATED);
    }

    @ApiOperation(value = "删除指定用户的指定角色")
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @ApiResponses({
            @ApiResponse(code = 204, message = "删除成功，没有返回内容"),
            @ApiResponse(code = 404, message = "没有找到用户id或角色id")
    })
    public ResponseEntity<Void> deleteRole(
            @ApiParam(name = "userId", value = "用户id", required = true, defaultValue = "1")
            @PathVariable("userId") final Long userId,
            @ApiParam(name = "roleId", value = "角色id", required = true, defaultValue = "2")
            @PathVariable("roleId") final Long roleId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            ResponseEntity.notFound().header("...").build();
        }
        // TODO: exists
        userOpt.get().getRoles().remove(roleOpt.get());
        userRepository.save(userOpt.get());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

/**
 * Role 控制器
 */
@Api(value = "/api", description = "角色相关操作的 API")
@RestController
@RequestMapping("/api")
class RoleController {

    private final RoleRepository repository;

    RoleController(RoleRepository repository) {
        this.repository = repository;
    }

    @ApiOperation(value = "查询所有角色")
    @ApiResponse(code = 200, message = "返回成功")
    @GetMapping("/roles")
    public ResponseEntity<Iterable<Role>> getAllRoles() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }
}
