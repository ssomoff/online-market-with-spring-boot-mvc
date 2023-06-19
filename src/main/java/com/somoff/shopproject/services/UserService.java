package com.somoff.shopproject.services;

import com.somoff.shopproject.entities.Cart;
import com.somoff.shopproject.entities.Role;
import com.somoff.shopproject.entities.User;
import com.somoff.shopproject.repositories.UserRepository;
import com.somoff.shopproject.services.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class UserService implements UserDetailsService, IUserService {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${activate.path}")
    private String activatePath;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmtpMailSender smtpMailSender;
    private final CartService cartService;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SmtpMailSender smtpMailSender, CartService cartService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.smtpMailSender = smtpMailSender;
        this.cartService = cartService;
    }

    @Override
    public void addUser(User user) {
        userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(new User());
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();

    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElse(new User());
    }

    @Override
    @Transactional
    public int createUser(User user, String address2, String rePassword, MultipartFile file) throws IOException {
        User userFromDb = findByEmail(user.getEmail());
        if (userFromDb.getEmail() != null) {
            return 0;
        }
        if (user.getPassword().equals(rePassword)) {
            user.setAddress(user.getAddress().concat(", ").concat(address2));
            Set<Role> roles = new HashSet<>();
            roles.add(Role.USER);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setActive(false);
            user.setRoles(roles);
            user.setActivationCode(UUID.randomUUID().toString());
            createDir();
            if (file != null && file.getOriginalFilename()!=null && !file.getOriginalFilename().isEmpty()) {
                String uuidFile = UUID.randomUUID().toString();
                String photoName = uuidFile + "." + file.getOriginalFilename();
                file.transferTo(new File(uploadPath + "/" + photoName));
                user.setPhotoName(photoName);
            } else {
                user.setPhotoName("admin.png");
            }
            addUser(user);
            createCart(user);
            sendMessage(user);
            return 1;
        }
        return 2;
    }

    @Override
    @Transactional
    public boolean createAdmin() {
        String email = "admin@shopproduct.ru";
        String password = "admin";
        User userFromDb = findByEmail(email);
        if (userFromDb != null) {
            return true;
        }
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ADMIN);
        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setFirstName("Admin");
        admin.setLastName("");
        admin.setRoles(roles);
        admin.setActive(true);
        admin.setActivationCode(null);
        createDir();
        admin.setPhotoName("admin.png");
        addUser(admin);
        createCart(admin);
        return false;
    }

    @Override
    @Transactional
    public int updateUser(User user, User userForm, String password, MultipartFile file) throws IOException {
        User updUser = findByEmail(user.getEmail());
        String emailFromDb = findByEmail(userForm.getEmail()).getEmail();
        if ((emailFromDb != null && !emailFromDb.equals(user.getEmail())) || (updUser == null)) {
            return 0;
        }
        updUser.setFirstName(userForm.getFirstName());
        updUser.setLastName(userForm.getLastName());
        updUser.setPhone(userForm.getPhone());
        boolean isEmailChanged = (userForm.getEmail() != null && !userForm.getEmail().equals(user.getEmail()));
        if (isEmailChanged) {
            updUser.setEmail(userForm.getEmail());
            updUser.setActive(false);
            updUser.setActivationCode(UUID.randomUUID().toString());
        }
        updUser.setAddress(userForm.getAddress());
        if (file != null && file.getOriginalFilename()!=null && !file.getOriginalFilename().isEmpty()) {
            String uuidFile = UUID.randomUUID().toString();
            String photoName = uuidFile + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadPath + "/" + photoName));
            updUser.setPhotoName(photoName);
        } else {
            updUser.setPhotoName(user.getPhotoName());
        }

        if (password != null && passwordEncoder.matches(password, user.getPassword())) {
            addUser(updUser);
            if (isEmailChanged) {
                sendMessage(updUser);
                return 3;
            }
            return 1;
        }
        return 2;
    }

    @Transactional
    public boolean activateAccount(String code) {
        User user = userRepository.findByActivationCode(code);
        if (user == null) {
            return false;
        }
        user.setActivationCode(null);
        user.setActive(true);
        addUser(user);
        return true;
    }

    @Transactional
    public void updateUserRole(User user, Long userId, Role role) {
        if (user.getRoles().contains(Role.ADMIN) && userId != 1) {
            User userFromDb = userRepository.findById(userId).orElse(null);
            assert userFromDb != null;
            Set<Role> roles = userFromDb.getRoles();
                roles.clear();
                roles.add(role);
                userFromDb.setRoles(roles);
                addUser(userFromDb);

        }
    }

    @Transactional
    public void activateAccount(User user, Long userId, boolean activate) {
        if (user.getRoles().contains(Role.ADMIN) && userId != 1) {
            User userFromDb = userRepository.findById(userId).orElse(null);
            assert userFromDb != null;
            userFromDb.setActive(activate);
            addUser(userFromDb);

        }
    }

    //create cart in bd and update user
    private void createCart(User user) {
        User updUser = findByEmail(user.getEmail());
        Cart newCart = cartService.createCart(updUser);
        updUser.setCart(newCart);
        addUser(updUser);
    }

    // create dir , if not exists
    private void createDir() {
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
    }

    // send activatedCode to mail of user
    private void sendMessage(User user) {
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            String message = String.format("Hello, %s! \n" +
                    "Welcome to ShopProduct. Follow the link below to complete the registration. \n" +
                    activatePath + "activate/%s", user.getFirstName(), user.getActivationCode());
            smtpMailSender.send(user.getEmail(), "Activation Code", message);
        }
    }
}
